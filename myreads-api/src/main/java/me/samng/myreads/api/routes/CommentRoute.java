package me.samng.myreads.api.routes;

import com.google.api.client.util.Strings;
import com.google.cloud.datastore.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.entities.CommentEntity;
import me.samng.myreads.api.entities.ReadingListElementEntity;

import java.util.ArrayList;
import java.util.Date;

public class CommentRoute {
    private static CommentEntity getCommentIfOnCorrectUserAndRLE(
        Datastore datastore,
        long userId,
        long rleId,
        long commentId) {

        CommentEntity commentEntity = DatastoreHelpers.getComment(datastore, commentId);
        if (commentEntity == null || commentEntity.readingListElementId != rleId || commentEntity.userId != userId) {
            return null;
        }
        return commentEntity;
    }

    // GET /users/{userId}/readingListElements/{readingListElementId}/comments
    public void getAllComments(RoutingContext routingContext) {
        long userId;
        long rleId;

        try {
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

        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.commentKind)
            .setFilter(StructuredQuery.PropertyFilter.eq("readingListElementId", rleId))
            .setFilter(StructuredQuery.PropertyFilter.eq("userId", userId))
            .build();
        Datastore datastore = DatastoreHelpers.getDatastore();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<CommentEntity> results = new ArrayList<CommentEntity>();
        queryresult.forEachRemaining(list -> { results.add(CommentEntity.fromEntity(list)); });

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(results.toArray()));
    }

    // POST /users/{userId}/readingListElements/{readingListElementId}/comments
    public void postComment(RoutingContext routingContext) {
        CommentEntity commentEntity;
        long userId;
        long readingListElementId;

        try {
            commentEntity = Json.decodeValue(routingContext.getBody(), CommentEntity.class);
            userId = Long.decode(routingContext.request().getParam("userId"));
            readingListElementId = Long.decode(routingContext.request().getParam("readingListElementId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        if (Strings.isNullOrEmpty(commentEntity.commentText())) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Cannot have empty comment text");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        // First verify that we have the right reading list element in the system by getting it, then add
        // the comment, then add the newly added comment's ID to the RLE.
        ReadingListElementEntity rleEntity = DatastoreHelpers.getReadingListElement(datastore, readingListElementId);
        if (rleEntity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        commentEntity.userId = userId;
        commentEntity.readingListElementId = readingListElementId;
        commentEntity.lastModified = new Date();
        long addedId = DatastoreHelpers.createComment(datastore, commentEntity);
        rleEntity.commentIds.add(addedId);
        DatastoreHelpers.updateReadingListElement(datastore, rleEntity, false);

        routingContext.response()
            .setStatusCode(HttpResponseStatus.CREATED.code())
            .putHeader("content-type", "text/plain")
            .end(Long.toString(addedId));
    }

    // PUT /users/{userId}/readingListElements/{readingListElementId}/comments/{commentId}
    public void putComment(RoutingContext routingContext) {
        CommentEntity commentEntity;
        long userId;
        long readingListElementId;
        try {
            commentEntity = Json.decodeValue(routingContext.getBody(), CommentEntity.class);
            commentEntity.id = Long.decode(routingContext.request().getParam("commentId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
            readingListElementId = Long.decode(routingContext.request().getParam("readingListElementId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        if (Strings.isNullOrEmpty(commentEntity.commentText())) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Cannot have empty comment text");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        if (getCommentIfOnCorrectUserAndRLE(datastore, userId, readingListElementId, commentEntity.id) == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        commentEntity.lastModified = new Date();
        if (DatastoreHelpers.updateComment(datastore, commentEntity, false)) {
            routingContext.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
        }
        else {
            routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
        }

        routingContext.response().putHeader("content-type", "text/plain").end();
    }

    // GET /users/{userId}/readingListElements/{readingListElementId}/comments/{commentId}
    public void getComment(RoutingContext routingContext) {
        long commentId;
        long readingListElementId;
        long userId;
        try {
            commentId = Long.decode(routingContext.request().getParam("commentId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
            readingListElementId = Long.decode(routingContext.request().getParam("readingListElementId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        CommentEntity commentEntity = getCommentIfOnCorrectUserAndRLE(datastore, userId, readingListElementId, commentId);
        if (commentEntity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(commentEntity));
    }

    // DELETE /users/{userId}/readingListElements/{readingListElementId}/comments/{commentId}
    public void deleteComment(RoutingContext routingContext) {
        long commentId;
        long readingListElementId;
        long userId;
        try {
            readingListElementId = Long.decode(routingContext.request().getParam("readingListElementId"));
            commentId = Long.decode(routingContext.request().getParam("commentId"));
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
        if (getCommentIfOnCorrectUserAndRLE(datastore, userId, readingListElementId, commentId) == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        ReadingListElementEntity rleEntity = DatastoreHelpers.getReadingListElement(datastore, readingListElementId);
        if (rleEntity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }
        rleEntity.commentIds.remove(commentId);
        DatastoreHelpers.updateReadingListElement(datastore, rleEntity, false);
        DatastoreHelpers.deleteComment(datastore, commentId);

        routingContext.response()
            .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
            .putHeader("content-type", "text/plain")
            .end();
    }
}
