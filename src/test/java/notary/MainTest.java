package notary;

import org.apache.http.cookie.Cookie;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class MainTest {

    @Test
    public void getCertificatePath() throws Exception {
        var path = Main.getCertificatePath(
                new URL("https://noleaks.eu"), Instant.parse("2001-01-01T01:01:01Z")
        );
        Assert.assertEquals(2, path.size());
    }

    @Test
    public void isCertificatePathInvalidPast() throws Exception {
        var path = Main.getCertificatePath(
                new URL("https://noleaks.eu"), Instant.parse("2001-01-01T01:01:01Z")
        );
        Assert.assertFalse(Main.isCertificatePathValid(path));
    }

    @Test
    public void isCertificatePathInvalidFuture() throws Exception {
        var path = Main.getCertificatePath(
                new URL("https://noleaks.eu"), Instant.parse("2031-01-01T01:01:01Z")
        );
        Assert.assertFalse(Main.isCertificatePathValid(path));
    }

    @Test
    public void isCertificatePathValidNow() throws Exception {
        var path = Main.getCertificatePath(
                new URL("https://noleaks.eu"), Instant.now()
        );
        Assert.assertTrue(Main.isCertificatePathValid(path));
    }

    @Test
    public void getCertificatePathHttp() throws Exception {
        Assert.assertTrue(Main.getCertificatePath(new URL("http://noleaks.eu"), Instant.now()).isEmpty());
    }

    @Test
    public void isCertificatePathInvalid() throws Exception {
        Assert.assertFalse(Main.isCertificatePathValid(Main.getCertificatePath(new URL("https://expired.badssl.com/"), Instant.now())));
        Assert.assertFalse(Main.isCertificatePathValid(Main.getCertificatePath(new URL("https://wrong.host.badssl.com/"), Instant.now())));
        Assert.assertFalse(Main.isCertificatePathValid(Main.getCertificatePath(new URL("https://self-signed.badssl.com/"), Instant.now())));
        Assert.assertFalse(Main.isCertificatePathValid(Main.getCertificatePath(new URL("https://untrusted-root.badssl.com/"), Instant.now())));
        Assert.assertFalse(Main.isCertificatePathValid(Main.getCertificatePath(new URL("https://no-common-name.badssl.com/"), Instant.now())));
        Assert.assertFalse(Main.isCertificatePathValid(Main.getCertificatePath(new URL("https://no-subject.badssl.com/"), Instant.now())));
        Assert.assertFalse(Main.isCertificatePathValid(Main.getCertificatePath(new URL("https://incomplete-chain.badssl.com/"), Instant.now())));
        Assert.assertFalse(Main.isCertificatePathValid(Main.getCertificatePath(new URL("https://reversed-chain.badssl.com/"), Instant.now())));
        Assert.assertFalse(Main.isCertificatePathValid(Main.getCertificatePath(new URL("https://revoked.badssl.com/"), Instant.now())));
        // TODO
        // Assert.assertFalse(Main.isCertificatePathValid(Main.getCertificatePath(new URL("https://pinning-test.badssl.com/"), Instant.now())));
    }

    @Test
    public void isFirstPartySameDomain() throws Exception {
        var oneUrl = new URL("https://noleaks.eu");
        var anotherUrl = new URL("https://noleaks.eu/");
        var oneCertificate = new TlsCertificate(
                "X.509",
                true,
                "an issuer",
                new BigInteger("1111"),
                new Date()
        );
        TlsCertificate anotherCertificate = new TlsCertificate(
                "X.509",
                true,
                "an issuer",
                new BigInteger("2222"),
                new Date()
        );

        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                null, null
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                null, null
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                oneCertificate, oneCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                oneCertificate, oneCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                null, anotherCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                oneCertificate, null
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                oneCertificate, anotherCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                null, anotherCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                oneCertificate, null
        ));
    }

    @Test
    public void isFirstPartySubDomain() throws Exception {
        var oneUrl = new URL("https://noleaks.eu");
        var anotherUrl = new URL("https://test.noleaks.eu/");
        var oneCertificate = new TlsCertificate(
                "X.509",
                true,
                "an issuer",
                new BigInteger("1111"),
                new Date()
        );
        TlsCertificate anotherCertificate = new TlsCertificate(
                "X.509",
                true,
                "an issuer",
                new BigInteger("2222"),
                new Date()
        );

        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                null, null
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                null, null
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                oneCertificate, oneCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                oneCertificate, oneCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                null, anotherCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                oneCertificate, null
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                oneCertificate, anotherCertificate
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                null, anotherCertificate
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                oneCertificate, null
        ));
    }

    @Test
    public void isFirstPartyTwoSubDomains() throws Exception {
        var oneUrl = new URL("https://tracker.noleaks.eu");
        var anotherUrl = new URL("https://test.noleaks.eu/");
        var oneCertificate = new TlsCertificate(
                "X.509",
                true,
                "an issuer",
                new BigInteger("1111"),
                new Date()
        );
        TlsCertificate anotherCertificate = new TlsCertificate(
                "X.509",
                true,
                "an issuer",
                new BigInteger("2222"),
                new Date()
        );

        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                null, null
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                null, null
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                oneCertificate, oneCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                oneCertificate, oneCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                null, anotherCertificate
        ));
        Assert.assertTrue(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                oneCertificate, null
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                oneCertificate, anotherCertificate
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                null, anotherCertificate
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                oneCertificate, null
        ));
    }

    @Test
    public void isThirdParty() throws Exception {
        var oneUrl = new URL("https://noleaks.eu");
        var anotherUrl = new URL("https://company.com/");
        var oneCertificate = new TlsCertificate(
                "X.509",
                true,
                "an issuer",
                new BigInteger("1111"),
                new Date()
        );
        TlsCertificate anotherCertificate = new TlsCertificate(
                "X.509",
                true,
                "an issuer",
                new BigInteger("2222"),
                new Date()
        );

        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                null, null
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                null, null
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                oneCertificate, oneCertificate
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                oneCertificate, oneCertificate
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                null, anotherCertificate
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("1.1.1.1"),
                oneCertificate, null
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                oneCertificate, anotherCertificate
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                null, anotherCertificate
        ));
        Assert.assertFalse(Main.isFirstParty(
                oneUrl, InetAddress.getByName("1.1.1.1"),
                anotherUrl, InetAddress.getByName("2.2.2.2"),
                oneCertificate, null
        ));
    }

    @Test
    public void isFirstPartyNoLeaks() throws Exception {
        var domain = new URL("https://noleaks.eu");
        var subDomain = new URL("https://www.noleaks.eu");
        var subSubDomain = new URL("http://tracker.test.noleaks.eu");
        var now = Instant.now();
        var sourceCerts = Main.getCertificatePath(domain, now).get(0);
        var targetCerts = Main.getCertificatePath(subDomain, now).get(0);
        var sourceIp = DnsOverHttpsResolver.resolveHostName(domain.getHost());
        var targetIp = DnsOverHttpsResolver.resolveHostName(subDomain.getHost());
        Assert.assertTrue(Main.isFirstParty(domain, sourceIp, subDomain, targetIp, sourceCerts, targetCerts));
        Assert.assertTrue(Main.isFirstParty(domain, sourceIp, subSubDomain, targetIp, sourceCerts, targetCerts));
    }

    @Test
    public void parseCookieSetSecurePersistent() throws Exception {
        var cookies = Main.parseCookieValue(
                new URL("https://www.example.com/ad.js"),
                "aaa_BBB=1614512925~rv=14~id=a9550a94fe5bd95c3677d71dd9a7b586; path=/; Expires=Sun, 28 Feb 2021 11:48:45 GMT; Secure"
        );
        var cookie = cookies.get(0);

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
        var cookies = Main.parseCookieValue(
                new URL("https://sm4xqadz4o.example.com/tracker"),
                "__uid=d6dce2070deda4fd4059da1f30d63df031614426531; expires=Sun, 13-Mar-21 11:48:51 GMT; path=/; domain=.example.com; HttpOnly; SameSite=Lax"
        );
        var cookie = cookies.get(0);

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
        var cookies = Main.parseCookieValue(
                new URL("https://example.com/tracker.gif"),
                "consentUUID=4543fb51-5de1-4bf9-8e99-4bd0e6454023; Max-Age=31536000; Path=/; Secure; SameSite=None"
        );
        var cookie = cookies.get(0);

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
        var cookies = Main.parseCookieValue(
                new URL("https://example.com/login"),
                "id=636904343fa934860ec5e27e1248e9ef"
        );
        var cookie = cookies.get(0);

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
        var cookies = Main.parseCookieValue(
                new URL("https://example.com/tracker.gif"),
                "key1=null; key2=0009fb605c47da8e9603a319d; consentUUID=4543fb51-5de1-4bf9-8e99-4bd0e6454023"
        );
        var cookie = cookies.get(0);

        Assert.assertEquals("key1", cookie.getName());
        Assert.assertEquals("null", cookie.getValue());
        Assert.assertEquals("/", cookie.getPath());
        Assert.assertEquals("example.com", cookie.getDomain());
        Assert.assertNull(cookie.getExpiryDate());
        Assert.assertFalse(cookie.isSecure());
        Assert.assertFalse(cookie.isPersistent());

        // TODO
        // cookie = cookies.get(2);
        // Assert.assertEquals("consentUUID", cookie.getName());
        // Assert.assertEquals("4543fb51-5de1-4bf9-8e99-4bd0e6454023", cookie.getValue());
        // Assert.assertEquals("/", cookie.getPath());
        // Assert.assertEquals("example.com", cookie.getDomain());
        // Assert.assertNull(cookie.getExpiryDate());
        // Assert.assertFalse(cookie.isSecure());
        // Assert.assertFalse(cookie.isPersistent());
    }
}
