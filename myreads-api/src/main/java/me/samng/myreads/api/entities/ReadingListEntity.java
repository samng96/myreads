package me.samng.myreads.api.entities;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.ListValue;
import com.google.cloud.datastore.Value;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.vertx.core.json.Json;
import lombok.Data;
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Accessors(fluent = true)
public class ReadingListEntity {
    @JsonProperty("id")
    public long id;

    @JsonProperty("userId")
    public long userId;

    @JsonProperty("name")
    public String name;

    @JsonProperty("description")
    public String description;

    @JsonProperty("tagIds")
    public List<Long> tagIds;

    @JsonProperty("readingListElementIds")
    public List<Long> readingListElementIds;

    @JsonProperty("deleted")
    public boolean deleted;

    public ReadingListEntity() {
        tagIds = new ArrayList<>();
        readingListElementIds = new ArrayList<>();
    }

    public static ReadingListEntity fromEntity(Entity e) {
        ReadingListEntity entity = Json.mapper.convertValue(Maps.toMap(e.getNames(), k -> {
            Value<?> value = e.getValue(k);
            if(value instanceof ListValue) {
                return ImmutableList.copyOf(((ListValue)value).get())
                    .stream().map(Value::get).collect(Collectors.toList());
            }
            Object thing = value.get();
            return thing;
        }), ReadingListEntity.class);
        entity.id = e.getKey().getId();
        return entity;
    }
}
