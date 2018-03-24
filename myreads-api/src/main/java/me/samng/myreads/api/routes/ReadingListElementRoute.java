package me.samng.myreads.api.routes;

import com.google.cloud.datastore.Datastore;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.EntityManager;
import me.samng.myreads.api.entities.ReadingListElementEntity;
import me.samng.myreads.api.entities.TagEntity;
import me.samng.myreads.api.entities.UserEntity;

import java.util.List;

public class ReadingListElementRoute {
    public static ReadingListElementEntity getReadingListElementIfUserOwnsIt(
        Datastore datastore,
        long userId,
        long readingListElementId) {

        ReadingListElementEntity rleEntity = DatastoreHelpers.getReadingListElement(datastore, readingListElementId);
        if (rleEntity == null || rleEntity.userId != userId) {
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
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
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

        rleEntity.userId = userId;
        long addedId = DatastoreHelpers.createReadingListElement(datastore, rleEntity);

        routingContext.response()
            .setStatusCode(HttpResponseStatus.CREATED.code())
            .putHeader("content-type", "text/plain")
            .end(Long.toString(addedId));
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
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListElementEntity rleEntity = getReadingListElementIfUserOwnsIt(datastore, userId, rleId);
        if (rleEntity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
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
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        if (getReadingListElementIfUserOwnsIt(datastore, userId, rleEntity.id) == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        if (DatastoreHelpers.updateReadingListElement(datastore, rleEntity, false)) {
            routingContext.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
        }
        else {
            routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
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
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        if (getReadingListElementIfUserOwnsIt(datastore, userId, rleId) == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }
        EntityManager.DeleteReadingListElement(datastore, rleId);

        routingContext.response()
            .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
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
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListElementEntity readingListElementEntity = getReadingListElementIfUserOwnsIt(datastore, userId, rleId);
        if (readingListElementEntity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        routingContext.response().setStatusCode(HttpResponseStatus.OK.code());
        List<Long> addedIds = EntityManager.AddTagsToReadingListElement(datastore, readingListElementEntity, tagIds);

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
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListElementEntity readingListElementEntity = getReadingListElementIfUserOwnsIt(datastore, userId, rleId);
        if (readingListElementEntity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
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
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListElementEntity readingListElementEntity = getReadingListElementIfUserOwnsIt(datastore, userId, rleId);
        if (readingListElementEntity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        if (readingListElementEntity.tagIds() == null || !readingListElementEntity.tagIds().contains(tagId)) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end("Tag not found");
            return;
        }

        DatastoreHelpers.deleteTagToReadingListElementMapping(datastore, userId, tagId, rleId);

        readingListElementEntity.tagIds().remove(tagId);
        if (DatastoreHelpers.updateReadingListElement(datastore, readingListElementEntity, false)) {
            routingContext.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
        } else {
            routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end();
    }

    // POST /users/{userId}/readingListElementsByTag
    public void getAllReadingListElementsByTag(RoutingContext routingContext) {
        // For now, we assume the body contains a single tag, but we can add
        // more extensibility to this later.
        long userId;
        long tagId;
        try {
            userId = Long.decode(routingContext.request().getParam("userId"));
            tagId = Json.decodeValue(routingContext.getBody(), long.class);
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        List<ReadingListElementEntity> rles = DatastoreHelpers.getAllReadingListElementsForUserWithTag(datastore, userId, tagId);

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(rles.toArray()));
    }
}
