## Link Handler Usage


### Building links

The [Link Handler][link-handler] is a Sling Model and can be adapted either from a request or a resource. It automatically reads the context-specific configuration for the Site URLs of the [URL Handler][url-handler] based on the resource path of the current request or the path of the resource adapted from.

Example:

```java
LinkHandler linkHandler = request.adaptTo(LinkHandler.class);

// build link stored in current resource
Link link = linkHandler.get(resource).build();

// build link targeting the given content page with a different selector and extension
Link link = linkHandler.get(contentPage).selector("sel1").extension("json").build();

// check if link is valid and get markup
if (link.isValid()) {
  String markup = link.getMarkup();
  // ...
}
```

Alternatively you can inject the `LinkHandler` into your Sling Model using the `@Self` annotation if the model itself adapts from request or resource.

The link handler uses a "builder pattern" so you can flexibly combine the different link generation options.
See [LinkBuilder][link-builder] for all options.


### Link properties in resource

When storing a link in a resource multiple properties are used to describe the link. The properties depend on the link type implementations, these are the most important properties supported by the built-in link types:

* `linkType`: Type of links as chosen by the editor (e.g. internal, external or media)
* `linkContentRef`: Path of internal content page to link to
* `linkMediaRef`: Path of media asset (e.g. DAM asset) to link to
* `linkExternalRef`: External URL to link to
* `linkWindowTarget`: Target for window to open link in (e.g. "_blank")

Further properties are defined in [LinkNameConstants][link-name-constants]. It is recommended to define an edit dialog that shows only the properties supported for the selected link type after choosing one.


### Using links in Sightly template

To use the link handler inside a sightly template it is recommended to create a generic Sling Model for calling the handler:

```java
@Model(adaptables = SlingHttpServletRequest.class)
public class ResourceLink {

  @Self private LinkHandler linkHandler;
  @SlingObject private Resource resource;
  private Link link;

  @PostConstruct
  protected void activate() {
    link = linkHandler.get(resource).build();
  }

  public boolean isValid() {
    return link.isValid();
  }

  public Map<String, String> getAttributes() {
    return link.getAnchorAttributes();
  }

}
```

Then you can use it inside your sightly template:

```html
<div data-sly-use.link="com.myapp.ResourceLink" data-sly-unwrap>
  <a data-sly-attribute="${link.attributes}" data-sly-test="{link.valid}">
    ${properties.linkTitle}
  </a>
</div>
<div class="cq-placeholder" data-emptytext="${component.title}" data-sly-test="${!link.valid}"></div>
```

In this case the anchor defined in the sightly template is used, but all attributes of the `a` element are overwritten with those returned by the link handler. This is primary the href attribute, but may contain further for defining the link target or custom metadata for user tracking.



### Configuring and tailoring the link resolving process

Optionally you can implement an interface to specify in more detail the link resolving needs of your application. For this you have to implement the [LinkHandlerConfig][link-handler-config] interface. You can extend from [AbstractLinkHandlerConfig][abstract-link-handler-config] and overwrite only what is required.

The class you implement is a Sling Model class, and should have an @Application annotation with the Application ID specified via the [Application Provider interface][config-application-provider] of the configuration infrastructure.

With this you can:

* Define which link types are supported by your application or include your own ones
* Define which markup builders are supported by your application or include your own ones
* Define custom pre- and postprocessors that are called before and after the link resolving takes place
* Implement a method which decides whether a content page is allowed to link to or not
* Implement a method which decides whether a content page is a redirect page or not

Example:

```java
@Model(adaptables = { SlingHttpServletRequest.class, Resource.class }, adapters = LinkHandlerConfig.class)
@Application(ApplicationProviderImpl.APPLICATION_ID)
public class LinkHandlerConfigImpl extends AbstractLinkHandlerConfig {

  private static final List<Class<? extends LinkType>> LINK_TYPES =
      ImmutableList.<Class<? extends LinkType>>of(
          InternalLinkType.class,
          ExternalLinkType.class,
          MediaLinkType.class
      );

  @Override
  public List<Class<? extends LinkType>> getLinkTypes() {
    return LINK_TYPES;
  }

  @Override
  public boolean isRedirect(Page page) {
    String template = page.getProperties().get(NameConstants.PN_TEMPLATE, String.class);
    return StringUtils.equals(template, "/apps/sample/templates/redirect");
  }

}
```

Schematic flow of link handling process:

1. Start link handler processing
2. Detect link type, store result in link request
3. Apply preprocessors on link request
4. Resolve link using link type, store result in link request
5. Generate markup using markup builder, store result in link request
6. Apply postprocessors on link request


[link-handler]: apidocs/io/wcm/handler/link/LinkHandler.html
[link-builder]: apidocs/io/wcm/handler/link/LinkBuilder.html
[link-name-constants]: apidocs/io/wcm/handler/link/LinkNameConstants.html
[link-handler-config]: apidocs/io/wcm/handler/link/spi/LinkHandlerConfig.html
[abstract-link-handler-config]: apidocs/io/wcm/handler/link/spi/helpers/AbstractLinkHandlerConfig.html
[url-handler]: ../url/
[config-application-provider]: ../../config/api/usage-spi.html#Application_provider
