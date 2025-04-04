# Queries
I learned a lot from writing the extension, but the actual utility is in the
queries. Knowing the structure of the [database](/docs/database.md) and some
SQL (or asking LLMs) you can ask for almost any info that you want.

Here are some examples that have been useful for me. I have also included these
in the built-in queries (that you're welcome to trim for your own use). If you
can think of any useful queries, please make an issue or a PR.

The comments are the name of the queries in the built-in set.

## All Request and Response Bodies
Here's a query to extract all request/response bodies that are not null.

```sql
-- All request bodies
-- All response bodies
SELECT JSON_EXTRACT(data, '$.body') AS body
FROM responses -- replace with responses
WHERE JSON_EXTRACT(data, '$.body') != '';
```

## All the JavaScript Files
Note, the default settings of the extension doesn't store the body of the
JavaScript files, but the request and response are logged. Creating such a list
is useful when you want to pass it to another tool to download/or analyze them.

There are multiple ways of doing this and I suggest you use them all to catch
all the edge cases.

Use the file extension in the URL:

```sql
-- All JavaScript Files-1
SELECT url FROM requests
WHERE url LIKE "%.js"
```

The `responses` table also has two columns:

1. `content_type` based on the response `Content-Type` header.
2. `inferred_content_type` is done by Burp and only has a few values.

```sql
-- All JavaScript Files-2
SELECT r.url
FROM requests r
JOIN responses res
ON r.request_id = res.request_id
WHERE res.inferred_content_type = 'SCRIPT'
OR res.content_type LIKE '%javascript%';
```

## Everything in the Table
If you want to see everything and also pair request and responses together.

```sql
-- Everything
SELECT r.*, res.*
FROM requests r
JOIN responses res
ON r.request_id = res.request_id
```

## Azure Response Headers
Some Azure responses have specific headers. E.g., [correlation vectors][cv] like
`ms-cv`, `ms-csv`, or `ms-correlationid`.

[cv]: https://github.com/microsoft/CorrelationVector

```sql
-- Azure response headers
SELECT distinct url
FROM requests r
JOIN responses res
ON r.request_id = res.request_id
WHERE res.header_names  LIKE "%ms-cv%" OR res.header_names LIKE "%ms-correlationid%"
```

### Activity within a Certain Duration
Our blue team friends want to track our activity. We can help them by giving
them our requests within a certain duration. The `Date` response
header has been converted into a Unix timestamp like `1743395234000`.

Note the `Date` header might be missing from the response.

Let's say we want all activity between 10 AM 2025-03-01 and 8 PM 2025-04-01.

```sql
-- Activity within Duration
SELECT r.url
FROM requests r
JOIN responses res
ON r.request_id = res.request_id
WHERE datetime(Date / 1000, 'unixepoch')
BETWEEN '2025-03-01 10:00:00' AND '2025-04-01 20:00:00'
```

## Filter by Referer
Sometimes it's useful to see all requests that have originated from a specific
page.

```sql
-- Filter by Referer
SELECT r.url, r.referer
FROM requests r
WHERE r.referer == "https://www.microsoft.com/"
```

## Filter by User-Agent
If you're proxying thickclients, you can differentiate in traffic with user
agents.

```sql
-- Filter by User-Agent
WITH extracted_user_agent AS (
  SELECT 
    (SELECT json_extract(value, '$.value') 
     FROM json_each(data, '$.headers') 
     WHERE json_extract(value, '$.name') = 'User-Agent') AS user_agent,
    url
  FROM requests
  WHERE user_agent LIKE "%mozilla%"
)
SELECT 
  user_agent,
  url
FROM extracted_user_agent;
```

## Server Response Header
The `Server` response header is not always set
Find out server types and versions if available.

`X-Powered-By` is similar and can be used to find info about the server.


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



### Content-Security-Policy
If it exists and extract the value.

### Cookies
Cookie names in the request.

Cookie names in the response and their domains/paths. E.g., what domain cookies are we getting?

## More Complex

### OpenAPI
Need processing on the client. Show a picture of the mapping from HTTP
request/response to OpenAPI.
