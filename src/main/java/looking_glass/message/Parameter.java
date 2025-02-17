package looking_glass.message;

import burp.api.montoya.http.message.params.ParsedHttpParameter;

public class Parameter {
    
    public String parameterType, name, value;

    public Parameter(String parameterType, String name, String value) {
        this.parameterType = parameterType;
        this.name = name;
        this.value = value;
    }

    public Parameter(ParsedHttpParameter parameter) {
        this.parameterType = parameter.type().toString();
        this.name = parameter.name();
        this.value = parameter.value();
    }
}

// Copied from the Burp API.
// public enum HttpParameterType
// {
//     URL,
//     BODY,
//     COOKIE,
//     XML,
//     XML_ATTRIBUTE,
//     MULTIPART_ATTRIBUTE,
//     JSON
// }
