package me.samng.myreads.api;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.common.collect.ImmutableList;
import me.samng.myreads.api.entities.*;
import me.samng.myreads.api.entities.indexes.TagToReadingListElementEntity;
import me.samng.myreads.api.entities.indexes.TagToReadingListEntity;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DatastoreHelpers {
    private static String keyPath = "/users/samng/gcp-samng-privatekey.json";
    public static String userKind = "user";
    public static String readingListKind = "readingList";
    public static String followedListKind = "followedList";
    public static String readingListElementKind = "readingListElement";
    public static String commentKind = "comment";
    public static String tagKind = "tag";
    public static String tagToReadingListElementKind = "tagToReadingListElement";
    public static String tagToReadingListKind = "tagToReadingList";
    private static KeyFactory keyFactory = new KeyFactory(MainVerticle.AppId);
    private static String deletedMoniker = "deleted";

    public static Datastore getDatastore() {
        DatastoreOptions options = null;
        try {
            options = DatastoreOptions.newBuilder()
                .setProjectId(MainVerticle.AppId)
                .setCredentials(GoogleCredentials.fromStream(
                    new FileInputStream(keyPath))).build();
        }
        catch (Exception e) {
            assert false;
        }

        return options.getService();
    }

    private static IncompleteKey newUserKey() {
        keyFactory.setKind(userKind);
        return keyFactory.newKey();
    }

    private static Key newUserKey(Long keyId) {
        keyFactory.setKind(userKind);
        return keyFactory.newKey(keyId);
    }

    private static IncompleteKey newCommentKey() {
        keyFactory.setKind(commentKind);
        return keyFactory.newKey();
    }

    private static Key newCommentKey(Long keyId) {
        keyFactory.setKind(commentKind);
        return keyFactory.newKey(keyId);
    }

    private static IncompleteKey newTagKey() {
        keyFactory.setKind(tagKind);
        return keyFactory.newKey();
    }

    private static Key newTagKey(Long keyId) {
        keyFactory.setKind(tagKind);
        return keyFactory.newKey(keyId);
    }

    private static IncompleteKey newReadingListKey() {
        keyFactory.setKind(readingListKind);
        return keyFactory.newKey();
    }

    private static Key newReadingListKey(Long keyId) {
        keyFactory.setKind(readingListKind);
        return keyFactory.newKey(keyId);
    }

    private static IncompleteKey newReadingListElementKey() {
        keyFactory.setKind(readingListElementKind);
        return keyFactory.newKey();
    }

    private static Key newReadingListElementKey(Long keyId) {
        keyFactory.setKind(readingListElementKind);
        return keyFactory.newKey(keyId);
    }

    private static IncompleteKey newFollowedListKey() {
        keyFactory.setKind(followedListKind);
        return keyFactory.newKey();
    }

    private static Key newFollowedListKey(Long keyId) {
        keyFactory.setKind(followedListKind);
        return keyFactory.newKey(keyId);
    }

    private static IncompleteKey newTagToReadingListKey() {
        keyFactory.setKind(tagToReadingListKind);
        return keyFactory.newKey();
    }

    private static Key newTagToReadingListKey(Long keyId) {
        keyFactory.setKind(tagToReadingListKind);
        return keyFactory.newKey(keyId);
    }

    private static IncompleteKey newTagToReadingListElementKey() {
        keyFactory.setKind(tagToReadingListElementKind);
        return keyFactory.newKey();
    }

    private static Key newTagToReadingListElementKey(Long keyId) {
        keyFactory.setKind(tagToReadingListElementKind);
        return keyFactory.newKey(keyId);
    }

    public static long createUser(Datastore datastore, UserEntity userEntity) {
        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newUserKey())
            .set("name", userEntity.name())
            .set("email", userEntity.email())
            .set("userId", userEntity.userId())
            .set("deleted", false)
            .build();
        Entity entity = datastore.add(insertEntity);
        return entity.getKey().getId();
    }

    public static long createReadingList(Datastore datastore, ReadingListEntity readingListEntity) {
        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newReadingListKey())
            .set("name", readingListEntity.name() == null ? "My Reading List" : readingListEntity.name())
            .set("description", readingListEntity.description() == null ? "" : readingListEntity.description())
            .set("userId", readingListEntity.userId)
            .set("tagIds", ImmutableList.copyOf(readingListEntity.tagIds().stream().map(LongValue::new).iterator()))
            .set("readingListElementIds", ImmutableList.copyOf(readingListEntity.readingListElementIds().stream().map(LongValue::new).iterator()))
            .set("deleted", false)
            .build();
        Entity entity = datastore.add(insertEntity);
        return entity.getKey().getId();
    }

    public static long createFollowedList(Datastore datastore, FollowedListEntity followedListEntity) {
        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newFollowedListKey())
            .set("userId", followedListEntity.userId())
            .set("listId", followedListEntity.listId())
            .set("ownerId", followedListEntity.ownerId())
            .set("deleted", false)
            .build();
        Entity entity = datastore.add(insertEntity);
        return entity.getKey().getId();
    }

    public static long createReadingListElement(Datastore datastore, ReadingListElementEntity rleEntity) {
        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newReadingListElementKey())
            .set("name", rleEntity.name() == null ? "My Reading List Element" : rleEntity.name())
            .set("description", rleEntity.description() == null ? "" : rleEntity.description())
            .set("userId", rleEntity.userId())
            .set("link", rleEntity.link() == null ? "" : rleEntity.link())
            .set("tagIds", ImmutableList.copyOf(rleEntity.tagIds().stream().map(LongValue::new).iterator()))
            .set("listIds", ImmutableList.copyOf(rleEntity.listIds().stream().map(LongValue::new).iterator()))
            .set("deleted", false)
            .build();

        Entity entity = datastore.add(insertEntity);
        return entity.getKey().getId();
    }

    public static long createComment(Datastore datastore, CommentEntity commentEntity) {
        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newCommentKey())
            .set("commentText", commentEntity.commentText())
            .set("userId", commentEntity.userId)
            .set("readingListElementId", commentEntity.readingListElementId)
            .set("lastModified", Timestamp.of(commentEntity.lastModified))
            .set("deleted", false)
            .build();
        Entity entity = datastore.add(insertEntity);
        return entity.getKey().getId();
    }

    public static long createTag(Datastore datastore, TagEntity tagEntity) {
        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newTagKey())
            .set("tagName", tagEntity.tagName())
            .set("deleted", false)
            .build();
        Entity addedEntity = datastore.add(insertEntity);
        return addedEntity.getKey().getId();
    }

    public static List<UserEntity> getAllUsers(Datastore datastore) {
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setFilter(PropertyFilter.eq(DatastoreHelpers.deletedMoniker, false))
            .setKind(DatastoreHelpers.userKind)
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<UserEntity> results = new ArrayList<>();
        queryresult.forEachRemaining(user -> results.add(UserEntity.fromEntity(user)));

        return results;
    }

    public static List<TagEntity> getAllTags(Datastore datastore) {
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.tagKind)
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<TagEntity> results = new ArrayList<TagEntity>();
        queryresult.forEachRemaining(list -> { results.add(TagEntity.fromEntity(list)); });

        return results;
    }

    public static List<ReadingListEntity> getAllReadingListsForUser(Datastore datastore, long userId) {
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.readingListKind)
            .setFilter(CompositeFilter.and(
                PropertyFilter.eq("userId", userId),
                PropertyFilter.eq(DatastoreHelpers.deletedMoniker, false)))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<ReadingListEntity> results = new ArrayList<>();
        queryresult.forEachRemaining(list -> results.add(ReadingListEntity.fromEntity(list)));

        return results;
    }

    public static List<ReadingListEntity> getAllReadingListsForUserWithTag(Datastore datastore, long userId, long tagId) {
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.tagToReadingListKind)
            .setFilter(CompositeFilter.and(
                PropertyFilter.eq("tagId", tagId),
                PropertyFilter.eq("userId", userId)))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<TagToReadingListEntity> map = new ArrayList<>();
        queryresult.forEachRemaining(list -> map.add(TagToReadingListEntity.fromEntity(list)));

        ArrayList<ReadingListEntity> results = new ArrayList<>();
        for (TagToReadingListEntity e : map) {
            results.add(DatastoreHelpers.getReadingList(datastore, e.readingListId));
        }

        return results;
    }

    public static List<ReadingListElementEntity> getAllReadingListElementsForUser(Datastore datastore, long userId, boolean unreadFilter) {
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.readingListElementKind)
            .setFilter(CompositeFilter.and(
                PropertyFilter.eq("userId", userId),
                PropertyFilter.eq(DatastoreHelpers.deletedMoniker, false)))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<ReadingListElementEntity> results = new ArrayList<>();
        queryresult.forEachRemaining(list -> {
            ReadingListElementEntity rle = ReadingListElementEntity.fromEntity(list);
            if (unreadFilter) {
                if (!rle.isRead) {
                    results.add(rle);
                }
            }
            else {
                results.add(rle);
            }
        });

        return results;
    }

    public static List<ReadingListElementEntity> getAllReadingListElementsForUserWithTag(Datastore datastore, long userId, long tagId) {
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.tagToReadingListElementKind)
            .setFilter(CompositeFilter.and(
                PropertyFilter.eq("tagId", tagId),
                PropertyFilter.eq("userId", userId)))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<TagToReadingListElementEntity> map = new ArrayList<>();
        queryresult.forEachRemaining(list -> map.add(TagToReadingListElementEntity.fromEntity(list)));

        ArrayList<ReadingListElementEntity> results = new ArrayList<>();
        for (TagToReadingListElementEntity e : map) {
            results.add(DatastoreHelpers.getReadingListElement(datastore, e.readingListElementId));
        }

        return results;
    }

    public static List<FollowedListEntity> getAllFollowedListsForUser(Datastore datastore, long userId) {
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.followedListKind)
            .setFilter(CompositeFilter.and(
                PropertyFilter.eq("userId", userId),
                PropertyFilter.eq(DatastoreHelpers.deletedMoniker, false)))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<FollowedListEntity> results = new ArrayList<>();
        queryresult.forEachRemaining(followedList -> results.add(FollowedListEntity.fromEntity(followedList)));

        return results;
    }

    public static List<FollowedListEntity> getAllFollowedListsForList(Datastore datastore, long readingListId) {
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.followedListKind)
            .setFilter(CompositeFilter.and(
                PropertyFilter.eq("listId", readingListId),
                PropertyFilter.eq(DatastoreHelpers.deletedMoniker, false)))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<FollowedListEntity> results = new ArrayList<>();
        queryresult.forEachRemaining(followedList -> results.add(FollowedListEntity.fromEntity(followedList)));

        return results;
    }

    public static UserEntity getUser(Datastore datastore, long userId) {
        Entity entity = null;
        try {
            Key key = DatastoreHelpers.newUserKey(userId);
            entity = datastore.get(key);
        }
        catch (IllegalArgumentException e) { }
        if (entity == null) {
            return null;
        }
        UserEntity userEntity = UserEntity.fromEntity(entity);
        if (userEntity.deleted) {
            return null;
        }
        return userEntity;
    }

    public static ReadingListEntity getReadingList(Datastore datastore, long readingListId) {
        Entity entity = null;
        try {
            Key key = DatastoreHelpers.newReadingListKey(readingListId);
            entity = datastore.get(key);
        }
        catch (IllegalArgumentException e) { }

        if (entity == null) {
            return null;
        }
        ReadingListEntity readingListEntity = ReadingListEntity.fromEntity(entity);
        if (readingListEntity.deleted) {
            return null;
        }
        return readingListEntity;
    }

    public static ReadingListElementEntity getReadingListElement(Datastore datastore, long readingListElementId) {
        Entity entity = null;
        try {
            Key key = DatastoreHelpers.newReadingListElementKey(readingListElementId);
            entity = datastore.get(key);
        }
        catch (IllegalArgumentException e) { }

        if (entity == null) {
            return null;
        }
        ReadingListElementEntity readingListElementEntity = ReadingListElementEntity.fromEntity(entity);
        if (readingListElementEntity.deleted) {
            return null;
        }
        return readingListElementEntity;
    }

    public static FollowedListEntity getFollowedList(Datastore datastore, long listId) {
        Entity entity = null;
        try {
            Key key = DatastoreHelpers.newFollowedListKey(listId);
            entity = datastore.get(key);
        }
        catch (IllegalArgumentException e) { }

        if (entity == null) {
            return null;
        }
        FollowedListEntity followedListEntity = FollowedListEntity.fromEntity(entity);
        if (followedListEntity.deleted) {
            return null;
        }
        return followedListEntity;
    }

    public static CommentEntity getComment(Datastore datastore, long commentId) {
        Entity entity = null;
        try {
            Key key = DatastoreHelpers.newCommentKey(commentId);
            entity = datastore.get(key);
        }
        catch (IllegalArgumentException e) { }

        if (entity == null) {
            return null;
        }
        CommentEntity commentEntity = CommentEntity.fromEntity(entity);
        if (commentEntity.deleted) {
            return null;
        }
        return commentEntity;
    }

    public static TagEntity getTag(Datastore datastore, long tagId) {
        Entity entity = null;
        try {
            Key key = DatastoreHelpers.newTagKey(tagId);
            entity = datastore.get(key);
        }
        catch (IllegalArgumentException e) { }

        if (entity == null) {
            return null;
        }
        TagEntity tagEntity = TagEntity.fromEntity(entity);
        if (tagEntity.deleted) {
            return null;
        }
        return tagEntity;
    }

    public static TagEntity getTagByName(Datastore datastore, String tagName) {
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.tagKind)
            .setFilter(PropertyFilter.eq("tagName", tagName))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // We should only have one, but since there may be errors, just return the first one.
        if (queryresult.hasNext()) {
            return TagEntity.fromEntity(queryresult.next());
        }
        return null;
    }

    public static List<TagEntity> getTagsByUser(Datastore datastore, long userId) {
        // We have two tables to get from - we have a tagToRL table and a tagToRLE table; get all the
        // RL and RLE Ids associated with the user, then merge the tags into one unique list, then resolve the tags
        // and return them.
        List<ReadingListElementEntity> rles = DatastoreHelpers.getAllReadingListElementsForUser(datastore, userId, false);
        List<ReadingListEntity> rls = DatastoreHelpers.getAllReadingListsForUser(datastore, userId);

        HashSet<Long> tagIds = new HashSet<Long>();
        rles.forEach(rle -> tagIds.addAll(rle.tagIds));
        rls.forEach(rl -> tagIds.addAll(rl.tagIds));

        // Since datastore thoroughly sucks and doesn't have any OR filters or GQL OR operators, we have
        // to do this manually by getting all the tags and checking containment on our set.
        List<TagEntity> tags = DatastoreHelpers.getAllTags(datastore);
        List<TagEntity> result = new ArrayList<TagEntity>();
        for (TagEntity tag : tags) {
            if (tagIds.contains(tag.id)) {
                result.add(tag);
            }
        }
        return result;
    }

    public static boolean updateUser(Datastore datastore, UserEntity userEntity, boolean updateForDelete) {
        Key key = DatastoreHelpers.newUserKey(userEntity.id());
        Entity newEntity = Entity.newBuilder(key)
            .set("name", userEntity.name())
            .set("email", userEntity.email())
            .set("userId", userEntity.userId())
            .set("deleted", updateForDelete)
            .build();

        try {
            datastore.update(newEntity);
            return true;
        }
        catch (DatastoreException e) {
            return false;
        }
    }

    public static boolean updateReadingList(Datastore datastore, ReadingListEntity readingListEntity, boolean updateForDelete) {
        Entity.Builder builder = Entity.newBuilder(DatastoreHelpers.newReadingListKey(readingListEntity.id))
            .set("name", readingListEntity.name())
            .set("description", readingListEntity.description())
            .set("userId", readingListEntity.userId())
            .set("tagIds", ImmutableList.copyOf(readingListEntity.tagIds().stream().map(LongValue::new).iterator()))
            .set("readingListElementIds", ImmutableList.copyOf(readingListEntity.readingListElementIds().stream().map(LongValue::new).iterator()))
            .set("deleted", updateForDelete);

        Entity newEntity = builder.build();
        try {
            datastore.update(newEntity);
            return true;
        }
        catch (DatastoreException e) {
            return false;
        }
    }

    public static boolean updateReadingListElement(Datastore datastore, ReadingListElementEntity readingListElementEntity, boolean updateForDelete) {
        Entity.Builder builder = Entity.newBuilder(DatastoreHelpers.newReadingListElementKey(readingListElementEntity.id))
            .set("name", readingListElementEntity.name())
            .set("description", readingListElementEntity.description())
            .set("userId", readingListElementEntity.userId())
            .set("link", readingListElementEntity.link())
            .set("tagIds", ImmutableList.copyOf(readingListElementEntity.tagIds().stream().map(LongValue::new).iterator()))
            .set("listIds", ImmutableList.copyOf(readingListElementEntity.listIds().stream().map(LongValue::new).iterator()))
            .set("commentIds", ImmutableList.copyOf(readingListElementEntity.commentIds().stream().map(LongValue::new).iterator()))
            .set("deleted", updateForDelete);
        Entity newEntity = builder.build();
        try {
            datastore.update(newEntity);
            return true;
        }
        catch (DatastoreException e) {
            return false;
        }
    }

    public static boolean updateComment(Datastore datastore, CommentEntity commentEntity, boolean updateForDelete) {
        Entity.Builder builder = Entity.newBuilder(DatastoreHelpers.newCommentKey(commentEntity.id))
            .set("commentText", commentEntity.commentText())
            .set("userId", commentEntity.userId())
            .set("readingListElementId", commentEntity.readingListElementId())
            .set("deleted", updateForDelete);

        Entity newEntity = builder.build();
        try {
            datastore.update(newEntity);
            return true;
        }
        catch (DatastoreException e) {
            return false;
        }
    }

    public static boolean updateFollowedList(Datastore datastore, FollowedListEntity followedList, boolean updateForDelete) {
        Entity.Builder builder = Entity.newBuilder(DatastoreHelpers.newFollowedListKey(followedList.id))
            .set("userId", followedList.userId())
            .set("listId", followedList.listId())
            .set("ownerId", followedList.ownerId())
            .set("orphaned", followedList.orphaned())
            .set("deleted", updateForDelete);

        Entity newEntity = builder.build();
        try {
            datastore.update(newEntity);
            return true;
        }
        catch (DatastoreException e) {
            return false;
        }
    }

    private static boolean updateTag(Datastore datastore, TagEntity tag, boolean updateForDelete) {
        Entity.Builder builder = Entity.newBuilder(DatastoreHelpers.newTagKey(tag.id))
            .set("tagName", tag.tagName())
            .set("deleted", updateForDelete);

        Entity newEntity = builder.build();
        try {
            datastore.update(newEntity);
            return true;
        }
        catch (DatastoreException e) {
            return false;
        }
    }

    public static void deleteFollowedList(Datastore datastore, long listId) {
        updateFollowedList(datastore, getFollowedList(datastore, listId), true);
    }

    public static void deleteUser(Datastore datastore, long userId) {
        updateUser(datastore, getUser(datastore, userId), true);
    }

    public static void deleteComment(Datastore datastore, long commentId) {
        updateComment(datastore, getComment(datastore,  commentId), true);
    }

    public static void deleteReadingListElement(Datastore datastore, long readingListElementId) {
        updateReadingListElement(datastore, getReadingListElement(datastore, readingListElementId), true);
    }

    public static void deleteReadingList(Datastore datastore, long readingListId) {
        updateReadingList(datastore,  getReadingList(datastore,  readingListId), true);
    }

    // Tag management methods.
    public static long addTagToReadingListElementMapping(Datastore datastore, long userId, long tagId, long readingListElementId) {
        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newTagToReadingListElementKey())
            .set("tagId", tagId)
            .set("userId", userId)
            .set("readingListElementId", readingListElementId)
            .build();
        Entity addedEntity = datastore.add(insertEntity);
        return addedEntity.getKey().getId();
    }

    public static void deleteTagToReadingListElementMapping(Datastore datastore, long userId, long tagId, long readingListElementId) {
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.tagToReadingListElementKind)
            .setFilter(CompositeFilter.and(
                PropertyFilter.eq("tagId", tagId),
                PropertyFilter.eq("userId", userId),
                PropertyFilter.eq("readingListElementId", readingListElementId)))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<TagToReadingListElementEntity> map = new ArrayList<>();
        queryresult.forEachRemaining(list -> map.add(TagToReadingListElementEntity.fromEntity(list)));

        for (TagToReadingListElementEntity e : map) {
            Key key = DatastoreHelpers.newTagToReadingListElementKey(e.id);
            datastore.delete(key);
        }
    }

    public static long addTagToReadingListMapping(Datastore datastore, long userId, long tagId, long readingListId) {
        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newTagToReadingListKey())
            .set("tagId", tagId)
            .set("userId", userId)
            .set("readingListId", readingListId)
            .build();
        Entity addedEntity = datastore.add(insertEntity);
        return addedEntity.getKey().getId();
    }

    public static void deleteTagToReadingListMapping(Datastore datastore, long userId, long tagId, long readingListId) {
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.tagToReadingListKind)
            .setFilter(CompositeFilter.and(
                PropertyFilter.eq("tagId", tagId),
                PropertyFilter.eq("userId", userId),
                PropertyFilter.eq("readingListId", readingListId)))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<TagToReadingListEntity> map = new ArrayList<>();
        queryresult.forEachRemaining(list -> map.add(TagToReadingListEntity.fromEntity(list)));

        for (TagToReadingListEntity e : map) {
            Key key = DatastoreHelpers.newTagToReadingListKey(e.id);
            datastore.delete(key);
        }
    }
}
