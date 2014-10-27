## About Media Handler

Media resolving, processing and markup generation.


### Maven Dependency

```xml
<dependency>
  <groupId>io.wcm</groupId>
  <artifactId>io.wcm.handler.media</artifactId>
  <version>0.2.0-SNAPSHOT</version>
</dependency>
```

### Documentation

* [Usage][usage]
* [API documentation][apidocs]
* [Changelog][changelog]


### Overview

The Media Handler provides:

* Build URLs for media assets like images and downloads (based on [URL Handler][url-handler])
* Pluggable media sources (with default implementations for DAM and binaries stored inside a content page)
* Pluggable markup builders for different media types (e.g. images, DAM video)
* Media format concept that allows to define expected output formats (with constraints for dimension, ratio, file type and others)
* Rendering virtual renditions to match the expected output format, optionally with cropping
* Support for responsive images by rendering multiple renditions at once for each breakpoint
* Pluggable media pre- and postprocessing to further tailoring the media handling process


[usage]: usage.html
[apidocs]: apidocs/
[changelog]: changes-report.html
[url-handler]: ../url/
