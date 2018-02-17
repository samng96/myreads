package me.samng.myreads.api.routes;

import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.*;
import com.google.common.collect.ImmutableList;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.entities.ReadingListEntity;

import java.util.ArrayList;

public class ReadingListRoute {
    private ReadingListEntity getListIfUserOwnsIt(
        Datastore datastore,
        long userId,
        long listId) {

        Key key = DatastoreHelpers.newReadingListsKey(listId);
        Entity entity = datastore.get(key);
        if (entity == null) {
            return null;
        }
        ReadingListEntity readingListEntity = ReadingListEntity.fromEntity(entity);
        if (readingListEntity.userId != userId) {
            return null;
        }
        return readingListEntity;
    }

    // Get all lists for a given user - /users/{userId}/readinglists
    public void getAllReadingLists(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        long userId = Long.decode(routingContext.request().getParam("userId"));

        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.readingListsKind)
            .setFilter(PropertyFilter.eq("userId", userId))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<ReadingListEntity> results = new ArrayList<ReadingListEntity>();
        queryresult.forEachRemaining(user -> { results.add(ReadingListEntity.fromEntity(user)); });

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(results));
    }

    // Post a new reading list - /users/{userId}/readingLists
    public void postReadingList(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListEntity readingListEntity = Json.decodeValue(routingContext.getBody(), ReadingListEntity.class);
        long userId = Long.decode(routingContext.request().getParam("userId"));

        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newReadingListsKey())
            .set("name", readingListEntity.name())
            .set("description", readingListEntity.description())
            .set("userId", userId)
            .set("tags", ImmutableList.copyOf(readingListEntity.tags().stream().map(LongValue::new).iterator()))
            .build();
        Entity addedEntity = datastore.add(insertEntity);

        routingContext.response()
            .setStatusCode(201)
            .putHeader("content-type", "text/plain")
            .end(Long.toString(addedEntity.getKey().getId()));
    }

    // Get a specific reading list, /users/{userId}/readingLists/{readingListId}
    public void getReadingList(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        long listId = Long.decode(routingContext.request().getParam("readingListId"));
        long userId = Long.decode(routingContext.request().getParam("userId"));

        ReadingListEntity readingListEntity = getListIfUserOwnsIt(datastore, userId, listId);
        if (readingListEntity == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(readingListEntity));
    }

    // Update a list, /users/{userId}/readingLists/{readingListId}
    public void putReadingList(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListEntity readingListEntity = Json.decodeValue(routingContext.getBody(), ReadingListEntity.class);
        readingListEntity.id = Long.decode(routingContext.request().getParam("readingListId"));
        long userId = Long.decode(routingContext.request().getParam("userId"));

        if (getListIfUserOwnsIt(datastore, userId, readingListEntity.id) == null) {
            if (readingListEntity == null) {
                routingContext.response()
                    .setStatusCode(404)
                    .putHeader("content-type", "text/plain")
                    .end();
                return;
            }
        }
        Entity newEntity = Entity.newBuilder(DatastoreHelpers.newReadingListsKey(readingListEntity.id))
            .set("name", readingListEntity.name())
            .set("description", readingListEntity.description())
            .set("userId", readingListEntity.userId())
            .set("tags", ImmutableList.copyOf(readingListEntity.tags().stream().map(LongValue::new).iterator()))
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
        long listId = Long.decode(routingContext.request().getParam("readingListId"));
        long userId = Long.decode(routingContext.request().getParam("userId"));

        if (getListIfUserOwnsIt(datastore, userId, listId) == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }
        datastore.delete(DatastoreHelpers.newReadingListsKey(listId));

        routingContext.response()
            .setStatusCode(204)
            .putHeader("content-type", "text/plain")
            .end();
    }
}