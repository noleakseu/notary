package notary;

import org.junit.Assert;
import org.junit.Test;

import java.net.URL;

public class MetaBuilderTest {

    @Test
    public void build() throws Exception {
        CookieInspection inspection = new CookieInspection();
        Visit visit = new Visit("tmp", Device.Type.iPhone, Visit.Type.Incognito, new URL("http://test.noleaks.eu"), 1);
        visit.inspect(inspection);
        String meta = new MetaBuilder()
                .append(visit)
                .append(inspection)
                .build();
        Assert.assertTrue(meta.contains(Visit.Type.Incognito.name()));
    }
}