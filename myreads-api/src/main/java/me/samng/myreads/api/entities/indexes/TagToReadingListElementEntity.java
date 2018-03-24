package me.samng.myreads.api.entities.indexes;

import com.google.cloud.datastore.Entity;
import com.google.common.collect.Maps;
import io.vertx.core.json.Json;
import lombok.Data;
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@Accessors(fluent = true)
public class TagToReadingListElementEntity {
    @JsonProperty("id")
    public long id;

    @JsonProperty("userId")
    public long userId;

    @JsonProperty("tagId")
    public long tagId;

    @JsonProperty("readingListElementId")
    public long readingListElementId;

    public static TagToReadingListElementEntity fromEntity(Entity e) {
        TagToReadingListElementEntity entity = Json.mapper.convertValue(Maps.toMap(e.getNames(), k -> e.getValue(k).get()), TagToReadingListElementEntity.class);
        entity.id = e.getKey().getId();
        return entity;
    }
}

