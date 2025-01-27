package looking_glass;

import java.time.ZonedDateTime;
import java.util.Optional;

// This is based on the burp.api.montoya.http.message.Cookie interface.
public class Cookie {
    private String name, value, domain, path;
    private Optional<ZonedDateTime> expiration;

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Cookie(burp.api.montoya.http.message.Cookie burpCookie) {
        this.name = burpCookie.name();
        this.value = burpCookie.value();
        this.domain = burpCookie.domain();
        this.path = burpCookie.path();
        this.expiration = burpCookie.expiration();
    }

    public String name() {
        return this.name;
    }

    public String value() {
        return this.value;
    }

    public String domain() {
        return this.domain;
    }

    public String path() {
        return this.path;
    }

    public Optional<ZonedDateTime> expiration() {
        return this.expiration;
    }
}
