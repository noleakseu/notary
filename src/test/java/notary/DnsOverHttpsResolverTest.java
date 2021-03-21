package notary;

import org.junit.Assert;
import org.junit.Test;

import java.net.UnknownHostException;

public class DnsOverHttpsResolverTest {

    @Test
    public void resolveHostName() throws UnknownHostException {
        Assert.assertEquals(
                "/51.15.79.110",
                DnsOverHttpsResolver.resolveHostName("test.noleaks.eu").toString()
        );
        Assert.assertEquals(
                "/51.15.79.110",
                DnsOverHttpsResolver.resolveHostName("noleaks.eu").toString()
        );
    }
}