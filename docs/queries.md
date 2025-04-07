# Queries
I learned a lot from writing the extension, but the actual utility is in the
queries. Knowing the structure of the [database](/docs/database.md) and some
SQL (or asking LLMs) you can ask for almost any info that you want.

## How to Use Queries
Queries are in SQL. Simply write your query in the panel and press `Run`. Any
results or errors will be in the bottom panel.

### Create New Queries
To add a new query, right-click anywhere on the list and select `New Query`. The
prompt will ask you for a name. The extension doesn't enforce unique naming but
it also doesn't overwrite queries with existing names.

![Sidebar menu](/.github/05-sidebar-menu.png)

### Save/Modify Queries
The initial value of the query is the contents in the query panel. After
changing, click the `Save` button to save the value to the currently selected
query. If you do not have any queries and press `Save`, you will see the
`New Query` prompt and can enter a name to create a new query.

**If you change a query and then select a new one without saving, the changes
will be lost**. See [issue #13][i13].

[i13]: https://github.com/parsiya/looking-glass/issues/13

### Rename Queries
Right-click a query and select `Rename Query` or double left-click it. You will
get a prompt to enter the new name.

### Delete Queries
Right-click a query and select `Delete Query` or middle-click it with your
mouse, then accept the confirmation prompt.

## Views
The database has some built-in views for popular queries. You can see them in
the [/src/main/resources/views.sql](/src/main/resources/views.sql) file. If you
have any suggestions for views, please make an issue or a PR.

My preference is adding a view and then using the view in the query. This
simplifies the queries.

You can add your own views by pasting them in the query box and clicking `Run`.
You will get this error which you can ignore because that button expects a query
with results: `Error running the query: query does not return ResultSet`.

## Sample Queries
Here are some examples that have been useful for me. I have also included these
in the built-in queries (that you're welcome to trim for your own use). The
comments are the name of the queries in the set.

### Filter by a Specific Tool
We can filter by the  `tool_source` in both request and response. E.g., all
requests from `Repeater`. This useful if we're asked for manual requests.

```sql
-- Repeater Requests
SELECT 
  req.request_id,
  req.url,
  req.method,
  res.status_code
FROM requests req
JOIN responses res ON req.request_id = res.request_id
WHERE req.tool_source = 'Repeater'
```

Change the string for other tools like `Scanner` and `Proxy`.

### All Request and Response Bodies
Here's a query to extract all request/response bodies that are not null.

```sql
SELECT JSON_EXTRACT(data, '$.body') AS body
FROM requests
WHERE JSON_EXTRACT(data, '$.body') != '';

SELECT JSON_EXTRACT(data, '$.body') AS body
FROM responses -- replace with responses
WHERE JSON_EXTRACT(data, '$.body') != '';
```

Or we can use the built-in views:

```sql
-- Request bodies
SELECT * FROM request_bodies

-- Response bodies
SELECT * FROM response_bodies

-- All bodies
SELECT * FROM all_bodies
```

### HTTP Requests
Burp provides an `ishttps` field for each request and it's stored in the
`is_https` column in the database. The value is `1` if true and `0` if not.

```sql
-- Insecure HTTP Requests
SELECT distinct url FROM requests
WHERE is_https == 0
```

### List JavaScript Files
Note, the default settings of the extension doesn't store the body of the
JavaScript files, but the request and response are logged. Creating such a list
is useful when you want to pass it to another tool to download/or analyze them.

There are many articles and tools for JavaScript analysis, so I will not discuss
them there. But some of the uses cases are:

1. Secret scanning
2. Endpoint discovery
3. Plain static analysis to find bugs like XSS. See my older Burp extension [ESLinter][eslinter].

[eslinter]: https://github.com/parsiya/eslinter

Using the built-in view:

```sql
-- All JavaScript Files
SELECT * FROM javascript_files
```

![all javascript files](/.github/11-js-files.jpg | width=500)

There are multiple ways of doing this and I suggest you use them all to catch
all the edge cases.

Use the file extension in the URL:

```sql
SELECT url FROM requests
WHERE url LIKE '%.js'
```

The `responses` table also has two columns:

1. `content_type` based on the response `Content-Type` header.
2. `inferred_content_type` is done by Burp and only has a few values.

```sql
SELECT r.url
FROM requests r
JOIN responses res
ON r.request_id = res.request_id
WHERE res.inferred_content_type = 'SCRIPT'
OR res.content_type LIKE '%javascript%';
```

If we merge these two and create a view, we get the view we used above:

```sql
CREATE VIEW javascript_files AS
SELECT req.url
FROM requests req
LEFT JOIN responses res
ON req.request_id = res.request_id
WHERE req.url LIKE '%.js'
OR res.inferred_content_type = 'SCRIPT'
OR res.content_type LIKE '%javascript%';
```

### Requests with JSON Payloads
The browser/tool should set the request's `Content-Type` header to
`application/json`.

```sql
-- JSON Payloads
SELECT url, method FROM requests
WHERE content_type LIKE "%json%"
```

### Requests with Authorization Header
All header names are also stored in `header_names` column for both requests and
responses.

```sql
-- Authorization Header
SELECT distinct url, method FROM requests
WHERE header_names LIKE "%authorization%"
```

![authorization header](/.github/08-authorization.jpg | width=450)

### Everything in the Database
If you want to see everything and also pair request and responses together, use
the built-in view.

```sql
-- Everything
select * from everything
```

### Azure Response Headers
Some Azure responses have specific headers. E.g., [correlation vectors][cv] like
`ms-cv`, `ms-csv`, or `ms-correlationid`.

[cv]: https://github.com/microsoft/CorrelationVector

```sql
-- Azure response headers
SELECT distinct url
FROM requests req
JOIN responses res
ON req.request_id = res.request_id
WHERE res.header_names  LIKE "%ms-cv%" OR res.header_names LIKE "%ms-correlationid%"
```

Or using the built-in view based on the query above:

```sql
select * from azure_response_headers
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

### Filter by Referer
Sometimes it's useful to see all requests that have originated from a specific
page.

```sql
-- Filter by Referer
SELECT req.url, req.referer
FROM requests req
WHERE req.referer == "https://www.microsoft.com/"
```

### Filter by User-Agent
If you're proxying thick clients, you can differentiate the traffic from
different programs with user agents.

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

## Server Information
The `Server` and `X-Powered-By` response headers are not always set, but might
contain information. We can extract them from response JSON.

Using the build-in view:

```sql
-- Server, x-powered-by
SELECT * FROM server_headers
```

Or you can run the query manually and add any other headers you're looking for:

```sql
WITH extracted_headers AS (
  SELECT 
    r.url,
    (SELECT json_extract(value, '$.value') 
     FROM json_each(res.data, '$.headers') 
     WHERE json_extract(value, '$.name') = 'Server') AS server_header,
    (SELECT json_extract(value, '$.value') 
     FROM json_each(res.data, '$.headers') 
     WHERE json_extract(value, '$.name') = 'X-Powered-By') AS x_powered_by_header
  FROM responses res
  JOIN requests r ON r.request_id == res.request_id
  WHERE res.header_names LIKE '%Server%' OR res.header_names LIKE '%X-Powered-By%'
)
SELECT DISTINCT
  url, 
  server_header, 
  x_powered_by_header
FROM extracted_headers;
```

### X-Forwarded- Headers
We can find those and see where we can mess with proxy servers. Let's select
every URL that has a header that has a `X-Forwarded-*` header.

```sql
-- X-Forwarded- Headers
SELECT 
  req.url,
  json_extract(headers.value, '$.name') AS header_name,
  json_extract(headers.value, '$.value') AS header_value
FROM responses res
JOIN requests req ON req.request_id = res.request_id
JOIN json_each(res.data, '$.headers') headers
WHERE json_extract(headers.value, '$.name') LIKE 'X-Forwarded-%'
```

Or use views:

```sql
-- x-forwarded headers
SELECT * FROM x_forwarded_headers
```

### Content-Security-Policy
The response table has a `content_security_policy` header that is `1` if the
response has one.

```sql
SELECT DISTINCT req.url
FROM responses res
JOIN requests req ON req.request_id = res.request_id
WHERE res.content_security_policy = 1;
```

```sql
-- Responses with CSP
SELECT * FROM responses_with_csp
```

### Access-Control- Headers
Useful for tagging CORS issue investigations. We will check for these headers:

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

```sql
SELECT DISTINCT 
  req.url,
  json_extract(h.value, '$.name') AS header_name,
  json_extract(h.value, '$.value') AS header_value
FROM responses res
JOIN requests req ON res.request_id = req.request_id
JOIN json_each(res.data, '$.headers') h
WHERE json_extract(h.value, '$.name') IN (
  'Access-Control-Allow-Credentials',
  'Access-Control-Allow-Headers',
  'Access-Control-Allow-Methods',
  'Access-Control-Allow-Origin',
  'Access-Control-Expose-Headers',
  'Access-Control-Max-Age',
  'Access-Control-Request-Headers',
  'Access-Control-Request-Method'
);
```

We also have a built-in view:

```sql
-- Access-Control Headers
SELECT * FROM responses_with_cors_headers
```

![CORS headers](/.github/04-cors.png)

### Request Cookies
What requests have a certain cookie.

```sql
-- Filter requests by cookie
SELECT DISTINCT
  req.url
FROM requests req
WHERE req.cookie_names LIKE '%cookie_name%';
```

We can use a similar query to discover where this cookie is set:

```sql
SELECT DISTINCT
  req.url
FROM responses res
JOIN requests req ON res.request_id = req.request_id
WHERE res.cookie_names LIKE '%cookie_name%';
```

Also return the value of the cookie that was set.

```sql
-- Filter responses by cookie
SELECT DISTINCT 
  req.url,
  json_extract(h.value, '$.value') AS cookie_value
FROM responses res
JOIN requests req ON res.request_id = req.request_id
JOIN json_each(res.data, '$.headers') h
WHERE json_extract(h.value, '$.name') = 'Set-Cookie'
AND json_extract(h.value, '$.value') LIKE '%cookie_name%';
```

### All Views
We can even show all the views we have created.

```sql
SELECT name, sql 
FROM sqlite_master 
WHERE type = 'view';
```

### Word List from all Parameters
This is useful if you want to create wordlists for fuzzing.

A good easy way is to look at the `parameter_names` column. It has a
comma-separated list of all request parameters.

```sql
SELECT parameter_names
FROM requests
WHERE Host Like "%ea.com%"
```

You can do a bit of post-processing to get a decent word list:

1. Replace `,` with ` `.
2. Split by ` `.
3. Remove duplicates.

You can do some of that in SQLite. The following query has some garbage in the
end, this is mainly because some requests have binary payloads that look like
JSON or XML parameters and Burp incorrectly classifies them as parameters. The
result is sorted so you can copy the top results.

You can add conditions to filter by `Host` column.

```sql
WITH RECURSIVE split_parameters AS (
    SELECT
        ROWID AS id,
        parameter_names AS original,
        SUBSTR(parameter_names, 0, INSTR(parameter_names || ',', ',')) AS param,
        SUBSTR(parameter_names, INSTR(parameter_names || ',', ',') + 1) AS remainder
    FROM requests
    UNION ALL
    SELECT
        id,
        original,
        SUBSTR(remainder, 0, INSTR(remainder || ',', ',')),
        SUBSTR(remainder, INSTR(remainder || ',', ',') + 1)
    FROM split_parameters
    WHERE remainder != ''
)
SELECT DISTINCT param
FROM split_parameters
WHERE param != ''
  AND param GLOB '[ -~]*';
```

![parameter names](/.github/12-params.png)

I've already paid for you to create as many queries as you can, they're free.
Sky's the limit!