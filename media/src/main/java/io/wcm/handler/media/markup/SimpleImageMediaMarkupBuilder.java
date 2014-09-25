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
import io.wcm.handler.media.MediaItem;
import io.wcm.handler.media.MediaMarkupBuilder;
import io.wcm.handler.media.MediaMetadata;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.Rendition;
import io.wcm.sling.models.annotations.AemObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.day.cq.wcm.api.WCMMode;

/**
 * Very basic implementation of {@link io.wcm.handler.media.MediaMarkupBuilder} for images.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
public class SimpleImageMediaMarkupBuilder implements MediaMarkupBuilder {

  @AemObject
  private WCMMode wcmMode;
  @Self
  private SlingHttpServletRequest request;

  @Override
  public final boolean accepts(MediaMetadata mediaMetadata) {
    // accept if rendition is an image rendition
    return mediaMetadata.getRendition() != null && mediaMetadata.getRendition().isImage();
  }

  @Override
  public final HtmlElement<?> build(MediaMetadata pMediaMetadata) {

    // render media element for rendition
    HtmlElement<?> mediaElement = getImageElement(pMediaMetadata);

    // further processing in edit or preview mode
    Resource resource = pMediaMetadata.getMediaReference().getResource();
    if (mediaElement != null && resource != null) {

      switch (wcmMode) {
        case EDIT:
          // enable drag&drop from content finder
          pMediaMetadata.getMediaSource().enableMediaDrop(mediaElement, pMediaMetadata.getMediaReference());
          break;

        case PREVIEW:
          // add diff decoration
          String refProperty = StringUtils.defaultString(pMediaMetadata.getMediaReference().getRefProperty(), MediaNameConstants.PN_MEDIA_REF);
          MediaMarkupBuilderUtil.addDiffDecoration(mediaElement, resource, refProperty, request);
          break;

        default:
          // do nothing
      }

    }

    return mediaElement;
  }

  /**
   * Create an IMG tag that displays the given rendition image.
   * @param mediaMetadata Media metadata
   * @return IMG tag with properties or null if media metadata is invalid
   */
  protected HtmlElement<?> getImageElement(MediaMetadata mediaMetadata) {
    Image img = null;

    MediaItem mediaItem = mediaMetadata.getMediaItem();
    Rendition rendition = mediaMetadata.getRendition();

    String url = null;
    if (rendition != null) {
      url = rendition.getMediaUrl();
    }

    if (url != null) {
      img = new Image(url);

      // Alternative text
      String altText = null;
      if (mediaItem != null) {
        altText = mediaItem.getAltText();
      }
      if (StringUtils.isNotEmpty(altText)) {
        img.setAlt(altText);
      }

      // set width/height,
      if (rendition != null) {
        int height = rendition.getHeight();
        if (height > 0) {
          img.setHeight(height);
        }
        int width = rendition.getWidth();
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
