## URL Handler general concepts

The wcm.io URL Handler is responsible for building and externalizing all types or URLs within an AEM project.

This page describes these basic concepts, the [usage][usage] page shows examples how to use them.


### Introduction

In most cases you do not use the URL handler directly to build URLs, but you use one of the higher-level handlers to do the job:

* [Link Handler][link-handler]: Used to build URLs and markup for "human-clickable" links to content pages, external links or DAM assets that results in a HTML anchor tag and may need more processing e.g. for SEO,  web tracking and link target validation
* [Media Handler][media-handler]: Used to build URLs and markup for displaying images or other media content inside a page
* [RichText Handler][richtext-handler]: Used to format a HTML fragment that may contain inline links oder media references

These handlers make use of the URL handler and its configuration internally. The link handler supports most or URL handler options to modify the generated URL by added selectors, extensions, suffix, parameters and fragment as well.

Example use cases where you should use the URL handler to build an URL directly:

* URLs to static resources from a client library
* URLs to Sling resources with selectors, extensions and suffixes
* URLs used for AJAX requests in JavaScript

The URL handler itself is also integrated into the AEM [Link Rewriter][aem-link-rewriter] (Sling Rewriter), so you do not need to explicitly externalize URLs by using the Java API when URLs are inserted in render scripts (e.g. HTL template).


### URL building

Building Sling URLs within an AEM application seems a simple task, yet it is non-trivial if all aspects are taken into account:

* Proper syntax and escaping
* Optional context path of AEM web application
* Optional URL shortening
* Namespace mangling in URLs
* Handling of complex Sling Suffix strings without clashes in the file system of AEM dispatcher cache
* Switch protocols from HTTP to HTTPs and vice versa
* Externalization with public host name

AEM supports only some of these features by it's [Externalizer][aem-externalizer] service and the [Link Rewriter][aem-link-rewriter] (Sling Rewriter), but it lacks proper multi-tenancy support and a good API.

The URL Handler supports all these features and provides a fluent Java API.


### Site root detection

AEM has no built-in concept for "what is a site" and "where is the site root". This makes it difficult because a lot of components need to know what is the site root, e.g. navigation components, breadcrumb components, or a Granite UI component to pick a path of the current site.

The [AEM Core WCM Components][aem-core-wcm-components] use content policies to define the site root for it's various components, but this does not work well because it has to be redefined for every component and template context. And if the same editable templates is defined for multiple sites with different root paths the editable template definition itself might need to be duplicated.

Therefore the URL handler introduces a way to detect the Site Root that is used by all wcm.io Handlers and can also be used easily by the project's AEM components.

By default the site root is detected by taking the root of the "inner-most" Sling Context-Aware Configuration context path as site root path. But you can easily apply your own concept how the root path is detected, e.g. by assuming a fixed absolute parent level. The point is: once this is configured in a central place, it's respected by all components and wcm.io Handlers in a consistent way.

The URL handler also provides a predefined `SiteRoot` model which can be directly used in HTL templates or injected in other Sling Models to get the current site root.

The site root detection also works in special conditions e.g. when the site is part of an AEM launch or an older version of the page is previewed in the version history mode.


### URL externalization

The URL handler can externalize URLs that point to AEM content pages or assets with the proper domain name.

Unlike the AEM [Externalizer][aem-externalizer] service the configuration of these domains is done via Sling Context-Aware configuration and thus fully supporting a multi-tenancy context.

Alternatively, the domain names which are configured as part of the Sling Mapping configuration can be used for externalization. This has to be enabled explicitly in the UrlHandlerConfig.


### URL modes

Whenever an URL is built it's possible to process it using an "URL mode". In the default mode URLs are not externalized if not required.

Built-in URL modes:

* **DEFAULT**: Default mode - Does generate a full externalized URL only if both siteUrl and siteUrlSecure parameter are set in context-specific configuration. If not set, only URLs without hostname are generated.
* **NO_HOSTNAME**: Does generate a externalized URL without any protocol and hostname, independent of any setting in context-specific configuration.
* **FULL_URL**: Enforce the generation of a full URL with protocol and hostname.
* **FULL_URL_FORCENONSECURE**: Enforce the generation of a full URL with protocol and hostname and non-secure mode.
* **FULL_URL_FORCESECURE**:  Enforce the generation of a full URL with protocol and hostname and secure mode.
* **FULL_URL_PROTOCOLRELATIVE**: Enforce the generation of a full URL with hostname and "//" as protocol (protocol-relative mode).
* **FULL_URL_PUBLISH**: Enforce the generation of a full URL with protocol and hostname.
* **FULL_URL_PUBLISH_FORCENONSECURE**:  Enforce the generation of a full URL with protocol and hostname and non-secure mode.
* **FULL_URL_PUBLISH_FORCESECURE**: Enforce the generation of a full URL with protocol and hostname and secure mode.
* **FULL_URL_PUBLISH_PROTOCOLRELATIVE**: Enforce the generation of a full URL with hostname and "//" as protocol (protocol-relative mode).


### Highly customizable

The URL Handler is highly customizable to adapt it to the needs of your project:

* Customize the site root detection
* Customize the detection of secure mode
* Define default URL mode and add custom URL mode implementations
* Make use of the integrator template feature


[usage]: usage.html
[link-handler]: ../link/
[media-handler]: ../media/
[richtext-handler]: ../richtext/
[aem-externalizer]: https://helpx.adobe.com/experience-manager/6-4/sites/developing/using/externalizer.html
[aem-link-rewriter]: https://helpx.adobe.com/experience-manager/using/creating-link-rewrite.html
[aem-core-wcm-components]: https://github.com/adobe/aem-core-wcm-components
