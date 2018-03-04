package me.samng.myreads.api.routes;

import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.entities.FollowedListEntity;

import java.util.ArrayList;

public class FollowedListRoute {
    private FollowedListEntity getFollowedListIfUserOwnsIt(
        Datastore datastore,
        long userId,
        long listId) {

        Key key = DatastoreHelpers.newFollowedListKey(listId);
        Entity entity = datastore.get(key);
        if (entity == null) {
            return null;
        }
        FollowedListEntity followedListEntity = FollowedListEntity.fromEntity(entity);
        if (followedListEntity.userId != userId) {
            return null;
        }
        return followedListEntity;
    }

    // Get all followed lists for a given user - /users/{userId}/followedLists
    public void getAllFollowedLists(RoutingContext routingContext) {
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
            .setKind(DatastoreHelpers.followedListKind)
            .setFilter(PropertyFilter.eq("userId", userId))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<FollowedListEntity> results = new ArrayList<FollowedListEntity>();
        queryresult.forEachRemaining(followedList -> { results.add(FollowedListEntity.fromEntity(followedList)); });

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(results));
    }

    // Post a new followed list - /users/{userId}/followedLists
    public void postFollowedList(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        FollowedListEntity followedListEntity;
        try {
            followedListEntity = Json.decodeValue(routingContext.getBody(), FollowedListEntity.class);
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }
        long userId = Long.decode(routingContext.request().getParam("userId"));

        if (userId == followedListEntity.ownerId()) {
            // TODO: Put a better error message in the 400.
            // Can't follow your own list.
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newFollowedListKey())
            .set("userId", userId)
            .set("listId", followedListEntity.listId())
            .set("ownerId", followedListEntity.ownerId())
            .build();
        Entity addedEntity = datastore.add(insertEntity);

        routingContext.response()
            .setStatusCode(201)
            .putHeader("content-type", "text/plain")
            .end(Long.toString(addedEntity.getKey().getId()));
    }

    // Delete a user, /users/{userId}/readingLists/{readingListId}
    public void deleteFollowedList(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        long listId;
        long userId;
        try {
            listId = Long.decode(routingContext.request().getParam("followedListId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        if (getFollowedListIfUserOwnsIt(datastore, userId, listId) == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }
        datastore.delete(DatastoreHelpers.newFollowedListKey(listId));

        routingContext.response()
            .setStatusCode(204)
            .putHeader("content-type", "text/plain")
            .end();
    }
}
