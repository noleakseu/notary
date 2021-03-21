package notary;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class Visibility {
    private final String file;
    private final String type;

    public Visibility(String file, String type) {
        this.file = file;
        this.type = type;
    }

    @JsonPropertyDescription("File name")
    public String getFile() {
        return file;
    }

    @JsonPropertyDescription("MIME type")
    public String getType() {
        return type;
    }
}