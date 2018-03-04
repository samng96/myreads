package me.samng.myreads.api;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.datastore.*;

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
}
