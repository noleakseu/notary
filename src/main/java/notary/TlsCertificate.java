package notary;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.math.BigInteger;
import java.util.Date;

/**
 * RFC5280
 */
class TlsCertificate {
    private final String type;
    private final Boolean valid;
    private final String issuer;
    private final BigInteger serial;
    private final Date expiryDate;

    public TlsCertificate(String type, Boolean valid, String issuer, BigInteger serial, Date expires) {
        this.type = type;
        this.valid = valid;
        this.issuer = issuer;
        this.serial = serial;
        this.expiryDate = expires;
    }

    public TlsCertificate(String type) {
        this.type = type;
        this.valid = null;
        this.issuer = null;
        this.serial = null;
        this.expiryDate = null;
    }

    @JsonProperty
    @JsonPropertyDescription("Valid, invalid or unknown")
    public Boolean isValid() {
        return this.valid;
    }

    @JsonPropertyDescription("The entity that has signed and issued the certificate")
    public String getIssuer() {
        return this.issuer;
    }

    @JsonPropertyDescription("The serial number assigned by the Certification Authority")
    public BigInteger getSerial() {
        return this.serial;
    }

    @JsonPropertyDescription("Expiration date")
    public Date getExpiryDate() {
        return expiryDate;
    }

    @JsonPropertyDescription("Certificate type")
    public String getType() {
        return type;
    }
}