package notary;

import com.google.common.net.InternetDomainName;
import io.javalin.Javalin;
import io.javalin.http.util.RateLimit;
import jdk.security.jarsigner.JarSigner;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SM;
import org.apache.http.impl.cookie.DefaultCookieSpec;
import org.apache.http.impl.cookie.RFC6265LaxSpec;
import org.apache.http.message.BasicHeader;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

class Main {
    private static final long VISIT_DURATION_SEC = 5;
    private static final int DAILY_LIMIT = 100;

    private static final String SNAPSHOT_NAME = "snapshot.zip";
    static final String SCHEMA_NAME = "schema.json";
    private static final String META_NAME = "meta.json";
    private static final String SUMMARY_NAME = "summary.html";

    public static final String DRIVER_FILE = "bin/chromedriver";
    public static final String CHROME_FILE = "bin/chrome-linux/chrome";

    private static final String ARG_URL = "url";
    private static final String ARG_LANGUAGE = "language";
    private static final String ARG_DEVICE = "device";
    private static final String ARG_KEYSTORE = "keystore";
    private static final String ARG_ALIAS = "alias";
    private static final String ARG_STOREPASS = "storepass";

    static final String APP_TITLE = Main.class.getPackage().getImplementationTitle();
    static final String APP_VERSION = Main.class.getPackage().getImplementationVersion();
    static final String APP_VENDOR = Main.class.getPackage().getImplementationVendor();
    public static final String APP_HOST = "0.0.0.0";
    public static final int APP_PORT = 8000;
    public static final String APP_NTP = "time.cloudflare.com";
    public static final String APP_DNS = "https://cloudflare-dns.com/dns-query";
    public static final String APP_TSA = "http://rfc3161timestamp.globalsign.com/advanced";

    /**
     * Entry point
     */
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("notary.jar").build()
                .defaultHelp(true)
                .description("" + APP_TITLE + " " + APP_VERSION + " by " + APP_VENDOR);
        parser.addArgument("-k", "--keystore")
                .required(true)
                .help("Specify P12 keystore");
        parser.addArgument("-a", "--alias")
                .required(true)
                .help("Specify keystore alias");
        parser.addArgument("-s", "--storepass")
                .required(true)
                .help("Specify keystore password");
        parser.addArgument("-d", "--device")
                .choices(Device.Type.values())
                .setDefault(Device.Type.Kindle)
                .help("Specify device (CLI mode only)");
        parser.addArgument("-l", "--language")
                .choices(SummaryBuilder.Language.values())
                .setDefault(SummaryBuilder.Language.en)
                .help("Specify language (CLI mode only)");
        parser.addArgument("-u", "--url")
                .setDefault("")
                .help("Specify URL (CLI mode only)");

