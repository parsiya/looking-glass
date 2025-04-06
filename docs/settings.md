# Extension Settings
All the extension configuration is done in the `Looking Glass` tab.

## Compatibility
This is a Java extension using the Montoya APIs, it will not work with old
versions of Burp. I know some friends who are still using Burp 1.7, this will not
work with those versions. I am not making a judgement call, I just used the new
API that is supported by the current version of Burp and hopefully supported
long-term.

## Extension Order
Extension order in Burp matters. If you have other extensions that create or
modify requests, Looking Glass (or other logging extensions) must be after them
in the extension order. Assume every requests are seen by the first extension in
the tab and move down the chain.

If you want Looking Glass to see every request, it needs to be last, or at least
after every other extension that manipulates requests.

## Select a Database
Before capturing, you have to select a SQLite database. This is usually a file
on your local machine.

You can do it in two ways:

1. The `Select DB` button.
2. Click `Capture Off`. If a DB is not selected, the extension will ask you to select a new one.

**Choosing an existing file will not overwrite the data.**

![select db](/.github/01-selectdb.gif)

## Capturing
Click on `Capture Off` to turn it to `Capture On` with a blue color. I tried to
make it look like the `Intercept` button.

The extension will register a handler that will intercept all requests and
responses regardless of the tool.

## Extension Settings
Click on the button with the same name. Don't forget to select `Apply & Close`
to save the configuration.

Settings are stored in Burp on your machine and not in the project.

![settings](/.github/10-settings.gif)

### Target Scope
I created a table similar to Burp's `Target > Scope` settings. Use the buttons
to add/remove domains. You can enable/disable each item individually.

**`www.[domain]` is a special case and always captured even if
`Include Subdomains` is not checked**.

If there are any enabled items in the `Include` list, `Exclude` is ignored. If
you have items in `Include`, but they're all disabled, the system will ignore
`Include` and move to `Exclude`. 

I understand you might like the edge case of including all subdomains for
`example.net` but want to ignore a few (e.g., `secret.example.net`), but the
extension logic doesn't allow this.

![include/exclude](/.github/02-include-exclude.png)

### Filter Request or Response Body
On the right side of the settings panel, we have filters for **bodies**. This
section is designed to be similar to the `HTTP History > Filter` section.

While in the previous section the entire request/response pair was ignored, in
this section we're just skipping the storage of the body and the
request/response will be stored. This is used to skip the body of binary files
or assets (e.g., images).

We can skip bodies two ways:

1. Size. Note the size is in MB as shown in the UI.
2. File extension
    1. Provide a list of comma separated extensions.
    2. Similar to include/exclude, only one of the `Store` and `Skip` can be active.

While I understand the list is awkward to edit, I wanted to use something
similar to an existing Burp setting that users are already familiar with. You
can always copy it outside, edit and paste it back or better yet, export to JSON
and edit as seen below.

### Startup Settings
If this box is checked, Looking Glass will start capturing when you start Burp
(or restart the extension). This is useful for people (like me) who forget to
turn on capture after setting up a filter.

Note that even if stop Burp with `Capture On`, the extension will not capture on
startup without this box.

### Import/Export Settings
You can export the settings to a JSON file and edit them manually. You can also
import settings from a JSON file. This is useful if you want to:

1. Have a project specific setting.
    1. The extension stores the settings in Burp so it can be used with the free version.
2. Import/export the list of domains.
3. Edit the list of extensions or the entire config in your editor.

If you import a setting, it's automatically saved even if you click `Cancel` on
this dialog.

## Import Proxy History
This is useful if you want to import older projects. This button will import the
entire proxy history to the database. Unfortunately, Burp extensions cannot
access data in other tools (e.g., Repeater) so we cannot import them. The only
way to capture data from other tools like Repeater, Scanner and other extensions
is to use the HTTP Handler and capture them live.

`Import proxy history` **uses the filters set in extension settings**.

![import proxy history](/.github/09-import-history.gif)