package looking_glass.message;

import java.time.ZonedDateTime;
import java.util.Optional;

public class Cookie  {
    public String name, value, domain, path, expiration;

    // Create a cookie from a burp.api.montoya.http.message.Cookie.
    public Cookie(burp.api.montoya.http.message.Cookie c) {
        this.name = c.name();
        this.value = c.value();
        this.domain = c.domain();
        this.path = c.path();
        this.expiration = c.expiration().map(ZonedDateTime::toString).orElse("");
    }

    // This can be used to create a response cookie.
    public Cookie(String name, String value, String domain, String path, Optional<ZonedDateTime> expiration) {
        this.name = name;
        this.value = value;
        this.domain = domain;
        this.path = path;
        this.expiration = expiration.map(ZonedDateTime::toString).orElse("");
    }

    // This can be used to create a request cookie.
    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
        this.domain = "";
        this.path = "";
        this.expiration = "";
    }
}
