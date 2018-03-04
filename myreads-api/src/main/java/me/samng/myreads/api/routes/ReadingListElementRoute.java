package me.samng.myreads.api.routes;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import io.vertx.ext.web.RoutingContext;
import me.samng.myreads.api.DatastoreHelpers;
import me.samng.myreads.api.entities.ReadingListElementEntity;

public class ReadingListElementRoute {
    public static ReadingListElementEntity getListIfUserOwnsIt(
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

    public void getAllReadingListElement(RoutingContext routingContext) {
    }

    public void postReadingListElement(RoutingContext routingContext) {
    }

    public void getReadingListElement(RoutingContext routingContext) {
    }

    public void putReadingListElement(RoutingContext routingContext) {
    }

    public void deleteReadingListElement(RoutingContext routingContext) {
    }
}
