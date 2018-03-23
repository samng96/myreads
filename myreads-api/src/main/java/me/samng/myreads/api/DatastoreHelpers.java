package me.samng.myreads.api;

import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.datastore.*;
import com.google.common.collect.ImmutableList;
import me.samng.myreads.api.entities.*;

import javax.xml.stream.events.Comment;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class DatastoreHelpers {
    private static String keyPath = "/users/samng/gcp-samng-privatekey.json";
    public static String userKind = "user";
    public static String readingListKind = "readingList";
    public static String followedListKind = "followedList";
    public static String readingListElementKind = "readingListElement";
    public static String commentKind = "comment";
    public static String tagKind = "tag";
    private static KeyFactory keyFactory = new KeyFactory(MainVerticle.AppId);

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

    public static long createUser(Datastore datastore, UserEntity userEntity) {
        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newUserKey())
            .set("name", userEntity.name())
            .set("email", userEntity.email())
            .set("userId", userEntity.userId())
            .build();
        Entity entity = datastore.add(insertEntity);
        return entity.getKey().getId();
    }

    public static long createReadingList(Datastore datastore, ReadingListEntity readingListEntity) {
        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newReadingListKey())
            .set("name", readingListEntity.name())
            .set("description", readingListEntity.description())
            .set("userId", readingListEntity.userId)
            .set("tagIds", ImmutableList.copyOf(readingListEntity.tagIds().stream().map(LongValue::new).iterator()))
            .set("readingListElementIds", ImmutableList.copyOf(readingListEntity.readingListElementIds().stream().map(LongValue::new).iterator()))
            .build();
        Entity entity = datastore.add(insertEntity);
        return entity.getKey().getId();
    }

    public static long createFollowedList(Datastore datastore, FollowedListEntity followedListEntity) {
        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newFollowedListKey())
            .set("userId", followedListEntity.userId())
            .set("listId", followedListEntity.listId())
            .set("ownerId", followedListEntity.ownerId())
            .build();
        Entity entity = datastore.add(insertEntity);
        return entity.getKey().getId();
    }

    public static long createReadingListElement(Datastore datastore, ReadingListElementEntity rleEntity) {
        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newReadingListElementKey())
            .set("name", rleEntity.name())
            .set("description", rleEntity.description())
            .set("userId", rleEntity.userId())
            .set("amazonLink", rleEntity.amazonLink())
            .set("tagIds", ImmutableList.copyOf(rleEntity.tagIds().stream().map(LongValue::new).iterator()))
            .set("listIds", ImmutableList.copyOf(rleEntity.listIds().stream().map(LongValue::new).iterator()))
            .build();

        Entity entity = datastore.add(insertEntity);
        return entity.getKey().getId();
    }

    public static long createComment(Datastore datastore, CommentEntity commentEntity) {
        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newCommentKey())
            .set("commentText", commentEntity.commentText())
            .set("userId", commentEntity.userId)
            .set("readingListElementId", commentEntity.readingListElementId).build();
        Entity entity = datastore.add(insertEntity);
        return entity.getKey().getId();
    }

    public static long createTag(Datastore datastore, TagEntity tagEntity) {
        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newTagKey())
            .set("tagName", tagEntity.tagName()).build();
        Entity addedEntity = datastore.add(insertEntity);
        return addedEntity.getKey().getId();
    }

    public static List<UserEntity> getAllUsers(Datastore datastore) {
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.userKind)
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<UserEntity> results = new ArrayList<>();
        queryresult.forEachRemaining(user -> results.add(UserEntity.fromEntity(user)));

        return results;
    }

    public static List<ReadingListEntity> getAllReadingListsForUser(
        Datastore datastore,
        long userId) {
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.readingListKind)
            .setFilter(PropertyFilter.eq("userId", userId))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<ReadingListEntity> results = new ArrayList<>();
        queryresult.forEachRemaining(list -> results.add(ReadingListEntity.fromEntity(list)));

        return results;
    }

    public static List<ReadingListElementEntity> getAllReadingListElementsForUser(
        Datastore datastore,
        long userId) {
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.readingListElementKind)
            .setFilter(PropertyFilter.eq("userId", userId))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<ReadingListElementEntity> results = new ArrayList<>();
        queryresult.forEachRemaining(list -> results.add(ReadingListElementEntity.fromEntity(list)));

        return results;
    }

    public static List<FollowedListEntity> getAllFollowedListsForUser(
        Datastore datastore,
        long userId) {
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.followedListKind)
            .setFilter(PropertyFilter.eq("userId", userId))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<FollowedListEntity> results = new ArrayList<>();
        queryresult.forEachRemaining(followedList -> results.add(FollowedListEntity.fromEntity(followedList)));

        return results;
    }

    public static UserEntity getUser(Datastore datastore, long userId) {
        Key key = DatastoreHelpers.newUserKey(userId);
        Entity entity = datastore.get(key);
        if (entity == null) {
            return null;
        }
        return UserEntity.fromEntity(entity);
    }

    public static ReadingListEntity getReadingList(Datastore datastore, long readingListId) {
        Key key = DatastoreHelpers.newReadingListKey(readingListId);
        Entity entity = datastore.get(key);

        if (entity == null) {
            return null;
        }
        return ReadingListEntity.fromEntity(entity);
    }

    public static ReadingListElementEntity getReadingListElement(Datastore datastore, long readingListElementId) {
        Key key = DatastoreHelpers.newReadingListElementKey(readingListElementId);
        Entity entity = datastore.get(key);
        if (entity == null) {
            return null;
        }
        return ReadingListElementEntity.fromEntity(entity);
    }

    public static FollowedListEntity getFollowedList(Datastore datastore, long listId) {
        Key key = DatastoreHelpers.newFollowedListKey(listId);
        Entity entity = datastore.get(key);
        if (entity == null) {
            return null;
        }
        return FollowedListEntity.fromEntity(entity);
    }

    public static CommentEntity getComment(Datastore datastore, long commentId) {
        Key key = DatastoreHelpers.newCommentKey(commentId);
        Entity entity = datastore.get(key);
        if (entity == null) {
            return null;
        }
        return CommentEntity.fromEntity(entity);
    }

    public static TagEntity getTag(Datastore datastore, long tagId) {
        Key key = DatastoreHelpers.newTagKey(tagId);
        Entity entity = datastore.get(key);
        if (entity == null) {
            return null;
        }
        TagEntity tagEntity = TagEntity.fromEntity(entity);
        return tagEntity;
    }

    public static boolean updateUser(Datastore datastore, UserEntity userEntity) {
        Key key = DatastoreHelpers.newUserKey(userEntity.id());
        Entity newEntity = Entity.newBuilder(key)
            .set("name", userEntity.name())
            .set("email", userEntity.email())
            .set("userId", userEntity.userId())
            .build();

        try {
            datastore.update(newEntity);
            return true;
        }
        catch (DatastoreException e) {
            return false;
        }
    }

    public static boolean updateReadingList(
        Datastore datastore,
        ReadingListEntity readingListEntity) {
        Entity.Builder builder = Entity.newBuilder(DatastoreHelpers.newReadingListKey(readingListEntity.id))
            .set("name", readingListEntity.name())
            .set("description", readingListEntity.description())
            .set("userId", readingListEntity.userId())
            .set("tagIds", ImmutableList.copyOf(readingListEntity.tagIds().stream().map(LongValue::new).iterator()))
            .set("readingListElementIds", ImmutableList.copyOf(readingListEntity.readingListElementIds().stream().map(LongValue::new).iterator()));

        Entity newEntity = builder.build();
        try {
            datastore.update(newEntity);
            return true;
        }
        catch (DatastoreException e) {
            return false;
        }
    }

    public static boolean updateReadingListElement(
        Datastore datastore,
        ReadingListElementEntity readingListElementEntity) {
            Entity.Builder builder = Entity.newBuilder(DatastoreHelpers.newReadingListElementKey(readingListElementEntity.id))
                .set("name", readingListElementEntity.name())
                .set("description", readingListElementEntity.description())
                .set("userId", readingListElementEntity.userId())
                .set("amazonLink", readingListElementEntity.amazonLink())
                .set("tagIds", ImmutableList.copyOf(readingListElementEntity.tagIds().stream().map(LongValue::new).iterator()))
                .set("listIds", ImmutableList.copyOf(readingListElementEntity.listIds().stream().map(LongValue::new).iterator()))
                .set("commentIds", ImmutableList.copyOf(readingListElementEntity.commentIds().stream().map(LongValue::new).iterator()));
        Entity newEntity = builder.build();
        try {
            datastore.update(newEntity);
            return true;
        }
        catch (DatastoreException e) {
            return false;
        }
    }

    public static boolean updateComment(Datastore datastore, CommentEntity commentEntity) {
        Entity.Builder builder = Entity.newBuilder(DatastoreHelpers.newCommentKey(commentEntity.id))
            .set("commentText", commentEntity.commentText())
            .set("userId", commentEntity.userId())
            .set("readingListElementId", commentEntity.readingListElementId());

        Entity newEntity = builder.build();
        try {
            datastore.update(newEntity);
            return true;
        }
        catch (DatastoreException e) {
            return false;
        }
    }

    public static void deleteFollowedList(Datastore datastore, long id) {
        Key key = DatastoreHelpers.newFollowedListKey(id);
        datastore.delete(key);
    }

    public static void deleteUser(Datastore datastore, long userId) {
        Key key = DatastoreHelpers.newUserKey(userId);
        datastore.delete(key);
    }

    public static void deleteComment(Datastore datastore, long commentId) {
        Key key = DatastoreHelpers.newCommentKey(commentId);
        datastore.delete(key);
    }

    public static void deleteReadingListElement(Datastore datastore, long readingListElementId) {
        Key key = DatastoreHelpers.newReadingListElementKey(readingListElementId);
        datastore.delete(key);
    }

    public static void deleteReadingList(Datastore datastore, long readingListId) {
        Key key = DatastoreHelpers.newReadingListKey(readingListId);
        datastore.delete(key);
    }

    public static void deleteTag(Datastore datastore, long tagId) {
        Key key = DatastoreHelpers.newTagKey(tagId);
        datastore.delete(key);
    }
}
