package notary;

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

import java.io.*;

import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {
    private static final long VISIT_DURATION_SEC = 5;
    private static final int DAILY_LIMIT = 1;

    private static final String SNAPSHOT_NAME = "snapshot.zip";
    private static final String SCHEMA_NAME = "schema.json";
    private static final String META_NAME = "meta.json";
    private static final String SUMMARY_NAME = "summary.html";

    public static final String DRIVER_FILE = "bin/chromedriver";
    public static final String CHROME_FILE = "bin/chrome-linux/chrome";

    public static final String ARG_URL = "url";
    public static final String ARG_LANGUAGE = "language";
    public static final String ARG_DEVICE = "device";
    public static final String ARG_KEYSTORE = "keystore";
    public static final String ARG_ALIAS = "alias";
    public static final String ARG_STOREPASS = "storepass";

    /**
     * Entry point
     */
    public static void main(String[] args) {
        String name = Main.class.getPackage().getImplementationTitle();
        String version = Main.class.getPackage().getImplementationVersion();
        String vendor = Main.class.getPackage().getImplementationVendor();

        ArgumentParser parser = ArgumentParsers.newFor(name.toLowerCase()).build()
                .defaultHelp(true)
                .description("" + name + " " + version + " by " + vendor);
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
            Namespace ns = parser.parseArgs(args);

            // unpack binaries
            try {
                unzip(Main.class.getClassLoader().getResourceAsStream("chrome-linux.zip"));
                unzip(Main.class.getClassLoader().getResourceAsStream("chromedriver_linux64.zip"));
                new File(DRIVER_FILE).setExecutable(true);
                new File(CHROME_FILE).setExecutable(true);
            } catch (IOException e) {
                System.exit(1);
            }

            // create signer
            JarSigner signer = new JarSigner.Builder(
                    (KeyStore.PrivateKeyEntry) KeyStore
                            .getInstance(new File(ns.getString(ARG_KEYSTORE)), ns.getString(ARG_STOREPASS).toCharArray())
                            .getEntry(ns.getString(ARG_ALIAS), new KeyStore.PasswordProtection(ns.getString(ARG_STOREPASS).toCharArray()))
            ).build();

            if (ns.get("url").equals("")) {
                // API mode
                Javalin app = Javalin.create();
                Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
                app.get("/", ctx -> {
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
                }).start("127.0.0.1", 8000);
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
        } catch (InterruptedException | IOException | InspectionException | CertificateException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableEntryException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Snapshot generator
     */
    public static void snapshot(URL url, Device.Type deviceType, SummaryBuilder.Language language, OutputStream stream) throws IOException, InspectionException, InterruptedException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(stream)) {
            // init documentation
            Builder jsonMeta = new MetaBuilder();
            Builder jsonSchema = new SchemaBuilder();
            //Builder summary = new SummaryBuilder(language);

            // init inspections
            Inspection visibility = new VisibilityInspection();
            Inspection modality = new ModalityInspection();
            Inspection resources = new ResourcesInspection();
            Inspection tlsCertificate = new TlsCertificateInspection();
            Inspection traffic = new TrafficInspection();
            Inspection cookie = new CookieInspection();
            Inspection etag = new EtagInspection();

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
                    .publish(zipOutputStream)
                    //.publish(summary)
                    .publish(jsonMeta)
                    .publish(jsonSchema);
            new Visit(userDataDir, deviceType, Visit.Type.Returning, url, VISIT_DURATION_SEC)
                    .inspect(traffic, cookie, etag)
                    .publish(zipOutputStream)
                    //.publish(summary)
                    .publish(jsonMeta)
                    .publish(jsonSchema);
            new Visit(userDataDir, deviceType, Visit.Type.Incognito, url, VISIT_DURATION_SEC)
                    .inspect(traffic, cookie, etag)
                    .publish(zipOutputStream)
                    //.publish(summary)
                    .publish(jsonMeta)
                    .publish(jsonSchema);

            // remove profile dir
            userData.delete();

            // add documentation
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
                    .build(zipOutputStream, SCHEMA_NAME);
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
                    .build(zipOutputStream, META_NAME);

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
     * @param target            Target URL
     * @param sourceCertificate Source TLS Certificate, if any
     * @param targetCertificate Target TLS Certificate, if any
     * @return True or false
     */
    public static boolean isFirstParty(URL source, URL target, @Nullable TlsCertificate sourceCertificate, @Nullable TlsCertificate targetCertificate) {
        if (null != sourceCertificate && null != targetCertificate) {
            return sourceCertificate.getIssuer().equals(targetCertificate.getIssuer()) && sourceCertificate.getSerial().equals(targetCertificate.getSerial());
        }
        if (source.getProtocol().equals(target.getProtocol())) {
            return source.getHost().equals(target.getHost());
        }
        return false;
    }

    /**
     * Check validity of the TLS Certificate Path
     *
     * @param path Certificate Path (aka Chain)
     * @return True, false of unknown
     */
    public static @Nullable
    Boolean isCertificatePathValid(@Nullable List<TlsCertificate> path) {
        Boolean isValid = null;
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
    public static @Nullable
    List<TlsCertificate> getCertificatePath(URL url, Instant instant) {
        List<TlsCertificate> path = null;
        try {
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.connect();
            path = new LinkedList<>();
            for (Certificate certificate : connection.getServerCertificates()) {
                if (certificate instanceof X509Certificate) {
                    Boolean isValid = false;
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
        try (ZipInputStream zipInputStream = new ZipInputStream(stream)) {
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
}
