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
import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.format.MediaFormat;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.models.annotations.Model;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Builds image element with data attribute containing sources for responsive image.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ConsumerType
public class ResponsiveImageMediaMarkupBuilder extends AbstractImageMediaMarkupBuilder {

  /**
   * Data attribute name for responsive image sources.
   */
  static final String PROP_RESPONSIVE_SOURCES = "resp-src";

  /**
   * Property name for image URL
   */
  static final String PROP_SRC = "src";

  @Override
  public final boolean accepts(Media media) {
    return media.getMediaRequest().getMediaArgs().isMediaFormatsMandatory()
        && media.getRendition() != null
        && media.getRenditions().size() > 1
        && media.isValid();
  }

  @Override
  public final HtmlElement<?> build(Media media) {
    HtmlElement<?> mediaElement = getImageElement(media);

    // set responsive image sources
    JSONArray sources = getResponsiveImageSources(media);
    setResponsiveImageSource(mediaElement, sources, media);

    // set additional attributes
    setAdditionalAttributes(mediaElement, media);

    // further processing in edit or preview mode
    applyWcmMarkup(mediaElement, media);

    return mediaElement;
  }

  /**
   * Create an IMG element with alt text.
   * @param media Media metadata
   * @return IMG element with properties
   */
  protected HtmlElement<?> getImageElement(Media media) {
    Image img = new Image();

    // Alternative text
    Asset asset = media.getAsset();
    String altText = null;
    if (asset != null) {
      altText = asset.getAltText();
    }
    if (StringUtils.isNotEmpty(altText)) {
      img.setAlt(altText);
    }

    return img;
  }

  /**
   * Collect responsive JSON metadata for all renditions as image sources.
   * @param media Media
   * @return JSON metadata
   */
  protected JSONArray getResponsiveImageSources(Media media) {
    Collection<Rendition> renditions = media.getRenditions();
    JSONArray sources = new JSONArray();
    for (Rendition rendition : renditions) {
      sources.put(toReponsiveImageSource(media, rendition));
    }
    return sources;
  }

  /**
   * Build JSON metadata for one rendition as image source.
   * @param media Media
   * @param rendition Rendition
   * @return JSON metadata
   */
  protected JSONObject toReponsiveImageSource(Media media, Rendition rendition) {
    try {
      JSONObject source = new JSONObject();
      MediaFormat mediaFormat = rendition.getMediaFormat();
      source.put(MediaNameConstants.PROP_BREAKPOINT, mediaFormat.getProperties().get(MediaNameConstants.PROP_BREAKPOINT));
      source.put(PROP_SRC, rendition.getUrl());
      return source;
    }
    catch (JSONException ex) {
      throw new RuntimeException("Error building JSON source.", ex);
    }
  }

  /**
   * Set attribute on media element for responsive image sources
   * @param mediaElement Media element
   * @param responsiveImageSources Responsive image sources JSON metadata
   * @param media Media
   */
  protected void setResponsiveImageSource(HtmlElement<?> mediaElement, JSONArray responsiveImageSources, Media media) {
    mediaElement.setData(PROP_RESPONSIVE_SOURCES, responsiveImageSources.toString());
  }

  @Override
  public final boolean isValidMedia(HtmlElement<?> element) {
    if (element instanceof Image) {
      Image img = (Image)element;
      // if it's a responsive image, we don't need the src attribute set
      return imageSourceIsNotEmpty(img)
          && !StringUtils.contains(img.getCssClass(), MediaNameConstants.CSS_DUMMYIMAGE);
    }
    return false;
  }

  private boolean imageSourceIsNotEmpty(Image img) {
    String imageSources = img.getData(PROP_RESPONSIVE_SOURCES);
    return StringUtils.isNotBlank(imageSources);
  }

}
