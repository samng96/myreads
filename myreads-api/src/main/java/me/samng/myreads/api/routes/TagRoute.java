package me.samng.myreads.api.routes;

import com.google.cloud.datastore.Datastore;
import com.google.common.base.Strings;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.entities.TagEntity;

import java.util.ArrayList;
import java.util.List;

public class TagRoute {
    // TODO: At some point, do we want to add the ability for users to get all the tags that they've used?

    public static List<TagEntity> getTagEntities(
        Datastore datastore,
        List<Long> tagIds) {
        ArrayList<TagEntity> results = new ArrayList<>();

        for (long tagId : tagIds) {
            TagEntity tagEntity = DatastoreHelpers.getTag(datastore, tagId);
            if (tagEntity != null) {
                results.add(tagEntity);
            }
        }
        return results;
    }

    // GET /tags
    public void getAllTags(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        List<TagEntity> results = DatastoreHelpers.getAllTags(datastore);

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(results.toArray()));
    }

    // POST /tags
    public void postTag(RoutingContext routingContext) {
        TagEntity tagEntity;

        try {
            tagEntity = Json.decodeValue(routingContext.getBody(), TagEntity.class);
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        if (Strings.isNullOrEmpty(tagEntity.tagName())) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Cannot have empty tag name");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        long addedId = DatastoreHelpers.createTag(datastore, tagEntity);

        routingContext.response()
            .setStatusCode(HttpResponseStatus.CREATED.code())
            .putHeader("content-type", "text/plain")
            .end(Long.toString(addedId));
    }

    // GET /tags/{tagId}
    public void getTag(RoutingContext routingContext) {
        long tagId;
        try {
            tagId = Long.decode(routingContext.request().getParam("tagId"));
        } catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        TagEntity tagEntity = DatastoreHelpers.getTag(datastore, tagId);
        if (tagEntity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(tagEntity));
    }

    // GET /tagByName/{tagName}
    public void getTagByName(RoutingContext routingContext) {
        String tagName;
        try {
            tagName = routingContext.request().getParam("tagName");
        } catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }

        Datastore datastore = DatastoreHelpers.getDatastore();
        TagEntity tagEntity = DatastoreHelpers.getTagByName(datastore, tagName);
        if (tagEntity == null) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(tagEntity));
    }
}
