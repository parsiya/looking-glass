# Database Structure
There are two tables in the database: `requests` and `responses`. The data
either comes from Burp or extracted from the text of the requests and responses,
HTTP is Hyper TEXT Transfer Protocol after all :).


## Table Structure
We have two tables.

## Requests Table
For `GET https://example.net/foo/bar?param1=val1&param2=val2`.

| Header Names    | Notes                                                                     |
| --------------- | ------------------------------------------------------------------------- |
| request_id      | Unique ID of the request in the table.                                    |
| url             | Complete URL with query string: `https://example.net/foo/bar?param1=...`. |
| method          | HTTP Method/Verb in UPPERCASE. E.g., GET/POST.                            |
| path            | Path including query strings: `/foo/bar?param1=val1&param2=val2`.         |
| host            | Request's domain. Usually the same as the `Host` Header: `example.net`.   |
| port            | Requests's port: `443`.                                                   |
| is_https        | Boolean: `1` if using HTTPS and `0` if not.                               |
| notes           | Any notes set in HTTP Proxy, otherwise empty.                             |
| tool_source     | Burp Tool that initiated the request. E.g., `Proxy`.                      |
| content_type    | Value of `Content-Type` header.                                           |
| content_length  | Value of `Content-Length` header as a number.                             |
| origin          | Value of `Origin` header.                                                 |
| referer         | Value of `Referer` header.                                                |
| parameter_names | Comma-separated list of all parameter names.                              |
| cookie_names    | Comma-separated list of all cookie names.                                 |
| header_names    | Comma-separated list of all headers names.                                |
| data            | The request object as a JSON string, [structure below][req-json].         |

[req-json]: #the-request-object-as-json

## Response Table
The other table is very similar:

| Name                    | Notes                                                                       |
| ----------------------- | --------------------------------------------------------------------------- |
| response_id             | Unique ID of the response in the table.                                     |
| request_id              | ID of the request that initiated this response in the `requests` table.     |
| status_code             | Status code as a number. E.g., `200`.                                       |
| reason_phrase           | Short description of status code. E.g., `OK` for `200`.                     |
| content_type            | Value of `Content-Type` header.                                             |
| inferred_content_type   | Inferred content type returned by Burp ([see below][inferred]).             |
| content_length          | Value of `Content-Length` header as a number.                               |
| date                    | Value of `Date` header (if any) as a Unix timestamp. E.g., `1743395234000`. |
| cookie_names            | Comma-separated list of all cookie names.                                   |
| tool_source             | Burp Tool that initiated the request. E.g., `Proxy`.                        |
| content_security_policy | Boolean: `1` if the `Content-Security-Policy` header exists, otherwise `0`. |
| header_names            | Comma-separated list of all headers names.                                  |
| data                    | The response object as a JSON string, [structure below][res-json].          |

[inferred]: #content_type-and-inferred_content_type
[res-json]: #response-object-as-json

## Columns
Some popular (and in my opinion useful) fields were extracted from the data and
stored as separate columns. I will explain some of them here.

### tool_source
If you're capturing with the HTTP Handler, then you can see all requests from
all Burp tools and all extensions that are above Looking Glass in the extension
order. Burp allows us to get this information and we store it in the
`tool_source` column.

If you've used the `Import Proxy History` feature, the requests only came from
the history and the value of this column is `Proxy` for all of them.

[burp.api.montoya.core.ToolType][tooltype] is an enum with all possible values
such as `Proxy`, `Repeater` and `Extensions` (Burp API doesn't allow us to see
which extension). Note we're storing the string value of the enums in the
database which is just the first letter capitalized. E.g., the value of `PROXY`
is stored as `Proxy`.

[tooltype]: https://portswigger.github.io/burp-extensions-montoya-api/javadoc/burp/api/montoya/core/ToolType.html

### parameter_names, header_names, and cookie_names
Each column contains a comma-separated list of **just the names**. This allows
us to quickly search for existence of a header or parameter name. If needed, we
can decide to extract their value from the `data` column.

### content_type and inferred_content_type
We store the actual value of the `Content-Type` header for both requests and
responses in the `content_type` column.

While this is a freeform header, the actual values set in the real world are
very limited. Burp also returns an inferred content type for responses. This is
an enum and a subset of all possible content-type values. This is stored in
[burp.api.montoya.http.message.MimeType][mime].

[mime]: https://portswigger.github.io/burp-extensions-montoya-api/javadoc/burp/api/montoya/http/message/MimeType.html

By converting the Burp's proxy filter to Bambda mode, I figure out which
MimeTypes are allowed by each checkbox. Instead of checking against specific
MimeTypes, it checks whether it's one of the unchecked ones.

![Burp proxy filter MIME types](/.github/03-mimetypes.png)

Burp detects 25 different types. Three types are always allowed (first row) and
the rest are in this format: [name of the checkbox] -> affected mimetype(s).

| Checkbox Name  | MIME types                                                                         |
| -------------- | ---------------------------------------------------------------------------------- |
| Always allowed | `NONE`, `UNRECOGNIZED`, `AMBIGUOUS`                                                |
| `HTML`         | `HTML`                                                                             |
| `Other text `  | `PLAIN_TEXT`, `RTF`                                                                |
| `Script`       | `SCRIPT`, `JSON`                                                                   |
| `Images`       | `IMAGE_UNKNOWN`, `IMAGE_JPEG`, `IMAGE_GIF`, `IMAGE_PNG`, `IMAGE_BMP`, `IMAGE_TIFF` |
| `XML`          | `XML`, `IMAGE_SVG_XML`                                                             |
| `Flash`        | `APPLICATION_FLASH`, `LEGACY_SER_AMF`                                              |
| `CSS`          | `CSS`                                                                              |
| `Other binary` | `UNRECOGNIZED`, `SOUND`, `VIDEO`, `FONT_WOFF`, `FONT_WOFF2`, `APPLICATION_UNKNOWN` |

