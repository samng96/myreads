package me.samng.myreads.api.entities;

import com.google.cloud.datastore.Entity;
import com.google.common.collect.Maps;
import io.vertx.core.json.Json;
import lombok.Data;
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@Accessors(fluent = true)
public class TagEntity {
    @JsonProperty("id")
    public long id;

    @JsonProperty("tagName")
    public String tagName;

    @JsonProperty("deleted")
    public boolean deleted;

    public static TagEntity fromEntity(Entity e) {
        TagEntity entity = Json.mapper.convertValue(Maps.toMap(e.getNames(), k -> e.getValue(k).get()), TagEntity.class);
        entity.id = e.getKey().getId();
        return entity;
    }
}
