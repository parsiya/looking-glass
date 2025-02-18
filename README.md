# Looking Glass
What it is, why I do this?

## Install Microsoft OpenJDK for Development
https://learn.microsoft.com/en-us/java/openjdk/install#install-on-debian

```bash
sudo apt update
sudo apt install wget lsb-release -y
wget https://packages.microsoft.com/config/debian/$(lsb_release -rs)/packages-microsoft-prod.deb -O packages-microsoft-prod.deb
sudo dpkg -i packages-microsoft-prod.deb
```

And then

```bash
sudo apt update
sudo apt install msopenjdk-21
```

Finally, change the default JDK
https://learn.microsoft.com/en-us/java/openjdk/install#change-the-default-jdk-on-linux

```bash
sudo update-java-alternatives --set msopenjdk-21-amd64
```

## gradle

```bash
sudo apt-get install zip
# installing gradle through aptitude will install gradle 4.4.1 from 2012.

# install sdkman
curl -s "https://get.sdkman.io" | bash
source "~/.sdkman/bin/sdkman-init.sh"
sdk install gradle 8.12

gradle wrapper --gradle-version 8.12
```

# Preprocessing

## Populate Content-Type
Burp gives you a `ContentType` field but it's only populated with certain types
it recognizes.

## Time stamp Header


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