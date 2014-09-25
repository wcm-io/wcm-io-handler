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
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaArgsType;
import io.wcm.handler.media.MediaMarkupBuilder;
import io.wcm.handler.media.MediaMetadata;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatHandler;
import io.wcm.sling.commons.request.RequestParam;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.commons.DiffInfo;
import com.day.cq.commons.DiffService;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Very basic implementation of {@link MediaMarkupBuilder} for images.
 */
public final class MediaMarkupBuilderUtil {

  private MediaMarkupBuilderUtil() {
    // static methods only
  }

  /**
   * Adds CSS classes that denote the changes to the media element when compared to a different version.
   * If no diff has been requested by the WCM UI, there won't be any changes to the element.
   * @param mediaElement Element to be decorated
   * @param resource Resource pointing to JCR node
   * @param refProperty Name of property for media library item reference. If null, default name is used.
   */
  public static void addDiffDecoration(HtmlElement<?> mediaElement, Resource resource, String refProperty,
      SlingHttpServletRequest request) {

    PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);
    Page currentPage = pageManager.getContainingPage(request.getResource());
    Page resourcePage = pageManager.getContainingPage(resource);

    String versionLabel = RequestParam.get(request, DiffService.REQUEST_PARAM_DIFF_TO);
    // Only try to diff when the resource is contained within the current page as the version number requested always
    // refers to the version history of the current page. So chances a resource on another page doesn't have a matching
    // version, and even if it has, it's comparing apples and oranges
    if (StringUtils.isNotEmpty(versionLabel)
        && currentPage != null && currentPage.equals(resourcePage)) {
      Resource versionedResource = DiffInfo.getVersionedResource(resource, versionLabel);
      if (versionedResource != null) {
        ValueMap currentProperties = resource.getValueMap();
        ValueMap oldProperties = versionedResource.getValueMap();

        String currentMediaRef = currentProperties.get(refProperty, String.class);
        String oldMediaRef = oldProperties.get(refProperty, String.class);
        if (!StringUtils.equals(currentMediaRef, oldMediaRef)) {
          if (StringUtils.isEmpty(currentMediaRef)) {
            mediaElement.addCssClass(MediaNameConstants.CSS_DIFF_REMOVED);
          }
          else if (StringUtils.isEmpty(oldMediaRef)) {
            mediaElement.addCssClass(MediaNameConstants.CSS_DIFF_ADDED);
          }
          else {
            mediaElement.addCssClass(MediaNameConstants.CSS_DIFF_UPDATED);
          }
        }
        else {

          // If the mediaRef itself hasn't changed, check the cropping coordinates
          String currentMediaCrop = currentProperties.get(MediaNameConstants.PN_MEDIA_CROP, String.class);
          String oldMediaCrop = oldProperties.get(MediaNameConstants.PN_MEDIA_CROP, String.class);
          if (!StringUtils.equals(currentMediaCrop, oldMediaCrop)) {
            mediaElement.addCssClass(MediaNameConstants.CSS_DIFF_UPDATED);
          }

          // we also could try to determine here whether it resolves to another rendition
          // or if the timestamp of the rendition has been updated (which would indicate the the binary payload has been
          // changed).
          // This however, is out of scope for this feature right now
        }
      }
      else {
        // The resource didn't exist in the old version at all
        mediaElement.addCssClass(MediaNameConstants.CSS_DIFF_ADDED);
      }
    }
  }

  /**
   * Get dimension from first media format defined in media args. Fall back to dummy min. dimension if none specified.
   * @param mediaMetadata Media metadata
   * @return Dimension
   */
  public static Dimension getMediaformatDimension(MediaMetadata mediaMetadata, MediaFormatHandler mediaFormatHandler) {
    // Create dummy image element to be displayed in Edit mode as placeholder.
    MediaArgsType mediaArgs = mediaMetadata.getMediaReference().getMediaArgs();
    String[] mediaFormats = mediaArgs.getMediaFormats();

    // detect width/height - either from media args, or from first media format
    int width = mediaArgs.getFixedWidth();
    int height = mediaArgs.getFixedHeight();
    if ((width == 0 || height == 0) && mediaFormats != null && mediaFormats.length > 0) {
      String firstMediaFormat = mediaArgs.getMediaFormats()[0];
      Dimension dimension = getMinDimension(firstMediaFormat, mediaFormatHandler);
      if (dimension != null) {
        width = dimension.getWidth();
        height = dimension.getHeight();
      }
    }

    // fallback to min width/height
    if (width == 0) {
      width = MediaMarkupBuilder.DUMMY_MIN_DIMENSION;
    }
    if (height == 0) {
      height = MediaMarkupBuilder.DUMMY_MIN_DIMENSION;
    }

    return new Dimension(width, height);
  }

  /**
   * Get minimum dimensions of given media format.
   * @param mediaFormatName Media format
   * @param mediaFormatHandler Media format handler
   * @return Minimum dimensions
   */
  private static Dimension getMinDimension(String mediaFormatName, MediaFormatHandler mediaFormatHandler) {
    MediaFormat mediaFormat = mediaFormatHandler.getMediaFormat(mediaFormatName);
    if (mediaFormat != null) {
      return mediaFormat.getMinDimension();
    }
    return null;
  }

}
