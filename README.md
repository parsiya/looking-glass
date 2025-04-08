# Looking Glass - Index, Query, Repeat
Looking Glass is a Burp extension that archives all requests and responses in a
database, enabling you to dig deep and discover insights via SQL queries.

Import traffic from all your Burp projects over time and search historical
traffic such as:

* Retrieve all JavaScript URLs for static analysis or secret scanning.
* All endpoints for a domain.
* Vulnerable endpoints for a newly discovered bug.
* Create a wordlists from all parameter names.

![logo](/.github/00-logo.png)

The name and the logo are based on the (now defunct) legendary
[Looking Glass Studios][looking-wiki], creator of [the Thief series][thief]
(one of my favorites). I didn't want to generate an AI logo based on it.

[looking-wiki]:https://en.wikipedia.org/wiki/Looking_Glass_Studios
[thief]: https://en.wikipedia.org/wiki/Thief_(series)

## Quick Start

1. Add the jar file from the `release` directory as an extension in Burp.
2. Navigate to the `Looking Glass` tab.
3. Click the `Capture Off` button, the extension will ask you to choose a DB file.
4. (Optional) Click `Extension Settings` and configure filters.
5. Interact with the application normally so Burp can see traffic.
6. Looking Glass will store all requests/responses from all tools in the database.
7. Query the database using SQL in the extension tab.

![intro](/.github/06-intro1.gif)

If you want to import data from existing projects.

1. Open the project.
2. Navigate to the `Looking Glass` tab.
3. Click the `Import Proxy History` button.
4. If you have not set a database connection, you will be asked to set one.
5. The data will be stored in the database.

Other docs:

* [docs/database.md][db]: The database schema and columns.
* [docs/queries.md][queries]: Sample queries for possible use cases.
* [docs/settings.md][settings]: Extension settings.

[db]: /docs/database.md
[queries]: /docs/queries.md
[settings]: /docs/settings.md

