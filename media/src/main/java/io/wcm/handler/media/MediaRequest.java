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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.wcm.commons.util.ToStringStyle;

/**
 * Holds all properties that are part of a media handling request.
 */
@ProviderType
public final class MediaRequest {

  private final Resource resource;
  private final String mediaRef;
  private final MediaArgs mediaArgs;
  private final String refProperty;
  private final String cropProperty;
  private final String rotationProperty;

  private ValueMap resourceProperties;

  /**
   * @param resource Resource containing reference to media asset
   * @param mediaArgs Additional arguments affection media resolving
   */
  public MediaRequest(@NotNull Resource resource, @Nullable MediaArgs mediaArgs) {
    this(resource, null, mediaArgs, null, null, null);
  }

  /**
   * @param mediaRef Reference to media item
   * @param mediaArgs Additional arguments affection media resolving
   */
  public MediaRequest(@NotNull String mediaRef, @Nullable MediaArgs mediaArgs) {
    this(null, mediaRef, mediaArgs, null, null, null);
  }

  /**
   * @param resource Resource containing reference to media asset
   * @param mediaRef Reference to media item
   * @param mediaArgs Additional arguments affection media resolving
   * @param refProperty Name of the property from which the media request is read
   * @param cropProperty Name of the property which contains the cropping parameters
   * @deprecated Use {@link #MediaRequest(Resource, String, MediaArgs, String, String, String)}
   */
  @Deprecated
  public MediaRequest(@Nullable Resource resource, @Nullable String mediaRef, @Nullable MediaArgs mediaArgs,
      @Nullable String refProperty, @Nullable String cropProperty) {
    this(resource, mediaRef, mediaArgs, refProperty, cropProperty, null);
  }

  /**
   * @param resource Resource containing reference to media asset
   * @param mediaRef Reference to media item
   * @param mediaArgs Additional arguments affection media resolving
   * @param refProperty Name of the property from which the media request is read
   * @param cropProperty Name of the property which contains the cropping parameters
   * @param rotationProperty Name of the property which contains the rotation parameter
   */
  public MediaRequest(@Nullable Resource resource, @Nullable String mediaRef, @Nullable MediaArgs mediaArgs,
      @Nullable String refProperty, @Nullable String cropProperty, @Nullable String rotationProperty) {
    this.resource = resource;
    this.mediaRef = mediaRef;
    this.mediaArgs = mediaArgs != null ? mediaArgs : new MediaArgs();
    this.refProperty = refProperty;
    this.cropProperty = cropProperty;
    this.rotationProperty = rotationProperty;

    // validate parameters
    if (this.resource != null && this.mediaRef != null) {
      throw new IllegalArgumentException("Set resource or media ref, not both.");
    }
  }

  /**
   * @return Resource containing reference to media asset
   */
  public @Nullable Resource getResource() {
    return this.resource;
  }

  /**
   * @return Reference to media item
   */
  public @Nullable String getMediaRef() {
    return this.mediaRef;
  }

  /**
   * @return Additional arguments affection media resolving
   */
  public @NotNull MediaArgs getMediaArgs() {
    return this.mediaArgs;
  }

  /**
   * @return Name of the property from which the media request is read
   */
  public @Nullable String getRefProperty() {
    return this.refProperty;
  }

  /**
   * @return Name of the property which contains the cropping parameters
   */
  public @Nullable String getCropProperty() {
    return this.cropProperty;
  }

  /**
   * @return Name of the property which contains the rotation parameter
   */
  public @Nullable String getRotationProperty() {
    return this.rotationProperty;
  }

  /**
   * @return Properties from resource containing target link. The value map is a copy
   *         of the original map so it is safe to change the property values contained in the map.
   */
  public @NotNull ValueMap getResourceProperties() {
    if (this.resourceProperties == null) {
      // create a copy of the original map
      this.resourceProperties = new ValueMapDecorator(new HashMap<String, Object>());
      if (this.resource != null) {
        this.resourceProperties.putAll(resource.getValueMap());
      }
    }
    return this.resourceProperties;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_OMIT_NULL_STYLE);
  }

}
