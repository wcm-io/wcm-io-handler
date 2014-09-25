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

import java.util.HashMap;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

/**
 * Defines a media reference
 */
public final class MediaReference implements Cloneable {

  private String mediaRef;
  private MediaArgsType mediaArgs;
  private Resource resource;
  private ValueMap resourceProperties;
  private String refProperty;
  private String cropProperty;

  /**
   * @param mediaRef Path to media library item
   * @param mediaArgs Affects rendition selection, media URL and markup generation
   */
  public MediaReference(String mediaRef, MediaArgsType mediaArgs) {
    this.mediaRef = mediaRef;
    this.mediaArgs = mediaArgs;
    this.resource = null;
    this.refProperty = null;
    this.cropProperty = null;
  }

  /**
   * @param resource Resource containing reference to media library item and optionally further properties like Alt. text
   *          If the resource contains an inline media element directly within the page this is used instead of a media reference.
   * @param refProperty Defines the name of the property from which the media reference is read, or node name for inline media.
   * @param mediaArgs Affects rendition selection, media URL and markup generation
   */
  public MediaReference(Resource resource, String refProperty, MediaArgsType mediaArgs) {
    this.mediaRef = null;
    this.mediaArgs = mediaArgs;
    this.resource = resource;
    this.refProperty = refProperty;
    this.cropProperty = null;
  }

  /**
   * @param resource Resource containing reference to media library item and optionally further properties like Alt. text
   *          If the resource contains an inline media element directly within the page this is used instead of a media reference.
   * @param refProperty Defines the name of the property from which the media reference is read, or node name for inline media.
   * @param cropProperty Defines the name of the property which contains the (optional) cropping parameters
   * @param mediaArgs Affects rendition selection, media URL and markup generation
   */
  public MediaReference(Resource resource, String refProperty, String cropProperty, MediaArgsType mediaArgs) {
    this.mediaRef = null;
    this.mediaArgs = mediaArgs;
    this.resource = resource;
    this.refProperty = refProperty;
    this.cropProperty = cropProperty;
  }

  /**
   * @return Path to media library item
   */
  public String getMediaRef() {
    return this.mediaRef;
  }

  /**
   * @param mediaRef Path to media library item
   */
  public void setMediaRef(String mediaRef) {
    this.mediaRef = mediaRef;
  }

  /**
   * @return Affects rendition selection, media URL and markup generation
   */
  public MediaArgsType getMediaArgs() {
    return this.mediaArgs;
  }

  /**
   * @param mediaArgs Affects rendition selection, media URL and markup generation
   */
  public void setMediaArgs(MediaArgsType mediaArgs) {
    this.mediaArgs = mediaArgs;
  }

  /**
   * @return Resource containing reference to media library item and optionally further properties like Alt. text
   *         If the resource contains an inline media element directly within the page this is used instead of a media reference.
   */
  public Resource getResource() {
    return this.resource;
  }

  /**
   * @param resource Resource containing reference to media library item and optionally further properties like Alt.
   *          text. If the resource contains an inline media element directly within the page this is used instead of a
   *          media reference.
   */
  public void setResource(Resource resource) {
    this.resource = resource;
    // clear "cached" resource properties map as well
    this.resourceProperties = null;
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
        ValueMap props = this.resource.getValueMap();
        if (props != null) {
          this.resourceProperties.putAll(props);
        }
      }
    }
    return this.resourceProperties;
  }

  /**
   * @return Defines the name of the property from which the media reference is read, or node name for inline media.
   */
  public String getRefProperty() {
    return this.refProperty;
  }

  /**
   * @param refProperty Defines the name of the property from which the media reference is read, or node name for inline media.
   */
  public void setRefProperty(String refProperty) {
    this.refProperty = refProperty;
  }

  /**
   * @return Defines the name of the property which contains the (optional) cropping parameters
   */
  public String getCropProperty() {
    return this.cropProperty;
  }

  /**
   * @param cropProperty Defines the name of the property which contains the (optional) cropping parameters
   */
  public void setCropProperty(String cropProperty) {
    this.cropProperty = cropProperty;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    MediaReference clone = (MediaReference)super.clone();

    // explicitly clone MediaArgs contents (primitive properties are properly cloned by super.clone()
    if (getMediaArgs() != null) {
      clone.setMediaArgs((MediaArgsType)getMediaArgs().clone());
    }

    return clone;
  }

}
