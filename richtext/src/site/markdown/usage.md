## RichText Handler Usage

The [RichText Handler][richtext-handler] is a Sling Model and can be adapted either from a request or a resource. It automatically reads the context-specific configuration for the Site URLs of the [URL Handler][url-handler] based on the resource path of the current request or the path of the resource adapted from.


### Build rich text

To build the markup of a rich text stored in repository:

```java
RichTextHandler richTextHandler = request.adaptTo(RichTextHandler.class);

// build markup for richtext stored in current resource
String markup = richTextHandler.get(resource).buildMarkup();

// build markup for richtext passed as string
String markup = richTextHandler.get(richTextString).buildMarkup();
```

### Rich text properties in resource

When the rich text is stored in a resource the property names used by the rich text in-place editor have to be used:

* `text`: Property for storing the rich text XHTML markup
* `textIsRich`: Denotes if the text property contains rich text (true) or plain text (false)


### Build HTML from plain text

To render plain text with line breaks as HTML with `<br/>`-Elements:

```java
RichTextHandler richTextHandler = request.adaptTo(RichTextHandler.class);

// build markup for plain text with line breakds passed as string
String markup = richTextHandler.get(plainTextString).textMode(TextMode.PLAIN).buildMarkup();
```


### Using rich text in Sightly template

To resolve a rich text inside a Sightly template you can use a generic Sling Model for calling the handler:

* [ResourceRichText](apidocs/io/wcm/handler/richtext/ui/ResourceRichText.html) - for rendering rich text
* [ResourceMultilineText](apidocs/io/wcm/handler/richtext/ui/ResourceMultilineText.html) - for converting line breaks in plain text

Sightly template example:

```html
<sly data-sly-use.richtext="io.wcm.handler.richtext.ui.ResourceRichText"/>
<div>
  ${richtext.markup @ context='html'}
</div>
<div class="cq-placeholder" data-emptytext="${component.title}" data-sly-test="${!richtext.valid}"></div>
```


### Configuring and tailoring the rich text processing

Optionally you can provide an OSGi service to specify in more detail the rich text processing needs of your application. For this you have to extend the [RichTextHandlerConfig][richtext-handler-config] class. Via [Context-Aware Services][sling-commons-caservices] you can make sure the SPI customization affects only resources (content pages, DAM assets) that are relevant for your application. Thus it is possible to provide different customizations for different applications running in the same AEM instance.

With this you can:

* Define additional or alternative [RewriteContentHandler][rewriter-content-handler] implementations that rewrite the rich text fragment before rendering it to the page

Example:

```java
@Component(service = RichTextHandlerConfig.class, property = {
    ContextAwareService.PROPERTY_CONTEXT_PATH_PATTERN + "=^/content/(dam/)?myapp(/.*)?$"
})
public class RichTextHandlerConfigImpl extends RichTextHandlerConfig {

  private static final List<Class<? extends RewriteContentHandler>> REWRITE_CONTENT_HANDLERS
      = ImmutableList.<Class<? extends RewriteContentHandler>>of(
      DefaultRewriteContentHandler.class, MyCustomRewriteContentHandler.class);

  @Override
  public List<Class<? extends RewriteContentHandler>> getRewriteContentHandlers() {
    return REWRITE_CONTENT_HANDLERS;
  }

}
```

[richtext-handler]: apidocs/io/wcm/handler/richtext/RichTextHandler.html
[richtext-handler-config]: apidocs/io/wcm/handler/richtext/spi/RichTextHandlerConfig.html
[rewriter-content-handler]: apidocs/io/wcm/handler/richtext/util/RewriteContentHandler.html
[url-handler]: ../url/
[sling-commons-caservices]: ../../sling/commons/context-aware-services.html
