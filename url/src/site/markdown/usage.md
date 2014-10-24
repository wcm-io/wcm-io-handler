## URL Handler usage


### Introduction

In most cases you do not use the URL handler directly to build URLs, but you use one of the higher-level handlers to do the job:

* [Link Handler][link-handler]: Used to build URLs and markup for links to content pages, external links or DAM assets
* [Media Handler][media-handler]: Used to build URLs and markup for displaying images or other media content inside a page
* [RichText Handler][richtext-handler]: Used to format a HTML fragment that may contain inline links oder media references

These handlers make use of the URL handler and its configuration internally. The link handler supports most or URL handler options to modify the generated URL by added selectors, extensions, suffix, parameters and fragment as well.

But if you want to build an URL directly, e.g. for links to resources in the docroot or client library, you can use the URL handler directly.


### Terminology

Within the URL Handler API the following terminology is used:

* Link URL: Link to a content page to which the user can navigate to with his browser
* Resource URL: Link of a resource that is included and loaded inside the current page (e.g. references to JavaScript, Stylesheets, Images, AJAX requests)

This distinction is important because in case of [Integrator Template Mode][integrator] those two types of URLs are handled differently.

For a Link URL always the target page to which the internal link points to has to be provided as well to check if the target page is valid and the template is allowed to link to. This is normally done automatically by the [Link Handler][link-handler].


### Building URLs

The URL Handler is a Sling Model and can be adapted either from a request or a resource. It automatically reads the context-specific configuration for the Site URLs based on the resource path of the current request or the path of the resource adapted from.

Example:

```java
UrlHandler urlHandler = request.adaptTo(UrlHandler.class);

// build externalized resource URL
String url = urlHandler.get(cssPath).buildExternalResourceUrl();

// build content link with selector and protocol change
String url = urlHandler.get(pagePath).selectors("form")
    .urlMode(UrlModes.FULL_URL_FORCESECURE).buildExternalLinkUrl(targetPage);
```

Alternatively you can inject the `UrlHandler` into your Sling Model using the `@Self` annotation if the model itself adapts from request or resource.

The URL handler uses a "builder pattern" so you can flexibly combine the different URL generation options.
See [URlBuilder][url-builder] for all options.

With the different Enumeration constants from [UrlModes][url-modes] you have fine-grained control how the URLs are externalized in the different modes. You can implement your own UrlModes as well.


### Context-specific parameters

To make sure that the externalization works properly you have to configure three Parameters in the [context-specific Configuration][config]:

* Site URL: Site URL on public access from outside, for non-secure access (HTTP).
* Site URL Secure: Site URL for public access from outside, for secure access (HTTPS).
* Site URL Author: Site URL on author instance.

Site URL is a protocol and hostname, e.g. http://www.mycompany.com
For accessing the parameter you can use the constants from [UrlParams][url-params] class.


### Configuring and tailoring the URL resolving process

Optionally you can implement an interface to specify in more detail the URL resolving needs of your application. For this you have to implement the [UrlHandlerConfig][url-handler-config] interface. You can extend from [AbstractUrlHandlerConfig][abstract-url-handler-config] and overwrite only what is required.

The class you implement is a Sling Model class, and should have an @Application annotation as well with the Application ID specified via the [Application Provider interface][config-application-provider] of the configuration infrastructure.

With this you can:

* Specify on which hierarchy level the root of your site is located (this is normally identically to the inner-most context-specific configuration scope)
* Detect whether a link to a page has to be switched to HTTPs (e.g. a forms template)
* Detect whether a page is an integrator template
* Define the default URL mode to use
* Defines supported integrator template modes

Example:

```java
@Model(adaptables = { SlingHttpServletRequest.class, Resource.class }, adapters = UrlHandlerConfig.class)
@Application(ApplicationProviderImpl.APPLICATION_ID)
public class UrlHandlerConfigImpl extends AbstractUrlHandlerConfig {

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


[link-handler]: ../link/
[media-handler]: ../media/
[richtext-handler]: ../richtext/
[integrator]: integrator.html
[url-builder]: apidocs/io/wcm/handler/url/UrlBuilder.html
[url-modes]: apidocs/io/wcm/handler/url/UrlModes.html
[url-params]: apidocs/io/wcm/handler/url/UrlParams.html
[url-handler-config]: apidocs/io/wcm/handler/url/spi/UrlHandlerConfig.html
[abstract-url-handler-config]: apidocs/io/wcm/handler/url/spi/helpers/AbstractUrlHandlerConfig.html
[config]: ../../config/
[config-application-provider]: ../../config/api/usage-spi.html#Application_provider
