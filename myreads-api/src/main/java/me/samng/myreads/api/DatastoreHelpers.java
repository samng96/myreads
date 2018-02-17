package me.samng.myreads.api;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.datastore.*;

import java.io.FileInputStream;

public class DatastoreHelpers {
    private static String keyPath = "/users/samng/gcp-samng-privatekey.json";
    public static String usersKind = "users";
    public static String readingListsKind = "readingLists";
    private static KeyFactory keyFactory = new KeyFactory(MainVerticle.AppId).setKind(usersKind);

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

    public static IncompleteKey newUsersKey() {
        keyFactory.setKind(usersKind);
        return keyFactory.newKey();
    }

    public static Key newUsersKey(Long keyId) {
        keyFactory.setKind(usersKind);
        return keyFactory.newKey(keyId);
    }

    public static IncompleteKey newReadingListsKey() {
        keyFactory.setKind(readingListsKind);
        return keyFactory.newKey();
    }

    public static Key newReadingListsKey(Long keyId) {
        keyFactory.setKind(readingListsKind);
        return keyFactory.newKey(keyId);
    }
}
