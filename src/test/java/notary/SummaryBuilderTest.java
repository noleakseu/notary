package notary;

import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

public class SummaryBuilderTest {

    @Test
    public void build() throws Exception {
        var report = new SummaryBuilder(UUID.randomUUID(), SummaryBuilder.Language.en);

        var first = new Visit("tmp", Device.Type.iPhone, Visit.Type.First, new URL("http://noleaks.eu"), 2);
        Whitebox.setInternalState(first, "startTime", new Date());
        Whitebox.setInternalState(first, "stopTime", new Date());
        Whitebox.setInternalState(first, "url", new URL("http://noleaks.eu"));
        Whitebox.setInternalState(first, "currentUrl", new URL("https://noleaks.eu/"));

        var incognito = new Visit("tmp", Device.Type.iPhone, Visit.Type.Incognito, new URL("http://noleaks.eu"), 2);
        Whitebox.setInternalState(incognito, "startTime", new Date());
        Whitebox.setInternalState(incognito, "stopTime", new Date());
        Whitebox.setInternalState(first, "url", new URL("http://noleaks.eu"));
        Whitebox.setInternalState(first, "currentUrl", new URL("https://noleaks.eu/"));

        var resources = new ResourcesInspection();
        Whitebox.setInternalState(resources, "firstParty", Arrays.asList(
                new Resource(new URL("https://example.com/styles.css"), InetAddress.getLocalHost().getHostAddress())
        ));
        Whitebox.setInternalState(resources, "thirdParty", Arrays.asList(
                new Resource(new URL("https://tracker.example.com/pixel.gif"), InetAddress.getLocalHost().getHostAddress())
        ));

        var tlsCertificate = new TlsCertificateInspection();
        Whitebox.setInternalState(tlsCertificate, "valid", true);
        Whitebox.setInternalState(tlsCertificate, "path", Arrays.asList(
                new TlsCertificate("X.509", true, "issuer", new BigInteger("1234567890"), new Date())
        ));

        var cookie = new CookieInspection();
        Whitebox.setInternalState(cookie, "firstParty", Main.parseCookieValue(
                new URL("https://example.com/ad.js"),
                "keyF=valuF; path=/; Expires=Sun, 28 Feb 2021 11:48:45 GMT; Secure"
        ));
        Whitebox.setInternalState(cookie, "thirdParty", Main.parseCookieValue(
                new URL("https://tracker.example.com/ad.js"),
                "keyT=valueT; path=/; Expires=Sun, 28 Feb 2021 11:48:45 GMT; Secure"
        ));

        var etag = new EtagInspection();
        Whitebox.setInternalState(etag, "firstParty", Arrays.asList(
                new Etag("https://example.com/pixel.gif", Collections.singletonMap("value", "hash"))
        ));
        Whitebox.setInternalState(etag, "thirdParty", Arrays.asList(
                new Etag("https://tracker.example.com/pixel.gif", Collections.singletonMap("value2", "hash2"))
        ));

        var traffic = new TrafficInspection();
        Whitebox.setInternalState(traffic, "artifacts", Arrays.asList(
                new Traffic("traffic.har", "application/json")
        ));

        var visibility = new VisibilityInspection();
        Whitebox.setInternalState(visibility, "artifacts", Arrays.asList(
                new Visibility("visibility.png", "image/png")
        ));

        var modality = new ModalityInspection();
        Whitebox.setInternalState(modality, "artifacts", Arrays.asList(
                new Modality("yes.png", "image/png", true),
                new Modality("no.png", "image/png", false),
                new Modality("unknown.png", "image/png", null)
        ));

        report
                .append(first)
                .append(incognito)
                .append(resources)
                .append(tlsCertificate)
                .append(cookie)
                .append(etag)
                .append(traffic)
                .append(visibility)
                .append(modality);

        System.out.println(report.build());
    }
}