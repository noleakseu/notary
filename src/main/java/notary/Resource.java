package notary;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.net.URL;

/**
 * RFC1945
 */
public class Resource {
    private final URL url;
    private final String ip;

    public Resource(URL url, String ip) {
        this.url = url;
        this.ip = ip;
    }

    @JsonPropertyDescription("Resource URL")
    public URL getUrl() {
        return url;
    }

    @JsonPropertyDescription("IP address")
    public String getIp() {
        return ip;
    }
}