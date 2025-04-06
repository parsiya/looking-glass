/*
 * These views are added to the database at creation. We can use these views to
 * simplify the queries.
 *
 * Note these will also change existing databases once they are opened. The
 * extension runs these against any database that is opened.
 *
 * You can add also them to current database by pasting them in the query panel
 * and clicking `Run`. If you get the following error, you can ignore it because
 * the button expects a query result:
 * `Error running the query: query does not return ResultSet`.
 */

-- View request bodies that are not empty
CREATE VIEW IF NOT EXISTS request_bodies AS
SELECT
  request_id,
  url,
  method,
  JSON_EXTRACT(data, '$.body') AS body
FROM requests
WHERE JSON_EXTRACT(data, '$.body') != '';

-- View response bodies that are not empty.
CREATE VIEW IF NOT EXISTS response_bodies AS
SELECT
  req.request_id AS request_id,
  res.response_id AS response_id,
  req.url AS url,
  req.method AS method,
  JSON_EXTRACT(res.data, '$.body') AS response_body
FROM requests req
JOIN responses res ON req.request_id = res.request_id
WHERE JSON_EXTRACT(res.data, '$.body') != '';

-- Return all request and response bodies
CREATE VIEW IF NOT EXISTS all_bodies AS
SELECT
  req.request_id AS request_id,
  res.response_id AS response_id,
  req.url AS url,
  req.method AS method,
  JSON_EXTRACT(req.data, '$.body') AS request_body,
  JSON_EXTRACT(res.data, '$.body') AS response_body
FROM requests req
JOIN responses res ON req.request_id = res.request_id
WHERE JSON_EXTRACT(req.data, '$.body') != '' OR JSON_EXTRACT(res.data, '$.body') != '';

-- View Javascript Files.
CREATE VIEW IF NOT EXISTS javascript_files AS
SELECT req.url AS url
FROM requests req
LEFT JOIN responses res
ON req.request_id = res.request_id
WHERE req.url LIKE '%.js'
OR res.inferred_content_type = 'SCRIPT'
OR res.content_type LIKE '%javascript%';

-- Return Everything
CREATE VIEW IF NOT EXISTS everything AS
SELECT req.*, res.*
FROM requests req
JOIN responses res
ON req.request_id = res.request_id;

-- Azure response headers
CREATE VIEW IF NOT EXISTS azure_response_headers AS
SELECT DISTINCT url
FROM requests req
JOIN responses res
ON req.request_id = res.request_id
WHERE res.header_names LIKE '%ms-cv%' OR res.header_names LIKE '%ms-correlationid%';

-- 'Server' and 'X-Powered-By' headers.
CREATE VIEW IF NOT EXISTS server_headers AS
WITH extracted_headers AS (
  SELECT
    req.url,
    (SELECT json_extract(value, '$.value')
     FROM json_each(res.data, '$.headers')
     WHERE json_extract(value, '$.name') = 'Server') AS server_header,
    (SELECT json_extract(value, '$.value')
     FROM json_each(res.data, '$.headers')
     WHERE json_extract(value, '$.name') = 'X-Powered-By') AS x_powered_by_header
  FROM responses res
  JOIN requests req ON req.request_id == res.request_id
  WHERE res.header_names LIKE '%Server%' OR res.header_names LIKE '%X-Powered-By%'
)
SELECT DISTINCT
  url,
  server_header,
  x_powered_by_header
FROM extracted_headers;

-- Return all X-Forwarded-* headers.
CREATE VIEW IF NOT EXISTS x_forwarded_headers AS
SELECT
  req.url,
  json_extract(h.value, '$.name') AS header_name,
  json_extract(h.value, '$.value') AS header_value
FROM responses res
JOIN requests req ON req.request_id = res.request_id
JOIN json_each(res.data, '$.headers') h
WHERE json_extract(h.value, '$.name') LIKE 'X-Forwarded-%';

-- Return all URLs with a content security policy in the response.
CREATE VIEW IF NOT EXISTS responses_with_csp AS
SELECT DISTINCT
  req.url
FROM responses res
JOIN requests req ON res.request_id = req.request_id
JOIN json_each(res.data, '$.headers') h
WHERE res.content_security_policy = 1
AND json_extract(h.value, '$.name') = 'Content-Security-Policy';

-- URLs with CORS headers and their values.
CREATE VIEW IF NOT EXISTS responses_with_cors_headers AS
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
