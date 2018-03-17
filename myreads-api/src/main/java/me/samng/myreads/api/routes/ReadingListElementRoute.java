package me.samng.myreads.api.routes;

import com.google.cloud.datastore.*;
import com.google.common.collect.ImmutableList;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.EntityManager;
import me.samng.myreads.api.entities.ReadingListElementEntity;
import me.samng.myreads.api.entities.TagEntity;

import java.util.ArrayList;
import java.util.List;

public class ReadingListElementRoute {
    public static ReadingListElementEntity getReadingListElementIfUserOwnsIt(
        Datastore datastore,
        long userId,
        long readingListElementId) {

        Key key = DatastoreHelpers.newReadingListElementKey(readingListElementId);
        Entity entity = datastore.get(key);
        if (entity == null) {
            return null;
        }
        ReadingListElementEntity rleEntity = ReadingListElementEntity.fromEntity(entity);
        if (rleEntity.userId != userId) {
            return null;
        }
        return rleEntity;
    }

    // GET /users/{userId}/readingListElements
    public void getAllReadingListElements(RoutingContext routingContext) {
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

        Datastore datastore = DatastoreHelpers.getDatastore();
        List<ReadingListElementEntity> results = DatastoreHelpers.getAllReadingListElementsForUser(datastore, userId);

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(results.toArray()));
    }

    // POST /users/{userId}/readingListElements
    public void postReadingListElement(RoutingContext routingContext) {
        ReadingListElementEntity rleEntity;
        long userId;
        try {
            rleEntity = Json.decodeValue(routingContext.getBody(), ReadingListElementEntity.class);
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        FullEntity.Builder<IncompleteKey> builder = Entity.newBuilder(DatastoreHelpers.newReadingListElementKey())
            .set("name", rleEntity.name())
            .set("description", rleEntity.description())
            .set("userId", userId)
            .set("amazonLink", rleEntity.amazonLink());
        if (rleEntity.tagIds != null) {
            builder.set("tagIds", ImmutableList.copyOf(rleEntity.tagIds().stream().map(LongValue::new).iterator()));
        }
        if (rleEntity.listIds != null) {
            builder.set("listIds", ImmutableList.copyOf(rleEntity.listIds().stream().map(LongValue::new).iterator()));
        }
        FullEntity<IncompleteKey> insertEntity = builder.build();
        Datastore datastore = DatastoreHelpers.getDatastore();
        Entity addedEntity = datastore.add(insertEntity);

        routingContext.response()
            .setStatusCode(201)
            .putHeader("content-type", "text/plain")
            .end(Long.toString(addedEntity.getKey().getId()));
    }

    // GET /users/{userId}/readingListElements/{readingListElementId}
    public void getReadingListElement(RoutingContext routingContext) {
        long rleId;
        long userId;
        try {
            rleId = Long.decode(routingContext.request().getParam("readingListElementId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListElementEntity rleEntity = getReadingListElementIfUserOwnsIt(datastore, userId, rleId);
        if (rleEntity == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(rleEntity));
    }

    // PUT /users/{userId}/readingListElements/{readingListElementId}
    public void putReadingListElement(RoutingContext routingContext) {
        ReadingListElementEntity rleEntity;
        long userId;
        try {
            rleEntity = Json.decodeValue(routingContext.getBody(), ReadingListElementEntity.class);
            rleEntity.id = Long.decode(routingContext.request().getParam("readingListElementId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        if (getReadingListElementIfUserOwnsIt(datastore, userId, rleEntity.id) == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        if (DatastoreHelpers.updateReadingListElementEntity(datastore, rleEntity)) {
            routingContext.response().setStatusCode(204);
        }
        else {
            routingContext.response().setStatusCode(404);
        }

        routingContext.response().putHeader("content-type", "text/plain").end();
    }

    // DELETE /users/{userId}/readingListElements/{readingListElementId}
    public void deleteReadingListElement(RoutingContext routingContext) {
        long rleId;
        long userId;
        try {
            rleId = Long.decode(routingContext.request().getParam("readingListElementId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        if (getReadingListElementIfUserOwnsIt(datastore, userId, rleId) == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }
        EntityManager.DeleteReadingListElement(datastore, rleId);

        routingContext.response()
            .setStatusCode(204)
            .putHeader("content-type", "text/plain")
            .end();
    }

    // POST /users/{userId}/readlingListElements/{readingListElementId}/addTags
    public void addTagsToReadingListElement(RoutingContext routingContext) {
        long rleId;
        long userId;
        Long[] tagIds;
        try {
            rleId = Long.decode(routingContext.request().getParam("readingListElementId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
            tagIds = Json.decodeValue(routingContext.getBody(), Long[].class);
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListElementEntity readingListElementEntity = getReadingListElementIfUserOwnsIt(datastore, userId, rleId);
        if (readingListElementEntity == null) {
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
        for (long tagId : tagIds) {
            boolean valid = true;

            if (readingListElementEntity.tagIds() != null && readingListElementEntity.tagIds().contains(tagId)) {
                continue;
            }

            if (readingListElementEntity.tagIds() == null) {
                readingListElementEntity.tagIds = new ArrayList<Long>();
            }
            readingListElementEntity.tagIds.add(tagId);

            if (DatastoreHelpers.updateReadingListElementEntity(datastore, readingListElementEntity)) {
                addedIds.add(tagId);
            } else {
                valid = false;
            }

            if (!valid) {
                // TODO: What error should we give here when we fail to update an entity? Should it really be
                // TODO: a 400? Or should this be some sort of 500? Or should we return some 202 type and retry?
                routingContext.response().setStatusCode(400);
                break;
            }
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(addedIds.toArray()));
    }

    // GET /users/{userId}/readlingListElements/{readingListElementId}/tags
    public void getTagsForReadingListElement(RoutingContext routingContext) {
        long rleId;
        long userId;
        try {
            rleId = Long.decode(routingContext.request().getParam("readingListElementId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListElementEntity readingListElementEntity = getReadingListElementIfUserOwnsIt(datastore, userId, rleId);
        if (readingListElementEntity == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        List<TagEntity> tags = TagRoute.getTagEntities(datastore, readingListElementEntity.tagIds());

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(tags.toArray()));
    }

    // DELETE /users/{userId}/readlingListElements/{readingListElementId}/tags/{tagId}
    public void removeTagFromReadingListElement(RoutingContext routingContext) {
        long rleId;
        long userId;
        long tagId;
        try {
            rleId = Long.decode(routingContext.request().getParam("readingListElementId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
            tagId = Long.decode(routingContext.request().getParam("tagId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListElementEntity readingListElementEntity = getReadingListElementIfUserOwnsIt(datastore, userId, rleId);
        if (readingListElementEntity == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        if (readingListElementEntity.tagIds() == null || !readingListElementEntity.tagIds().contains(tagId)) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end("Tag not found");
            return;
        }

        readingListElementEntity.tagIds().remove(tagId);
        if (DatastoreHelpers.updateReadingListElementEntity(datastore, readingListElementEntity)) {
            routingContext.response().setStatusCode(204);
        } else {
            routingContext.response().setStatusCode(404);
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end();
    }
}
