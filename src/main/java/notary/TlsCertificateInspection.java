package notary;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import net.lightbody.bmp.BrowserMobProxyServer;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class TlsCertificateInspection implements Inspection {
    private Boolean valid = null;
    private List<TlsCertificate> path = null;

    @Override
    public String getInspection() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void beforeLoad(BrowserMobProxyServer proxy, URL url) {
        this.path = Main.getCertificatePath(url, NtpClock.getInstance().instant());
        this.valid = Main.isCertificatePathValid(this.path);
    }

    @Override
    public void onLoad(ChromeDriver driver) {
    }

    @Override
    public Map<String, byte[]> afterLoad(BrowserMobProxyServer proxy, Visit.Type visitType) {
        return null;
    }

    @JsonPropertyDescription("The Certificate has valid certificate path")
    public Boolean isValid() {
        return this.valid;
    }

    @JsonPropertyDescription("Certificate path")
    public List<TlsCertificate> getPath() {
        return this.path;
    }
}
