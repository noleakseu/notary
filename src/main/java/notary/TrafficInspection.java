package notary;

import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.proxy.CaptureType;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

class TrafficInspection implements Inspection {
    private final List<Traffic> artifacts = new LinkedList<>();

    @Override
    public String getInspection() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void beforeLoad(BrowserUpProxyServer proxy, URL url) {
        final EnumSet<CaptureType> captureTypes = CaptureType.getAllContentCaptureTypes();
        captureTypes.addAll(CaptureType.getHeaderCaptureTypes());
        captureTypes.addAll(CaptureType.getCookieCaptureTypes());
        proxy.setHarCaptureTypes(captureTypes);
        proxy.newHar();
        proxy.getCurrentHarPage().setStartedDateTime(Date.from(NtpClock.getInstance().instant()));
    }

    @Override
    public void onLoad(ChromeDriver driver) {
    }

    @Override
    public Map<String, byte[]> afterLoad(BrowserUpProxyServer proxy, Visit.Type visitType) throws IOException {
        String file = getInspection() + "." + visitType.name() + ".har";
        final Traffic traffic = new Traffic(file, "application/json");
        this.artifacts.add(traffic);
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        proxy.getHar().writeTo(stream);
        return ImmutableMap.of(file, stream.toByteArray());
    }

    @JsonPropertyDescription("HTTP Archive files")
    public List<Traffic> getArtifacts() {
        return this.artifacts;
    }
}
