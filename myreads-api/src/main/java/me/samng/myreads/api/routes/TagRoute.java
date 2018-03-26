package me.samng.myreads.api.routes;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.entities.TagEntity;

import java.util.ArrayList;
import java.util.List;

public class TagRoute {

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
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind(DatastoreHelpers.tagKind)
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<TagEntity> results = new ArrayList<TagEntity>();
        queryresult.forEachRemaining(list -> { results.add(TagEntity.fromEntity(list)); });

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
}
