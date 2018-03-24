package me.samng.myreads.api.entities.indexes;

import com.google.cloud.datastore.Entity;
import com.google.common.collect.Maps;
import io.vertx.core.json.Json;
import lombok.Data;
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@Accessors(fluent = true)
public class TagToReadingListEntity {
    @JsonProperty("id")
    public long id;

    @JsonProperty("tagId")
    public long tagId;

    @JsonProperty("readingListId")
    public long readingListId;

    public static TagToReadingListEntity fromEntity(Entity e) {
        TagToReadingListEntity entity = Json.mapper.convertValue(Maps.toMap(e.getNames(), k -> e.getValue(k).get()), TagToReadingListEntity.class);
        entity.id = e.getKey().getId();
        return entity;
    }
}
