package notary;

import org.apache.http.cookie.Cookie;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class MainTest {

    @Test
    public void getCertificatePath() throws Exception {
        List<TlsCertificate> path = Main.getCertificatePath(
                new URL("https://noleaks.eu"), Instant.parse("2001-01-01T01:01:01Z")
        );
        Assert.assertEquals(2, path.size());
    }

    @Test
    public void isCertificatePathValidPast() throws Exception {
        List<TlsCertificate> path = Main.getCertificatePath(
                new URL("https://noleaks.eu"), Instant.parse("2001-01-01T01:01:01Z")
        );
        Assert.assertFalse(Main.isCertificatePathValid(path));
    }

    @Test
    public void isCertificatePathValidFuture() throws Exception {
        List<TlsCertificate> path = Main.getCertificatePath(
                new URL("https://noleaks.eu"), Instant.parse("2031-01-01T01:01:01Z")
        );
        Assert.assertFalse(Main.isCertificatePathValid(path));
    }

    @Test
    public void isCertificatePathValidNow() throws Exception {
        List<TlsCertificate> path = Main.getCertificatePath(
                new URL("https://noleaks.eu"), Instant.now()
        );
        Assert.assertTrue(Main.isCertificatePathValid(path));
    }

    @Test
    public void isCertificatePathValidHttp() throws Exception {
        List<TlsCertificate> path = Main.getCertificatePath(
                new URL("http://noleaks.eu"), Instant.now()
        );
        Assert.assertNull(path);
        Assert.assertNull(Main.isCertificatePathValid(path));
    }

    @Test
    public void isFirstPartySameProtocolAndHost() throws Exception {
        TlsCertificate sourceCertificate = new TlsCertificate(
                "X.509",
                true,
                "an issuer",
                new BigInteger("2222"),
                new Date()
        );
        TlsCertificate targetCertificate = new TlsCertificate(
                "X.509",
                true,
                "an issuer",
                new BigInteger("2222"),
                new Date()
        );

        Assert.assertTrue(Main.isFirstParty(
                new URL("https://noleaks.eu"),
                new URL("https://noleaks.eu"),
                sourceCertificate, targetCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                new URL("http://noleaks.eu"),
                new URL("http://noleaks.eu"),
                null, null
        ));

        Assert.assertTrue(Main.isFirstParty(
                new URL("https://test.noleaks.eu"),
                new URL("https://test.noleaks.eu"),
                sourceCertificate, targetCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                new URL("http://test.noleaks.eu"),
                new URL("http://test.noleaks.eu"),
                null, null
        ));

        Assert.assertTrue(Main.isFirstParty(
                new URL("https://noleaks.eu/"),
                new URL("https://noleaks.eu"),
                sourceCertificate, targetCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                new URL("https://noleaks.eu"),
                new URL("https://noleaks.eu/"),
                sourceCertificate, targetCertificate
        ));

        Assert.assertTrue(Main.isFirstParty(
                new URL("https://noleaks.eu/?a"),
                new URL("https://noleaks.eu/?b"),
                sourceCertificate, targetCertificate
        ));
    }

    @Test
    public void isFirstPartySameCertificate() throws Exception {
        TlsCertificate sourceCertificate = new TlsCertificate(
                "X.509",
                true,
                "an issuer",
                new BigInteger("2222"),
                new Date()
        );
        TlsCertificate targetCertificate = new TlsCertificate(
                "X.509",
                true,
                "an issuer",
                new BigInteger("2222"),
                new Date()
        );

        Assert.assertTrue(Main.isFirstParty(
                new URL("https://noleaks.eu"),
                new URL("https://example.eu"),
                sourceCertificate, targetCertificate
        ));
        Assert.assertFalse(Main.isFirstParty(
                new URL("http://noleaks.eu"),
                new URL("http://example.eu"),
                null, null
        ));

        Assert.assertTrue(Main.isFirstParty(
                new URL("https://test.noleaks.eu"),
                new URL("https://test.example.eu"),
                sourceCertificate, targetCertificate
        ));
        Assert.assertFalse(Main.isFirstParty(
                new URL("http://test.noleaks.eu"),
                new URL("http://test.example.eu"),
                null, null
        ));

        Assert.assertTrue(Main.isFirstParty(
                new URL("https://noleaks.eu/"),
                new URL("https://example.eu"),
                sourceCertificate, targetCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                new URL("https://noleaks.eu"),
                new URL("https://example.eu/"),
                sourceCertificate, targetCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                new URL("https://noleaks.eu/?a"),
                new URL("https://example.eu/?b"),
                sourceCertificate, targetCertificate
        ));

        Assert.assertTrue(Main.isFirstParty(
                new URL("https://noleaks.eu"),
                new URL("https://test.noleaks.eu"),
                sourceCertificate, targetCertificate
        ));
        Assert.assertFalse(Main.isFirstParty(
                new URL("http://noleaks.eu"),
                new URL("http://test.noleaks.eu"),
                null, null
        ));
        Assert.assertFalse(Main.isFirstParty(
                new URL("https://noleaks.eu"),
                new URL("http://test.noleaks.eu"),
                sourceCertificate, null
        ));
        Assert.assertFalse(Main.isFirstParty(
                new URL("http://noleaks.eu"),
                new URL("https://test.noleaks.eu"),
                null, targetCertificate
        ));
    }

    @Test
    public void isFirstParty() throws Exception {
        URL source = new URL("https://noleaks.eu");
        URL target = new URL("https://www.noleaks.eu");
        Instant now = Instant.now();
        Assert.assertTrue(
                Main.isFirstParty(
                        source,
                        target,
                        Main.getCertificatePath(source, now).get(0),
                        Main.getCertificatePath(target, now).get(0)
                )
        );
    }

    @Test
    public void parseCookieSetSecurePersistent() throws Exception {
        List<Cookie> cookies = Main.parseCookieValue(
                new URL("https://www.example.com/ad.js"),
                "aaa_BBB=1614512925~rv=14~id=a9550a94fe5bd95c3677d71dd9a7b586; path=/; Expires=Sun, 28 Feb 2021 11:48:45 GMT; Secure"
        );
        Cookie cookie = cookies.get(0);

        Assert.assertEquals("aaa_BBB", cookie.getName());
        Assert.assertEquals("1614512925~rv=14~id=a9550a94fe5bd95c3677d71dd9a7b586", cookie.getValue());
        Assert.assertEquals("/", cookie.getPath());
        Assert.assertEquals("www.example.com", cookie.getDomain());
        Assert.assertEquals(Date.from(Instant.parse("2021-02-28T11:48:45Z")), cookie.getExpiryDate());
        Assert.assertTrue(cookie.isSecure());
        Assert.assertTrue(cookie.isPersistent());
    }

    @Test
    public void parseCookieSetInsecurePersistent() throws Exception {
        List<Cookie> cookies = Main.parseCookieValue(
                new URL("https://sm4xqadz4o.example.com/tracker"),
                "__uid=d6dce2070deda4fd4059da1f30d63df031614426531; expires=Sun, 13-Mar-21 11:48:51 GMT; path=/; domain=.example.com; HttpOnly; SameSite=Lax"
        );
        Cookie cookie = cookies.get(0);

        Assert.assertEquals("__uid", cookie.getName());
        Assert.assertEquals("d6dce2070deda4fd4059da1f30d63df031614426531", cookie.getValue());
        Assert.assertEquals("/", cookie.getPath());
        Assert.assertEquals("example.com", cookie.getDomain());
        Assert.assertEquals(Date.from(Instant.parse("2021-03-13T11:48:51Z")), cookie.getExpiryDate());
        Assert.assertFalse(cookie.isSecure());
        Assert.assertTrue(cookie.isPersistent());
    }

    @Test
    public void parseCookieMaxAgePersistent() throws Exception {
        List<Cookie> cookies = Main.parseCookieValue(
                new URL("https://example.com/tracker.gif"),
                "consentUUID=4543fb51-5de1-4bf9-8e99-4bd0e6454023; Max-Age=31536000; Path=/; Secure; SameSite=None"
        );
        Cookie cookie = cookies.get(0);

        Assert.assertEquals("consentUUID", cookie.getName());
        Assert.assertEquals("4543fb51-5de1-4bf9-8e99-4bd0e6454023", cookie.getValue());
        Assert.assertEquals("/", cookie.getPath());
        Assert.assertEquals("example.com", cookie.getDomain());
        Assert.assertNotEquals(null, cookie.getExpiryDate());
        Assert.assertTrue(cookie.isSecure());
        Assert.assertTrue(cookie.isPersistent());
    }

    @Test
    public void parseCookieSession() throws Exception {
        List<Cookie> cookies = Main.parseCookieValue(
                new URL("https://example.com/login"),
                "id=636904343fa934860ec5e27e1248e9ef"
        );
        Cookie cookie = cookies.get(0);

        Assert.assertEquals("id", cookie.getName());
        Assert.assertEquals("636904343fa934860ec5e27e1248e9ef", cookie.getValue());
        Assert.assertEquals("/", cookie.getPath());
        Assert.assertEquals("example.com", cookie.getDomain());
        Assert.assertNull(cookie.getExpiryDate());
        Assert.assertFalse(cookie.isSecure());
        Assert.assertFalse(cookie.isPersistent());
    }

    @Test
    public void parseCookieMultiple() throws Exception {
        List<Cookie> cookies = Main.parseCookieValue(
                new URL("https://example.com/tracker.gif"),
                "key1=null; key2=0009fb605c47da8e9603a319d; consentUUID=4543fb51-5de1-4bf9-8e99-4bd0e6454023"
        );
        Cookie cookie = cookies.get(0);

        Assert.assertEquals("key1", cookie.getName());
        Assert.assertEquals("null", cookie.getValue());
        Assert.assertEquals("/", cookie.getPath());
        Assert.assertEquals("example.com", cookie.getDomain());
        Assert.assertNull(cookie.getExpiryDate());
        Assert.assertFalse(cookie.isSecure());
        Assert.assertFalse(cookie.isPersistent());

        cookie = cookies.get(2);
        Assert.assertEquals("consentUUID", cookie.getName());
        Assert.assertEquals("4543fb51-5de1-4bf9-8e99-4bd0e6454023", cookie.getValue());
        Assert.assertEquals("/", cookie.getPath());
        Assert.assertEquals("example.com", cookie.getDomain());
        Assert.assertNull(cookie.getExpiryDate());
        Assert.assertFalse(cookie.isSecure());
        Assert.assertFalse(cookie.isPersistent());
    }
}
