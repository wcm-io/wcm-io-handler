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
package io.wcm.handler.mediasource.dam.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.impl.ImageTransformation;
import io.wcm.handler.mediasource.dam.AssetRendition;

/**
 * Helper class for calculating crop dimensions for auto-cropping.
 */
class DamAutoCropping {

  private final Asset asset;
  private final MediaArgs mediaArgs;

  DamAutoCropping(@NotNull Asset asset, @NotNull MediaArgs mediaArgs) {
    this.asset = asset;
    this.mediaArgs = mediaArgs;
  }

  public List<CropDimension> calculateAutoCropDimensions() {
    Stream<MediaFormat> mediaFormats = Arrays.stream(
        ObjectUtils.defaultIfNull(mediaArgs.getMediaFormats(), new MediaFormat[0]));
    return mediaFormats
        .map(this::calculateAutoCropDimension)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private CropDimension calculateAutoCropDimension(@NotNull MediaFormat mediaFormat) {
    double ratio = mediaFormat.getRatio();
    if (ratio > 0) {
      RenditionMetadata rendition = DamAutoCropping.getWebRenditionForCropping(asset);
      if (rendition != null && rendition.getWidth() > 0 && rendition.getHeight() > 0) {
        return ImageTransformation.calculateAutoCropDimension(rendition.getWidth(), rendition.getHeight(), ratio);
      }
    }
    return null;
  }

  /**
   * Get web first rendition for asset.
   * This is the same logic as implemented in
   * <code>/libs/cq/gui/components/authoring/editors/clientlibs/core/inlineediting/js/ImageEditor.js</code>.
   * @param asset Asset
   * @return Web rendition or null if none found
   */
  public static @Nullable RenditionMetadata getWebRenditionForCropping(@NotNull Asset asset) {
    return asset.getRenditions().stream()
        .filter(AssetRendition::isWebRendition)
        .findFirst()
        .map(rendition -> new RenditionMetadata(rendition))
        .orElse(null);
  }

}
