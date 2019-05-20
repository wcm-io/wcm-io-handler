## URL Handler usage


### Building URLs

The [URL Handler][url-handler] is a Sling Model and can be adapted either from a request or a resource. It automatically reads the context-specific configuration for the Site URLs based on the resource path of the current request or the path of the resource adapted from.

Example:

```java
UrlHandler urlHandler = request.adaptTo(UrlHandler.class);

// build externalized resource URL
String url = urlHandler.get(cssPath).buildExternalResourceUrl();

// build content page link with selector and protocol change
String url = urlHandler.get(targetPage).selectors("form")
    .urlMode(UrlModes.FULL_URL_FORCESECURE).buildExternalLinkUrl();
```

Alternatively you can inject the `UrlHandler` into your Sling Model using the `@Self` annotation if the model itself adapts from request or resource.

The URL handler uses a "builder pattern" so you can flexibly combine the different URL generation options.
See [URlBuilder][url-builder] for all options.

With the different Enumeration constants from [UrlModes][url-modes] you have fine-grained control how the URLs are externalized in the different modes. You can implement your own UrlModes as well.


### Configuring domain names for URL externalization

To make sure that the externalization works properly you have to configure three Parameters in the [Context-Aware Configuration][caconfig] for configuration "wcm.io Handler Site URLs" (internal name: `io.wcm.handler.url.SiteConfig`):

* `siteUrl`: Site URL on public access from outside, for non-secure access (HTTP).
* `siteUrlSecure`: Site URL for public access from outside, for secure access (HTTPS).
* `siteUrlAuthor`: Site URL on author instance.

Site URL is a protocol and hostname, e.g. `http://www.mycompany.com`.


### Site Root detection

The URL handler provides a Sling Model class [SiteRoot][siteroot-model] which provides a simple API to get the current site root either in a HTL script or in another Sling Model. The model can be adapted either from a request or a resource.

Example usage in a Sling Model:

```java
@Model(adaptables = SlingHttpServletRequest.class)
public class MyModel {

  @Self
  private SiteRoot siteRoot;

  @PostConstruct
  private void activate() {
    // get site root page
    Page rootPage = siteRoot.getRootPage();
  }

}
```


### Configuring and tailoring the URL resolving process

Optionally you can provide an OSGi service to specify in more detail the URL resolving needs of your application. For this you have to extend the [UrlHandlerConfig][url-handler-config] class. Via [Context-Aware Services][sling-commons-caservices] you can make sure the SPI customization affects only resources (content pages, DAM assets) that are relevant for your application. Thus it is possible to provide different customizations for different applications running in the same AEM instance.

With this you can:

* Specify on which hierarchy level the root of your site is located (if not specified the inner-most context-aware configuration context path is used)
* Detect whether a link to a page has to be switched to HTTPs (e.g. a forms template)
* Detect whether a page is an integrator template
* Define the default URL mode to use
* Defines supported integrator template modes

Example:

```java
@Component(service = UrlHandlerConfig.class, property = {
    ContextAwareService.PROPERTY_CONTEXT_PATH_PATTERN + "=^/content/(dam/)?myapp(/.*)?$"
})
public class UrlHandlerConfigImpl extends UrlHandlerConfig {

  public static final int SITE_ROOT_LEVEL = 3;

  @Override
  public int getSiteRootLevel(String contextPath) {
    return SITE_ROOT_LEVEL;
  }

  @Override
  public boolean isSecure(Page page) {
    // check if template has a template that needs HTTPs
  }

}
```


[url-handler]: apidocs/io/wcm/handler/url/UrlHandler.html
[integrator]: integrator.html
[url-builder]: apidocs/io/wcm/handler/url/UrlBuilder.html
[url-modes]: apidocs/io/wcm/handler/url/UrlModes.html
[url-params]: apidocs/io/wcm/handler/url/UrlParams.html
[url-handler-config]: apidocs/io/wcm/handler/url/spi/UrlHandlerConfig.html
[siteroot-model]: apidocs/io/wcm/handler/url/ui/SiteRoot.html
[caconfig]: ../../caconfig/
[sling-commons-caservices]: ../../sling/commons/context-aware-services.html
