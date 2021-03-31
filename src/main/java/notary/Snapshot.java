package notary;

import java.time.Instant;

public class Snapshot {
    private final String url;
    private final Throwable exception;
    private final Instant timestamp;

    public Snapshot(String url, Throwable exception) {
        this.url = url;
        this.exception = exception;
        this.timestamp = NtpClock.getInstance().instant();
    }

    public Snapshot(String url) {
        this.url = url;
        this.exception = null;
        this.timestamp = NtpClock.getInstance().instant();
    }

    public String getUrl() {
        return url;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Throwable getException() {
        return exception;
    }
}
