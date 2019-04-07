## wcm.io Handler

Link, Media and RichText Handler infrastructure.

The current version of wcm.io Handler is 1.x. Guideline for migration from [wcm.io Handler 0.x][wcmio-handler-0x]: [Migrate from wcm.io Handler 0.x to 1.x][wcmio-handler-migration]


### Overview

* [URL Handler](url/): URL resolving and processing.
* [Link Handler](link/): Link resolving, processing and markup generation.
* [Media Handler](media/): Media resolving, processing and markup generation.
* [RichText Handler](richtext/): RichText processing and markup generation.
* [Handler Commons](commons/): Functionality shared by the handler implementations.

To see how this all works together have a look at the [sample application][wcmio-samples].

To set up a new AEM project with wcm.io Handler support use the [wcm.io Maven Archetype for AEM][wcmio-maven-archetype-aem].


### GitHub Repository

Sources: https://github.com/wcm-io/wcm-io-handler


[wcmio-samples]: http://wcm.io/samples/
[wcmio-handler-0x]: http://wcm.io/handler-0.x/
[wcmio-handler-migration]: https://wcm-io.atlassian.net/wiki/x/dhn9Ag
[wcmio-maven-archetype-aem]: http://wcm.io/tooling/maven/archetypes/aem/
