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
package io.wcm.handler.media.spi;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.annotation.versioning.ConsumerType;

import com.google.common.collect.ImmutableList;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.Asset;
import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.format.MediaFormat;

/**
 * Via {@link MediaSource} OSGi services applications can define additional media sources supported by
 * {@link MediaHandler}.
 */
@ConsumerType
public abstract class MediaSource {

  /**
   * @return Media source ID
   */
  public abstract String getId();

  /**
   * @return Name of the property in which the primary media request is stored
   */
  public abstract String getPrimaryMediaRefProperty();

  /**
   * Checks whether a media request can be handled by this media source
   * @param mediaRequest Media request
   * @return true if this media source can handle the given media request
   */
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
   * Checks whether a media request string can be handled by this media source
   * @param mediaRef Media request string
   * @return true if this media source can handle the given media request
   */
  public abstract boolean accepts(String mediaRef);

  /**
   * Resolves a media request
   * @param media Media metadata
   * @return Resolved media metadata. Never null.
   */
  public abstract Media resolveMedia(Media media);

  /**
   * Create a ExtJS drop area for given HTML element to enable drag and drop of media library items
   * from content finder to this element.
   * @param element Html element
   * @param mediaRequest Media request to detect media args and property names
   */
  public abstract void enableMediaDrop(HtmlElement<?> element, MediaRequest mediaRequest);

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

  /**
   * Resolves single rendition (or multiple renditions if {@link MediaArgs#isMediaFormatsMandatory()} is true
   * and sets the resolved rendition and the URL of the first (best-matching) rendition in the media object.
   * @param media Media object
   * @param asset Asset
   * @param mediaArgs Media args
   * @return true if all requested renditions could be resolved (at least one or all if
   *         {@link MediaArgs#isMediaFormatsMandatory()} was set to true)
   */
  protected final boolean resolveRenditions(Media media, Asset asset, MediaArgs mediaArgs) {
    if (mediaArgs.getMediaFormats() != null && mediaArgs.getMediaFormats().length > 1 && mediaArgs.isMediaFormatsMandatory()) {
      return resolveAllMandatoryRenditions(media, asset, mediaArgs);
    }
    else {
      return resolveFirstMatchRenditions(media, asset, mediaArgs);
    }
  }

  /**
   * Check if a matching rendition is found for any of the provided media formats and other media args.
   * The first match is returned.
   * @param media Media
   * @param asset Asset
   * @param mediaArgs Media args
   * @return true if a rendition was found
   */
  private boolean resolveFirstMatchRenditions(Media media, Asset asset, MediaArgs mediaArgs) {
    Rendition rendition = asset.getRendition(mediaArgs);
    if (rendition != null) {
      media.setRenditions(ImmutableList.of(rendition));
      media.setUrl(rendition.getUrl());
      return true;
    }
    return false;
  }

  /**
   * Iterates over all defined media format and tries to find a matching rendition for each of them
   * in combination with the other media args.
   * @param media Media
   * @param asset Asset
   * @param mediaArgs Media args
   * @return true if for *all* media formats a rendition could be found.
   */
  private boolean resolveAllMandatoryRenditions(Media media, Asset asset, MediaArgs mediaArgs) {
    boolean allResolved = true;
    List<Rendition> resolvedRenditions = new ArrayList<>();
    for (MediaFormat mediaFormat : mediaArgs.getMediaFormats()) {
      MediaArgs renditionMediaArgs = mediaArgs.clone();
      renditionMediaArgs.mediaFormat(mediaFormat);
      renditionMediaArgs.mediaFormatsMandatory(false);
      Rendition rendition = asset.getRendition(renditionMediaArgs);
      if (rendition != null) {
        resolvedRenditions.add(rendition);
      }
      else {
        allResolved = false;
      }
    }
    media.setRenditions(resolvedRenditions);
    if (!resolvedRenditions.isEmpty()) {
      media.setUrl(resolvedRenditions.get(0).getUrl());
    }
    return allResolved;
  }

}
