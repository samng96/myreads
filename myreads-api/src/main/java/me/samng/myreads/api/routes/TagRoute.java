package me.samng.myreads.api.routes;

import com.google.cloud.datastore.*;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.entities.TagEntity;

import java.util.ArrayList;

public class TagRoute {
    private TagEntity getTagEntity(
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
            .end(Json.encode(results));
    }

    // POST /tags
    public void postTag(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        TagEntity tagEntity;

        try {
            tagEntity = Json.decodeValue(routingContext.getBody(), TagEntity.class);
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        FullEntity<IncompleteKey> insertEntity = Entity.newBuilder(DatastoreHelpers.newTagKey())
            .set("tagName", tagEntity.tagName()).build();
        Entity addedEntity = datastore.add(insertEntity);

        routingContext.response()
            .setStatusCode(201)
            .putHeader("content-type", "text/plain")
            .end(Long.toString(addedEntity.getKey().getId()));
    }

    // GET /tags/{tagId}
    public void getTag(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        long tagId;
        try {
            tagId = Long.decode(routingContext.request().getParam("tagId"));
        } catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        TagEntity tagEntity = getTagEntity(datastore, tagId);
        if (tagEntity == null) {
            routingContext.response()
                .setStatusCode(404)
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
        Datastore datastore = DatastoreHelpers.getDatastore();
        Key key;
        try {
            key = DatastoreHelpers.newTagKey(Long.decode(routingContext.request().getParam("tagId")));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }
        datastore.delete(key);
        // TODO: Not enough to just delete the tag, gotta clean up the system when it gets deleted.

        routingContext.response()
            .setStatusCode(204)
            .putHeader("content-type", "text/plain")
            .end();
    }
}
