## RichText Handler Usage

The [RichText Handler][richtext-handler] is a Sling Model and can be adapted either from a request or a resource. It automatically reads the context-specific configuration for the Site URLs of the [URL Handler][url-handler] based on the resource path of the current request or the path of the resource adapted from.


### Build rich text

To build the markup of a rich text stored in repository:

```java
RichTextHandler richTextHandler = request.adaptTo(RichTextHandler.class);

// build markup for richtext stored in current resource (in a property named `text`)
String markup = richTextHandler.get(resource).buildMarkup();

// build markup for richtext passed as string
String markup = richTextHandler.get(richTextString).buildMarkup();
```


### Build HTML from plain text

To render plain text with line breaks as HTML with `<br/>`-Elements:

```java
RichTextHandler richTextHandler = request.adaptTo(RichTextHandler.class);

// build markup for plain text with line breakds passed as string
String markup = richTextHandler.get(plainTextString).textMode(TextMode.PLAIN).buildMarkup();
```


### Using rich text in Sightly template

To use the richtext handler inside a sightly template it is recommended to create a generic Sling Model for calling the handler:

```java
@Model(adaptables = SlingHttpServletRequest.class)
public class ResourceRichText {

  @Self private RichTextHandler richTextHandler;
  @SlingObject private Resource resource;
  private String markup;

  @PostConstruct
  protected void activate() {
    markup = richTextHandler.get(resource).buildMarkup();
  }

  public boolean isValid() {
    return StringUtils.isNotBlank(markup);
  }

  public String getMarkup() {
    return markup;
  }

}
```

Then you can use it inside your sightly template:

```html
<div data-sly-use.richtext="com.myapp.ResourceRichText" data-sly-unwrap>
  ${richtext.markup @ context='html'}
</div>
<div class="cq-placeholder" data-emptytext="${component.title}" data-sly-test="${!richtext.valid}"></div>
```


[richtext-handler]: apidocs/io/wcm/handler/richtext/RichTextHandler.html
[url-handler]: ../url/
