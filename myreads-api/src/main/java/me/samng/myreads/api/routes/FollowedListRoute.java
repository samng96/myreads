package me.samng.myreads.api.routes;

import com.google.cloud.datastore.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.entities.FollowedListEntity;
import me.samng.myreads.api.entities.UserEntity;

import java.util.List;

public class FollowedListRoute {
    private FollowedListEntity getFollowedListIfUserOwnsIt(
        Datastore datastore,
        long userId,
        long listId) {

        FollowedListEntity followedListEntity = DatastoreHelpers.getFollowedList(datastore, listId);
        if (followedListEntity == null || followedListEntity.userId != userId) {
            return null;
        }
        return followedListEntity;
    }

    // Get all followed lists for a given user - /users/{userId}/followedLists
    public void getAllFollowedLists(RoutingContext routingContext) {
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
        List<FollowedListEntity> results = DatastoreHelpers.getAllFollowedListsForUser(datastore, userId);

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(results.toArray()));
    }

    // Post a new followed list - /users/{userId}/followedLists
    public void postFollowedList(RoutingContext routingContext) {
        FollowedListEntity followedListEntity;
        try {
            followedListEntity = Json.decodeValue(routingContext.getBody(), FollowedListEntity.class);
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request body");
            return;
        }
        long userId = Long.decode(routingContext.request().getParam("userId"));

        if (userId == followedListEntity.ownerId()) {
            // Can't follow your own list.
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Can't follow your own list");
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

        followedListEntity.userId = userId;
        long addedId = DatastoreHelpers.createFollowedList(datastore, followedListEntity);

        routingContext.response()
            .setStatusCode(HttpResponseStatus.CREATED.code())
            .putHeader("content-type", "text/plain")
            .end(Long.toString(addedId));
    }

    // Delete a user, /users/{userId}/readingLists/{readingListId}
    public void deleteFollowedList(RoutingContext routingContext) {
        long listId;
        long userId;
        try {
            listId = Long.decode(routingContext.request().getParam("followedListId"));
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
        if (getFollowedListIfUserOwnsIt(datastore, userId, listId) == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }
        DatastoreHelpers.deleteFollowedList(datastore, listId);

        routingContext.response()
            .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
            .putHeader("content-type", "text/plain")
            .end();
    }
}
