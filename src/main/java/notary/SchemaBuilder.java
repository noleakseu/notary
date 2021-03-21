package notary;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

import java.util.LinkedList;
import java.util.List;

public final class SchemaBuilder extends Builder {
    private final JsonSchemaGenerator generator;

    @JsonProperty()
    @JsonPropertyDescription("Inspections")
    private final List<JsonSchema> inspections = new LinkedList<>();

    @JsonProperty()
    @JsonPropertyDescription("Visits")
    private final List<JsonSchema> visits = new LinkedList<>();

    public SchemaBuilder() {
        this.generator = new JsonSchemaGenerator(this.mapper);
    }

    public SchemaBuilder append(Inspection... inspections) throws JsonMappingException {
        for (Inspection inspection : inspections) {
            this.inspections.add(this.generator.generateSchema(inspection.getClass()));
        }
        return this;
    }

    public SchemaBuilder append(Visit visit) throws JsonMappingException {
        this.visits.add(this.generator.generateSchema(visit.getClass()));
        return this;
    }
}
