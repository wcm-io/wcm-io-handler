/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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
package io.wcm.handler.mediasource.dam.impl.dynamicmedia;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.format.Ratio;
import io.wcm.handler.mediasource.dam.impl.DamContext;

/**
 * Build part of dynamic media/scene7 URL to render renditions.
 */
public final class DynamicMediaPath {

  /**
   * Maximum width/height we support for dynamic media delivery. (should by made configurable)
   */
  private static final long MAX_WIDTH_HEIGHT = 4000;

  private DynamicMediaPath() {
    // static methods only
  }

  /**
   * Build media path for rendering with dynamic media/scene7.
   * @param damContext DAM context objects
   * @param width Width
   * @param height Height
   * @return Media path
   */
  public static @NotNull String build(@NotNull DamContext damContext, long width, long height) {
    return build(damContext, width, height, null, null);
  }

  /**
   * Build media path for rendering with dynamic media/scene7.
   * @param damContext DAM context objects
   * @param width Width
   * @param height Height
   * @param cropDimension Crop dimension
   * @param rotation Rotation
   * @return Media path
   */
  public static @NotNull String build(@NotNull DamContext damContext, long width, long height,
      @Nullable CropDimension cropDimension, @Nullable Integer rotation) {
    Dimension dimension = calcWidthHeight(width, height);

    if (cropDimension != null && cropDimension.isAutoCrop() && rotation == null) {
      // auto-crop applied - check for matching image profile and use predefined cropping preset if match found
      Optional<NamedDimension> smartCroppingDef = getSmartCropDimension(damContext, width, height);
      if (smartCroppingDef.isPresent()) {
        return damContext.getDynamicMediaObject() + "%3A" + smartCroppingDef.get().getName();
      }
    }

    StringBuffer result = new StringBuffer();
    result.append(damContext.getDynamicMediaObject()).append("?");
    if (cropDimension != null) {
      result.append("crop=").append(cropDimension.getCropStringWidthHeight()).append("&");
    }
    if (rotation != null) {
      result.append("rotate=").append(rotation).append("&");
    }
    result.append("wid=").append(dimension.getWidth()).append("&")
        .append("hei=").append(dimension.getHeight()).append("&")
        // cropping/width/height is pre-calculated to fit with original ratio, make sure there are no 1px background lines visible
        .append("fit=stretch");
    return result.toString();
  }

  /**
   * Checks if width or height is bigger than the allowed max. width/height.
   * Reduces both to the max limit keeping aspect ration is required.
   * @param width With
   * @param height Height
   * @return Dimension with capped width/height
   */
  private static Dimension calcWidthHeight(long width, long height) {
    if (width > MAX_WIDTH_HEIGHT) {
      double ratio = Ratio.get(width, height);
      long newWidth = MAX_WIDTH_HEIGHT;
      long newHeight = Math.round(newWidth / ratio);
      return calcWidthHeight(newWidth, newHeight);
    }
    if (height > MAX_WIDTH_HEIGHT) {
      double ratio = Ratio.get(width, height);
      long newHeight = MAX_WIDTH_HEIGHT;
      long newWidth = Math.round(newHeight * ratio);
      return new Dimension(newWidth, newHeight);
    }
    return new Dimension(width, height);
  }

  private static Optional<NamedDimension> getSmartCropDimension(@NotNull DamContext damContext, long width, long height) {
    ImageProfile imageProfile = damContext.getImageProfile();
    if (imageProfile != null) {
      return imageProfile.getSmartCropDefinitions().stream()
          .filter(def -> (def.getWidth() == width) && (def.getHeight() == height))
          .findFirst();
    }
    return Optional.empty();
  }

}
