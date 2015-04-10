## Media Handler Usage


### Building media

The [Media Handler][media-handler] is a Sling Model and can be adapted either from a request or a resource. It automatically reads the context-specific configuration for the Site URLs of the [URL Handler][url-handler] based on the resource path of the current request or the path of the resource adapted from.

Example:

```java
MediaHandler mediaHandler = request.adaptTo(MediaHandler.class);

// build media referenced in current resource
Media media = mediaHandler.get(resource).build();

// build media referenced by path with a specific media format
Media media = mediaHandler.get("/content/dam/sample/asset1.jpg").mediaFormat(MediaFormats.FORMAT1).build();

// check if media is valid and get markup
if (media.isValid()) {
  String markup = media.getMarkup();
  // ...
}
```

Alternatively you can inject the `MediaHandler` into your Sling Model using the `@Self` annotation if the model itself adapts from request or resource.

The media handler uses a "builder pattern" so you can flexibly combine the different media generation options.
See [MediaBuilder][media-builder] for all options.


### Media properties in resource

When referencing a media in a resource multiple properties are used to describe the media reference. Some of the properties depend on the media source implementation. These are the most common properties:

* `mediaRef`: Reference/path to the media asset
* `mediaCrop`: Cropping parameters for image
* `mediaAltText`: Alternative text for media

Further properties are defined in [MediaNameConstants][media-name-constants]. It is recommended to define an edit dialog which either allows selection a media asset from repository (e.g. browsing DAM hierarchy), or to upload a binary file to the page.


### Media formats

Media formats describe expected output formats of media assets or images. They are defined as constants using a builder pattern. The most simple type of media format is defining an image with a  fixed dimension:

```java
public static final MediaFormat CONTENT_480 = MediaFormatBuilder.create("content_480", APPLICATION_ID)
    .label("Content Standard")
    .fixedDimension(480, 270)
    .extensions("gif", "jpg", "png")
    .build();
```

It is also possible to define a format which matches certain min/max-sizes and a ratio:

```java
public static final MediaFormat GALLERY_LARGE = create("galleryLarge", APPLICATION_ID)
    .label("Gallery Large")
    .minWidth(1000)
    .minHeight(500)
    .ratio(2.0d)
    .extensions("gif", "jpg", "png")
    .build();
```

Or a media format defining downloads only restricting by file extensions:

```java
public static final MediaFormat DOWNLOAD = create("download", APPLICATION_ID)
    .label("Download")
    .extensions("pdf", "zip", "ppt", "pptx", "doc", "docx")
    .build();
```

These media format definitions have to be provided to the media handling using an OSGi service implementing the [MediaFormatProvider][media-format-provider] interface. For convenience it is possible to extend [AbstractMediaFormatProvider][abstract-media-format-provider] and extract the defined formats from the public static fields of a class.

When resolving a media reference it is possible to specify one or multiple media formats. If the media asset contains a rendition that exactly matches the format it is returned. If it contains a rendition that is bigger but has the requested ratio i dynamically downscaled rendition is returned. If cropping parameters are defined they are applied before checking against the media format. If no rendition matches or can be rescaled the resolving process failed and the returned media is invalid. In Edit Mode `DummyImageMediaMarkupBuilder` is then used to render a drop area instead to which an DAM asset can be assigned via drag&drop.

To resolve multiple renditions at once for a responsive image it is possible to specify a list of mandatory media formats:

```java
MediaHandler mediaHandler = request.adaptTo(MediaHandler.class);

// build media with a list of mandatory media formats
Media media = mediaHandler.get(resource)
    .mandatoryMediaFormats(MediaFormats.FORMAT1, MediaFormats.FORMAT2).build();

// get all renditions
if (media.isValid()) {
  for (Rendition rendition : renditions) {
    // check rendition
  }
}
```

A custom markup builder can then generated the image tag with metadata for all breakpoints (depending on the frontend solution).


### Using media in Sightly template

To use the media handler inside a sightly template it is recommended to create a generic Sling Model for calling the handler:

```java
@Model(adaptables = SlingHttpServletRequest.class)
public class ResourceMedia {

  @RequestAttribute private String mediaFormat;
  @Self private MediaHandler mediaHandler;
  @SlingObject private Resource resource;

  private Media media;

  @PostConstruct
  protected void activate() {
    media = mediaHandler.get(resource, new MediaArgs(mediaFormat)).build();
  }

  public boolean isValid() {
    return media.isValid();
  }

  public String getMarkup() {
    return media.getMarkup();
  }

}
```

Then you can use it inside your sightly template:

```html
<div class="box-padding"
    data-sly-use.media="${'com.myapp.ResourceMedia' @ mediaFormat='content_480'}">

  <figure class="image-small" data-sly-test="${media.valid}">
    <a href="conference.shtml">
      <img data-sly-unwrap data-sly-text="${media.markup @ context='html'}"/>
    </a>
  </figure>

</div>
```

In this case the dummy `<img/>` is replaced with the media markup of the media handler, which is not necessarily is an `img` element, but may be any markup (e.g. a `video` or `audio` or `div` element with custom markup).


### Configuring and tailoring the media resolving process

