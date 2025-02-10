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

## More Complex

### OpenAPI
Need processing on the client. Show a picture of the mapping from HTTP
request/response to OpenAPI.

