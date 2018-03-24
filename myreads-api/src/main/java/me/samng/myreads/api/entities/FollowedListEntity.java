package me.samng.myreads.api.entities;

import com.google.cloud.datastore.Entity;
import com.google.common.collect.Maps;
import io.vertx.core.json.Json;
import lombok.Data;
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@Accessors(fluent = true)
public class FollowedListEntity {
    @JsonProperty("id")
    public long id;

    @JsonProperty("ownerId")
    public long ownerId;

    @JsonProperty("listId")
    public long listId;

    @JsonProperty("userId")
    public long userId;

    @JsonProperty("orphaned")
    public boolean orphaned;

    @JsonProperty("deleted")
    public boolean deleted;

    public FollowedListEntity() {
        orphaned = false;
        deleted = false;
    }

    public static FollowedListEntity fromEntity(Entity e) {
        FollowedListEntity entity = Json.mapper.convertValue(Maps.toMap(e.getNames(), k -> e.getValue(k).get()), FollowedListEntity.class);
        entity.id = e.getKey().getId();
        return entity;
    }
}
