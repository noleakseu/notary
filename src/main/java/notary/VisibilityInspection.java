package notary;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.ImmutableMap;
import net.lightbody.bmp.BrowserMobProxyServer;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class VisibilityInspection implements Inspection {
    private byte[] screenshot;
    private final List<Visibility> artifacts = new LinkedList<>();

    @Override
    public String getInspection() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void beforeLoad(BrowserMobProxyServer proxy, URL url) {
    }

    @Override
    public void onLoad(ChromeDriver driver) {
        this.screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    @Override
    public Map<String, byte[]> afterLoad(BrowserMobProxyServer proxy, Visit.Type visitType) {
        String file = getInspection() + "." + visitType.name() + ".png";
        this.artifacts.add(new Visibility(file, "image/png"));
        return ImmutableMap.of(file, this.screenshot);
    }

    @JsonPropertyDescription("Visibility artifacts")
    public List<Visibility> getArtifacts() {
        return this.artifacts;
    }
}
