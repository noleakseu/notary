package notary;

import org.junit.Assert;
import org.junit.Test;

import java.net.URL;

public class VisitTest {

    @Test
    public void testModalityInspection() throws Exception {
        var inspection = new ModalityInspection();
        Assert.assertFalse(
                new Visit("tmp", Device.Type.iPhone, Visit.Type.Incognito, new URL("https://noleaks.eu"), 1)
                        .inspect(inspection)
                        .publish()
                        .isEmpty()
        );
        Assert.assertTrue(inspection.getArtifacts().get(0).isScrollable());
        Assert.assertEquals("image/png", inspection.getArtifacts().get(0).getType());
        Assert.assertEquals("ModalityInspection.Incognito.png", inspection.getArtifacts().get(0).getFile());
    }

    @Test
    public void testVisibilityInspection() throws Exception {
        var inspection = new VisibilityInspection();
        Assert.assertFalse(
                new Visit("tmp", Device.Type.iPhone, Visit.Type.Incognito, new URL("https://noleaks.eu"), 1)
                        .inspect(inspection)
                        .publish()
                        .isEmpty()
        );
        Assert.assertEquals("image/png", inspection.getArtifacts().get(0).getType());
        Assert.assertEquals("VisibilityInspection.Incognito.png", inspection.getArtifacts().get(0).getFile());
    }

    @Test
    public void testTlsCertificateInspection() throws Exception {
        var inspection = new TlsCertificateInspection();
        Assert.assertTrue(
                new Visit("tmp", Device.Type.iPhone, Visit.Type.Incognito, new URL("https://noleaks.eu"), 1)
                        .inspect(inspection)
                        .publish()
                        .isEmpty()
        );
        Assert.assertTrue(inspection.isValid());
        Assert.assertEquals("CN=R3, O=Let's Encrypt, C=US", inspection.getPath().get(0).getIssuer());
    }

    @Test
    public void testTrafficInspection() throws Exception {
        var inspection = new TrafficInspection();
        Assert.assertFalse(
                new Visit("tmp", Device.Type.iPhone, Visit.Type.Incognito, new URL("https://noleaks.eu"), 1)
                        .inspect(inspection)
                        .publish()
                        .isEmpty()
        );
        Assert.assertEquals("application/json", inspection.getArtifacts().get(0).getType());
        Assert.assertEquals("TrafficInspection.Incognito.har", inspection.getArtifacts().get(0).getFile());
    }

    @Test
    public void testResourcesInspection() throws Exception {
        var inspection = new ResourcesInspection();
        Assert.assertTrue(
                new Visit("tmp", Device.Type.iPhone, Visit.Type.Incognito, new URL("http://test.noleaks.eu"), 1)
                        .inspect(inspection)
                        .publish()
                        .isEmpty()
        );
        Assert.assertEquals(8, inspection.getFirstParty().size());
        Assert.assertEquals(1, inspection.getThirdParty().size());
    }

    @Test
    public void testCookieInspection() throws Exception {
        var inspection = new CookieInspection();
        Assert.assertTrue(
                new Visit("tmp", Device.Type.iPhone, Visit.Type.Incognito, new URL("http://test.noleaks.eu"), 1)
                        .inspect(inspection)
                        .publish()
                        .isEmpty()
        );
        Assert.assertEquals(1, inspection.getFirstParty().size());
        Assert.assertEquals(0, inspection.getThirdParty().size());
    }

    @Test
    public void testEtagInspection() throws Exception {
        var inspection = new EtagInspection();
        Assert.assertTrue(
                new Visit("tmp", Device.Type.iPhone, Visit.Type.Incognito, new URL("http://test.noleaks.eu"), 1)
                        .inspect(inspection)
                        .publish()
                        .isEmpty()
        );
        Assert.assertEquals(3, inspection.getFirstParty().size());
        Assert.assertEquals(0, inspection.getThirdParty().size());
    }

    @Test
    public void testIncapsulaProtection() throws Exception {
        var inspection = new VisibilityInspection();
        Assert.assertFalse(
                new Visit("tmp", Device.Type.PC, Visit.Type.Incognito, new URL("https://coursehero.com/"), 10)
                        .inspect(inspection)
                        .publish()
                        .isEmpty()
        );
        Assert.assertEquals("image/png", inspection.getArtifacts().get(0).getType());
        Assert.assertEquals("VisibilityInspection.Incognito.png", inspection.getArtifacts().get(0).getFile());
    }
}