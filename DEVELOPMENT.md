# Development
The instructions are tailored for a Debian distro (under WSL2). But they should
not be that different for other setups.

## JDK
Any JDK should work.

I am using the Microsoft OpenJDK for Development at:
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
2. In VS Code, use the `ctrl+shift+b` to run the main build task.

## Debugging
I use VS Code. You can debug your Burp extensions using my blog post at
[Developing and Debugging Java Burp Extensions with Visual Studio Code].

[debug]: https://parsiya.net/blog/2019-12-02-developing-and-debugging-java-burp-extensions-with-visual-studio-code/

One catch (search for 2025 in the blog to see the details): If you're running
Burp on Windows and developing in WSL, you have to set the IP address in
[.vscode/launch.json](/.vscode/launch.json) to your Windows machine's IP address
(generally the IP assigned by your local router/modem).

## Some Development Gotchas
So you (and future me) don't troubleshoot for several hours, again.

### Requests without Responses
The extension handler doesn't log requests without responses. An HTTP handler

### Content-Type
Burp gives you a `ContentType` field but it's only populated with certain types
it recognizes. ZZZ add details here.

### Time stamp Header
Not all requests have timestamp headers. Responses _should_ according to the
RFC, but may not. ZZZ add how I used the Apache HttpComponents DateUtils.

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