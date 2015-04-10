## URL Suffix Builder and Parser


### Introduction

Sling supports a special part in it's URL called the "Suffix". The suffix is appended to the full Sling URL after the file extension separated by a slash. The suffix can contain slashes by itself and may look like another path and filename. See [Sling URL decomposition][sling-url-decomposition] for details.

The suffix can be used for passing various information in the URL without the need to append it as URL parameters (which may affect caching in filesystem-based caching scenarios).

The URL Handler bundle provides two classes to help managing parameters in suffixes:

* [SuffixBuilder][suffix-builder] - helps building a suffix with properly encoded parameters
* [SuffixParser][suffix-parser] - helps parsing a suffix that was built with SuffixBuilder


### SuffixBuilder

Using a builder pattern you can put parameters into the suffix.

Examples:

```java
// Suffix with some params
String suffix = new SuffixBuilder().put("param1", "value1").put("param2", "value2").build();

// Params with special chars
String suffix = new SuffixBuilder().put("param1", "25,00 €").put("param2", "x=1!").build();

// Suffix with path to resource relative to page resource (e.g. inside a page)
String suffix = new SuffixBuilder().resource(myResource, currentPage.getContentResource());
```

Additionally it is possible it instantiate a SuffixBuilder with special "state keeping" strategies that allow inherit all of a certain set of suffix parameters from request to request. See [JavaDocs][suffix-builder] for details.


### SuffixParser

The suffix parser allows to get the parameters from the suffix.

Examples:

```java
// Suffix with some params
SuffixParser parser = new SuffixParser(request);
String param1 = parser.get("param1", String.class);
String param2 = parser.get("param2", "defaultValue");

// Suffix with path to resource relative to page resource (e.g. inside a page)
Resource myResource = parser.getResource(currentPage.getContentResource());
```

The parser support different data types and optional filter strategies to validate/filter the resources that are referenced in the suffix. See [JavaDocs][suffix-parser] for details.



[sling-url-decomposition]: http://sling.apache.org/documentation/the-sling-engine/url-decomposition.html
[suffix-builder]: apidocs/io/wcm/handler/url/suffix/SuffixBuilder.html
[suffix-parser]: apidocs/io/wcm/handler/url/suffix/SuffixParser.html
