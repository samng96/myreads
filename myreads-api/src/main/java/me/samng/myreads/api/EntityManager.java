package me.samng.myreads.api;

import com.google.cloud.datastore.*;
import me.samng.myreads.api.entities.FollowedListEntity;
import me.samng.myreads.api.entities.ReadingListElementEntity;
import me.samng.myreads.api.entities.ReadingListEntity;

import java.util.List;

public class EntityManager {

    public static boolean DeleteUser(Datastore datastore, long userId) {
        // When we delete a user, we need to clean up all their lists, followed lists, and reading list elements.
        // We need to delete the reading list elements first, because when we delete a list, we check each element
        // in the list to ensure that we're not orphaning any RLEs - if we are, we delete those as well, so ordering
        // matters here.

        // Clean up the reading list elements.
        List<ReadingListElementEntity> rles = DatastoreHelpers.getAllReadingListElementsForUser(datastore, userId);
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
            Key key = DatastoreHelpers.newFollowedListKey(e.id);
            datastore.delete(key);
        }

        // Finally, delete the user.
        Key key = DatastoreHelpers.newUserKey(userId);
        datastore.delete(key);
        return true;
    }

    public static boolean DeleteReadingListElement(Datastore datastore, long readingListElementId) {
        // When we delete a reading list element, we need to remove it from all the lists that it belongs to.
        // We also have to delete all comments.
        ReadingListElementEntity rle = DatastoreHelpers.getReadingListElement(datastore, readingListElementId);
        ReadingListEntity list;
        Key key;

        for (long listId : rle.listIds) {
            list = DatastoreHelpers.getReadingList(datastore, listId);

            assert list.readingListElementIds().contains(rle.id);
            list.readingListElementIds().remove(rle.id);
            if (!DatastoreHelpers.updateReadingListEntity(datastore, list)) {
                return false;
            }
        }

        for (long commentId : rle.commentIds) {
            key = DatastoreHelpers.newCommentKey(commentId);
            datastore.delete(key);
        }

        key = DatastoreHelpers.newReadingListElementKey(readingListElementId);
        datastore.delete(key);
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
            DatastoreHelpers.updateReadingListElementEntity(datastore, rle);

            // If this RLE is going to be orphaned, delete it.
            if (rle.listIds.size() == 0 ) {
                EntityManager.DeleteReadingListElement(datastore, rle.id);
            }
        }

        Key key = DatastoreHelpers.newReadingListKey(readingListId);
        datastore.delete(key);
        return true;
    }
}
