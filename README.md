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

The readme will discuss some sample queries. See the entire table structure
and more queries in [docs/query.md](docs/query.md).

The extension comes with some built-in queries. You can also import/export
queries to JSON.

### All Paths for a Host

```sql
SELECT distinct path
FROM requests
WHERE host LIKE "%ea.com%"
```


# Usecases

## Simple

### All the routes in a hostname
What are the routes for our target

### All parameters named X
If we find that a specific parameter is vulnerable, we want to see where it is.

### All requests/responses within a certain duration
If we want to tell the blue team about all our requests during an operation.

### Request/Response with a specific payload
Help with blue team tracking

### Request/Response with a specific response header
Like ms-cv, correlation ID and so on. Helps the blue team.

### Filter by Referer
What are all the requests that have originated from a specific page.

### Sec-whatever Headers
Figure out which ones are populated if the action was a user action and what the
value is. Credit where you learned from, another student in the Burp course.

```
Sec-Fetch-Dest
Sec-Fetch-Mode
Sec-Fetch-Site
Sec-Fetch-User
```

### X-Forwarded- Headers
We can find those and see where we can mess with proxy servers.

### Access-Control- Headers
Useful for tagging CORS issue investigations.

```
Access-Control-Allow-Credentials
Access-Control-Allow-Headers
Access-Control-Allow-Methods
Access-Control-Allow-Origin
Access-Control-Expose-Headers
Access-Control-Max-Age
Access-Control-Request-Headers
Access-Control-Request-Method
```

### Server Response header
Find out server types and versions if available.

`X-Powered-By` is similar and can be used to find info about the server.

### Content-Security-Policy
If it exists and extract the value.

### Cookies
Cookie names in the request.

Cookie names in the response and their domains/paths. E.g., what domain cookies are we getting?

## More Complex

### OpenAPI
Need processing on the client. Show a picture of the mapping from HTTP
request/response to OpenAPI.

# TODO

## Request/Response Length Limit
Add a number in the settings, if the length of the body of the request or
response is over that number, the body will not be stored. The rest of the
fields will still be populated, but we will replace the value of the body with
an empty string, this helps the size of the DB.

## Settings Modal
This requires us to create a settings modal. The modal can have fields for the
settings like the one above and a button to set the DB. It can also have a
button like the intercept in burp to turn off/on logging.

## Do Not Store Body of some File types
Similar to the above, we can also entirely skip storing the body of some file extensions. E.g., images.

This can be determined from two lists:

* file extensions
* mime-types that come from the `Content-Type` response header.

Add a field in the settings modal that lets people provide these file extensions. Have a built-in default value, the default Burp filter for HTTP History is a good start.

`js,gif,jpg,png,css` and more like `webp`, `wott` and so on. All the fonts and such.

Figure out how Burp does it when it categorizes something as image or binary or css. Burp gives us a "I figure out the mime-type" field, too.

## Development
See [DEVELOPMENT.md](DEVELOPMENT.md).

## Literature Review
It all started a few years ago on a Twitter thread where famous bug bounty
hunters were talking about storing requests/responses in (elasticsearch?)
databases. After leaving the game hacking world and going back to (mostly)
web/cloud applications, I realized I need to create such an extension.

I found a few other extensions that logged Burp requests/responses. Now you
might say, why create your own? Well, because none of them did exactly what I
wanted. I wanted to to query individual fields (e.g., `host`) in an
easy-to-setup local database.

[Logger++][logger-plus] by [Corey Arthur][corey] and [Soroush Dalili][irsdl]
(hey, I know some of these people) is one of the most famous Burp extensions.
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
specific formats. It has am interesting feature to merge requests based on a
specific header. Richard, I love you man, but Ruby?

[dump]: https://blog.stratumsecurity.com/2017/08/01/dump-a-burp-plugin-to-dump-http-s-requests-responses-to-a-file-system/
[dump-gh]: https://github.com/crashgrindrips/burp-dump

[SQLite logger for Burp Suite][sql-logger] is a Java Burp extension by Andras
Veres-Szentkiralyi that also logs requests/responses to a SQLite database.

[sql-logger]: https://github.com/silentsignal/burp-sqlite-logger