package notary;

import com.fasterxml.jackson.annotation.*;

import java.net.URL;
import java.util.Date;

/**
 * RFC6265
 */
class Cookie {
    private final URL url;
    private final String name;
    private final String value;
    private final String domain;
    private Date expiryDate;
    private boolean persistent;

    public Cookie(URL url, org.apache.http.cookie.Cookie cookie) {
        this.url = url;
        this.name = cookie.getName();
        this.value = cookie.getValue();
        this.domain = cookie.getDomain();
    }

    @JsonIgnore
    public boolean equals(org.apache.http.cookie.Cookie cookie) {
        return this.domain.equals(cookie.getDomain()) && this.name.equals(cookie.getName());
    }

    @JsonIgnore
    public void update(org.apache.http.cookie.Cookie cookie) {
        if (null == this.expiryDate && null != cookie.getExpiryDate()) {
            this.expiryDate = cookie.getExpiryDate();
            this.persistent = cookie.isPersistent();
        }
    }

    @JsonPropertyDescription("Resource URL")
    public URL getUrl() {
        return url;
    }

    @JsonPropertyDescription("Cookie name")
    public String getName() {
        return name;
    }

    @JsonPropertyDescription("Cookie value")
    public String getValue() {
        return value;
    }

    @JsonPropertyDescription("Cookie domain")
    public String getDomain() {
        return domain;
    }

    @JsonPropertyDescription("Expiration date")
    public Date getExpiryDate() {
        return expiryDate;
    }

    @JsonPropertyDescription("Persistent among browsing sessions")
    public boolean isPersistent() {
        return persistent;
    }
}