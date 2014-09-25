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

/**
 * Crop dimension with left, top, width and height as integer.
 */
public final class CropDimension extends Dimension {

  private final int left;
  private final int top;

  /**
   * @param left Left in pixels
   * @param top Top in pixels
   * @param width Width in pixels
   * @param height Height in pixels
   */
  public CropDimension(int left, int top, int width, int height) {
    super(width, height);
    this.left = left;
    this.top = top;
  }

  /**
   * @return Left in pixels
   */
  public int getLeft() {
    return this.left;
  }

  /**
   * @return Top in pixels
   */
  public int getTop() {
    return this.top;
  }

  /**
   * @return Right in pixels
   */
  public int getRight() {
    return getLeft() + getWidth();
  }

  /**
   * @return Bottom in pixels
   */
  public int getBottom() {
    return getTop() + getHeight();
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
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  /**
   * @return Crop string.
   *         Please note: Crop string contains no width/height as 3rd/4th parameter but right, bottom.
   */
  public String getCropString() {
    return getLeft() + "," + getTop() + "," + getRight() + "," + getBottom();
  }

  /**
   * @return Rectangle
   */
  public Rectangle2D getRectangle() {
    return new Rectangle(getLeft(), getTop(), getWidth(), getHeight());
  }

  /**
   * Get crop dimension from crop string.
   * Please note: Crop string contains not width/height as 3rd/4th parameter but right, bottom.
   * @param cropString Cropping string from CQ5 smartimage widget
   * @return Crop dimension instance
   * @throws IllegalArgumentException if crop string syntax is invalid
   */
  public static CropDimension fromCropString(String cropString) {
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
    int x1 = NumberUtils.toInt(parts[0]);
    int y1 = NumberUtils.toInt(parts[1]);
    int x2 = NumberUtils.toInt(parts[2]);
    int y2 = NumberUtils.toInt(parts[3]);
    int width = x2 - x1;
    int height = y2 - y1;
    if (x1 < 0 || y1 < 0 || width <= 0 || height <= 0) {
      throw new IllegalArgumentException("Invalid crop string: '" + cropString + "'.");
    }
    return new CropDimension(x1, y1, width, height);
  }

}
