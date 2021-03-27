package notary;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

class NtpClock extends Clock {
    private final NTPUDPClient client;
    private static final int TIMEOUT = 2000;
    private InetAddress ip;
    private static NtpClock instance;

    private NtpClock() {
        client = new NTPUDPClient();
    }

    public static NtpClock getInstance() {
        if (null == instance) {
            instance = new NtpClock();
        }
        return instance;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zoneId) {
        return ntp();
    }

    @Override
    public Instant instant() {
        return Instant.now(ntp());
    }

    private Clock ntp() {
        try {
            if (null == ip) {
                ip = DnsOverHttpsResolver.resolveHostName(Main.APP_NTP);
            }
            client.open();
            client.setDefaultTimeout(TIMEOUT);
            client.setSoTimeout(TIMEOUT);
            TimeInfo time = client.getTime(ip);
            client.close();
            return Clock.fixed(Instant.ofEpochMilli(time.getReturnTime()), this.getZone());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}
