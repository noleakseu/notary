package notary;

import net.lightbody.bmp.proxy.dns.NativeResolver;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RFC8484
 */
class DnsOverHttpsResolver extends NativeResolver {
    private static final int TIMEOUT = 2000;
    private static final ConcurrentHashMap<String, Collection<InetAddress>> cache = new ConcurrentHashMap<>();
    private static final DohResolver resolver = new DohResolver(Main.APP_DNS, 1, Duration.ofMillis(TIMEOUT));

    @Override
    public Collection<InetAddress> resolveRemapped(String host) {
        try {
            cache.putIfAbsent(host, Collections.singletonList(resolveHostName(host)));
            return cache.get(host);
        } catch (UnknownHostException e) {
            return Collections.emptyList();
        }
    }

    static InetAddress resolveHostName(String hostName) throws UnknownHostException {
        try {
            final Message response = resolver.send(
                    Message.newQuery(
                            Record.newRecord(Name.fromString(hostName + "."), Type.A, DClass.IN)
                    )
            );
            if (response.getRcode() != Rcode.NOERROR) {
                throw new UnknownHostException(response.toString());
            }
            for (final Record record : response.getSectionArray(Section.ANSWER)) {
                if (record instanceof ARecord) {
                    return InetAddress.getByName(((ARecord) record).getAddress().getHostAddress());
                }
            }
        } catch (IOException e) {
            throw new UnknownHostException(e.toString());
        }
        throw new UnknownHostException("Unknown host " + hostName);
    }
}
