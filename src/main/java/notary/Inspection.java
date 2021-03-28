package notary;

import com.browserup.bup.BrowserUpProxyServer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.URL;
import java.util.Map;

public interface Inspection {

    @JsonPropertyDescription("Inspection name")
    String getInspection();

    @JsonIgnore
    void beforeLoad(BrowserUpProxyServer proxy, URL url);

    @JsonIgnore
    void onLoad(ChromeDriver driver);

    @JsonIgnore
    Map<String, byte[]> afterLoad(BrowserUpProxyServer proxy, Visit.Type visitType) throws NotaryException;
}
