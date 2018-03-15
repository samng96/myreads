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
                .end("Invalid request parameters");
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
                .end("Invalid request parameters");
            return;
        }

        FullEntity.Builder<IncompleteKey> builder = Entity.newBuilder(DatastoreHelpers.newReadingListKey())
            .set("name", readingListEntity.name())
            .set("description", readingListEntity.description())
            .set("userId", userId);
        if (readingListEntity.tagIds != null) {
            builder.set("tagIds", ImmutableList.copyOf(readingListEntity.tagIds().stream().map(LongValue::new).iterator()));
        }
        if (readingListEntity.readingListElementIds != null) {
            builder.set("readingListElementIds", ImmutableList.copyOf(readingListEntity.readingListElementIds().stream().map(LongValue::new).iterator()));
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
                .end("Invalid request parameters");
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
                .end("Invalid request parameters");
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

    // Delete a reading list, /users/{userId}/readingLists/{readingListId}
    // TODO: when we delete a list, we need to unlink all RLEs that were linked to this list.
    //
    // TODO: when we delete a list, we need to do something about the followed lists - do we soft delete here
    // TODO: and allow the user to see that it's a list that's no longer around? Or do we have a singleton
    // TODO: that is a deleted list that the follow then points to? Likely the latter.
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
                .end("Invalid request parameters");
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
                .end("Invalid request parameters");
            return;
        }

        ReadingListEntity readingListEntity = getListIfUserOwnsIt(datastore, userId, listId);
        if (readingListEntity == null ||
            readingListEntity.readingListElementIds() == null ||
            !readingListEntity.readingListElementIds().contains(rleId)) {
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
            .end();
    }

    // Add an RLE to this list, /users/{userId}/readingLists/{readingListId}/addReadingListElement
    // Note that the payload is an array of RLE Ids.
    public void addReadingListElementsToReadingList(RoutingContext routingContext) {
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
                .end("Invalid request parameters");
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

        // Note that we're not transactional! As a result, we'll return the list of Ids that we've successfully added,
        // regardless of whether or not we have errors on the overall operation.
        ArrayList<Long> addedIds = new ArrayList<Long>();
        routingContext.response().setStatusCode(200);
        for (long rleId : rleIds) {
            boolean valid = true;

            if (readingListEntity.readingListElementIds() != null && readingListEntity.readingListElementIds().contains(rleId)) {
                continue;
            }

            // We need to add it to our reading list, but we also need to add it to the RLE.
            ReadingListElementEntity rleEntity = ReadingListElementRoute.getReadingListElementIfUserOwnsIt(datastore, userId, rleId);
            assert rleEntity != null;
            assert rleEntity.listIds == null || !rleEntity.listIds.contains(listId);

            if (readingListEntity.readingListElementIds() == null) {
                readingListEntity.readingListElementIds = new ArrayList<Long>();
            }
            readingListEntity.readingListElementIds.add(rleId);

            if (rleEntity.listIds() == null) {
                rleEntity.listIds = new ArrayList<Long>();
            }
            rleEntity.listIds.add(listId);

            if (DatastoreHelpers.updateReadingListEntity(datastore, readingListEntity) &&
                DatastoreHelpers.updateReadingListElementEntity(datastore, rleEntity)) {
                addedIds.add(rleId);
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
            .end(Json.encode(addedIds));
    }

    // POST /users/{userId}/readlingLists/{readingListId}/addTags
    public void addTagsToReadingList(RoutingContext routingContext) {
    }

    // GET /users/{userId}/readlingLists/{readingListId}/tags
    public void getTagsForReadingList(RoutingContext routingContext) {
    }

    // DELETE /users/{userId}/readlingLists/{readingListId}/tags/{tagId}
    public void removeTagFromReadingList(RoutingContext routingContext) {
    }
}
