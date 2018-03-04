package me.samng.myreads.api.routes;

import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.common.collect.ImmutableList;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.entities.ReadingListElementEntity;
import me.samng.myreads.api.entities.ReadingListEntity;

import java.util.ArrayList;

public class ReadingListRoute {
    private ReadingListEntity getListIfUserOwnsIt(
        Datastore datastore,
        long userId,
        long listId) {

        Key key = DatastoreHelpers.newReadingListKey(listId);
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
        long userId;
        try {
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.readingListKind)
            .setFilter(PropertyFilter.eq("userId", userId))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<ReadingListEntity> results = new ArrayList<ReadingListEntity>();
        queryresult.forEachRemaining(list -> { results.add(ReadingListEntity.fromEntity(list)); });

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(results));
    }

    // Post a new reading list - /users/{userId}/readingLists
    public void postReadingList(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListEntity readingListEntity;
        long userId;
        try {
            readingListEntity = Json.decodeValue(routingContext.getBody(), ReadingListEntity.class);
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        FullEntity.Builder<IncompleteKey> builder = Entity.newBuilder(DatastoreHelpers.newReadingListKey())
            .set("name", readingListEntity.name())
            .set("description", readingListEntity.description())
            .set("userId", userId);
        if (readingListEntity.tagIds != null) {
            builder.set("tagIds", ImmutableList.copyOf(readingListEntity.tagIds().stream().map(LongValue::new).iterator()));
        }
        FullEntity<IncompleteKey> insertEntity = builder.build();
        Entity addedEntity = datastore.add(insertEntity);

        routingContext.response()
            .setStatusCode(201)
            .putHeader("content-type", "text/plain")
            .end(Long.toString(addedEntity.getKey().getId()));
    }

    // Get a specific reading list, /users/{userId}/readingLists/{readingListId}
    public void getReadingList(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        long listId;
        long userId;
        try {
            listId = Long.decode(routingContext.request().getParam("readingListId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

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
        ReadingListEntity readingListEntity;
        long userId;
        try {
            readingListEntity = Json.decodeValue(routingContext.getBody(), ReadingListEntity.class);
            readingListEntity.id = Long.decode(routingContext.request().getParam("readingListId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        if (getListIfUserOwnsIt(datastore, userId, readingListEntity.id) == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        if (DatastoreHelpers.updateReadingListEntity(datastore, readingListEntity)) {
            routingContext.response().setStatusCode(204);
        }
        else {
            routingContext.response().setStatusCode(404);
        }

        routingContext.response().putHeader("content-type", "text/plain").end();
    }

    // Delete a user, /users/{userId}/readingLists/{readingListId}
    public void deleteReadingList(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        long listId;
        long userId;
        try {
            listId = Long.decode(routingContext.request().getParam("readingListId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        if (getListIfUserOwnsIt(datastore, userId, listId) == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }
        datastore.delete(DatastoreHelpers.newReadingListKey(listId));

        routingContext.response()
            .setStatusCode(204)
            .putHeader("content-type", "text/plain")
            .end();
    }

    // Remove an RLE from this list, /users/{userId}/readingLists/{readingListId}/readingListElements/{readingListElementId}
    public void deleteReadingListElementFromReadingList(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        long listId;
        long userId;
        long rleId;
        try {
            listId = Long.decode(routingContext.request().getParam("readingListId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
            rleId = Long.decode(routingContext.request().getParam("readingListElementId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        ReadingListEntity readingListEntity = getListIfUserOwnsIt(datastore, userId, listId);
        if (readingListEntity == null || !readingListEntity.readingListElementIds().contains(rleId)) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        // We need to remove it from our reading list, but we also need to remove it from the RLE.
        ReadingListElementEntity rleEntity = ReadingListElementRoute.getReadingListElementIfUserOwnsIt(datastore, userId, rleId);
        assert rleEntity != null;
        assert rleEntity.listIds.contains(listId);

        readingListEntity.readingListElementIds.remove(rleId);
        rleEntity.listIds.remove(listId);

        if (DatastoreHelpers.updateReadingListEntity(datastore, readingListEntity) &&
            DatastoreHelpers.updateReadingListElementEntity(datastore, rleEntity)) {
            routingContext.response().setStatusCode(204);
        }
        else {
            routingContext.response().setStatusCode(404);
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(readingListEntity));
    }

    // Add an RLE to this list, /users/{userId}/readingLists/{readingListId}/addReadingListElement
    // Note taht the payload is an array of RLE Ids.
    public void addReadingListElementToReadingList(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        long listId;
        long userId;
        Long[] rleIds;
        try {
            listId = Long.decode(routingContext.request().getParam("readingListId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
            rleIds = Json.decodeValue(routingContext.getBody(), Long[].class);
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        ReadingListEntity readingListEntity = getListIfUserOwnsIt(datastore, userId, listId);
        if (readingListEntity == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        // Note that we're not transactional!
        // TODO: If we fail, return the of ids that we successfully updated.
        routingContext.response().setStatusCode(204);
        for (long rleId : rleIds) {
            boolean valid = true;

            if (readingListEntity.readingListElementIds().contains(rleId)) {
                continue;
            }

            // We need to add it to our reading list, but we also need to add it to the RLE.
            ReadingListElementEntity rleEntity = ReadingListElementRoute.getReadingListElementIfUserOwnsIt(datastore, userId, rleId);
            assert rleEntity != null;
            assert !rleEntity.listIds.contains(listId);

            readingListEntity.readingListElementIds.add(rleId);
            rleEntity.listIds.add(listId);

            if (DatastoreHelpers.updateReadingListEntity(datastore, readingListEntity) &&
                DatastoreHelpers.updateReadingListElementEntity(datastore, rleEntity)) {
            } else {
                valid = false;
            }

            if (!valid) {
                routingContext.response().setStatusCode(404);
                break;
            }
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(readingListEntity));
    }
}
