package me.samng.myreads.api.entities;

import com.google.cloud.datastore.*;
import com.google.common.collect.Maps;
import io.vertx.core.json.Json;
import lombok.Data;
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@Accessors(fluent = true)
public class UserEntity {
    @JsonProperty("id")
    public long id;

    @JsonProperty("email")
    public String email;

    @JsonProperty("name")
    public String name;

    @JsonProperty("userId")
    public String userId;

    @JsonProperty("externalId")
    public String externalId;

    @JsonProperty("deleted")
    public boolean deleted;

    public UserEntity() {
        deleted = false;
    }

    public static UserEntity fromEntity(Entity e) {
        UserEntity entity = Json.mapper.convertValue(Maps.toMap(e.getNames(), k -> e.getValue(k).get()), UserEntity.class);
        entity.id = e.getKey().getId();
        return entity;
    }
}
