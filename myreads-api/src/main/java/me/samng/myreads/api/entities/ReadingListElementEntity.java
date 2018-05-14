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
@Accessors(fluent=true)
public class ReadingListElementEntity {
    @JsonProperty("id")
    public long id;

    @JsonProperty("userId")
    public long userId;

    @JsonProperty("listIds")
    public List<Long> listIds;

    @JsonProperty("name")
    public String name;

    @JsonProperty("description")
    public String description;

    @JsonProperty("link")
    public String link;

    @JsonProperty("tagIds")
    public List<Long> tagIds;

    @JsonProperty("commentIds")
    public List<Long> commentIds;

    @JsonProperty("isRead")
    public boolean isRead;

    @JsonProperty("deleted")
    public boolean deleted;

    public ReadingListElementEntity() {
        tagIds = new ArrayList<>();
        commentIds = new ArrayList<>();
        listIds = new ArrayList<>();
        deleted = false;
    }

    public static ReadingListElementEntity fromEntity(Entity e) {
        ReadingListElementEntity entity = Json.mapper.convertValue(Maps.toMap(e.getNames(), k -> {
            Value<?> value = e.getValue(k);
            if(value instanceof ListValue) {
                return ImmutableList.copyOf(((ListValue)value).get())
                    .stream().map(Value::get).collect(Collectors.toList());
            }
            Object thing = value.get();
            return thing;
        }), ReadingListElementEntity.class);
        entity.id = e.getKey().getId();
        return entity;
    }
}
