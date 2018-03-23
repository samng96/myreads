package me.samng.myreads.api.routes;

import com.google.cloud.datastore.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.EntityManager;
import me.samng.myreads.api.entities.UserEntity;

import java.util.ArrayList;
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

        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newUserKey())
            .set("name", userEntity.name())
            .set("email", userEntity.email())
            .set("userId", userEntity.userId())
            .build();
        Datastore datastore = DatastoreHelpers.getDatastore();
        Entity addedEntity = datastore.add(insertEntity);

        routingContext.response()
                .setStatusCode(HttpResponseStatus.CREATED.code())
                .putHeader("content-type", "text/plain")
                .end(Long.toString(addedEntity.getKey().getId()));
    }

    // Get a specific user, /users/{userId}
    public void getUser(RoutingContext routingContext) {
        Key key;
        try {
            key = DatastoreHelpers.newUserKey(Long.decode(routingContext.request().getParam("userId")));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }
        Datastore datastore = DatastoreHelpers.getDatastore();
        Entity entity = datastore.get(key);

        if (entity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        routingContext.response()
                .putHeader("content-type", "text/plain")
                .end(Json.encode(UserEntity.fromEntity(entity)));
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

        // First get the entity
        Key key = DatastoreHelpers.newUserKey(userEntity.id());
        Entity newEntity = Entity.newBuilder(key)
            .set("name", userEntity.name())
            .set("email", userEntity.email())
            .set("userId", userEntity.userId())
            .build();
        try {
            Datastore datastore = DatastoreHelpers.getDatastore();
            datastore.update(newEntity);
            routingContext.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
        }
        catch (DatastoreException e) {
            routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
        }

        routingContext.response().putHeader("content-type", "text/plain").end();
    }

    // Delete a user, /users/{userId}
    public void deleteUser(RoutingContext routingContext) {
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
        int statusCode = HttpResponseStatus.NO_CONTENT.code();
        if (!EntityManager.DeleteUser(datastore, userId)) {
            statusCode = HttpResponseStatus.BAD_REQUEST.code();
        }

        routingContext.response()
                .setStatusCode(statusCode)
                .putHeader("content-type", "text/plain")
                .end();
    }
}
