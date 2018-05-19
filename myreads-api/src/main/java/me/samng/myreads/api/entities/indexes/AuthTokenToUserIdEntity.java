package me.samng.myreads.api.entities.indexes;

import com.google.cloud.datastore.Entity;
import com.google.common.collect.Maps;
import io.vertx.core.json.Json;
import org.codehaus.jackson.annotate.JsonProperty;

public class AuthTokenToUserIdEntity {
    @JsonProperty("id")
    public long id;

    @JsonProperty("authToken")
    public String authToken;

    @JsonProperty("userId")
    public long userId;

    public static AuthTokenToUserIdEntity fromEntity(Entity e) {
        AuthTokenToUserIdEntity entity = Json.mapper.convertValue(Maps.toMap(e.getNames(), k -> e.getValue(k).get()), AuthTokenToUserIdEntity.class);
        entity.id = e.getKey().getId();
        return entity;
    }
}