        try {
            // parse args
            Namespace ns = parser.parseArgs(args);

            // unzip binaries
            unzip(Main.class.getClassLoader().getResourceAsStream("chrome-linux.zip"));
            unzip(Main.class.getClassLoader().getResourceAsStream("chromedriver_linux64.zip"));
            new File(DRIVER_FILE).setExecutable(true);
            new File(CHROME_FILE).setExecutable(true);

            // create signer
            final KeyStore keyStore = KeyStore.getInstance(new File(ns.getString(ARG_KEYSTORE)), ns.getString(ARG_STOREPASS).toCharArray());
            final JarSigner signer = new JarSigner.Builder(
                    (KeyStore.PrivateKeyEntry) keyStore
                            .getEntry(ns.getString(ARG_ALIAS), new KeyStore.PasswordProtection(ns.getString(ARG_STOREPASS).toCharArray()))
            ).tsa(new URI(APP_TSA)).build();

            if (ns.get("url").equals("")) {
                // API mode
                Javalin app = Javalin.create(
                        config -> {
                            config.enableCorsForAllOrigins();
                            config.showJavalinBanner = false;
                        }
                );
                app.exception(NotaryException.class, (e, ctx) -> {
                    ctx.status(500);
                    ctx.result("NotaryException " + e.getMessage());
                });
                app.after("/", ctx -> {
                    ctx.res.setHeader("Server", "" + APP_TITLE + " " + APP_VERSION);
                    ctx.res.setDateHeader("Date", NtpClock.getInstance().instant().toEpochMilli());
                });
                app.get("/", ctx -> {
                    // status
                    if (ctx.queryParamMap().isEmpty()) {
                        ctx.res.addHeader("Content-Type", "text/html; charset=UTF-8");
                        status(ctx.res.getOutputStream());
                        return;
                    }

                    // snapshot
                    new RateLimit(ctx).requestPerTimeUnit(DAILY_LIMIT, TimeUnit.DAYS);
                    ctx.res.addHeader("Content-Type", "application/zip");
                    ctx.res.addHeader("Content-Disposition", "attachment; filename=\"" + SNAPSHOT_NAME + "\"");
                    ctx.res.addHeader("Content-Transfer-Encoding", "binary");

                    File zip = Files.createTempFile("zip-", ".tmp").toFile();
                    zip.deleteOnExit();
                    snapshot(
                            new URL(ctx.queryParam(ARG_URL)),
                            Device.Type.valueOf(ctx.queryParam(ARG_DEVICE)),
                            SummaryBuilder.Language.valueOf(ctx.queryParam(ARG_LANGUAGE)),
                            new FileOutputStream(zip)
                    );
                    signer.sign(new ZipFile(zip), ctx.res.getOutputStream());
                    zip.delete();
                });
                Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
                app.start(APP_HOST, APP_PORT);
            } else {
                // CLI mode
                File zip = Files.createTempFile("zip-", ".tmp").toFile();
                zip.deleteOnExit();
                snapshot(
                        new URL(ns.get(ARG_URL)),
                        ns.get(ARG_DEVICE),
                        ns.get(ARG_LANGUAGE),
                        new FileOutputStream(zip)
                );
                signer.sign(new ZipFile(zip), new FileOutputStream(SNAPSHOT_NAME));
                zip.delete();
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        } catch (URISyntaxException | IOException | NotaryException | CertificateException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableEntryException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Snapshot generator
     */
    public static void snapshot(URL url, Device.Type deviceType, SummaryBuilder.Language language, OutputStream stream) throws NotaryException {
        try (final ZipOutputStream zipOutputStream = new ZipOutputStream(stream)) {
            // init documentation
            final Builder jsonMeta = new MetaBuilder();
            final Builder jsonSchema = new SchemaBuilder();
            //final Builder summary = new SummaryBuilder(language);

            // init inspections
            final Inspection visibility = new VisibilityInspection();
            final Inspection modality = new ModalityInspection();
            final Inspection resources = new ResourcesInspection();
            final Inspection tlsCertificate = new TlsCertificateInspection();
            final Inspection traffic = new TrafficInspection();
            final Inspection cookie = new CookieInspection();
            final Inspection etag = new EtagInspection();

            // create profile dir
            File userData = Files.createTempDirectory("profile").toFile();
            userData.deleteOnExit();
            String userDataDir = userData.getAbsolutePath();

            // inspect
            new Visit(userDataDir, deviceType, Visit.Type.First, url, VISIT_DURATION_SEC)
                    .inspect(
                            visibility,
                            modality,
                            resources,
                            tlsCertificate,
                            traffic,
                            cookie,
                            etag
                    )
                    .publish(zipOutputStream, FileTime.from(NtpClock.getInstance().instant()))
                    //.publish(summary)
                    .publish(jsonMeta)
                    .publish(jsonSchema);
            new Visit(userDataDir, deviceType, Visit.Type.Returning, url, VISIT_DURATION_SEC)
                    .inspect(traffic, cookie, etag)
                    .publish(zipOutputStream, FileTime.from(NtpClock.getInstance().instant()))
                    //.publish(summary)
                    .publish(jsonMeta)
                    .publish(jsonSchema);
            new Visit(userDataDir, deviceType, Visit.Type.Incognito, url, VISIT_DURATION_SEC)
                    .inspect(traffic, cookie, etag)
                    .publish(zipOutputStream, FileTime.from(NtpClock.getInstance().instant()))
                    //.publish(summary)
                    .publish(jsonMeta)
                    .publish(jsonSchema);

            // remove profile dir
            userData.delete();

            // add documentation
            Instant now = NtpClock.getInstance().instant();
/*            summary
                    .append(
                            visibility,
                            modality,
                            resources,
                            tlsCertificate,
                            traffic,
                            cookie,
                            etag
                    )
                    .build(zipOutputStream, SUMMARY_NAME);*/
            jsonSchema
                    .append(
                            visibility,
                            modality,
                            resources,
                            tlsCertificate,
                            traffic,
                            cookie,
                            etag
                    )
                    .build(zipOutputStream, SCHEMA_NAME, FileTime.from(now));
            jsonMeta
                    .append(
                            visibility,
                            modality,
                            resources,
                            tlsCertificate,
                            traffic,
                            cookie,
                            etag
                    )
                    .build(zipOutputStream, META_NAME, FileTime.from(now));

        } catch (IOException e) {
            throw new NotaryException(e.getMessage());
        }
    }

    /**
     * Distinct Cookies
     *
     * @param browserCookies Inspected cookies
     * @param cookies        Recorded cookies
     */
    public static void distinctCookies(List<Cookie> browserCookies, List<org.apache.http.cookie.Cookie> cookies) {
        cookies.forEach((cookie) -> {
            for (Cookie item : browserCookies) {
                if (item.equals(cookie)) {
                    item.update(cookie);
                    return;
                }
            }
            browserCookies.add(new Cookie(cookie));
        });
    }

    /**
     * Distinct ETags
     *
     * @param etags Inspected ETags
     * @param etag  Recorded ETag
     */
    public static void distinctEtags(List<Etag> etags, Etag etag) {
        for (Etag item : etags) {
            if (item.equals(etag)) {
                return;
            }
        }
        etags.add(etag);
    }

    /**
     * Parse Set-Cookie and Cookie headers
     *
     * @param url         Request URL
     * @param headerValue Header value
     * @return Cookie list
     * @throws MalformedCookieException Exception
     */
    public static List<org.apache.http.cookie.Cookie> parseCookieValue(URL url, String headerValue) throws MalformedCookieException {
        int port = (url.getPort() < 0) ? 80 : url.getPort();
        boolean isSecure = "https".equals(url.getProtocol()) || "wss".equals(url.getProtocol());
        try {
            return new RFC6265LaxSpec().parse(
                    new BasicHeader(SM.SET_COOKIE, headerValue),
                    new CookieOrigin(url.getHost(), port, url.getPath(), isSecure)
            );
        } catch (MalformedCookieException e) {
            return new DefaultCookieSpec().parse(
                    new BasicHeader(SM.COOKIE, headerValue),
                    new CookieOrigin(url.getHost(), port, url.getPath(), isSecure)
            );
        }
    }

    /**
     * Match source and target URL
     *
     * @param source            Source URL
     * @param sourceIp          Source IP
     * @param target            Target URL
     * @param targetIp          Target IP
     * @param sourceCertificate Source TLS Certificate, if any
     * @param targetCertificate Target TLS Certificate, if any
     * @return True or false
     */
    public static boolean isFirstParty(URL source, InetAddress sourceIp, URL target, InetAddress targetIp, @Nullable TlsCertificate sourceCertificate, @Nullable TlsCertificate targetCertificate) {
        if (source.getHost().equals(target.getHost())) {
            return true;
        }
        if (source.getHost().endsWith(InternetDomainName.from(target.getHost()).topPrivateDomain().toString()) || target.getHost().endsWith(InternetDomainName.from(source.getHost()).topPrivateDomain().toString())) {
            if (null != sourceCertificate && null != targetCertificate && sourceCertificate.getIssuer().equals(targetCertificate.getIssuer()) && sourceCertificate.getSerial().equals(targetCertificate.getSerial())) {
                return true;
            }
            return sourceIp.equals(targetIp);
        }
        return false;
    }

    /**
     * Check validity of the TLS Certificate Path
     *
     * @param path Certificate Path (aka Chain)
     * @return True or false
     */
    public static boolean isCertificatePathValid(@Nullable List<TlsCertificate> path) {
        boolean isValid = false;
        if (null != path) {
            for (TlsCertificate certificate : path) {
                if (null == certificate.isValid()) {
                    continue;
                }
                isValid = certificate.isValid();
                if (!isValid) {
                    break;
                }
            }
        }
        return isValid;
    }

    /**
     * Retrieve TLS Certificate Path and validate it at given timestamp
     *
     * @param url     Web-page URL
     * @param instant Timestamp
     * @return TLS Certificate Path
     */
    @SuppressWarnings("WrapperTypeMayBePrimitive")
    public static List<TlsCertificate> getCertificatePath(URL url, Instant instant) {
        final List<TlsCertificate> path = new LinkedList<>();
        try {
            System.setProperty("com.sun.net.ssl.checkRevocation", "true");
            System.setProperty("com.sun.security.enableCRLDP", "true");
            Security.setProperty("ocsp.enable", "true");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.connect();
            for (Certificate certificate : connection.getServerCertificates()) {
                if (certificate instanceof X509Certificate) {
                    boolean isValid = false;
                    try {
                        ((X509Certificate) certificate).checkValidity(Date.from(instant));
                        isValid = true;
                    } catch (CertificateExpiredException | CertificateNotYetValidException ignored) {
                    }
                    path.add(
                            new TlsCertificate(
                                    certificate.getType(),
                                    isValid,
                                    ((X509Certificate) certificate).getIssuerDN().getName(),
                                    ((X509Certificate) certificate).getSerialNumber(),
                                    ((X509Certificate) certificate).getNotAfter()
                            )
                    );
                } else {
                    path.add(new TlsCertificate(certificate.getType()));
                }
            }
            connection.disconnect();
        } catch (IOException | ClassCastException ignored) {
        }
        return path;
    }

    /**
     * @param stream Resource stream
     * @throws IOException Exception
     */
    private static void unzip(InputStream stream) throws IOException {
        try (final ZipInputStream zipInputStream = new ZipInputStream(stream)) {
            Path targetDir = Paths.get("bin");
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (null != zipEntry) {
                Path path = targetDir.resolve(zipEntry.getName()).normalize();
                if (!path.startsWith(targetDir)) {
                    throw new IOException("Bad entry: " + zipEntry.getName());
                }
                if (zipEntry.getName().endsWith(File.separator)) {
                    Files.createDirectories(path);
                } else {
                    if (path.getParent() != null) {
                        if (Files.notExists(path.getParent())) {
                            Files.createDirectories(path.getParent());
                        }
                    }
                    Files.copy(zipInputStream, path, StandardCopyOption.REPLACE_EXISTING);
                }
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
        }
    }

    /**
     * @param stream Output
     */
    private static void status(OutputStream stream) {
        final Context ctx = new Context();
        ctx.setVariable("javaVendor", System.getProperty("java.vendor"));
        ctx.setVariable("javaVersion", System.getProperty("java.version"));
        ctx.setVariable("osName", System.getProperty("os.name"));
        ctx.setVariable("osVersion", System.getProperty("os.version"));
        ctx.setVariable("title", APP_TITLE);
        ctx.setVariable("version", APP_VERSION);
        ctx.setVariable("vendor", APP_VENDOR);
        ctx.setVariable("dns", APP_DNS);
        ctx.setVariable("ntp", APP_NTP);
        ctx.setVariable("tsa", APP_TSA);
        final TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(new ClassLoaderTemplateResolver());
        templateEngine.process("/templates/status.html", ctx, new OutputStreamWriter(stream));
    }
}
