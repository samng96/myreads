package me.samng.myreads.api;

import com.google.cloud.datastore.Datastore;
import me.samng.myreads.api.entities.FollowedListEntity;
import me.samng.myreads.api.entities.ReadingListElementEntity;
import me.samng.myreads.api.entities.ReadingListEntity;
import me.samng.myreads.api.entities.UserEntity;

import java.util.ArrayList;
import java.util.List;

public class EntityManager {
    public static long singletonDeletedListId = -96;

    public static long UpsertUser(Datastore datastore, UserEntity userEntity) {
        UserEntity currentUser = DatastoreHelpers.getUser(datastore, userEntity.id);
        long userId;

        if (currentUser == null) {
            userId = DatastoreHelpers.createUser(datastore, userEntity);
            DatastoreHelpers.addAuthTokenToUserIdMapping(datastore, userEntity.externalId, userId);
        }
        else {
            userId = userEntity.id;
            DatastoreHelpers.updateAuthTokenToUserIdMapping(datastore, userEntity.externalId, userId);
            DatastoreHelpers.updateUser(datastore, userEntity, false);
        }
        return userId;
    }

    public static boolean DeleteUser(Datastore datastore, long userId) {
        // When we delete a user, we need to clean up all their lists, followed lists, and reading list elements.
        // We need to delete the reading list elements first, because when we delete a list, we check each element
        // in the list to ensure that we're not orphaning any RLEs - if we are, we delete those as well, so ordering
        // matters here.

        // Clean up the reading list elements.
        List<ReadingListElementEntity> rles = DatastoreHelpers.getAllReadingListElementsForUser(datastore, userId, false, false);
        for (ReadingListElementEntity e : rles) {
            if (!EntityManager.DeleteReadingListElement(datastore, e.id)) {
                return false;
            }
        }

        // Clean up the reading lists.
        List<ReadingListEntity> lists = DatastoreHelpers.getAllReadingListsForUser(datastore, userId);
        for (ReadingListEntity e : lists) {
            if (!EntityManager.DeleteReadingList(datastore, e.id)) {
                return false;
            }
        }

        // Clean up the followed lists.
        List<FollowedListEntity> followedLists = DatastoreHelpers.getAllFollowedListsForUser(datastore, userId);
        for (FollowedListEntity e : followedLists) {
            DatastoreHelpers.deleteFollowedList(datastore, e.id);
        }

        // Finally, delete the user.
        DatastoreHelpers.deleteUser(datastore, userId);
        return true;
    }

    public static boolean DeleteReadingListElement(Datastore datastore, long readingListElementId) {
        // When we delete a reading list element, we need to remove it from all the lists that it belongs to.
        // We also have to delete all comments.
        ReadingListElementEntity rle = DatastoreHelpers.getReadingListElement(datastore, readingListElementId);
        ReadingListEntity list;

        for (long listId : rle.listIds) {
            list = DatastoreHelpers.getReadingList(datastore, listId);

            assert list.readingListElementIds().contains(rle.id);
            list.readingListElementIds().remove(rle.id);
            if (!DatastoreHelpers.updateReadingList(datastore, list, false)) {
                return false;
            }
        }

        for (long commentId : rle.commentIds) {
            DatastoreHelpers.deleteComment(datastore, commentId);
        }

        for (long tagId : rle.tagIds) {
            DatastoreHelpers.deleteTagToReadingListElementMapping(datastore, rle.userId, tagId, rle.id);
        }

        DatastoreHelpers.deleteReadingListElement(datastore, readingListElementId);
        return true;
    }

    public static boolean DeleteReadingList(Datastore datastore, long readingListId) {
        // When we delete a reading list, we just need to make sure that each RLE isn't only attached to this list.
        // If so, delete it as well.
        ReadingListEntity list = DatastoreHelpers.getReadingList(datastore, readingListId);
        ReadingListElementEntity rle;

        for (long rleId : list.readingListElementIds) {
            rle = DatastoreHelpers.getReadingListElement(datastore, rleId);

            assert rle.listIds.contains(readingListId);
            rle.listIds.remove(readingListId);
            DatastoreHelpers.updateReadingListElement(datastore, rle, false);

            // If this RLE is going to be orphaned, delete it.
            if (rle.listIds.size() == 0) {
                EntityManager.DeleteReadingListElement(datastore, rle.id);
            }
        }

        for (long tagId : list.tagIds) {
            DatastoreHelpers.deleteTagToReadingListMapping(datastore, list.userId, tagId, list.id);
        }

        DatastoreHelpers.deleteReadingList(datastore, readingListId);

        // Lastly, for each other user that was following this list, we'll need to move those
        // followed lists to point at the singleton deleted list moniker.
        List<FollowedListEntity> followedLists = DatastoreHelpers.getAllFollowedListsForList(datastore, readingListId);
        for (FollowedListEntity f : followedLists) {
            f.listId = EntityManager.singletonDeletedListId;
            f.orphaned = true;
            DatastoreHelpers.updateFollowedList(datastore, f, false);
        }
        return true;
    }

