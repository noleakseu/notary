package notary;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * https://www.jsonschemavalidator.net
 */
class MetaBuilder extends Builder {
    @JsonProperty()
    private final String $schema = Main.SCHEMA_NAME;

    @JsonProperty()
    private final String title = Main.APP_TITLE;

    @JsonProperty()
    private final String version = Main.APP_VERSION;

    @JsonProperty()
    private final String vendor = Main.APP_VENDOR;

    @JsonProperty()
    @JsonPropertyDescription("Inspections")
    private final List<Inspection> inspections = new LinkedList<>();

    @JsonProperty()
    @JsonPropertyDescription("Visits")
    private final List<Visit> visits = new LinkedList<>();

    @Override
    public MetaBuilder append(Inspection... inspections) {
        Collections.addAll(this.inspections, inspections);
        return this;
    }

    @Override
    public MetaBuilder append(Visit visit) {
        this.visits.add(visit);
        return this;
    }
}
