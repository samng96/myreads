package me.samng.myreads.api.entities;

import lombok.Data;
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

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

    @JsonProperty("tags")
    public List<Long> tags;
}
