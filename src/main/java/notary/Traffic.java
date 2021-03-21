package notary;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * HTTP Archive
 */
public class Traffic {
    private final String file;
    private final String type;

    public Traffic(String file, String type) {
        this.file = file;
        this.type = type;
    }

    @JsonPropertyDescription("HTTP Archive file")
    public String getFile() {
        return file;
    }

    @JsonPropertyDescription("MIME type")
    public String getType() {
        return type;
    }
}
