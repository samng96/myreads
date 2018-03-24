package me.samng.myreads.api.routes;

import com.google.cloud.datastore.Datastore;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.EntityManager;
import me.samng.myreads.api.entities.ReadingListElementEntity;
import me.samng.myreads.api.entities.ReadingListEntity;
import me.samng.myreads.api.entities.TagEntity;
import me.samng.myreads.api.entities.UserEntity;

import java.util.ArrayList;
import java.util.List;

public class ReadingListRoute {
    private ReadingListEntity getListIfUserOwnsIt(
        Datastore datastore,
        long userId,
        long listId) {
        ReadingListEntity readingListEntity = DatastoreHelpers.getReadingList(datastore, listId);
        if (readingListEntity == null || readingListEntity.userId != userId) {
            return null;
        }
        return readingListEntity;
    }

    // Get all lists for a given user - /users/{userId}/readinglists
    public void getAllReadingLists(RoutingContext routingContext) {
        long userId;
        try {
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        List<ReadingListEntity> results = DatastoreHelpers.getAllReadingListsForUser(datastore, userId);

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(results.toArray()));
    }

    // Post a new reading list - /users/{userId}/readingLists
    public void postReadingList(RoutingContext routingContext) {
        ReadingListEntity readingListEntity;
        long userId;
        try {
            readingListEntity = Json.decodeValue(routingContext.getBody(), ReadingListEntity.class);
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        UserEntity userEntity = DatastoreHelpers.getUser(datastore, userId);
        if (userEntity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        readingListEntity.userId = userId;
        long addedId = DatastoreHelpers.createReadingList(datastore, readingListEntity);

        routingContext.response()
            .setStatusCode(HttpResponseStatus.CREATED.code())
            .putHeader("content-type", "text/plain")
            .end(Long.toString(addedId));
    }

    // Get a specific reading list, /users/{userId}/readingLists/{readingListId}
    public void getReadingList(RoutingContext routingContext) {
        long listId;
        long userId;
        try {
            listId = Long.decode(routingContext.request().getParam("readingListId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListEntity readingListEntity = getListIfUserOwnsIt(datastore, userId, listId);
        if (readingListEntity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
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
        ReadingListEntity readingListEntity;
        long userId;
        try {
            readingListEntity = Json.decodeValue(routingContext.getBody(), ReadingListEntity.class);
            readingListEntity.id = Long.decode(routingContext.request().getParam("readingListId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        if (getListIfUserOwnsIt(datastore, userId, readingListEntity.id) == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        if (DatastoreHelpers.updateReadingList(datastore, readingListEntity, false)) {
            routingContext.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
        }
        else {
            routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
        }

        routingContext.response().putHeader("content-type", "text/plain").end();
    }

    // Delete a reading list, /users/{userId}/readingLists/{readingListId}
    public void deleteReadingList(RoutingContext routingContext) {
        long listId;
        long userId;
        try {
            listId = Long.decode(routingContext.request().getParam("readingListId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        if (getListIfUserOwnsIt(datastore, userId, listId) == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }
        EntityManager.DeleteReadingList(datastore, listId);

        routingContext.response()
            .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
            .putHeader("content-type", "text/plain")
            .end();
    }

    // Remove an RLE from this list, /users/{userId}/readingLists/{readingListId}/readingListElements/{readingListElementId}
    public void deleteReadingListElementFromReadingList(RoutingContext routingContext) {
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
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListEntity readingListEntity = getListIfUserOwnsIt(datastore, userId, listId);
        if (readingListEntity == null ||
            readingListEntity.readingListElementIds() == null ||
            !readingListEntity.readingListElementIds().contains(rleId)) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
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

        if (DatastoreHelpers.updateReadingList(datastore, readingListEntity, false) &&
            DatastoreHelpers.updateReadingListElement(datastore, rleEntity, false)) {
            routingContext.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
        }
        else {
            routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end();
    }

    // Add an RLE to this list, /users/{userId}/readingLists/{readingListId}/addReadingListElement
    // Note that the payload is an array of RLE Ids.
    public void addReadingListElementsToReadingList(RoutingContext routingContext) {
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
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListEntity readingListEntity = getListIfUserOwnsIt(datastore, userId, listId);
        if (readingListEntity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        // Note that we're not transactional! As a result, we'll return the list of Ids that we've successfully added,
        // regardless of whether or not we have errors on the overall operation.
        ArrayList<Long> addedIds = new ArrayList<Long>();
        routingContext.response().setStatusCode(HttpResponseStatus.OK.code());
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

            if (DatastoreHelpers.updateReadingList(datastore, readingListEntity, false) &&
                DatastoreHelpers.updateReadingListElement(datastore, rleEntity, false)) {
                addedIds.add(rleId);
            } else {
                valid = false;
            }

            if (!valid) {
                routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
                break;
            }
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(addedIds.toArray()));
    }

    // POST /users/{userId}/readlingLists/{readingListId}/addTags
    public void addTagsToReadingList(RoutingContext routingContext) {
        long listId;
        long userId;
        Long[] tagIds;
        try {
            listId = Long.decode(routingContext.request().getParam("readingListId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
            tagIds = Json.decodeValue(routingContext.getBody(), Long[].class);
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListEntity readingListEntity = getListIfUserOwnsIt(datastore, userId, listId);
        if (readingListEntity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        // Note that we're not transactional! As a result, we'll return the list of Ids that we've successfully added,
        // regardless of whether or not we have errors on the overall operation.
        ArrayList<Long> addedIds = new ArrayList<Long>();
        routingContext.response().setStatusCode(HttpResponseStatus.OK.code());
        for (long tagId : tagIds) {
            boolean valid = true;

            if (readingListEntity.tagIds() != null && readingListEntity.tagIds().contains(tagId)) {
                continue;
            }

            if (readingListEntity.tagIds() == null) {
                readingListEntity.tagIds = new ArrayList<Long>();
            }
            readingListEntity.tagIds.add(tagId);

            if (DatastoreHelpers.updateReadingList(datastore, readingListEntity, false)) {
                addedIds.add(tagId);
            } else {
                valid = false;
            }

            if (!valid) {
                // TODO: What error should we give here when we fail to update an entity? Should it really be
                // TODO: a HttpResponseStatus.NOT_FOUND.code()? Or should this be some sort of 500? Or should we return some 202 type and retry?
                routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
                break;
            }
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(addedIds.toArray()));
    }

    // GET /users/{userId}/readlingLists/{readingListId}/tags
    public void getTagsForReadingList(RoutingContext routingContext) {
        long listId;
        long userId;
        try {
            listId = Long.decode(routingContext.request().getParam("readingListId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListEntity readingListEntity = getListIfUserOwnsIt(datastore, userId, listId);
        if (readingListEntity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        List<TagEntity> tags = TagRoute.getTagEntities(datastore, readingListEntity.tagIds());

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(tags.toArray()));
    }

    // DELETE /users/{userId}/readlingLists/{readingListId}/tags/{tagId}
    public void removeTagFromReadingList(RoutingContext routingContext) {
        long listId;
        long userId;
        long tagId;
        try {
            listId = Long.decode(routingContext.request().getParam("readingListId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
            tagId = Long.decode(routingContext.request().getParam("tagId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListEntity readingListEntity = getListIfUserOwnsIt(datastore, userId, listId);
        if (readingListEntity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        if (readingListEntity.tagIds() == null || !readingListEntity.tagIds().contains(tagId)) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end("Tag not found");
            return;
        }

        readingListEntity.tagIds().remove(tagId);
        if (DatastoreHelpers.updateReadingList(datastore, readingListEntity, false)) {
            routingContext.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
        } else {
            routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end();
    }
}
