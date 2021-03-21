package notary;

import org.junit.Assert;
import org.junit.Test;

import java.net.URL;

public class VisitTest {

    @Test
    public void testModalityInspection() throws Exception {
        ModalityInspection inspection = new ModalityInspection();
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
        VisibilityInspection inspection = new VisibilityInspection();
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
        TlsCertificateInspection inspection = new TlsCertificateInspection();
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
        TrafficInspection inspection = new TrafficInspection();
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
        ResourcesInspection inspection = new ResourcesInspection();
        Assert.assertTrue(
                new Visit("tmp", Device.Type.iPhone, Visit.Type.Incognito, new URL("http://test.noleaks.eu"), 1)
                        .inspect(inspection)
                        .publish()
                        .isEmpty()
        );
        Assert.assertEquals(3, inspection.getFirstParty().size());
        Assert.assertEquals(6, inspection.getThirdParty().size());
    }

    @Test
    public void testCookieInspection() throws Exception {
        CookieInspection inspection = new CookieInspection();
        Assert.assertTrue(
                new Visit("tmp", Device.Type.iPhone, Visit.Type.Incognito, new URL("http://test.noleaks.eu"), 1)
                        .inspect(inspection)
                        .publish()
                        .isEmpty()
        );
        Assert.assertEquals(0, inspection.getFirstParty().size());
        Assert.assertEquals(1, inspection.getThirdParty().size());
    }

    @Test
    public void testEtagInspection() throws Exception {
        EtagInspection inspection = new EtagInspection();
        Assert.assertTrue(
                new Visit("tmp", Device.Type.iPhone, Visit.Type.Incognito, new URL("http://test.noleaks.eu"), 1)
                        .inspect(inspection)
                        .publish()
                        .isEmpty()
        );
        Assert.assertEquals(2, inspection.getFirstParty().size());
        Assert.assertEquals(1, inspection.getThirdParty().size());
    }
}