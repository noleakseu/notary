package notary;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.lightbody.bmp.BrowserMobProxyServer;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SM;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class CookieInspection implements Inspection {
    private final List<Cookie> firstParty = new LinkedList<>();
    private final List<Cookie> thirdParty = new LinkedList<>();
    private final Multimap<String, String> anyParty = ArrayListMultimap.create();
    private URL source;

    @Override
    public String getInspection() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void beforeLoad(BrowserMobProxyServer proxy, URL url) {
        this.source = url;
        proxy.addResponseFilter((server, contents, messageInfo) -> {
            if (server.headers().contains(SM.SET_COOKIE)) {
                this.anyParty.put(messageInfo.getOriginalUrl(), server.headers().get(SM.SET_COOKIE));
            }
            if (messageInfo.getOriginalRequest().headers().contains(SM.COOKIE)) {
                this.anyParty.put(messageInfo.getOriginalUrl(), messageInfo.getOriginalRequest().headers().get(SM.COOKIE));
            }
        });
    }

    @Override
    public void onLoad(ChromeDriver driver) {
    }

    @Override
    public Map<String, byte[]> afterLoad(BrowserMobProxyServer proxy, Visit.Type visitType) throws InspectionException {
        try {
            Instant now = NtpClock.getInstance().instant();
            List<TlsCertificate> sourcePath = Main.getCertificatePath(this.source, now);
            for (Map.Entry<String, String> party : this.anyParty.entries()) {
                final URL targetUrl = new URL(party.getKey());
                List<TlsCertificate> targetPath = Main.getCertificatePath(targetUrl, now);
                InetAddress targetIp = proxy.getHostNameResolver().resolve(targetUrl.getHost()).iterator().next();
                InetAddress sourceIp = proxy.getHostNameResolver().resolve(this.source.getHost()).iterator().next();
                if (Main.isFirstParty(this.source, sourceIp, targetUrl, targetIp, !sourcePath.isEmpty() ? sourcePath.get(0) : null, !targetPath.isEmpty() ? targetPath.get(0) : null)) {
                    Main.distinctCookies(this.firstParty, Main.parseCookieValue(targetUrl, party.getValue()));
                } else {
                    Main.distinctCookies(this.thirdParty, Main.parseCookieValue(targetUrl, party.getValue()));
                }
            }
        } catch (MalformedURLException | MalformedCookieException e) {
            throw new InspectionException(e.getMessage());
        }
        return null;
    }

    @JsonPropertyDescription("Distinct first-party identifiers sent by the browser")
    public List<Cookie> getFirstParty() {
        return this.firstParty;
    }

    @JsonPropertyDescription("Distinct third-party identifiers sent by the browser")
    public List<Cookie> getThirdParty() {
        return this.thirdParty;
    }
}
