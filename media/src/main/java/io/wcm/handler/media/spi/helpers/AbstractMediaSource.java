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
package io.wcm.handler.media.spi.helpers;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.spi.MediaSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;

/**
 * Common media source functionality
 */
public abstract class AbstractMediaSource implements MediaSource {

  @Override
  public boolean accepts(MediaRequest mediaRequest) {
    // if an explicit media request is set check this first
    if (StringUtils.isNotEmpty(mediaRequest.getMediaRef())) {
      return accepts(mediaRequest.getMediaRef());
    }
    // otherwise check resource which contains media request properties
    ValueMap props = mediaRequest.getResourceProperties();
    // check for matching media source ID in link resource
    String mediaSourceId = props.get(MediaNameConstants.PN_MEDIA_SOURCE, String.class);
    if (StringUtils.isNotEmpty(mediaSourceId)) {
      return StringUtils.equals(mediaSourceId, getId());
    }
    // if not link type is set at all check if link ref attribute contains a valid link
    else {
      String refProperty = StringUtils.defaultString(mediaRequest.getRefProperty(), getPrimaryMediaRefProperty());
      String mediaRef = props.get(refProperty, String.class);
      return accepts(mediaRef);
    }
  }

  /**
   * Get media request path to media library
   * @param mediaRequest Media request
   * @return Path or null if not present
   */
  protected final String getMediaRef(MediaRequest mediaRequest) {
    if (StringUtils.isNotEmpty(mediaRequest.getMediaRef())) {
      return mediaRequest.getMediaRef();
    }
    else if (mediaRequest.getResource() != null) {
      String refProperty = getMediaRefProperty(mediaRequest);
      return mediaRequest.getResource().getValueMap().get(refProperty, String.class);
    }
    else {
      return null;
    }
  }

  /**
   * Get property name containing the media request path
   * @param mediaRequest Media request
   * @return Property name
   */
  protected final String getMediaRefProperty(MediaRequest mediaRequest) {
    String refProperty = mediaRequest.getRefProperty();
    if (StringUtils.isEmpty(refProperty)) {
      refProperty = MediaNameConstants.PN_MEDIA_REF;
    }
    return refProperty;
  }

  /**
   * Get (optional) crop dimensions form resource
   * @param mediaRequest Media request
   * @return Crop dimension or null if not set or invalid
   */
  protected final CropDimension getMediaCropDimension(MediaRequest mediaRequest) {
    if (mediaRequest.getResource() != null) {
      String cropProperty = getMediaCropProperty(mediaRequest);
      String cropString = mediaRequest.getResource().getValueMap().get(cropProperty, String.class);
      if (StringUtils.isNotEmpty(cropString)) {
        try {
          return CropDimension.fromCropString(cropString);
        }
        catch (IllegalArgumentException ex) {
          // ignore
        }
      }
    }
    return null;
  }

  /**
   * Get property name containing the cropping parameters
   * @param mediaRequest Media request
   * @return Property name
   */
  protected final String getMediaCropProperty(MediaRequest mediaRequest) {
    String cropProperty = mediaRequest.getCropProperty();
    if (StringUtils.isEmpty(cropProperty)) {
      cropProperty = MediaNameConstants.PN_MEDIA_CROP;
    }
    return cropProperty;
  }

}