## The data Column
The `data` column is the JSON presentation of the `Request` and `Response` Java
classes. These classes are very similar to the ones provided by Burp. Using this
data as a string allows us to use SQLite's [json_extract][json_extract] function
to pull values.

[json_extract]: https://www.sqlite.org/json1.html#the_json_extract_function

We will use this in [queries](queries.md) extensively. For now, let's discuss
some of the nested objects.

### Parameter Object
Each request has `List<Parameter>` where
[Parameter.java](/src/main/java/looking_glass/message/Parameter.java) has three fields:

```java
// fields
public String parameterType, name, value;
```

Or as a JSON string:

```json
{
  // removed
  "parameters": [
    {
      "parameterType": "URL",
      "name": "param1",
      "value": "value1"
    },
    {
      "parameterType": "URL",
      "name": "param2",
      "value": "value2"
    }
    // removed
  ]
}
```

[ParameterType][paramtype-api] is reported by Burp can be one of:
`BODY, COOKIE, JSON, MULTIPART_ATTRIBUTE, URL, XML, XML_ATTRIBUTE`.

[paramtype-api]: https://portswigger.github.io/burp-extensions-montoya-api/javadoc/burp/api/montoya/http/message/params/HttpParameterType.html

### Cookie Object
Each request and response has `List<Cookie>` where the
[Cookie.java](/src/main/java/looking_glass/message/Cookie.java) object has:

```java
public String name, value, domain, path, expiration;
```

Request cookies only use `name` and `value` with the rest of the fields set to
`""`. As a JSON string it looks like this. 

```json
{
  "cookies": [
    {
      "name": "cookie1",
      "value": "cookievalue1",
      "domain": "",
      "path": "",
      "expiration": ""
    },
    {
      "name": "cookie2",
      "value": "cookievalue2",
      "domain": "",
      "path": "",
      "expiration": ""
    }
  ]
}
```

A response cookie utilizes all fields:

```json
{
  "cookies": [
    {
      "name": "cookie1",
      "value": "value1",
      "domain": "microsoft.com",
      "path": "/",
      "expiration": "2026-03-30T21:27:29-07:00[America/Los_Angeles]"
    }
  ]
}
```

### Header Object
Both requests and responses have `List<Header>` where the
[Header.java](/src/main/java/looking_glass/message/Header.java) object has:

```java
public String name, value;
```

Which looks like your basic key/value pair JSON string:

```json
// Assume this is a response.
{
  "headers": [
    {
      "name": "Content-Length",
      "value": "153"
    },
    {
      "name": "Content-Type",
      "value": "application/json"
    },
    {
      "name": "Server",
      "value": "Microsoft-HTTPAPI/2.0"
    }
  ]
}
```

### The Request Object as JSON
The [Request.java](/src/main/java/looking_glass/message/Request.java) class
looks like. The JSON representation of `GET https://example.net/foo/bar?p=v` is:

```json
{
  "url": "https://example.net/foo/bar?param1=val1&param2=val2",
  "method": "GET",
  "path": "/foo/bar?param1=val1&param2=val2",
  "httpVersion": "HTTP/2",
  "body": "",
  "parameters": [
    {
      "parameterType": "URL",
      "name": "param1",
      "value": "val1"
    },
    {
      "parameterType": "URL",
      "name": "param2",
      "value": "val2"
    }
  ],
  "headers": [
    {
      "name": "Host",
      "value": "example.net"
    },
    {
      "name": "Cookie",
      "value": "cookie1=cookievalue1; cookie2=cookievalue2"
    },
    // removed
  ],
  "cookies": [
    {
      "name": "cookie1",
      "value": "cookievalue1",
      "domain": "",
      "path": "",
      "expiration": ""
    },
    {
      "name": "cookie2",
      "value": "cookievalue2",
      "domain": "",
      "path": "",
      "expiration": ""
    }
  ],
  "host": "example.net",
  "port": 443,
  "isHttps": true,
  "authorizationType": null,
  "highlightColor": "NONE",
  "notes": "",
  "toolSource": "Proxy",
  "contentType": "text/plain",
  "origin": "https://www.microsoft.com",
  "referer": "https://www.microsoft.com/",
  "contentLength": 0,
  "parameterNames": "param1,param2",
  "cookieNames": "cookie1,cookie2",
  "headerNames": "Host,Cookie,..."
}
```

### Response Object as JSON
The response object as a JSON string is very similar to the request.

```json
{
  "statusCode": 200,
  "reasonPhrase": "OK",
  "httpVersion": "HTTP/2",
  "body": "body removed",
  "contentType": "text/html;charset=utf-8",
  "burpMimeType": "HTML",
  "contentLength": 215108,
  "date": "2025-03-31T04:27:26Z",
  "toolSource": "Proxy",
  "headers": [
    {
      "name": "Content-Type",
      "value": "text/html;charset=utf-8"
    },
    {
      "name": "Set-Cookie",
      "value": "cookie1=value1; path=/; Expires=Sun, 29 Jun 2025 04:27:26 GMT; Secure; SameSite=None"
    }
  ],
  "cookies": [
    {
      "name": "cookie1",
      "value": "value1",
      "domain": null,
      "path": "/",
      "expiration": "2025-06-28T21:27:26-07:00[America/Los_Angeles]"
    }
  ],
  "contentSecurityPolicy": false,
  "headerNames": "Content-Type,Set-Cookie,...",
  "cookieNames": "cookie1"
}
```
