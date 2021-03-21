package notary;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.ImmutableMap;
import net.lightbody.bmp.BrowserMobProxyServer;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ModalityInspection implements Inspection {
    private static final long PAUSE = 500;
    private final List<Modality> artifacts = new LinkedList<>();
    private byte[] screenshot;
    private Boolean scrollable;

    @Override
    public String getInspection() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void beforeLoad(BrowserMobProxyServer proxy, URL url) {
    }

    @Override
    public void onLoad(ChromeDriver driver) {
        this.scrollable = null;
        boolean isScrollable = Boolean.parseBoolean(((JavascriptExecutor) driver)
                .executeScript("return document.body.scrollHeight > window.innerHeight")
                .toString()
        );
        if (isScrollable) {
            long scrollCurrentPosition = Long.parseLong(((JavascriptExecutor) driver)
                    .executeScript("return window.pageYOffset")
                    .toString()
            );
            Actions actions = new Actions(driver);
            actions
                    .keyDown(Keys.CONTROL)
                    .sendKeys(Keys.END)
                    .pause(PAUSE)
                    .perform();
            long scrollBottomPosition = Long.parseLong(((JavascriptExecutor) driver)
                    .executeScript("return window.pageYOffset")
                    .toString()
            );
            this.scrollable = scrollBottomPosition > scrollCurrentPosition;
            this.screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        }
    }

    @Override
    public Map<String, byte[]> afterLoad(BrowserMobProxyServer proxy, Visit.Type visitType) {
        String file = getInspection() + "." + visitType.name() + ".png";
        Modality modality = new Modality(file, "image/png", this.scrollable);
        this.artifacts.add(modality);
        return ImmutableMap.of(file, this.screenshot);
    }

    @JsonPropertyDescription("Modality artifacts")
    public List<Modality> getArtifacts() {
        return this.artifacts;
    }
}
