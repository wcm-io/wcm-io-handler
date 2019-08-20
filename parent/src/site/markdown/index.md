## wcm.io Handler

Link, Media and RichText Handler infrastructure.

The current version of wcm.io Handler is 1.x. Guideline for migration from [wcm.io Handler 0.x][wcmio-handler-0x]: [Migrate from wcm.io Handler 0.x to 1.x][wcmio-handler-migration]


### Overview

* [General concepts][general-concepts]  for all handlers
* [URL Handler][url-handler]: URL resolving and processing.
* [Link Handler][link-handler]: Link resolving, processing and markup generation.
* [Media Handler][media-handler]: Media resolving, processing and markup generation.
* [RichText Handler][richtext-handler]: Rich text processing and markup generation.
* [Handler Commons][handler-commons]: Functionality shared by the handler implementations.
* [System configuration][configuration]

To see how this all works together have a look at the [sample application][wcmio-samples].

To set up a new AEM project with wcm.io Handler support use the [wcm.io Maven Archetype for AEM][wcmio-maven-archetype-aem].


### GitHub Repository

Sources: https://github.com/wcm-io/wcm-io-handler


[general-concepts]: general-concepts.html
[url-handler]: url/
[link-handler]: link/
[media-handler]: media/
[richtext-handler]: richtext/
[handler-commons]: commons/
[configuration]: configuration.html
[wcmio-samples]: https://wcm.io/samples/
[wcmio-handler-0x]: https://wcm.io/handler-0.x/
[wcmio-handler-migration]: https://wcm-io.atlassian.net/wiki/x/dhn9Ag
[wcmio-maven-archetype-aem]: https://wcm.io/tooling/maven/archetypes/aem/
