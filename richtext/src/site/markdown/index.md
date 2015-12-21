## About RichText Handler

RichText processing and markup generation.


### Maven Dependency

```xml
<dependency>
  <groupId>io.wcm</groupId>
  <artifactId>io.wcm.handler.richtext</artifactId>
  <version>0.5.2</version>
</dependency>
```

### Documentation

* [Usage][usage]
* [API documentation][apidocs]
* [Changelog][changelog]


### Overview

The RichText Handler provides:

* Parse and format rich text produced by the AEM RichText Editor
* Detect inline links and images and format them using [Link Handler][link-handler] and [Media Handler][media-handler]
* Support XHTML and Plain Text
* Generic Sling Models for usage in views: [Sling Models][ui-package]


[usage]: usage.html
[apidocs]: apidocs/
[changelog]: changes-report.html
[link-handler]: ../link/
[media-handler]: ../media/
[ui-package]: apidocs/io/wcm/handler/richtext/ui/package-summary.html
