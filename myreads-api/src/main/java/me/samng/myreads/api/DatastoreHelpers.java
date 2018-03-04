package me.samng.myreads.api;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.datastore.*;
import com.google.common.collect.ImmutableList;
import me.samng.myreads.api.entities.ReadingListElementEntity;
import me.samng.myreads.api.entities.ReadingListEntity;

import java.io.FileInputStream;

public class DatastoreHelpers {
    private static String keyPath = "/users/samng/gcp-samng-privatekey.json";
    public static String userKind = "user";
    public static String readingListKind = "readingList";
    public static String followedListKind = "followedList";
    public static String readingListElementKind = "readingListElement";
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

    public static IncompleteKey newReadingListKey() {
        keyFactory.setKind(readingListKind);
        return keyFactory.newKey();
    }

    public static Key newReadingListKey(Long keyId) {
        keyFactory.setKind(readingListKind);
        return keyFactory.newKey(keyId);
    }

    public static IncompleteKey newReadingElementListKey() {
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

    public static boolean updateReadingListEntity(
        Datastore datastore,
        ReadingListEntity readingListEntity) {
        Entity.Builder builder = Entity.newBuilder(DatastoreHelpers.newReadingListKey(readingListEntity.id))
            .set("name", readingListEntity.name())
            .set("description", readingListEntity.description())
            .set("userId", readingListEntity.userId());
        if (readingListEntity.tagIds != null)
        {
            builder.set("tagIds", ImmutableList.copyOf(readingListEntity.tagIds().stream().map(LongValue::new).iterator()));
        }
        if (readingListEntity.readingListElementIds != null)
        {
            builder.set("readingListElementIds", ImmutableList.copyOf(readingListEntity.readingListElementIds().stream().map(LongValue::new).iterator()));
        }
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
            Entity.Builder builder = Entity.newBuilder(DatastoreHelpers.newReadingListKey(readingListElementEntity.id))
                .set("name", readingListElementEntity.name())
                .set("description", readingListElementEntity.description())
                .set("userId", readingListElementEntity.userId())
                .set("amazonLink", readingListElementEntity.amazonLink());

        if (readingListElementEntity.tagIds != null)
        {
            builder.set("tagIds", ImmutableList.copyOf(readingListElementEntity.tagIds().stream().map(LongValue::new).iterator()));
        }
        if (readingListElementEntity.listIds != null)
        {
            builder.set("listIds", ImmutableList.copyOf(readingListElementEntity.listIds().stream().map(LongValue::new).iterator()));
        }
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
