/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.handler.media.markup;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Image;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.impl.DummyImageServlet;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.spi.MediaSource;
import io.wcm.handler.url.UrlHandler;
import io.wcm.handler.url.suffix.SuffixBuilder;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.contenttype.FileExtension;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.osgi.annotation.versioning.ConsumerType;

import com.day.cq.wcm.api.WCMMode;
import com.google.common.collect.ImmutableSortedMap;

/**
 * Generates a rendered dummy image as edit placeholder in WCM edit mode with information about image sizes
 * and media format name.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ConsumerType
public class DummyResponsiveImageMediaMarkupBuilder extends AbstractImageMediaMarkupBuilder {

  @Self
  private Adaptable adaptable;
  @Self
  private UrlHandler urlHandler;
  @Self
  private MediaHandlerConfig mediaHandlerConfig;

  @Override
  public final boolean accepts(Media media) {
    // accept if not rendition was found and in edit mode
    // and multiple media formats are mandatory, and dummy image is not suppressed
    MediaArgs mediaArgs = media.getMediaRequest().getMediaArgs();
    MediaFormat[] mediaFormats = mediaArgs.getMediaFormats();
    return (!media.isValid() || media.getRendition() == null)
        && getWcmMode() != null
        && getWcmMode() != WCMMode.DISABLED
        && (mediaFormats != null && mediaFormats.length > 1)
        && mediaArgs.isDummyImage() && mediaArgs.isMediaFormatsMandatory();
  }

  @Override
  public final HtmlElement<?> build(Media media) {
    HtmlElement<?> mediaElement = getImageElement(media);

    // set responsive image sources
    JSONArray sources = getResponsiveImageSources(media.getMediaRequest().getMediaArgs().getMediaFormats());
    setResponsiveImageSource(mediaElement, sources);

    // set additional attributes
    setAdditionalAttributes(mediaElement, media.getMediaRequest().getMediaArgs());

    // enable drag&drop for media source - if none is specified use first one defined in config
    MediaSource mediaSource = media.getMediaSource();
    if (mediaSource == null && !mediaHandlerConfig.getSources().isEmpty()) {
      Class<? extends MediaSource> mediaSourceClass = mediaHandlerConfig.getSources().iterator().next();
      mediaSource = AdaptTo.notNull(adaptable, mediaSourceClass);
    }
    if (mediaSource != null) {
      mediaSource.enableMediaDrop(mediaElement, media.getMediaRequest());
    }

    return mediaElement;
  }

  /**
   * Create an IMG element.
   * @param media Media metadata
   * @return IMG element with properties
   */
  protected HtmlElement<?> getImageElement(Media media) {
    Image img = new Image().addCssClass(MediaNameConstants.CSS_DUMMYIMAGE);
    return img;
  }

  /**
   * Collect responsive JSON metadata for all renditions as image sources.
   * @param mediaFormats Media formats
   * @return JSON metadata
   */
  protected JSONArray getResponsiveImageSources(MediaFormat[] mediaFormats) {
    JSONArray sources = new JSONArray();
    for (MediaFormat mediaFormat : mediaFormats) {
      sources.put(toReponsiveImageSource(mediaFormat));
    }
    return sources;
  }

  /**
   * Build JSON metadata for one rendition as image source.
   * @param mediaFormat Media format
   * @return JSON metadata
   */
  protected JSONObject toReponsiveImageSource(MediaFormat mediaFormat) {
    String url = urlHandler.get(DummyImageServlet.PATH)
        .extension(FileExtension.PNG)
        .suffix(buildDummyServletSuffix(mediaFormat))
        .build();
    try {
      JSONObject source = new JSONObject();
      source.put(MediaNameConstants.PROP_BREAKPOINT, mediaFormat.getProperties().get(MediaNameConstants.PROP_BREAKPOINT));
      source.put(ResponsiveImageMediaMarkupBuilder.PROP_SRC, url);
      return source;
    }
    catch (JSONException ex) {
      throw new RuntimeException("Error building JSON source.", ex);
    }
  }

  private String buildDummyServletSuffix(MediaFormat format) {
    SuffixBuilder builder = new SuffixBuilder(getRequest());
    return builder.build(ImmutableSortedMap.<String, String>naturalOrder()
        .put(DummyImageServlet.SUFFIX_WIDTH, Long.toString(format.getWidth()))
        .put(DummyImageServlet.SUFFIX_HEIGHT, Long.toString(format.getHeight()))
        .put(DummyImageServlet.SUFFIX_MEDIA_FORMAT_NAME, format.getLabel())
        .build());
  }

  /**
   * Set attribute on media element for responsive image sources
   * @param mediaElement Media element
   * @param responsiveImageSources Responsive image sources JSON metadata
   */
  protected void setResponsiveImageSource(HtmlElement<?> mediaElement, JSONArray responsiveImageSources) {
    mediaElement.setData(ResponsiveImageMediaMarkupBuilder.PROP_RESPONSIVE_SOURCES, responsiveImageSources.toString());
  }

  @Override
  public final boolean isValidMedia(HtmlElement<?> element) {
    return false;
  }

}