Optionally you can implement an interface to specify in more detail the media resolving needs of your application. For this you have to implement the [MediaHandlerConfig][media-handler-config] interface. You can extend from [AbstractMediaHandlerConfig][abstract-media-handler-config] and overwrite only what is required.

The class you implement is a Sling Model class, and should have an @Application annotation with the Application ID specified via the [Application Provider interface][config-application-provider] of the configuration infrastructure.

With this you can:

* Define which media sources are supported by your application or include your own ones
* Define which markup builders are supported by your application or include your own ones
* Define custom pre- and postprocessors that are called before and after the media resolving takes place
* Define which media formats may be used for downloads that means link targets of the [Link Handler][link-handler]
* Implement a method which returns the default quality when writing images with lossy compression

Example:

```java
@Model(adaptables = { SlingHttpServletRequest.class, Resource.class }, adapters = MediaHandlerConfig.class)
@Application(ApplicationProviderImpl.APPLICATION_ID)
public class MediaHandlerConfigImpl extends AbstractMediaHandlerConfig {

  private static final Set<MediaFormat> DOWNLOAD_MEDIA_FORMATS =
      ImmutableSet.of(
          MediaFormats.DOWNLOAD
      );
  private static final List<Class<? extends MediaMarkupBuilder>> MEDIA_MARKUP_BUILDERS =
      ImmutableList.<Class<? extends MediaMarkupBuilder>>of(
          SimpleImageMediaMarkupBuilder.class,
          DummyImageMediaMarkupBuilder.class
      );

  @Override
  public List<Class<? extends MediaMarkupBuilder>> getMarkupBuilders() {
    return MEDIA_MARKUP_BUILDERS;
  }

  @Override
  public Set<MediaFormat> getDownloadMediaFormats() {
    return DOWNLOAD_MEDIA_FORMATS;
  }

}
```

Schematic flow of media handling process:

1. Start media handler processing
2. Detect media source, store result in media request
3. Apply preprocessors on media request
4. Resolve media using media source, store result in media request
5. Generate markup using markup builder, store result in media request
6. Apply postprocessors on media request


### Responsive Images

In a responsive web project there is often the need to show images with same ratio but different resolutions depending on the target device, screen size and pixel depth. The Media Handler helps you on this with special markup builders.

Example - define a media format with a certain ratio and minimum sizes (big enough for for the largest resolution on the website):

```
public static final MediaFormat MF_16_9 = create("mf_16_9", APPLICATION_ID)
    .label("Media 16:9")
    .minWidth(1600)
    .minHeight(900)
    .ratio(16, 9)
    .extensions("gif", "jpg", "jpeg", "png")
    .build();
```

Configure the media handling process to use special markup builders:

```java
@Model(adaptables = { SlingHttpServletRequest.class, Resource.class }, adapters = MediaHandlerConfig.class)
@Application(ApplicationProviderImpl.APPLICATION_ID)
public class MediaHandlerConfigImpl extends AbstractMediaHandlerConfig {

  private static final List<Class<? extends MediaMarkupBuilder>> MEDIA_MARKUP_BUILDERS =
      ImmutableList.<Class<? extends MediaMarkupBuilder>>of(
          ResponsiveImageMediaMarkupBuilder.class,
          DummyResponsiveImageMediaMarkupBuilder.class
      );

  @Override
  public List<Class<? extends MediaMarkupBuilder>> getMarkupBuilders() {
    return MEDIA_MARKUP_BUILDERS;
  }

}
```

When building the markup for the media element define the required resolutions for each breakpoint:

```java
Media media = mediaHandler.get(resource)
    .mandatoryMediaFormats(new ResponsiveMediaFormatsBuilder(MediaFormats.MF_16_9)
        .breakpoint("S0", 712, 400)
        .breakpoint("M1", 401, 225)
        .breakpoint("L1", 837, 471)
        .build())
    .build();
```

This results in a markup like this:

```html
<img alt="Alt. Text" data-resp-src="[
  {'mq':'S0','src':'/path/mymedia.712.400.jpg'},
  {'mq':'M1','src':'/path/mymedia.401.225.jpg'},
  {'mq':'L1','src':'/path/mymedia.837.471.jpg'}]"/>
```

You can customize the markup that is generated by subclassing `ResponsiveImageMediaMarkupBuilder` and bring in your own logic.




[media-handler]: apidocs/io/wcm/handler/media/MediaHandler.html
[media-builder]: apidocs/io/wcm/handler/media/MediaBuilder.html
[media-name-constants]: apidocs/io/wcm/handler/media/MediaNameConstants.html
[media-handler-config]: apidocs/io/wcm/handler/media/spi/MediaHandlerConfig.html
[abstract-media-handler-config]: apidocs/io/wcm/handler/media/spi/helpers/AbstractMediaHandlerConfig.html
[media-format-provider]: apidocs/io/wcm/handler/media/spi/MediaFormatProvider.html
[abstract-media-format-provider]: apidocs/io/wcm/handler/media/spi/helpers/AbstractMediaFormatProvider.html
[url-handler]: ../url/
[link-handler]: ../link/
[config-application-provider]: ../../config/api/usage-spi.html#Application_provider
