import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.datastore.*;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.io.FileInputStream;

public class UserRoute {
    private String keyPath = "/users/samng/gcp-samng-privatekey.json";

    // Get all users
    public void getAllUsers(RoutingContext routingContext) {
        // For running in GCP
        // Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

        // For running against local emulator.
        DatastoreOptions options = null;
        try {
            options = DatastoreOptions.newBuilder()
                .setProjectId(MainVerticle.AppId)
                .setCredentials(GoogleCredentials.fromStream(
                    new FileInputStream(keyPath))).build();
        }
        catch (Exception e) {
        }

        Datastore datastore = options.getService();
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind("users")
            .setOrderBy(StructuredQuery.OrderBy.desc("priority"))
            .build();
        QueryResults<Entity> users = datastore.run(query);

        routingContext.response()
                .putHeader("content-type", "text/plain")
                .end(Json.encode(users));
    }

    // Post a new user
    public void postUser(RoutingContext routingContext) {
        routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "text/plain")
                .end("postUser");
    }

    // Get a specific user, /users/{userId}
    public void getUser(RoutingContext routingContext) {
        routingContext.response()
                .putHeader("content-type", "text/plain")
                .end("getUser");
    }

    // Update a user, /users/{userId}
    public void putUser(RoutingContext routingContext) {
        routingContext.response()
                .putHeader("content-type", "text/plain")
                .end("putUser");
    }

    // Delete a user, /users/{userId}
    public void deleteUser(RoutingContext routingContext) {
        routingContext.response()
                .setStatusCode(204)
                .putHeader("content-type", "text/plain")
                .end("deleteUser");
    }
}
