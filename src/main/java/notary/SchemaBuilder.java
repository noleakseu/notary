package notary;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

/**
 * https://www.jsonschemavalidator.net
 */
class SchemaBuilder extends Builder {
    @JsonProperty()
    private final String $schema = "https://json-schema.org/draft/2020‑12/schema";

    @JsonProperty()
    private final String title = "" + Main.APP_VENDOR + " " + Main.APP_TITLE + " " + Main.APP_VERSION;

    @JsonProperty()
    private final String type = "object";

    @JsonProperty()
    private final Properties properties = new Properties();

    static class Properties {
        @JsonProperty()
        @JsonPropertyDescription("Unique ID")
        private final StringSchema id = new StringSchema();

        @JsonProperty()
        @JsonPropertyDescription("Inspections")
        private final ArraySchema inspections = new ArraySchema();

        @JsonProperty()
        @JsonPropertyDescription("Visits")
        private final ArraySchema visits = new ArraySchema();
    }

    private final JsonSchemaGenerator generator;
    private final List<Visit> visits = new ArrayList<>();

    public SchemaBuilder() {
        this.generator = new JsonSchemaGenerator(this.mapper);
    }

    public SchemaBuilder append(Inspection... inspections) throws JsonMappingException {
        final JsonSchema[] schemas = new JsonSchema[inspections.length];
        for (int i = 0; i < inspections.length; i++) {
            schemas[i] = this.generator.generateSchema(inspections[i].getClass());
        }
        this.properties.inspections.setItems(new ArraySchema.ArrayItems(schemas));
        return this;
    }

    public SchemaBuilder append(Visit visit) {
        this.visits.add(visit);
        return this;
    }

    @Override
    public String build() throws IOException {
        appendVisits();
        return super.build();
    }

    @Override
    public void build(ZipOutputStream zipOutputStream, String fileName, FileTime now) throws IOException {
        appendVisits();
        super.build(zipOutputStream, fileName, now);
    }

    private void appendVisits() throws JsonMappingException {
        final JsonSchema[] schemas = new JsonSchema[this.visits.size()];
        for (int i = 0; i < this.visits.size(); i++) {
            schemas[i] = this.generator.generateSchema(this.visits.get(i).getClass());
        }
        this.properties.visits.setItems(new ArraySchema.ArrayItems(schemas));
    }
}
