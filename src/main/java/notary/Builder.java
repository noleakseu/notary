package notary;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

abstract public class Builder {
    protected static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    protected final ObjectMapper mapper;

    @JsonProperty()
    protected final String name = this.getClass().getPackage().getImplementationTitle();

    @JsonProperty()
    protected final String version = this.getClass().getPackage().getImplementationVersion();

    @JsonProperty()
    protected final String vendor = this.getClass().getPackage().getImplementationVendor();

    public Builder() {
        FORMAT.setTimeZone(TimeZone.getTimeZone(NtpClock.getInstance().getZone()));
        this.mapper = new ObjectMapper().setDateFormat(FORMAT);
    }

    abstract public Builder append(Inspection... inspections) throws JsonMappingException;

    abstract public Builder append(Visit visit) throws JsonMappingException;

    public String build() throws IOException {
        return this.mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(this);
    }

    public void build(ZipOutputStream zipOutputStream, String fileName) throws IOException {
        ZipEntry entry = new ZipEntry(fileName);
        byte[] data = this.build().getBytes(StandardCharsets.UTF_8);
        entry.setSize(data.length);
        zipOutputStream.putNextEntry(entry);
        zipOutputStream.write(data);
        zipOutputStream.closeEntry();
    }
}
