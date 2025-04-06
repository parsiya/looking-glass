# Development
The instructions are tailored for a Debian distro (under WSL2). But they should
not be that different for other setups.

## JDK
Any JDK should work. I am using the Microsoft OpenJDK for Development at:
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
# installing gradle through aptitude in Debian 12 will install gradle 4.4.1 from 2012 :\

# install sdkman
curl -s "https://get.sdkman.io" | bash
source "~/.sdkman/bin/sdkman-init.sh"
sdk install gradle 8.12

gradle wrapper --gradle-version 8.12
```

## Building

1. `./gradlew jar`
2. In VS Code, use `ctrl+shift+b` to run the main build task.

## Debugging
I use VS Code. You can debug your Burp extensions using my blog post at
[Developing and Debugging Java Burp Extensions with Visual Studio Code][debug].

[debug]: https://parsiya.net/blog/2019-12-02-developing-and-debugging-java-burp-extensions-with-visual-studio-code/

One catch (search for 2025 in the blog to see the details): If you're running
Burp on Windows and developing in WSL, you have to set the IP address in
[.vscode/launch.json](/.vscode/launch.json) to your Windows machine's IP address
(generally the IP assigned by your local router/modem).

## Some Development Gotchas
So you (and future me) don't troubleshoot for several hours, again.

### Requests without Responses
**The extension handler doesn't log requests without responses.** An
[HTTPhandler][httphandler] has to override two callback methods.

* `RequestToBeSentAction`: Is called for every outgoing request.
* `ResponseReceivedAction`: Is called for every incoming response ([HttpResponseReceived][resp]).

The `HttpResponseReceived` object has a method named `initiatingRequest()` that
can be used to get the request attached with it. I chose to do the capturing in
`ResponseReceivedAction`. This will skip requests without responses because they
are only seen in `RequestToBeSentAction`.

Capturing all outgoing requests means I had to find a way to deduplicate them
when the response returned. I thought of one viable way of doing this. I am
documenting it here for future me (and others).

1. Create a list of all outgoing requests.
2. Every time you see a response, log it and remove the initiating request from the list.
    1. This can be used with the `hashCode()` method in Java, but is it good enough?
3. Every N seconds, log all the requests in the list.

**Problem:** If a request is send, just before step 3 and the response returns after
step 3, it's been logged twice. Once as an individual request and once with the
response.

**Discussion about `hashCode()`.** It looks like this method is good for primitive
types, but every other object has to define it for itself. Is it a unique
identifier for the custom `HttpRequest` Burp object? I don't know, I would
assume it is, because it's most likely using the `hashCode` of the field values.

TODO: See how `Logger++` does it, [Link to its HttpHandler][li].

[httphandler]: https://portswigger.github.io/burp-extensions-montoya-api/javadoc/burp/api/montoya/http/handler/HttpHandler.html
[resp]: https://portswigger.github.io/burp-extensions-montoya-api/javadoc/burp/api/montoya/http/handler/HttpResponseReceived.html
[li]: https://github.com/nccgroup/LoggerPlusPlus/blob/785c85e06eb64e3b0982f31c6dcf5ad925f281db/src/main/java/com/nccgroup/loggerplusplus/logview/processor/LogProcessor.java#L82


### HTTP Date Header
Not all requests have the `Date` header. Responses _should_ according to the
RFC, but may not. I used a file from the Apache HttpComponents DateUtils. The
Apache HttpComponents 5.4.2 project Licensed under the Apache License which is
compatible with this project's license, MIT. See [LICENSE](/LICENSE) for details.

### Burp Parameters
Sometimes Burp gets confused when detecting parameter and gives us gibberish.
E.g., one of the requests sent after navigating to `google.com` is a POST
request that looks like this:

```
POST /$rpc/google.internal.onegoogle.asyncdata.v1.AsyncDataService/GetAsyncData HTTP/2
Host: ogads-pa.clients6.google.com
// removed

[538,"",538,"en","com",0,null,0,0,"","19046228-1:",1,0,null,89978449,null,[1]]
```

Burp will tell us the request has 17 parameters of type JSON with no names and
the values indicated above so the `parameter_names` column will be a like `,,,`.