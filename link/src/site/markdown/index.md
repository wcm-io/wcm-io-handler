## About Link Handler

Link resolving, processing and markup generation.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.link/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.link)


### Documentation

* [Usage][usage]
* [API documentation][apidocs]
* [Changelog][changelog]


### Overview

The Link Handler provides:

* Build URLs for links of different types (based on [URL Handler][url-handler])
* Pluggable link types (with default implementations for link to content pages, link to media assets, external links)
* Pluggable markup builders for links
* Pluggable link pre- and postprocessing to further tailoring the link handling process
* Generic Sling Models for usage in views: [Sling Models][ui-package]


[usage]: usage.html
[apidocs]: apidocs/
[changelog]: changes-report.html
[url-handler]: ../url/
[ui-package]: apidocs/io/wcm/handler/link/ui/package-summary.html
