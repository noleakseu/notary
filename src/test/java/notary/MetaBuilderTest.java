package notary;

import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.UUID;

public class MetaBuilderTest {

    @Test
    public void build() throws Exception {
        var inspection = new CookieInspection();
        var visit = new Visit("tmp", Device.Type.iPhone, Visit.Type.Incognito, new URL("http://test.noleaks.eu"), 1);
        visit.inspect(inspection);
        var meta = new MetaBuilder(UUID.randomUUID())
                .append(visit)
                .append(inspection)
                .build();
        Assert.assertTrue(meta.contains(Visit.Type.Incognito.name()));
    }
}