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
package io.wcm.handler.media;

import io.wcm.handler.media.args.MediaArgs;
import io.wcm.handler.media.args.MediaArgsType;

import java.util.HashMap;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import aQute.bnd.annotation.ProviderType;

/**
 * Holds all properties that are part of a media handling request.
 */
@ProviderType
public final class MediaRequest {

  private final Resource resource;
  private final String mediaRef;
  private final MediaArgsType mediaArgs;
  private final String refProperty;
  private final String cropProperty;

  private ValueMap resourceProperties;

  /**
   * @param resource Resource containing reference to media asset
   * @param mediaArgs Additional arguments affection media resolving
   */
  public MediaRequest(Resource resource, MediaArgsType mediaArgs) {
    this(resource, null, mediaArgs, null, null);
  }

  /**
   * @param mediaRef Reference to media item
   * @param mediaArgs Additional arguments affection media resolving
   */
  public MediaRequest(String mediaRef, MediaArgsType mediaArgs) {
    this(null, mediaRef, mediaArgs, null, null);
  }

  /**
   * @param resource Resource containing reference to media asset
   * @param mediaRef Reference to media item
   * @param mediaArgs Additional arguments affection media resolving
   * @param refProperty Name of the property from which the media request is read
   * @param cropProperty Name of the property which contains the cropping parameters
   */
  public MediaRequest(Resource resource, String mediaRef, MediaArgsType mediaArgs,
      String refProperty, String cropProperty) {
    this.resource = resource;
    this.mediaRef = mediaRef;
    this.mediaArgs = mediaArgs != null ? mediaArgs : new MediaArgs();
    this.refProperty = refProperty;
    this.cropProperty = cropProperty;

    // validate parameters
    if (this.resource != null && this.mediaRef != null) {
      throw new IllegalArgumentException("Set resource or media ref, not both.");
    }
  }

  /**
   * @return Resource containing reference to media asset
   */
  public Resource getResource() {
    return this.resource;
  }

  /**
   * @return Reference to media item
   */
  public String getMediaRef() {
    return this.mediaRef;
  }

  /**
   * @return Additional arguments affection media resolving
   */
  public MediaArgsType getMediaArgs() {
    return this.mediaArgs;
  }

  /**
   * @return Name of the property from which the media request is read
   */
  public String getRefProperty() {
    return this.refProperty;
  }

  /**
   * @return Name of the property which contains the cropping parameters
   */
  public String getCropProperty() {
    return this.cropProperty;
  }

  /**
   * @return Properties from resource containing target link. The value map is a copy
   *         of the original map so it is safe to change the property values contained in the map.
   */
  public ValueMap getResourceProperties() {
    if (this.resourceProperties == null) {
      // create a copy of the original map
      this.resourceProperties = new ValueMapDecorator(new HashMap<String, Object>());
      if (this.resource != null) {
        this.resourceProperties.putAll(resource.getValueMap());
      }
    }
    return this.resourceProperties;
  }

}
