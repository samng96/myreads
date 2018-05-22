package me.samng.myreads.api.routes;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Key;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.EntityManager;
import me.samng.myreads.api.entities.UserEntity;

import java.util.List;

public class UserRoute {
    // Get all users
    public void getAllUsers(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        List<UserEntity> results = DatastoreHelpers.getAllUsers(datastore);

        routingContext.response()
                .putHeader("content-type", "text/plain")
                .end(Json.encode(results.toArray()));
    }

    // Post a new user
    public void postUser(RoutingContext routingContext) {
        UserEntity userEntity;
        try {
            userEntity = Json.decodeValue(routingContext.getBody(), UserEntity.class);
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request body");
            return;
        }
        Datastore datastore = DatastoreHelpers.getDatastore();
        long addedId = EntityManager.UpsertUser(datastore, userEntity);

        routingContext.response()
                .setStatusCode(HttpResponseStatus.CREATED.code())
                .putHeader("content-type", "text/plain")
                .end(Long.toString(addedId));
    }

    // Get a specific user, /users/{userId}
    public void getUser(RoutingContext routingContext) {
        Key key;
        long userId = -1;
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
        UserEntity entity = DatastoreHelpers.getUser(datastore, userId);

        if (entity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        routingContext.response()
                .putHeader("content-type", "text/plain")
                .end(Json.encode(entity));
    }

    // Update a user, /users/{userId}
    public void putUser(RoutingContext routingContext) {
        UserEntity userEntity;
        try {
            userEntity = Json.decodeValue(routingContext.getBody(), UserEntity.class);
            userEntity.id = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        EntityManager.UpsertUser(datastore, userEntity);

        routingContext.response()
            .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
            .putHeader("content-type", "text/plain")
            .end();
    }

    // Delete a user, /users/{userId}
    public void deleteUser(RoutingContext routingContext) {
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
        int statusCode = HttpResponseStatus.NO_CONTENT.code();
        if (!EntityManager.DeleteUser(datastore, userId)) {
            statusCode = HttpResponseStatus.BAD_REQUEST.code();
        }

        routingContext.response()
                .setStatusCode(statusCode)
                .putHeader("content-type", "text/plain")
                .end();
    }

    // Get a user given their auth token, POST /getUserByAuthToken with the auth token in the payload.
    public void getUserByAuthToken(RoutingContext routingContext) {
        String authToken;
        try {
            authToken = routingContext.getBodyAsString();
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request body");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        UserEntity user = DatastoreHelpers.getUserByAuthToken(datastore, authToken);

        if (user == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .putHeader("Access-Control-Allow-Origin", "http://localhost:4200")
            .end(Json.encode(user));
    }
}