**Privacy Warning**: The value of parameters, headers and cookies are stored in
the local database file. This includes passwords and tokens. While this sounds
scary, the same information is stored in Burp project files. I have a feature
planned that allows the user to block storage of sensitive values. See [issue
#8][i8].

[i8]: https://github.com/parsiya/looking-glass/issues/8

## Extension Settings
Looking Glass uses a local SQLite database to store the data. You don't need to
install extra drivers or setup a server.

At a high-level, you can select your database, include/exclude (sub)domains and
ignore the body of requests/responses over a certain size or specific
extensions.

For more detailed configuration please see [docs/settings.md][settings].

## What is Captured?
If you turn on capture, the extension registers a [HTTP Handler][httphandler]
and logs every request right before leaving Burp and every response right after 
returning to Burp. You can configure the domains in the extension settings.

**Request/responses are not deduplicated**, if the same request is seen 10
times, it will be logged 10 times.

[httphandler]: https://portswigger.github.io/burp-extensions-montoya-api/javadoc/burp/api/montoya/http/handler/HttpHandler.html

## Queries
You can log all day and not be wiser. Knowledge comes from mining the data (lol).

Queries are SQL and we have two tables: `requests` and `responses`. The value of
the `request_id` column for each response points to the `requests` table.

The extension has a few built-in queries to get you started. You can also
import/export queries from/to JSON.

See the database structure at [docs/database.md][db] and more queries at
[docs/queries.md][queries]. If you have any suggestions for queries, please
create an issue or a pull request.

Here are a few queries. The comment on top of each query is the name of the
query in the built-in set. You're welcome to trim them for your own usage.

### List Paths for a Host
You want to see all the paths for a host. This is usually used to create a list
to pass to a different tool.

```sql
-- Paths for a Host
SELECT DISTINCT path FROM requests
WHERE host LIKE "%ea.com%"
```

![paths for ea.com](/.github/07-paths.jpg)

### List JavaScript Files
This is useful if you want to retrieve all JavaScript URLs you've seen. You can
pass the URLs to a separate tool to download (e.g., `wget` or `cURL`). Then you
can use the files for secret scanning, endpoint discovery, or plain static
analysis.

```sql
-- All JavaScript Files
SELECT * FROM javascript_files
```

![all javascript files](/.github/11-js-files.jpg | width=500)

### Requests with a Specific Parameter Name
The `parameter_names` contains a comma separated list of all parameter names in
a request. We can easily search it. This might be useful when you're looking for
a specific vulnerability across all your targets. E.g., a new CVE has been
released that affects all parameters named `update`.

```sql
-- Search Parameter Name
SELECT DISTINCT url FROM requests
WHERE parameter_names LIKE "%update%"
```

We can combine it with the HTTP method (or verb if you prefer).

```sql
-- Search Parameter Name and Method
SELECT DISTINCT url FROM requests
WHERE parameter_names LIKE "%update%"
AND method == "POST"
```

### Word List of Parameter Names
Use the built-in query (needs some post processing). See the section in
[docs/queries.md][queries] for the entire query and discussion.

![parameter names](/.github/13-params2.png)

## Development
See [DEVELOPMENT.md](DEVELOPMENT.md).

## Literature Review
Why did I create this and why didn't I use an existing tool?

### Motivation
It all started a few years ago from a Twitter thread where some bug bounty
hunters were talking about storing requests/responses in (elasticsearch? I
think) databases. After leaving the game hacking world and going back to
(mostly) web/cloud (RIP skillz), I realized I needed such an extension.

I want to fix the gap between "searching in each Burp project manually" and
"setting up servers in the cloud". Now folks can get started without paying for
cloud.

While you can get many of this information from individual projects in Burp
using Bambdas (or other places like the sitemap), you cannot bulk search in
hundreds of Burp projects. This database gives us the ability to discover points
of interests across all of our tests even when they've happened in the past.

Because we're using a SQLite database, you are not limited to the extension. You
can use any software or program to do query the data. E.g., importing it to
Kusto so you can query them in the cloud (and also because I like KQL more than
SQL).

### Naming
Originally, I wanted to name the extension [Simurgh][sim-wiki], after the
legendary bird in Persian mythology. Simurgh is a bird of wisdom and I feel like
it could be a great mascot.

Using tool names based on Persian mythology was inspired by [Pishi][pishi]
(kitty or cat in Persian) and the SQLi tool `Havij` (carrot in Persian). That
would also mean I could use this [not-AI generate-kick-ass-without-copyright
logo][sim-logo].

However, my time at videogame company has thought me that marketing is half of
the battle. Your tool will not be popular if people cannot pronounce the name.
So, I will do it for another tool later.

As mentioned above, the name Looking Glass is based on the legendary game
studio. Looking Glass actually means mirror, but the logo of the studio shows a
paper telescope (which is not a looking glass). This contradiction has stayed
with me ever since I saw it decades ago.

[sim-wiki]: https://en.wikipedia.org/wiki/Simurgh
[sim-logo]: https://en.wikipedia.org/wiki/Simurgh#/media/File:Senmurv.svg
[pishi]: https://r00tkitsmm.github.io/fuzzing/2024/11/08/Pishi.html

### Similar Tools
I found a few other extensions that logged Burp requests/responses. Why create
my own? None of them did exactly what I wanted. I wanted to to query individual
fields (e.g., `host`) in a setup-free local database.

**[Logger++][logger-plus]** by [Corey Arthur][corey] and [Soroush Dalili][irsdl]
(hey, I know them) is one of the most famous Burp extensions.
It's used for logging everything in Burp and supports exporting the results to
CSV or elasticsearch.

[logger-plus]: https://github.com/nccgroup/LoggerPlusPlus
[corey]: https://x.com/coreyd97/
[irsdl]: https://x.com/irsdl/

**[Log Requests to SQLite][log-req]** by [Dominique Righetto][righetto], is
similar to Looking Glass. It logs all requests/responses to a SQLite database,
can pause/resume capture and has a built-in list of extensions to skip (mostly
images). While it doesn't have a separate section for customizing the domains,
it can use the scope in Burp. I think it's a neat feature that I might add to
Looking Glass.

[log-req]: https://github.com/righettod/log-requests-to-sqlite
[righetto]: https://www.righettod.eu/

**[Dump - a Burp plugin to dump HTTP(S) requests/responses to a file system][dump]**
by Richard Springs ([source on GitHub][dump-gh]). Incidentally, Richard was my
teammate at EA. Dump is a Ruby Burp extension that exports the traffic to
specific formats. It has an interesting feature to merge requests based on a
header.

[dump]: https://blog.stratumsecurity.com/2017/08/01/dump-a-burp-plugin-to-dump-http-s-requests-responses-to-a-file-system/
[dump-gh]: https://github.com/crashgrindrips/burp-dump

**[SQLite logger for Burp Suite][sql-logger]** is a Java Burp extension by Andras
Veres-Szentkiralyi that also logs requests/responses to a SQLite database.

[sql-logger]: https://github.com/silentsignal/burp-sqlite-logger
