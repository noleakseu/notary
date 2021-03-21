package notary;

import org.junit.Assert;
import org.junit.Test;

import java.net.URL;

public class SchemaBuilderTest {

    @Test
    public void build() throws Exception {
        CookieInspection inspection = new CookieInspection();
        Visit visit = new Visit("tmp", Device.Type.iPhone, Visit.Type.Incognito, new URL("http://test.noleaks.eu"), 1);
        visit.inspect(inspection);
        String schema = new SchemaBuilder()
                .append(visit)
                .append(inspection)
                .build();
        Assert.assertTrue(schema.contains(Visit.Type.Incognito.name()));
    }
}