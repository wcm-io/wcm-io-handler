## About URL Handler

URL resolving and processing.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.url/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.url)


### Documentation

* [Usage][usage]
* [Suffix Builder and Parser][suffix-builder-parser]
* [Integrator Template Mode][integrator]
* [Sling Rewriter Integration][rewriter]
* [Granite UI components][graniteui-components]
* [API documentation][apidocs]
* [Changelog][changelog]


### Overview

The URL Handler provides:

* Building URLs from path, selectors, extension, suffix query string an fragment parts
* Externalizing links for page links and frontend resources
* Supporting different URL Modes for externalizing to HTTP/HTTPs, with full hostname or protocol-relative mode
* Hostnames used for externalization for HTTP and HTTPs are stored in [Context-Aware Configuration][caconfig]
* Rewrites URLs to current site
* [Suffix Builder and Parser][suffix-builder-parser] for passing around information via Sling Suffix string
* Supports externalizing URLs for [Integrator Template Mode][integrator] with placeholders or Full URLs
* Supports externalizing URLs in generated markup via [Sling Rewriter][rewriter]
* Generic Sling Models for usage in views: [Sling Models][ui-package]
* Generic [Granite UI components][graniteui-components] that can be used in link dialogs


### AEM Version Support Matrix

|URL Handler version |AEM version supported
|--------------------|----------------------
|1.2.x or higher     |AEM 6.2 or up
|1.0.x, 1.1.x        |AEM 6.1 or up
|0.x                 |AEM 6.0 or up


### Dependencies

To use this module you have to deploy also:

|---|---|---|
| [wcm.io Sling Commons](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.commons) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.commons/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.commons) |
| [wcm.io AEM Sling Models Extensions](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.models) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.models/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.models) |
| [wcm.io WCM Commons](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.commons) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.commons/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.commons) |
| [wcm.io WCM Granite UI Extensions](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.ui.granite) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.ui.granite/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.ui.granite) |
| [wcm.io Handler Commons](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.commons) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.commons/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.commons) |



[usage]: usage.html
[suffix-builder-parser]: suffix-builder-parser.html
[integrator]: integrator.html
[rewriter]: rewriter.html
[ui-package]: apidocs/io/wcm/handler/url/ui/package-summary.html
[graniteui-components]: graniteui-components.html
[apidocs]: apidocs/
[changelog]: changes-report.html
[caconfig]: ../../caconfig/
