package notary;

import com.browserup.bup.BrowserUpProxyServer;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.*;

class ResourcesInspection implements Inspection {
    private final List<Resource> firstParty = new LinkedList<>();
    private final List<Resource> thirdParty = new LinkedList<>();
    private final Map<String, String> anyParty = new HashMap<>();
    private URL source;

    @Override
    public String getInspection() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void beforeLoad(BrowserUpProxyServer proxy, URL url) {
        this.source = url;
        proxy.addResponseFilter((server, contents, messageInfo) -> this.anyParty.putIfAbsent(messageInfo.getOriginalUrl(), messageInfo.getOriginalUrl()));
    }

    @Override
    public void onLoad(ChromeDriver driver) {
    }

    @Override
    public Map<String, byte[]> afterLoad(BrowserUpProxyServer proxy, Visit.Type visitType) throws NotaryException {
        try {
            InetAddress sourceIp = proxy.getHostNameResolver().resolve(this.source.getHost()).iterator().next();
            Instant now = NtpClock.getInstance().instant();
            final Map<String, List<TlsCertificate>> cache = new HashMap<>();
            List<TlsCertificate> sourcePath = Main.getCertificatePath(this.source, now);
            for (String target : this.anyParty.values()) {
                final URL targetUrl = new URL(target);
                if (!cache.containsKey(targetUrl.getHost())) {
                    cache.put(targetUrl.getHost(), Main.getCertificatePath(targetUrl, now));
                }
                final InetAddress targetIp = proxy.getHostNameResolver().resolve(targetUrl.getHost()).iterator().next();
                if (Main.isFirstParty(this.source, sourceIp, targetUrl, targetIp, !sourcePath.isEmpty() ? sourcePath.get(0) : null, !cache.get(targetUrl.getHost()).isEmpty() ? cache.get(targetUrl.getHost()).get(0) : null)) {
                    firstParty.add(new Resource(targetUrl, targetIp.getHostAddress()));
                } else {
                    thirdParty.add(new Resource(targetUrl, targetIp.getHostAddress()));
                }
            }
        } catch (MalformedURLException e) {
            throw new NotaryException(e.getMessage());
        }
        return null;
    }

    @JsonPropertyDescription("Distinct first-party resources requested by the browser")
    public List<Resource> getFirstParty() {
        return this.firstParty;
    }

    @JsonPropertyDescription("Distinct third-party resources requested by the browser")
    public List<Resource> getThirdParty() {
        return this.thirdParty;
    }
}
