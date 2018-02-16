package me.samng.myreads.api.routes;

import com.google.cloud.datastore.*;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.entities.UserEntity;

import java.util.ArrayList;

public class ReadingListRoute {
    // Get all lists for a given user - /users/{userId}/readinglists
    public void getAllReadingLists(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();

        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.datastoreKind)
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<UserEntity> results = new ArrayList<UserEntity>();
        queryresult.forEachRemaining(user -> { results.add(UserEntity.fromEntity(user)); });

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(results));
    }

    // Post a new reading list - /users/{userId}/readingLists
    public void postReadingList(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        UserEntity userEntity = Json.decodeValue(routingContext.getBody(), UserEntity.class);

        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.keyFactory.newKey())
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

    // Get a specific reading list, /users/{userId}/readingLists/{readingListId}
    public void getReadingList(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        Datastore datastore = DatastoreHelpers.getDatastore();

        Key key = DatastoreHelpers.keyFactory.newKey(Long.decode(id));
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

    // Update a list, /users/{userId}/readingLists/{readingListId}
    public void putReadingList(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        UserEntity userEntity = Json.decodeValue(routingContext.getBody(), UserEntity.class);
        userEntity.id = Long.decode(routingContext.request().getParam("id"));

        // First get the entity
        Key key = DatastoreHelpers.keyFactory.newKey(userEntity.id());
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

    // Delete a user, /users/{userId}/readingLists/{readingListId}
    public void deleteReadingList(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        Key key = DatastoreHelpers.keyFactory.newKey(Long.decode(routingContext.request().getParam("id")));
        datastore.delete(key);

        routingContext.response()
            .setStatusCode(204)
            .putHeader("content-type", "text/plain")
            .end();
    }
}
