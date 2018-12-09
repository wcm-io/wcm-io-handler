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


### Dependencies

To use this module you have to deploy also:

|---|---|---|
| [wcm.io Sling Commons](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.commons) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.commons/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.commons) |
| [wcm.io AEM Sling Models Extensions](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.models) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.models/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.models) |
| [wcm.io WCM Commons](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.commons) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.commons/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.commons) |
| [wcm.io Handler Commons](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.commons) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.commons/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.commons) |
| [wcm.io URL Handler](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.url) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.url/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.url) |
| [wcm.io Media Handler](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.media) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.media/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.media) |


[usage]: usage.html
[apidocs]: apidocs/
[changelog]: changes-report.html
[url-handler]: ../url/
[ui-package]: apidocs/io/wcm/handler/link/ui/package-summary.html
