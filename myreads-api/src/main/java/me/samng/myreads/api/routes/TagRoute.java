package me.samng.myreads.api.routes;

import com.google.cloud.datastore.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.entities.TagEntity;

import java.util.ArrayList;
import java.util.List;

public class TagRoute {
    private static TagEntity getTagEntity(
        Datastore datastore,
        long tagId) {

        Key key = DatastoreHelpers.newTagKey(tagId);
        Entity entity = datastore.get(key);
        if (entity == null) {
            return null;
        }
        TagEntity tagEntity = TagEntity.fromEntity(entity);
        return tagEntity;
    }

    public static List<TagEntity> getTagEntities(
        Datastore datastore,
        List<Long> tagIds) {
        ArrayList<TagEntity> results = new ArrayList<>();

        for (long tagId : tagIds) {
            results.add(getTagEntity(datastore, tagId));
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

        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newTagKey())
            .set("tagName", tagEntity.tagName()).build();
        Datastore datastore = DatastoreHelpers.getDatastore();
        Entity addedEntity = datastore.add(insertEntity);

        routingContext.response()
            .setStatusCode(HttpResponseStatus.CREATED.code())
            .putHeader("content-type", "text/plain")
            .end(Long.toString(addedEntity.getKey().getId()));
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
        TagEntity tagEntity = getTagEntity(datastore, tagId);
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

    // DELETE /tags/{tagId}
    public void deleteTag(RoutingContext routingContext) {
        Key key;
        try {
            key = DatastoreHelpers.newTagKey(Long.decode(routingContext.request().getParam("tagId")));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .putHeader("content-type", "text/plain")
                .end("Invalid request parameters");
            return;
        }
        Datastore datastore = DatastoreHelpers.getDatastore();
        datastore.delete(key);
        // TODO: Not enough to just delete the tag, gotta clean up the system when it gets deleted.

        routingContext.response()
            .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
            .putHeader("content-type", "text/plain")
            .end();
    }
}
