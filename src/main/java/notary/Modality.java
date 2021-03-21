package notary;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class Modality {
    private final String file;
    private final String type;
    private final Boolean scrollable;

    public Modality(String file, String type, Boolean scrollable) {
        this.file = file;
        this.type = type;
        this.scrollable = scrollable;
    }

    @JsonPropertyDescription("File name")
    public String getFile() {
        return file;
    }

    @JsonPropertyDescription("MIME type")
    public String getType() {
        return type;
    }

    @JsonProperty
    @JsonPropertyDescription("Scrollable, not scrollable or unknown")
    public Boolean isScrollable() {
        return scrollable;
    }
}