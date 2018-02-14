import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.io.FileInputStream;
import java.util.ArrayList;

public class UserRoute {
    private String keyPath = "/users/samng/gcp-samng-privatekey.json";

    private Datastore getDatastore() {
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

        return options.getService();
    }

    // Get all users
    public void getAllUsers(RoutingContext routingContext) {
        Datastore datastore = getDatastore();

        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind("users")
            .setOrderBy(StructuredQuery.OrderBy.desc("priority"))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<Entity> results = new ArrayList<Entity>();
        queryresult.forEachRemaining(user -> { results.add(user); });

        routingContext.response()
                .putHeader("content-type", "text/plain")
                .end(Json.encode(results));
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
        String userId = routingContext.request().getParam("userId");
        Datastore datastore = getDatastore();

        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind("users")
            .setFilter(PropertyFilter.eq("userId", userId))
            .setOrderBy(StructuredQuery.OrderBy.desc("priority"))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        if (!queryresult.hasNext()) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain");
        }

        Entity result = queryresult.next();
        assert !queryresult.hasNext();

        routingContext.response()
                .putHeader("content-type", "text/plain")
                .end(Json.encode(result));
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
