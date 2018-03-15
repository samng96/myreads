package me.samng.myreads.api.routes;

import com.google.cloud.datastore.*;
import com.google.common.collect.ImmutableList;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.entities.ReadingListElementEntity;

import java.util.ArrayList;

public class ReadingListElementRoute {
    public static ReadingListElementEntity getReadingListElementIfUserOwnsIt(
        Datastore datastore,
        long userId,
        long readingListElementId) {

        Key key = DatastoreHelpers.newReadingListElementKey(readingListElementId);
        Entity entity = datastore.get(key);
        if (entity == null) {
            return null;
        }
        ReadingListElementEntity rleEntity = ReadingListElementEntity.fromEntity(entity);
        if (rleEntity.userId != userId) {
            return null;
        }
        return rleEntity;
    }

    // GET /users/{userId}/readingListElements
    public void getAllReadingListElements(RoutingContext routingContext) {
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
            .setKind(DatastoreHelpers.readingListElementKind)
            .setFilter(StructuredQuery.PropertyFilter.eq("userId", userId))
            .build();
        QueryResults<Entity> queryresult = datastore.run(query);

        // Iterate through the results to actually fetch them, then serialize them and return.
        ArrayList<ReadingListElementEntity> results = new ArrayList<ReadingListElementEntity>();
        queryresult.forEachRemaining(list -> { results.add(ReadingListElementEntity.fromEntity(list)); });

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(results));
    }

    // POST /users/{userId}/readingListElements
    public void postReadingListElement(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListElementEntity rleEntity;
        long userId;
        try {
            rleEntity = Json.decodeValue(routingContext.getBody(), ReadingListElementEntity.class);
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        FullEntity.Builder<IncompleteKey> builder = Entity.newBuilder(DatastoreHelpers.newReadingListElementKey())
            .set("name", rleEntity.name())
            .set("description", rleEntity.description())
            .set("userId", userId)
            .set("amazonLink", rleEntity.amazonLink());
        if (rleEntity.tagIds != null) {
            builder.set("tagIds", ImmutableList.copyOf(rleEntity.tagIds().stream().map(LongValue::new).iterator()));
        }
        if (rleEntity.listIds != null) {
            builder.set("listIds", ImmutableList.copyOf(rleEntity.listIds().stream().map(LongValue::new).iterator()));
        }
        FullEntity<IncompleteKey> insertEntity = builder.build();
        Entity addedEntity = datastore.add(insertEntity);

        routingContext.response()
            .setStatusCode(201)
            .putHeader("content-type", "text/plain")
            .end(Long.toString(addedEntity.getKey().getId()));
    }

    // GET /users/{userId}/readingListElements/{readingListElementId}
    public void getReadingListElement(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        long rleId;
        long userId;
        try {
            rleId = Long.decode(routingContext.request().getParam("readingListElementId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        ReadingListElementEntity rleEntity = getReadingListElementIfUserOwnsIt(datastore, userId, rleId);
        if (rleEntity == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(Json.encode(rleEntity));
    }

    // PUT /users/{userId}/readingListElements/{readingListElementId}
    public void putReadingListElement(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        ReadingListElementEntity rleEntity;
        long userId;
        try {
            rleEntity = Json.decodeValue(routingContext.getBody(), ReadingListElementEntity.class);
            rleEntity.id = Long.decode(routingContext.request().getParam("readingListElementId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        if (getReadingListElementIfUserOwnsIt(datastore, userId, rleEntity.id) == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        if (DatastoreHelpers.updateReadingListElementEntity(datastore, rleEntity)) {
            routingContext.response().setStatusCode(204);
        }
        else {
            routingContext.response().setStatusCode(404);
        }

        routingContext.response().putHeader("content-type", "text/plain").end();
    }

    // DELETE /users/{userId}/readingListElements/{readingListElementId}
    // TODO: When we delete an RLE, make sure we remove it from every list that it's a part of. Should probably
    // TODO: make the delete a helper method on datastoreHelpers, because we'll also want to untag it, and
    // TODO: if it was the last element with that tag, we can remove the tag potentially? We also have to remove
    // TODO: all comments attached to the RLE when it deletes.
    public void deleteReadingListElement(RoutingContext routingContext) {
        Datastore datastore = DatastoreHelpers.getDatastore();
        long rleId;
        long userId;
        try {
            rleId = Long.decode(routingContext.request().getParam("readingListElementId"));
            userId = Long.decode(routingContext.request().getParam("userId"));
        }
        catch (Exception e) {
            routingContext.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }

        if (getReadingListElementIfUserOwnsIt(datastore, userId, rleId) == null) {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "text/plain")
                .end();
            return;
        }
        datastore.delete(DatastoreHelpers.newReadingListElementKey(rleId));

        routingContext.response()
            .setStatusCode(204)
            .putHeader("content-type", "text/plain")
            .end();
    }

    // TODO: Need to handle adding/removing tags from an RLE
}
