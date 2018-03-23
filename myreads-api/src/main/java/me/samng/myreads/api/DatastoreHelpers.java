package me.samng.myreads.api;

import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.datastore.*;
import com.google.common.collect.ImmutableList;
import me.samng.myreads.api.entities.*;

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

    public static IncompleteKey newUserKey() {
        keyFactory.setKind(userKind);
        return keyFactory.newKey();
    }

    public static Key newUserKey(Long keyId) {
        keyFactory.setKind(userKind);
        return keyFactory.newKey(keyId);
    }

    public static IncompleteKey newCommentKey() {
        keyFactory.setKind(commentKind);
        return keyFactory.newKey();
    }

    public static Key newCommentKey(Long keyId) {
        keyFactory.setKind(commentKind);
        return keyFactory.newKey(keyId);
    }

    public static IncompleteKey newTagKey() {
        keyFactory.setKind(tagKind);
        return keyFactory.newKey();
    }

    public static Key newTagKey(Long keyId) {
        keyFactory.setKind(tagKind);
        return keyFactory.newKey(keyId);
    }

    public static IncompleteKey newReadingListKey() {
        keyFactory.setKind(readingListKind);
        return keyFactory.newKey();
    }

    public static Key newReadingListKey(Long keyId) {
        keyFactory.setKind(readingListKind);
        return keyFactory.newKey(keyId);
    }

    public static IncompleteKey newReadingListElementKey() {
        keyFactory.setKind(readingListElementKind);
        return keyFactory.newKey();
    }

    public static Key newReadingListElementKey(Long keyId) {
        keyFactory.setKind(readingListElementKind);
        return keyFactory.newKey(keyId);
    }

    public static IncompleteKey newFollowedListKey() {
        keyFactory.setKind(followedListKind);
        return keyFactory.newKey();
    }

    public static Key newFollowedListKey(Long keyId) {
        keyFactory.setKind(followedListKind);
        return keyFactory.newKey(keyId);
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

    public static ReadingListEntity getReadingList(Datastore datastore, long readingListId) {
        Key key = DatastoreHelpers.newReadingListKey(readingListId);
        return ReadingListEntity.fromEntity(datastore.get(key));
    }

    public static ReadingListElementEntity getReadingListElement(Datastore datastore, long readingListElementId) {
        Key key = DatastoreHelpers.newReadingListElementKey(readingListElementId);
        return ReadingListElementEntity.fromEntity(datastore.get(key));
    }

    public static boolean updateReadingListEntity(
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

    public static boolean updateReadingListElementEntity(
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

    public static boolean updateCommentEntity(Datastore datastore, CommentEntity commentEntity) {
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
}