    public static ReadingListElementEntity GetReadingListElementIfUserOwnsIt(
        Datastore datastore,
        long userId,
        long readingListElementId) {

        ReadingListElementEntity rleEntity = DatastoreHelpers.getReadingListElement(datastore, readingListElementId);
        if (rleEntity == null || rleEntity.userId != userId) {
            return null;
        }
        return rleEntity;
    }

    public static List<Long> AddReadingListElementsToReadingList(
        Datastore datastore,
        long userId,
        ReadingListEntity readingListEntity,
        Long[] readingListElementIds) {
        // Note that we're not transactional! As a result, we'll return the list of Ids that we've successfully added,
        // regardless of whether or not we have errors on the overall operation.

        ArrayList<Long> addedIds = new ArrayList<>();
        for (long rleId : readingListElementIds) {
            if (readingListEntity.readingListElementIds() != null && readingListEntity.readingListElementIds().contains(rleId)) {
                continue;
            }

            // We need to add it to our reading list, but we also need to add it to the RLE.
            ReadingListElementEntity rleEntity = EntityManager.GetReadingListElementIfUserOwnsIt(datastore, userId, rleId);
            assert rleEntity != null;
            assert rleEntity.listIds == null || !rleEntity.listIds.contains(readingListEntity.id);

            if (readingListEntity.readingListElementIds() == null) {
                readingListEntity.readingListElementIds = new ArrayList<Long>();
            }
            if (!readingListEntity.readingListElementIds.contains(rleId)) {
                readingListEntity.readingListElementIds.add(rleId);
            }

            if (rleEntity.listIds() == null) {
                rleEntity.listIds = new ArrayList<Long>();
            }
            if (!rleEntity.listIds.contains(readingListEntity.id)) {
                rleEntity.listIds.add(readingListEntity.id);
            }

            if (DatastoreHelpers.updateReadingList(datastore, readingListEntity, false) &&
                DatastoreHelpers.updateReadingListElement(datastore, rleEntity, false)) {
                addedIds.add(rleId);
            } else {
                // Note that we're swallowing an error here, but the user will have to retry the action anyway,
                // so there's no harm done.
                break;
            }
        }
        return addedIds;
    }

    public static List<Long> AddTagsToReadingList(Datastore datastore, ReadingListEntity readingListEntity, Long[] tagIds) {
        // Note that we're not transactional! As a result, we'll return the list of Ids that we've successfully added,
        // regardless of whether or not we have errors on the overall operation.
        ArrayList<Long> addedIds = new ArrayList<>();
        for (long tagId : tagIds) {
            boolean valid = true;

            if (readingListEntity.tagIds() != null && readingListEntity.tagIds().contains(tagId)) {
                continue;
            }

            if (readingListEntity.tagIds() == null) {
                readingListEntity.tagIds = new ArrayList<>();
            }
            readingListEntity.tagIds.add(tagId);

            if (DatastoreHelpers.updateReadingList(datastore, readingListEntity, false)) {
                addedIds.add(tagId);
            } else {
                break;
            }

            DatastoreHelpers.addTagToReadingListMapping(datastore, readingListEntity.userId, tagId, readingListEntity.id);
        }

        return addedIds;
    }

    public static List<Long> AddTagsToReadingListElement(Datastore datastore, ReadingListElementEntity readingListElementEntity, Long[] tagIds) {
        // Note that we're not transactional! As a result, we'll return the list of Ids that we've successfully added,
        // regardless of whether or not we have errors on the overall operation.
        ArrayList<Long> addedIds = new ArrayList<Long>();
        for (long tagId : tagIds) {
        boolean valid = true;

            if (readingListElementEntity.tagIds() != null && readingListElementEntity.tagIds().contains(tagId)) {
                continue;
            }

            if (readingListElementEntity.tagIds() == null) {
                readingListElementEntity.tagIds = new ArrayList<>();
            }
            readingListElementEntity.tagIds.add(tagId);

            if (DatastoreHelpers.updateReadingListElement(datastore, readingListElementEntity, false)) {
                addedIds.add(tagId);
            } else {
                break;
            }

            DatastoreHelpers.addTagToReadingListElementMapping(datastore, readingListElementEntity.userId, tagId, readingListElementEntity.id);
        }

        return addedIds;
    }
}
