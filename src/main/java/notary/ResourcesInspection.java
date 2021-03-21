package notary;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import net.lightbody.bmp.BrowserMobProxyServer;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ResourcesInspection implements Inspection {
    private final List<Resource> firstParty = new LinkedList<>();
    private final List<Resource> thirdParty = new LinkedList<>();
    private final Map<String, String> anyParty = new HashMap<>();
    private URL source;
    private List<TlsCertificate> sourcePath;

    @Override
    public String getInspection() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void beforeLoad(BrowserMobProxyServer proxy, URL url) {
        this.source = url;
        this.sourcePath = Main.getCertificatePath(url, NtpClock.getInstance().instant());
        proxy.addResponseFilter((server, contents, messageInfo) -> this.anyParty.putIfAbsent(messageInfo.getOriginalUrl(), messageInfo.getOriginalUrl()));
    }

    @Override
    public void onLoad(ChromeDriver driver) {
    }

    @Override
    public Map<String, byte[]> afterLoad(BrowserMobProxyServer proxy, Visit.Type visitType) throws InspectionException {
        try {
            for (String target : this.anyParty.values()) {
                URL targetUrl = new URL(target);
                List<TlsCertificate> targetPath = Main.getCertificatePath(targetUrl, NtpClock.getInstance().instant());
                if (Main.isFirstParty(this.source, targetUrl, null != this.sourcePath ? this.sourcePath.get(0) : null, null != targetPath ? targetPath.get(0) : null)) {
                    firstParty.add(new Resource(targetUrl, DnsOverHttpsResolver.resolveHostName(targetUrl.getHost()).getHostAddress()));
                } else {
                    thirdParty.add(new Resource(targetUrl, DnsOverHttpsResolver.resolveHostName(targetUrl.getHost()).getHostAddress()));
                }
            }
        } catch (MalformedURLException | UnknownHostException e) {
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
