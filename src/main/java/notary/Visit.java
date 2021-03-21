package notary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.io.BaseEncoding;
import net.lightbody.bmp.BrowserMobProxyServer;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final public class Visit {
    public enum Type {
        First,
        Returning,
        Incognito
    }

    @JsonProperty()
    @JsonPropertyDescription("Start UTC timestamp")
    private Date startTime;

    @JsonProperty()
    @JsonPropertyDescription("Stop UTC timestamp")
    private Date stopTime;

    @JsonProperty()
    @JsonPropertyDescription("Type of the visit")
    private final Type visitType;

    @JsonProperty()
    @JsonPropertyDescription("URL of the Web-page")
    private final URL url;

    @JsonProperty()
    @JsonPropertyDescription("URL of the Web-page after potential redirects")
    private URL currentUrl;

    @JsonProperty()
    @JsonPropertyDescription("Simulated device")
    private final Device.Type deviceType;

    @JsonProperty()
    @JsonPropertyDescription("Effective Chrome options")
    private final Map<String, String> chromeOptions = new HashMap<>();

    private String browserName;
    private String platformName;
    private String browserVersion;

    private static class NetworkInterface {
        @JsonProperty
        @JsonPropertyDescription("Interface name")
        final String name;

        @JsonProperty
        @JsonPropertyDescription("MAC address")
        final String mac;

        @JsonProperty
        @JsonPropertyDescription("IP addresses")
        final List<String> ip;

        NetworkInterface(String name, String mac, List<String> ip) {
            this.name = name;
            this.mac = mac;
            this.ip = ip;
        }
    }

    @JsonProperty()
    @JsonPropertyDescription("Network interfaces")
    private final List<NetworkInterface> networkInterfaces = new LinkedList<>();

    @JsonProperty()
    @JsonPropertyDescription("Effective system properties")
    private final Map<String, String> systemProperties = new HashMap<>();

    private final long duration;
    private final String userDataDir;
    private final List<Map<String, byte[]>> artifacts = new LinkedList<>();

    @JsonIgnore
    public Visit(String userDataDir, Device.Type deviceType, Type visitType, URL url, long durationSec) {
        this.visitType = visitType;
        this.url = url;
        this.userDataDir = userDataDir;
        this.deviceType = deviceType;
        this.duration = durationSec;
    }

    @JsonIgnore
    public Visit inspect(Inspection... inspections) throws InspectionException, InterruptedException, IOException {
        // setup
        System.setProperty("webdriver.chrome.driver", Main.DRIVER_FILE);

        final ChromeOptions options = new ChromeOptions();
        options.setBinary(Main.CHROME_FILE);
        options.addArguments(
                "--user-data-dir=" + this.userDataDir,
                "--window-size=" + this.deviceType.getWidth() + "," + this.deviceType.getHeight(),
                "--disable-default-apps",
                "--disable-extensions",
                "--no-default-browser-check",
                "--disable-sync",
                "--disable-translate",
                "--enable-automation",
                "--disable-account-consistency",
                "--disable-browser-side-navigation",
                "--disable-gpu",
                "--remote-debugging-port=0"
        );
        if (Type.Incognito == this.visitType) {
            options.addArguments("--incognito");
        }

        // https://chromedriver.chromium.org/mobile-emulation
        if (this.deviceType.isMobile()) {
            Map<String, Object> deviceMetrics = new HashMap<>();
            deviceMetrics.put("width", this.deviceType.getWidth());
            deviceMetrics.put("height", this.deviceType.getHeight());
            deviceMetrics.put("pixelRatio", this.deviceType.getScaleFactor());
            Map<String, Object> mobileEmulation = new HashMap<>();
            mobileEmulation.put("deviceMetrics", deviceMetrics);
            mobileEmulation.put("userAgent", this.deviceType.getUserAgent());
            options.setExperimentalOption("mobileEmulation", mobileEmulation);
        }

        final DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability(CapabilityType.HAS_TOUCHSCREEN, this.deviceType.hasTouch());

        desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        desiredCapabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

        final BrowserMobProxyServer proxy = new BrowserMobProxyServer();
        proxy.setHostNameResolver(new DnsOverHttpsResolver());
        proxy.setMitmDisabled(false);
        proxy.start(0);

        final Proxy seleniumProxy = new Proxy();
        final String proxyStr = "localhost:" + proxy.getPort();
        seleniumProxy.setHttpProxy(proxyStr);
        seleniumProxy.setSslProxy(proxyStr);
        desiredCapabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
        options.merge(desiredCapabilities);
        ChromeDriver driver = new ChromeDriver(options);

        // beforeLoad
        this.browserName = driver.getCapabilities().getBrowserName();
        this.platformName = driver.getCapabilities().getPlatform().name();
        this.browserVersion = driver.getCapabilities().getVersion();
        for (Inspection inspection : inspections) {
            inspection.beforeLoad(proxy, this.url);
        }

        // dump environment
        options.asMap().forEach((key, value) -> this.chromeOptions.put(key, value.toString()));
        System.getProperties().forEach((key, value) -> this.systemProperties.put(key.toString(), value.toString()));
        for (java.net.NetworkInterface networkInterface : Collections.list(java.net.NetworkInterface.getNetworkInterfaces())) {
            this.networkInterfaces.add(
                    new NetworkInterface(
                            networkInterface.getName(),
                            null != networkInterface.getHardwareAddress() ? BaseEncoding.base16().encode(networkInterface.getHardwareAddress()) : null,
                            Collections
                                    .list(networkInterface.getInetAddresses())
                                    .stream()
                                    .map(InetAddress::getHostAddress)
                                    .collect(Collectors.toCollection(LinkedList::new))
                    )
            );
        }

        // page load
        this.startTime = Date.from(NtpClock.getInstance().instant());
        driver.get(this.url.toString());
        Thread.sleep(1000 * this.duration);
        this.currentUrl = new URL(driver.getCurrentUrl());
        for (Inspection inspection : inspections) {
            inspection.onLoad(driver);
        }
        this.stopTime = Date.from(NtpClock.getInstance().instant());

        // afterLoad
        for (Inspection inspection : inspections) {
            Map<String, byte[]> artifact = inspection.afterLoad(proxy, this.visitType);
            if (null != artifact) {
                this.artifacts.add(artifact);
            }
        }

        // teardown
        driver.quit();
        proxy.stop();
        return this;
    }

    @JsonIgnore
    public Type getType() {
        return this.visitType;
    }

    @JsonIgnore
    public Date getStartTime() {
        return startTime;
    }

    @JsonIgnore
    public Date getStopTime() {
        return stopTime;
    }

    @JsonIgnore
    public Device.Type getDeviceType() {
        return deviceType;
    }

    @JsonIgnore
    public URL getUrl() {
        return url;
    }

    @JsonIgnore
    public URL getCurrentUrl() {
        return currentUrl;
    }

    @JsonIgnore
    public String getBrowserName() {
        return browserName;
    }

    @JsonIgnore
    public String getPlatformName() {
        return platformName;
    }

    @JsonIgnore
    public String getBrowserVersion() {
        return browserVersion;
    }

    @JsonIgnore
    public List<Map<String, byte[]>> publish() throws IOException {
        return this.artifacts;
    }

    @JsonIgnore
    public Visit publish(ZipOutputStream zipOutputStream) throws IOException {
        for (Map<String, byte[]> artifacts : this.artifacts) {
            for (Map.Entry<String, byte[]> artifact : artifacts.entrySet()) {
                ZipEntry entry = new ZipEntry(artifact.getKey());
                entry.setSize(artifact.getValue().length);
                zipOutputStream.putNextEntry(entry);
                zipOutputStream.write(artifact.getValue());
                zipOutputStream.closeEntry();
            }
        }
        return this;
    }

    @JsonIgnore
    public Visit publish(Builder builder) throws JsonMappingException {
        builder.append(this);
        return this;
    }
}
