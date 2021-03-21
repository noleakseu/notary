package notary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import net.lightbody.bmp.BrowserMobProxyServer;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.URL;
import java.util.Map;

public interface Inspection {

    @JsonPropertyDescription("Inspection name")
    String getInspection();

    @JsonIgnore
    void beforeLoad(BrowserMobProxyServer proxy, URL url);

    @JsonIgnore
    void onLoad(ChromeDriver driver);

    @JsonIgnore
    Map<String, byte[]> afterLoad(BrowserMobProxyServer proxy, Visit.Type visitType) throws InspectionException;
}
