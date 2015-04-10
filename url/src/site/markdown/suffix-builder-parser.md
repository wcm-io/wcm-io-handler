## URL Suffix Builder and Parser


### Introduction

Sling supports a special part in it's URL called the "Suffix". The suffix is appended to the full Sling URL after the file extension separated by a slash. The suffix can contain slashes by itself and may look like another path and filename. See [Sling URL decomposition][sling-url-decomposition] for details.

The suffix can be used for passing various information in the URL without the need to append it as URL parameters (which may affect caching in filesystem-based caching scenarios).

The URL Handler bundle provides two classes to help managing parameters in suffixes:

* [SuffixBuilder][suffix-builder] - helps building a suffix with properly encoded parameters
* [SuffixParser][suffix-parser] - helps parsing a suffix that was built with SuffixBuilder


### SuffixBuilder

// TODO

Example:

```java
// TODO
```


### SuffixParser


// TODO

Example:

```java
// TODO
```



[sling-url-decomposition]: http://sling.apache.org/documentation/the-sling-engine/url-decomposition.html
[suffix-builder]: apidocs/io/wcm/handler/url/suffix/SuffixBuilder.html
[suffix-parser]: apidocs/io/wcm/handler/url/suffix/SuffixParser.html
