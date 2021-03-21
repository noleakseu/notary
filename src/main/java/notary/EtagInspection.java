package notary;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import net.lightbody.bmp.BrowserMobProxyServer;
import org.apache.commons.codec.digest.DigestUtils;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EtagInspection implements Inspection {
    private static final String HEADER = "ETag";
    private final List<Etag> firstParty = new LinkedList<>();
    private final List<Etag> thirdParty = new LinkedList<>();
    private final Multimap<String, Map<String, String>> anyParty = ArrayListMultimap.create();  // url, etag, sha1
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
    public Map<String, byte[]> afterLoad(BrowserMobProxyServer proxy, Visit.Type visitType) throws InspectionException {
        try {
            for (Map.Entry<String, Map<String, String>> party : this.anyParty.entries()) {
                URL targetUrl = new URL(party.getKey());
                List<TlsCertificate> targetPath = Main.getCertificatePath(targetUrl, NtpClock.getInstance().instant());
                if (Main.isFirstParty(this.source, targetUrl, null != this.sourcePath ? this.sourcePath.get(0) : null, null != targetPath ? targetPath.get(0) : null)) {
                    Main.distinctEtags(this.firstParty, new Etag(party.getKey(), party.getValue()));
                } else {
                    Main.distinctEtags(this.thirdParty, new Etag(party.getKey(), party.getValue()));
                }
            }
        } catch (MalformedURLException e) {
            throw new InspectionException(e.getMessage());
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
