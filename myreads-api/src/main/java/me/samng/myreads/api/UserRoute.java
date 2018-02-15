package me.samng.myreads.api;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.datastore.*;
import me.samng.myreads.api.entities.UserEntity;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.io.FileInputStream;
import java.util.ArrayList;

public class UserRoute {
    private String keyPath = "/users/samng/gcp-samng-privatekey.json";
    private String datastoreKind = "users";
    private KeyFactory keyFactory = new KeyFactory(MainVerticle.AppId).setKind(datastoreKind);

    private Datastore getDatastore() {
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

    // Get all users
    public void getAllUsers(RoutingContext routingContext) {
        Datastore datastore = getDatastore();

        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(datastoreKind)
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<UserEntity> results = new ArrayList<UserEntity>();
        queryresult.forEachRemaining(user -> { results.add(UserEntity.fromEntity(user)); });

        routingContext.response()
                .putHeader("content-type", "text/plain")
                .end(Json.encode(results));
    }

    // Post a new user
    public void postUser(RoutingContext routingContext) {
        Datastore datastore = getDatastore();
        UserEntity userEntity = Json.decodeValue(routingContext.getBody(), UserEntity.class);

        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(keyFactory.newKey())
            .set("name", userEntity.name())
            .set("email", userEntity.email())
            .set("userId", userEntity.userId())
            .build();
        Entity addedEntity = datastore.add(insertEntity);

        routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "text/plain")
                .end(Long.toString(addedEntity.getKey().getId()));
    }

    // Get a specific user, /users/{userId}
    public void getUser(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        Datastore datastore = getDatastore();

        Key key = keyFactory.newKey(Long.decode(id));
        Entity entity = datastore.get(key);

        if (entity == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        routingContext.response()
                .putHeader("content-type", "text/plain")
                .end(Json.encode(UserEntity.fromEntity(entity)));
    }

    // Update a user, /users/{userId}
    public void putUser(RoutingContext routingContext) {
        Datastore datastore = getDatastore();
        UserEntity userEntity = Json.decodeValue(routingContext.getBody(), UserEntity.class);
        userEntity.id = Long.decode(routingContext.request().getParam("id"));

        // First get the entity
        Key key = keyFactory.newKey(userEntity.id());
        Entity newEntity = Entity.newBuilder(key)
            .set("name", userEntity.name())
            .set("email", userEntity.email())
            .set("userId", userEntity.userId())
            .build();
        try {
            datastore.update(newEntity);
            routingContext.response().setStatusCode(204);
        }
        catch (DatastoreException e) {
            routingContext.response().setStatusCode(404);
        }

        routingContext.response().putHeader("content-type", "text/plain").end();
    }

    // Delete a user, /users/{userId}
    public void deleteUser(RoutingContext routingContext) {
        Datastore datastore = getDatastore();
        Key key = keyFactory.newKey(Long.decode(routingContext.request().getParam("id")));
        datastore.delete(key);

        routingContext.response()
                .setStatusCode(204)
                .putHeader("content-type", "text/plain")
                .end();
    }
}
