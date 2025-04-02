package looking_glass.message;

import burp.api.montoya.http.message.HttpHeader;

// Represents a header in an HTTP request or response. It's created so I can
// convert the headers to JSON. I cannot convert the Burp HttpHeader object to
// JSON correctly.
public class Header implements HttpHeader {
    public String name, value;

    public Header(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Header(HttpHeader header) {
        this.name = header.name();
        this.value = header.value();
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.name + ": " + this.value;
    }
}