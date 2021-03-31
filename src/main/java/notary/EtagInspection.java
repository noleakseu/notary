package notary;

import com.browserup.bup.BrowserUpProxyServer;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.apache.commons.codec.digest.DigestUtils;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class EtagInspection implements Inspection {
    private static final String HEADER = "ETag";
    private final List<Etag> firstParty = new LinkedList<>();
    private final List<Etag> thirdParty = new LinkedList<>();
    private final Multimap<String, Map<String, String>> anyParty = ArrayListMultimap.create();  // url, etag, sha1
    private URL source;

    @Override
    public String getInspection() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void beforeLoad(BrowserUpProxyServer proxy, URL url) {
        this.source = url;
        proxy.addResponseFilter((server, contents, messageInfo) -> {
            if (server.headers().contains(HEADER)) {
                this.anyParty.put(
                        messageInfo.getUrl(),
                        ImmutableMap.of(server.headers().get(HEADER), DigestUtils.sha1Hex(contents.getBinaryContents()))
                );
            }
        });
    }

    @Override
    public void onLoad(ChromeDriver driver) {
    }

    @Override
    public Map<String, byte[]> afterLoad(BrowserUpProxyServer proxy, Visit.Type visitType) throws MalformedURLException {
        InetAddress sourceIp = proxy.getHostNameResolver().resolve(this.source.getHost()).iterator().next();
        Instant now = NtpClock.getInstance().instant();
        final Map<String, List<TlsCertificate>> cache = new HashMap<>();
        List<TlsCertificate> sourcePath = Main.getCertificatePath(this.source, now);
        for (Map.Entry<String, Map<String, String>> party : this.anyParty.entries()) {
            final URL targetUrl = new URL(party.getKey());
            if (!cache.containsKey(targetUrl.getHost())) {
                cache.put(targetUrl.getHost(), Main.getCertificatePath(targetUrl, now));
            }
            InetAddress targetIp = proxy.getHostNameResolver().resolve(targetUrl.getHost()).iterator().next();
            if (Main.isFirstParty(this.source, sourceIp, targetUrl, targetIp, !sourcePath.isEmpty() ? sourcePath.get(0) : null, !cache.get(targetUrl.getHost()).isEmpty() ? cache.get(targetUrl.getHost()).get(0) : null)) {
                Main.distinctEtags(this.firstParty, new Etag(party.getKey(), party.getValue()));
            } else {
                Main.distinctEtags(this.thirdParty, new Etag(party.getKey(), party.getValue()));
            }
        }
        return null;
    }

    @JsonPropertyDescription("Distinct first-party identifiers received by the browser")
    public List<Etag> getFirstParty() {
        return this.firstParty;
    }

    @JsonPropertyDescription("Distinct third-party identifiers received by the browser")
    public List<Etag> getThirdParty() {
        return this.thirdParty;
    }
}
