package notary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

final public class NtpClock extends Clock {

    private static NtpClock instance;

    private NtpClock() {
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
        return Clock.system(zoneId);
    }

    @Override
    public Instant instant() {
        return Instant.now(Clock.system(this.getZone()));
    }
}
