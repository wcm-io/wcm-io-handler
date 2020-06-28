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

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jdom2.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.ImmutableList;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Span;
import io.wcm.handler.media.imagemap.ImageMapArea;
import io.wcm.handler.media.spi.MediaSource;
import io.wcm.wcm.commons.util.ToStringStyle;

/**
 * Holds information about a media request processed and resolved by {@link MediaHandler}.
 */
@ProviderType
@JsonInclude(Include.NON_EMPTY)
public final class Media {

  private final @NotNull MediaSource mediaSource;
  private @NotNull MediaRequest mediaRequest;
  private HtmlElement<?> element;
  private String url;
  private Asset asset;
  private Collection<Rendition> renditions;
  private CropDimension cropDimension;
  private Integer rotation;
  private List<ImageMapArea> map;
  private MediaInvalidReason mediaInvalidReason;
  private String markup;

  /**
   * @param mediaSource Media source
   * @param mediaRequest Processed media request
   */
  public Media(@NotNull MediaSource mediaSource, @NotNull MediaRequest mediaRequest) {
    this.mediaSource = mediaSource;
    this.mediaRequest = mediaRequest;
  }

  /**
   * @return Media source
   */
  @JsonIgnore
  public @NotNull MediaSource getMediaSource() {
    return this.mediaSource;
  }

  /**
   * @return Media handling request
   */
  @JsonIgnore
  public @NotNull MediaRequest getMediaRequest() {
    return this.mediaRequest;
  }

  /**
   * @param mediaRequest Media handling request
   */
  public void setMediaRequest(@NotNull MediaRequest mediaRequest) {
    this.mediaRequest = mediaRequest;
  }

  /**
   * @return Html element
   */
  @JsonIgnore
  public HtmlElement<?> getElement() {
    return this.element;
  }

  /**
   * @return Media HTML element serialized to string. Returns null if media element is null.
   */
  @JsonIgnore
  public String getMarkup() {
    if (markup == null && this.element != null) {
      if (this.element instanceof Span) {
        // in case of span get inner HTML markup, do not include span element itself
        StringBuilder result = new StringBuilder();
        for (Element child : this.element.getChildren()) {
          result.append(child.toString());
        }
        markup = result.toString();
      }
      else {
        markup = this.element.toString();
      }
    }
    return markup;
  }

  /**
   * @param value Html element
   */
  public void setElement(HtmlElement<?> value) {
    this.element = value;
    this.markup = null;
  }

  /**
   * @return Media URL
   */
  public String getUrl() {
    return this.url;
  }

  /**
   * @param value Media URL
   */
  public void setUrl(String value) {
    this.url = value;
  }

  /**
   * Get media item info that was resolved during media handler processing
   * @return Media item
   */
  public Asset getAsset() {
    return this.asset;
  }

  /**
   * Set media item that was resolved during media handler processing
   * @param asset Media item
   */
  public void setAsset(Asset asset) {
    this.asset = asset;
  }

  /**
   * Get first (and best-match) rendition that was resolved during media handler processing
   * @return Rendition
   */
  @JsonIgnore
  public Rendition getRendition() {
    if (this.renditions == null || this.renditions.isEmpty()) {
      return null;
    }
    return this.renditions.iterator().next();
  }

  /**
   * Get all renditions that were resolved during media handler processing
   * @return Renditions
   */
  public Collection<Rendition> getRenditions() {
    if (this.renditions == null) {
      return ImmutableList.<Rendition>of();
    }
    else {
      return this.renditions;
    }
  }

  /**
   * Set all renditions that was resolved during media handler processing
   * @param renditions Renditions
   */
  public void setRenditions(Collection<Rendition> renditions) {
    this.renditions = renditions;
  }

  /**
   * @return Crop dimensions (optional)
   */
  @JsonIgnore
  public @Nullable CropDimension getCropDimension() {
    return this.cropDimension;
  }

  /**
   * @param cropDimension Crop dimensions (optional)
   */
  public void setCropDimension(@Nullable CropDimension cropDimension) {
    this.cropDimension = cropDimension;
  }

  /**
   * @return Image rotation (optional)
   */
  @JsonIgnore
  public @Nullable Integer getRotation() {
    return this.rotation;
  }

  /**
   * @param rotation Image Rotation (optional)
   */
  public void setRotation(@Nullable Integer rotation) {
    this.rotation = rotation;
  }

  /**
   * @return Image map (optional)
   */
  @JsonIgnore
  public @Nullable List<ImageMapArea> getMap() {
    return this.map;
  }

  /**
   * @param map Image map (optional)
   */
  public void setMap(@Nullable List<ImageMapArea> map) {
    this.map = map;
  }

  /**
   * @return true if link is valid and was resolved successfully
   */
  public boolean isValid() {
    return (mediaInvalidReason == null);
  }

  /**
   * @return Reason why the requested media could not be resolved and is invalid
   */
  @JsonIgnore
  public MediaInvalidReason getMediaInvalidReason() {
    return this.mediaInvalidReason;
  }

  /**
   * @param mediaInvalidReason Reason why the requested media could not be resolved and is invalid
   */
  public void setMediaInvalidReason(MediaInvalidReason mediaInvalidReason) {
    this.mediaInvalidReason = mediaInvalidReason;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_OMIT_NULL_STYLE);
  }

}
