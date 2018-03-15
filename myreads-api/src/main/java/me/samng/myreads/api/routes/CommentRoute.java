package me.samng.myreads.api.routes;

import com.google.cloud.datastore.*;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.entities.CommentEntity;

import java.util.ArrayList;

public class CommentRoute {
    private static CommentEntity getCommentIfOnCorrectUserAndRLE(
        Datastore datastore,
        long userId,
        long rleId,
        long commentId) {

        Key key = DatastoreHelpers.newCommentKey(commentId);
        Entity entity = datastore.get(key);
        if (entity == null) {
            return null;
        }
        CommentEntity commentEntity = CommentEntity.fromEntity(entity);
        if (commentEntity.readingListElementId != rleId || commentEntity.userId != userId) {
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
                .setStatusCode(400)
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
            .end(Json.encode(results));
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
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newCommentKey())
            .set("commentText", commentEntity.commentText())
            .set("userId", userId)
            .set("readingListElementId", readingListElementId).build();
        Datastore datastore = DatastoreHelpers.getDatastore();
        Entity addedEntity = datastore.add(insertEntity);

        routingContext.response()
            .setStatusCode(201)
            .putHeader("content-type", "text/plain")
            .end(Long.toString(addedEntity.getKey().getId()));
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
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        if (getCommentIfOnCorrectUserAndRLE(datastore, userId, readingListElementId, commentEntity.id) == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        if (DatastoreHelpers.updateCommentEntity(datastore, commentEntity)) {
            routingContext.response().setStatusCode(204);
        }
        else {
            routingContext.response().setStatusCode(404);
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
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        CommentEntity commentEntity = getCommentIfOnCorrectUserAndRLE(datastore, userId, readingListElementId, commentId);
        if (commentEntity == null) {
            routingContext.response()
                .setStatusCode(404)
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
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        if (getCommentIfOnCorrectUserAndRLE(datastore, userId, readingListElementId, commentId) == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }
        datastore.delete(DatastoreHelpers.newCommentKey(commentId));

        routingContext.response()
            .setStatusCode(204)
            .putHeader("content-type", "text/plain")
            .end();
    }
}
