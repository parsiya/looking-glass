# Looking Glass - Index, Query, Repeat
Looking Glass is a Burp extension that stores all requests/responses in a
database. You can query this data to discover endpoints, data, and anything you
might have missed.

The extension uses the Montoya APIs. It's incompatible with older versions of
Burp that don't use support this API.

I've tried to create a user experience that looks like Burp so you do not have
to learn a new GUI.

## Quick Start

1. Add the jar file from the `release` directory as an extension in Burp.
2. Navigate to the `Looking Glass` tab.
3. Click the `Capture Off` button, the extension will ask you to choose a DB file.
    1. You can also click `Select DB` to do the same.
4. (Optional) Click `Extension Settings` and configure filters.
5. Interact with the application normally.
6. Looking Glass will store all requests/responses from all tools in the database.
7. Query the database using SQL in the extension or use a separate program.

If you want to import data from older projects.

1. Navigate to the `Looking Glass` tab.
2. Click the `Import Proxy History` button.
3. You will be asked to 

**Privacy Warning**: The value of parameters, headers and cookies are stored in
the database. This includes passwords and tokens. The same are also stored in
your Burp projects, so keep them all safe.

I have a feature planned that allows the user to block storage of values (except
some that are not sensitive but useful) in the database. See issue #8.

## Extension Settings
Looking Glass uses a local SQLite database to store the data. You don't need to
install extra drivers or setup a server. Everything is local.

At a high-level, you can select your database, include/exclude (sub)domains and
skip storing the body of requests over a certain size or with specific
extensions.

For more detailed configuration please see
[docs/settings.md](docs/settings.md).

## What's Logged?
If you turn on capture, the extension registers a [HTTP Handler][httphandler]
and logs every request right before leaving Burp and every response right after 
returning to Burp. You can configure the domains in the extension settings.

Request/responses are not deduplicated, if the same request is sent 10 times,
it will be logged 10 times.

[httphandler]: https://portswigger.github.io/burp-extensions-montoya-api/javadoc/burp/api/montoya/http/handler/HttpHandler.html

## Queries
Just logging data is not useful. The utility of the extension for me is being
able to gather insights from the bulk data.

Queries are SQL and we have two tables: `requests` and `responses`. The value of
the `request_id` column for each response points to the `requests` table.

The extension comes with some built-in queries to get you started. You can also
import/export queries from/to JSON. Here are a few examples. See the database
structure at [docs/database.md](docs/database.md) and more queries at
[docs/queries.md](docs/queries.md).

If you have any suggestions for queries, please create an issue or a pull
request.

### HTTP Requests
Burp provides a `ishttps` field for each request and it's stored in the
`is_https` column in the database. The value is `1` if true and `0` if not.

```sql
SELECT distinct url FROM requests
WHERE is_https == 0
```

### List Paths for a Host
You want to see all the paths for a host. This is usually used to create a list
to pass to a different tool.

```sql
SELECT distinct path FROM requests
WHERE host LIKE "%ea.com%"
```

### Requests with a Specific Parameter Name
The `parameter_names` contains a comma separated list of all parameter names in
a request. We can easily search it. This might be useful when you're looking for
a specific vulnerability across all your targets. E.g., a new CVE has been
released that affects all parameters named `update`.

```sql
SELECT url FROM requests
WHERE parameter_names LIKE "%update%"
```

We can combine it with the HTTP method (or verb if you prefer).

```sql
SELECT url FROM requests
WHERE parameter_names LIKE "%update%"
AND method == "POST"
```

### Requests with JSON Payloads
The browser/tool should set the request's `Content-Type` header to
`application/json`.

```sql
SELECT url, method FROM requests
WHERE content_type LIKE "%json%"
```

### Requests with Authorization Header
All header names are also stored in `header_names` column for both requests and
responses.

```sql
SELECT distinct url, method FROM requests
WHERE header_names LIKE "%authorization%"
```

As mentioned above. See [docs/queries.md](docs/queries.md) for more. 

## Development
See [DEVELOPMENT.md](DEVELOPMENT.md).

## Literature Review
Why did I create this and why didn't I use another tool?

### Motivation
It all started a few years ago from a Twitter thread where some bug bounty
hunters were talking about storing requests/responses in (elasticsearch? I think)
databases. After leaving the game hacking world and going back to (mostly)
web/cloud applications (RIP), I realized I need to create such an extension.

I want to fix the gap between "searching in each Burp project manually" and
"setting up some servers in the cloud" so folks can get started without paying
for cloud.

While you can get many of this information from Burp using Bambdas (or other
places like the sitemap), you cannot bulk search in hundreds of Burp projects.
This tool gives us the ability to discover points of interests across all of our
tests even when they've happened in the past.

### Similar Tools
I found a few other extensions that logged Burp requests/responses. Now you
might say, why create your own? Well, because none of them did exactly what I
wanted. I wanted to to query individual fields (e.g., `host`) in an
easy-to-setup local database.

[Logger++][logger-plus] by [Corey Arthur][corey] and [Soroush Dalili][irsdl]
(hey, I know some of them) is one of the most famous Burp extensions.
It's used for logging everything in Burp and supports exporting the results to
CSV or elasticsearch.

[logger-plus]: https://github.com/nccgroup/LoggerPlusPlus elasticsearch.
[corey]: https://x.com/coreyd97/
[irsdl]: https://x.com/irsdl/

[Log Requests to SQLite][log-req] by [Dominique Righetto][righetto], is similar
to Looking Glass. It logs all requests/responses to a SQLite database, can
pause/resume capture and has a built-in list of extensions to skip (mostly
images). While it doesn't have a separate section for customizing the domains,
it can use the scope in Burp. I think it's a neat feature that I might add to
Looking Glass.

[log-req]: https://github.com/righettod/log-requests-to-sqlite
[righetto]: https://www.righettod.eu/

[Dump - a Burp plugin to dump HTTP(S) requests/responses to a file system][dump]
by Richard Springs ([source at GitHub][dump-gh]). Incidentally, Richard was my
old teammate at EA. Dump is a Ruby Burp extension that exports the traffic to
specific formats. It has an interesting feature to merge requests based on a
specific header.

[dump]: https://blog.stratumsecurity.com/2017/08/01/dump-a-burp-plugin-to-dump-http-s-requests-responses-to-a-file-system/
[dump-gh]: https://github.com/crashgrindrips/burp-dump

[SQLite logger for Burp Suite][sql-logger] is a Java Burp extension by Andras
Veres-Szentkiralyi that also logs requests/responses to a SQLite database.

[sql-logger]: https://github.com/silentsignal/burp-sqlite-logger