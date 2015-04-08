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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Basic implementation of {@link io.wcm.handler.media.spi.MediaMarkupBuilder} for images.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ConsumerType
public class SimpleImageMediaMarkupBuilder extends AbstractImageMediaMarkupBuilder {

  @Override
  public final boolean accepts(Media media) {
    // accept if rendition is an image rendition, and resolving was successful
    return media.isValid()
        && media.getRendition() != null
        && media.getRendition().isImage();
  }

  @Override
  public final HtmlElement<?> build(Media media) {

    // render media element for rendition
    HtmlElement<?> mediaElement = getImageElement(media);

    // set additional attributes
    setAdditionalAttributes(mediaElement, media.getMediaRequest().getMediaArgs());

    // further processing in edit or preview mode
    applyWcmMarkup(mediaElement, media);

    return mediaElement;
  }

  /**
   * Create an IMG tag that displays the given rendition image.
   * @param media Media metadata
   * @return IMG tag with properties or null if media metadata is invalid
   */
  protected HtmlElement<?> getImageElement(Media media) {
    Image img = null;

    Asset asset = media.getAsset();
    Rendition rendition = media.getRendition();

    String url = null;
    if (rendition != null) {
      url = rendition.getUrl();
    }

    if (url != null) {
      img = new Image(url);

      // Alternative text
      String altText = null;
      if (asset != null) {
        altText = asset.getAltText();
      }
      if (StringUtils.isNotEmpty(altText)) {
        img.setAlt(altText);
      }

      // set width/height,
      if (rendition != null) {
        long height = rendition.getHeight();
        if (height > 0) {
          img.setHeight(height);
        }
        long width = rendition.getWidth();
        if (width > 0) {
          img.setWidth(width);
        }
      }
    }

    return img;
  }

  @Override
  public final boolean isValidMedia(HtmlElement<?> element) {
    if (element instanceof Image) {
      Image img = (Image)element;
      return StringUtils.isNotEmpty(img.getSrc())
          && !StringUtils.contains(img.getCssClass(), MediaNameConstants.CSS_DUMMYIMAGE);
    }
    return false;
  }

}
