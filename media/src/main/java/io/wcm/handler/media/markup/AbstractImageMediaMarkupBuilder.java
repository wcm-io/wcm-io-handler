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
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.spi.MediaMarkupBuilder;
import io.wcm.sling.models.annotations.AemObject;

import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.osgi.annotation.versioning.ConsumerType;

import com.day.cq.wcm.api.WCMMode;

/**
 * Shared functionality for {@link io.wcm.handler.media.spi.MediaMarkupBuilder} image implementations.
 */
@ConsumerType
public abstract class AbstractImageMediaMarkupBuilder implements MediaMarkupBuilder {

  @AemObject(optional = true)
  private WCMMode wcmMode;

  @SlingObject(optional = true)
  private SlingHttpServletRequest request;

  /**
   * Apply Markup for Drag&amp;Drop mode and Diff decoration in WCM edit/preview mode.
   * @param mediaElement Media element
   * @param media Media
   */
  protected void applyWcmMarkup(HtmlElement<?> mediaElement, Media media) {
    // further processing in edit or preview mode
    Resource resource = media.getMediaRequest().getResource();
    if (mediaElement != null && resource != null && wcmMode != null) {

      switch (wcmMode) {
        case EDIT:
          // enable drag&drop from content finder
          media.getMediaSource().enableMediaDrop(mediaElement, media.getMediaRequest());
          break;

        case PREVIEW:
          // enable drag&drop from content finder
          media.getMediaSource().enableMediaDrop(mediaElement, media.getMediaRequest());
          // add diff decoration
          if (request != null) {
            String refProperty = StringUtils.defaultString(media.getMediaRequest().getRefProperty(), MediaNameConstants.PN_MEDIA_REF);
            MediaMarkupBuilderUtil.addDiffDecoration(mediaElement, resource, refProperty, request);
          }
          break;

        default:
          // do nothing
      }

    }
  }

  /**
   * Set additional attributes on the media element from the MediaArgs properties.
   * @param mediaElement Media element
   * @param mediaArgs Media args
   */
  protected void setAdditionalAttributes(HtmlElement<?> mediaElement, MediaArgs mediaArgs) {
    for (Entry<String, Object> entry : mediaArgs.getProperties().entrySet()) {
      if (StringUtils.equals(entry.getKey(), MediaNameConstants.PROP_CSS_CLASS)) {
        mediaElement.addCssClass(entry.getValue().toString());
      }
      else {
        mediaElement.setAttribute(entry.getKey(), entry.getValue().toString());
      }
    }
  }


  /**
   * @return Current WCM Mode (may be null)
   */
  protected final WCMMode getWcmMode() {
    return this.wcmMode;
  }

  /**
   * @return Current request
   */
  protected final SlingHttpServletRequest getRequest() {
    return this.request;
  }

}
