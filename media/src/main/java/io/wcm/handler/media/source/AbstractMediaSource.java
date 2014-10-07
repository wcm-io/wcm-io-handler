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
package io.wcm.handler.media.source;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaReference;
import io.wcm.handler.media.MediaSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;

/**
 * Common media source functionality
 */
public abstract class AbstractMediaSource implements MediaSource {

  @Override
  public boolean accepts(MediaReference mediaReference) {
    // if an explicit media reference is set check this first
    if (StringUtils.isNotEmpty(mediaReference.getMediaRef())) {
      return accepts(mediaReference.getMediaRef());
    }
    // otherwise check resource which contains media reference properties
    ValueMap props = mediaReference.getResourceProperties();
    // check for matching media source ID in link resource
    String mediaSourceId = props.get(MediaNameConstants.PN_MEDIA_SOURCE, String.class);
    if (StringUtils.isNotEmpty(mediaSourceId)) {
      return StringUtils.equals(mediaSourceId, getId());
    }
    // if not link type is set at all check if link ref attribute contains a valid link
    else {
      String refProperty = StringUtils.defaultString(mediaReference.getRefProperty(), getPrimaryMediaRefProperty());
      String mediaRef = props.get(refProperty, String.class);
      return accepts(mediaRef);
    }
  }

  /**
   * Get media reference path to media library
   * @param mediaReference Media reference
   * @return Path or null if not present
   */
  protected final String getMediaRef(MediaReference mediaReference) {
    if (StringUtils.isNotEmpty(mediaReference.getMediaRef())) {
      return mediaReference.getMediaRef();
    }
    else if (mediaReference.getResource() != null) {
      String refProperty = getMediaRefProperty(mediaReference);
      return mediaReference.getResource().getValueMap().get(refProperty, String.class);
    }
    else {
      return null;
    }
  }

  /**
   * Get property name containing the media reference path
   * @param mediaReference Media reference
   * @return Property name
   */
  protected final String getMediaRefProperty(MediaReference mediaReference) {
    String refProperty = mediaReference.getRefProperty();
    if (StringUtils.isEmpty(refProperty)) {
      refProperty = MediaNameConstants.PN_MEDIA_REF;
    }
    return refProperty;
  }

  /**
   * Get (optional) crop dimensions form resource
   * @param mediaReference Media reference
   * @return Crop dimension or null if not set or invalid
   */
  protected final CropDimension getMediaCropDimension(MediaReference mediaReference) {
    if (mediaReference.getResource() != null) {
      String cropProperty = getMediaCropProperty(mediaReference);
      String cropString = mediaReference.getResource().getValueMap().get(cropProperty, String.class);
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
   * @param mediaReference Media reference
   * @return Property name
   */
  protected final String getMediaCropProperty(MediaReference mediaReference) {
    String cropProperty = mediaReference.getCropProperty();
    if (StringUtils.isEmpty(cropProperty)) {
      cropProperty = MediaNameConstants.PN_MEDIA_CROP;
    }
    return cropProperty;
  }

}
