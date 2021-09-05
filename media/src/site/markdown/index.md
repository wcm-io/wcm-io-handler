## About Media Handler

Media resolving, processing and markup generation.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.media/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.media)


### Documentation

* [Usage][usage]
* [General concepts][general-concepts]
* [Granite UI components][graniteui-components]
* [Component properties][component-properties]
* [System configuration][configuration]
* [File format support][file-format-support]
* [Dynamic Media support][dynamic-media]
* [API documentation][apidocs]
* [Changelog][changelog]


### Overview

The Media Handler provides:

* Build URLs for media assets like images and downloads (based on [URL Handler][url-handler])
* Pluggable media sources (with default implementations for DAM and binaries stored inside a content page)
* Pluggable markup builders for different media types (e.g. images, DAM video)
* Media format concept that allows to define expected output formats (with constraints for dimension, ratio, file type and others)
* Rendering virtual renditions to match the expected output format
* Supports manual and automatic cropping and rotation of images and image maps
* Support for responsive images by rendering multiple renditions at once for each breakpoint
* Define media handling via Java API, content policies or component properties
* Pluggable media pre- and postprocessing to further tailoring the media handling process
* Generic Sling Models for usage in views: [Sling Models][ui-package]
* Generic HTL Placeholder template
* Generic [Granite UI components][graniteui-components] that can be used in media/image component dialogs
* Support for [Dynamic Media][dynamic-media]

Read the [general concepts][general-concepts] to get an overview of the functionality.


### AEM Version Support Matrix

|Media Handler version |AEM version supported
|----------------------|----------------------
|1.10.x or higher      |AEM 6.4.5+, AEMaaCS
|1.8.x - 1.9.x         |AEM 6.3.3+, AEM 6.4.5+
|1.2.x - 1.7.x         |AEM 6.2+
|1.0.x - 1.1.x         |AEM 6.1+
|0.x                   |AEM 6.0+


### Dependencies

To use this module you have to deploy also:

|---|---|---|
| [wcm.io Sling Commons](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.commons) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.commons/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.commons) |
| [wcm.io AEM Sling Models Extensions](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.models) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.models/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.sling.models) |
| [wcm.io WCM Commons](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.commons) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.commons/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.commons) |
| [wcm.io WCM Granite UI Extensions](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.ui.granite) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.ui.granite/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.wcm.ui.granite) |
| [wcm.io Handler Commons](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.commons) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.commons/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.commons) |
| [wcm.io URL Handler](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.url) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.url/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.handler.url) |


[usage]: usage.html
[general-concepts]: general-concepts.html
[graniteui-components]: graniteui-components.html
[component-properties]: component-properties.html
[configuration]: configuration.html
[file-format-support]: file-format-support.html
[dynamic-media]: dynamic-media.html
[apidocs]: apidocs/
[changelog]: changes-report.html
[url-handler]: ../url/
[ui-package]: apidocs/io/wcm/handler/media/ui/package-summary.html
