package me.samng.myreads.api.entities;

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
}
