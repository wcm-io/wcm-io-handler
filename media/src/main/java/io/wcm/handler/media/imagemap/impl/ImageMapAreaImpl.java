/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.handler.media.imagemap.impl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.imagemap.ImageMapArea;

/**
 * Implementation of {@link ImageMapArea}.
 * @param <T> Link result type
 */
public class ImageMapAreaImpl<T> implements ImageMapArea<T> {

  private final String shape;
  private final String coordinates;
  private final String relativeCoordinates;
  private final @Nullable T link;
  private final String linkUrl;
  private final String linkWindowTarget;
  private final String altText;

  /**
   * @param shape Shape
   * @param coordinates coordinates
   * @param relativeCoordinates Relative coordinates
   * @param linkUrl href
   * @param linkWindowTarget Target
   * @param altText Alt. text
   */
  public ImageMapAreaImpl(@NotNull String shape, @NotNull String coordinates, @Nullable String relativeCoordinates,
      @Nullable T link, @NotNull String linkUrl, @Nullable String linkWindowTarget, @Nullable String altText) {
    this.shape = shape;
    this.coordinates = coordinates;
    this.relativeCoordinates = relativeCoordinates;
    this.link = link;
    this.linkUrl = linkUrl;
    this.linkWindowTarget = linkWindowTarget;
    this.altText = altText;
  }

  @Override
  public @NotNull String getShape() {
    return shape;
  }

  @Override
  public @NotNull String getCoordinates() {
    return coordinates;
  }

  @Override
  public @Nullable String getRelativeCoordinates() {
    return relativeCoordinates;
  }

  @Override
  public @NotNull String getLinkUrl() {
    return linkUrl;
  }

  @Override
  public @Nullable String getLinkWindowTarget() {
    return linkWindowTarget;
  }

  @Override
  public @Nullable String getAltText() {
    return altText;
  }

  @Override
  public @Nullable T getLink() {
    return link;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, "link");
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, "link");
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
