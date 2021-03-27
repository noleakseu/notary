package notary;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import net.lightbody.bmp.BrowserMobProxyServer;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    public void beforeLoad(BrowserMobProxyServer proxy, URL url) {
        this.source = url;
        proxy.addResponseFilter((server, contents, messageInfo) -> this.anyParty.putIfAbsent(messageInfo.getOriginalUrl(), messageInfo.getOriginalUrl()));
    }

    @Override
    public void onLoad(ChromeDriver driver) {
    }

    @Override
    public Map<String, byte[]> afterLoad(BrowserMobProxyServer proxy, Visit.Type visitType) throws InspectionException {
        try {
            Instant now = NtpClock.getInstance().instant();
            List<TlsCertificate> sourcePath = Main.getCertificatePath(this.source, now);
            for (String target : this.anyParty.values()) {
                URL targetUrl = new URL(target);
                List<TlsCertificate> targetPath = Main.getCertificatePath(targetUrl, now);
                InetAddress targetIp = proxy.getHostNameResolver().resolve(targetUrl.getHost()).iterator().next();
                InetAddress sourceIp = proxy.getHostNameResolver().resolve(this.source.getHost()).iterator().next();
                if (Main.isFirstParty(this.source, sourceIp, targetUrl, targetIp, !sourcePath.isEmpty() ? sourcePath.get(0) : null, !targetPath.isEmpty() ? targetPath.get(0) : null)) {
                    firstParty.add(new Resource(targetUrl, targetIp.getHostAddress()));
                } else {
                    thirdParty.add(new Resource(targetUrl, targetIp.getHostAddress()));
                }
            }
        } catch (MalformedURLException e) {
            throw new InspectionException(e.getMessage());
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
