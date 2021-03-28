package notary;

import com.browserup.bup.BrowserUpProxyServer;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.URL;
import java.util.List;
import java.util.Map;

class TlsCertificateInspection implements Inspection {
    private boolean valid;
    private List<TlsCertificate> path = null;

    @Override
    public String getInspection() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void beforeLoad(BrowserUpProxyServer proxy, URL url) {
        this.path = Main.getCertificatePath(url, NtpClock.getInstance().instant());
        this.valid = Main.isCertificatePathValid(this.path);
    }

    @Override
    public void onLoad(ChromeDriver driver) {
    }

    @Override
    public Map<String, byte[]> afterLoad(BrowserUpProxyServer proxy, Visit.Type visitType) {
        return null;
    }

    @JsonPropertyDescription("The Certificate has valid certificate path")
    public boolean isValid() {
        return this.valid;
    }

    @JsonPropertyDescription("Certificate path")
    public List<TlsCertificate> getPath() {
        return this.path;
    }
}
