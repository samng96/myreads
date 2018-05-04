package me.samng.myreads.api.entities;

import com.google.cloud.datastore.Entity;
import com.google.common.collect.Maps;
import io.vertx.core.json.Json;
import lombok.Data;
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@Accessors(fluent = true)
public class CommentEntity {
    @JsonProperty("id")
    public long id;

    @JsonProperty("userId")
    public long userId;

    @JsonProperty("readingListElementId")
    public long readingListElementId;

    @JsonProperty("commentText")
    public String commentText;

    @JsonProperty("deleted")
    public boolean deleted;

    // TODO: Add a date posted to the comment.
    public CommentEntity() {
        deleted = false;
    }

    public static CommentEntity fromEntity(Entity e) {
        CommentEntity entity = Json.mapper.convertValue(Maps.toMap(e.getNames(), k -> e.getValue(k).get()), CommentEntity.class);
        entity.id = e.getKey().getId();
        return entity;
    }
}
