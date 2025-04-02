# Queries
We will discuss some sample queries. This should help you get started. If you have
any suggestions for queries, please create an issue or a pull request.




### All the JavaScript Files
Note, the default settings of the extension doesn't store the body of the
JavaScript files, but we have still stored the request. This is useful when you
want to pass this to separate tool to download them.

There are multiple ways of doing this and I suggest you use them all to catch
all the edge cases.

Use the file extension in the URL:

```sql
SELECT url FROM requests
WHERE url LIKE "%.js"
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


### All requests/responses within a certain duration
If we want to tell the blue team about all our requests during an operation.


### Request/Response with a specific payload
Help with blue team tracking


### Request/Response with a specific response header
Like ms-cv, correlation ID and so on. Helps the blue team.

### Filter by Referer
What are all the requests that have originated from a specific page.




# Usecases

## Simple









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
