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

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Crop dimension with left, top, width and height as integer.
 */
@ProviderType
public final class CropDimension extends Dimension {

  private final long left;
  private final long top;
  private final boolean autoCrop;

  /**
   * @param left Left in pixels
   * @param top Top in pixels
   * @param width Width in pixels
   * @param height Height in pixels
   */
  public CropDimension(long left, long top, long width, long height) {
    this(left, top, width, height, false);
  }

  /**
   * @param left Left in pixels
   * @param top Top in pixels
   * @param width Width in pixels
   * @param height Height in pixels
   * @param autoCrop Mark this dimension as auto-cropped
   */
  public CropDimension(long left, long top, long width, long height, boolean autoCrop) {
    super(width, height);
    this.left = left;
    this.top = top;
    this.autoCrop = autoCrop;
  }

  /**
   * @return Left in pixels
   */
  public long getLeft() {
    return this.left;
  }

  /**
   * @return Top in pixels
   */
  public long getTop() {
    return this.top;
  }

  /**
   * @return Right in pixels
   */
  public long getRight() {
    return getLeft() + getWidth();
  }

  /**
   * @return Bottom in pixels
   */
  public long getBottom() {
    return getTop() + getHeight();
  }

  /**
   * @return true if is dimenions is marked as auto-cropped
   */
  public boolean isAutoCrop() {
    return this.autoCrop;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
        .append("left", getLeft())
        .append("top", getTop())
        .append("width", getWidth())
        .append("height", getHeight())
        .build();
  }

  /**
   * @return Crop string with left,top,right,bottom.
   */
  public @NotNull String getCropString() {
    return getLeft() + "," + getTop() + "," + getRight() + "," + getBottom();
  }

  /**
   * @return Crop string with left,top,width,height.
   */
  public @NotNull String getCropStringWidthHeight() {
    return getLeft() + "," + getTop() + "," + getWidth() + "," + getHeight();
  }

  /**
   * @return Rectangle
   */
  public @NotNull Rectangle2D getRectangle() {
    return new Rectangle((int)getLeft(), (int)getTop(), (int)getWidth(), (int)getHeight());
  }

  /**
   * Get crop dimension from crop string.
   * Please note: Crop string contains not width/height as 3rd/4th parameter but right, bottom.
   * @param cropString Cropping string from AEM inplace editor
   * @return Crop dimension instance
   * @throws IllegalArgumentException if crop string syntax is invalid
   */
  public static @NotNull CropDimension fromCropString(@NotNull String cropString) {
    if (StringUtils.isEmpty(cropString)) {
      throw new IllegalArgumentException("Invalid crop string: '" + cropString + "'.");
    }

    // strip off optional size parameter after "/"
    String crop = cropString;
    if (StringUtils.contains(crop, "/")) {
      crop = StringUtils.substringBefore(crop, "/");
    }

    String[] parts = StringUtils.split(crop, ",");
    if (parts.length != 4) {
      throw new IllegalArgumentException("Invalid crop string: '" + cropString + "'.");
    }
    long x1 = NumberUtils.toLong(parts[0]);
    long y1 = NumberUtils.toLong(parts[1]);
    long x2 = NumberUtils.toLong(parts[2]);
    long y2 = NumberUtils.toLong(parts[3]);
    long width = x2 - x1;
    long height = y2 - y1;
    if (x1 < 0 || y1 < 0 || width <= 0 || height <= 0) {
      throw new IllegalArgumentException("Invalid crop string: '" + cropString + "'.");
    }
    return new CropDimension(x1, y1, width, height);
  }

}
