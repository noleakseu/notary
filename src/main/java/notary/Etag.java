package notary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.Map;

/**
 * RFC7232
 */
public class Etag {
    private final String resource;
    private String value;
    private String contentHash;

    public Etag(String resource, Map<String, String> valueHashPair) {
        this.resource = resource;
        valueHashPair.forEach((value, hash) -> {
            this.value = value;
            this.contentHash = hash;
        });
    }

    @JsonIgnore
    public boolean equals(Etag etag) {
        return this.resource.equals(etag.getResource()) && this.value.equals(etag.getValue()) && this.contentHash.equals(etag.getContentHash());
    }

    @JsonPropertyDescription("ETag resource")
    public String getResource() {
        return resource;
    }

    @JsonPropertyDescription("ETag value")
    public String getValue() {
        return value;
    }

    @JsonPropertyDescription("Content hash")
    public String getContentHash() {
        return contentHash;
    }
}