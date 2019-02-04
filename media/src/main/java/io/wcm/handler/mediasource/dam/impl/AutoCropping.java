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

import static io.wcm.handler.mediasource.dam.impl.DamRendition.DEFAULT_WEB_RENDITION_PATTERN;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.format.MediaFormat;

/**
 * Helper class for calculating crop dimensions for auto-cropping.
 */
class AutoCropping {

  private final Asset asset;
  private final MediaArgs mediaArgs;

  AutoCropping(Asset asset, MediaArgs mediaArgs) {
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

  private CropDimension calculateAutoCropDimension(MediaFormat mediaFormat) {
    double ratio = mediaFormat.getRatio();
    if (ratio > 0) {
      RenditionMetadata rendition = getWebEnabledRendition();
      if (rendition != null && rendition.getWidth() > 0 && rendition.getHeight() > 0) {
        return calculateAutoCropDimension(rendition.getWidth(), rendition.getHeight(), ratio);
      }
    }
    return null;
  }

  static CropDimension calculateAutoCropDimension(long givenWidth, long givenHeight, double expectedRatio) {
    double givenRatio = (double)givenWidth / (double)givenHeight;
    long width;
    long height;
    long top;
    long left;
    if (givenRatio > expectedRatio) {
      width = Math.round(givenHeight * expectedRatio);
      height = givenHeight;
      top = 0;
      left = Math.round(((double)givenWidth - (double)width) / 2d);
    }
    else {
      width = givenWidth;
      height = Math.round(givenWidth / expectedRatio);
      top = Math.round(((double)givenHeight - (double)height) / 2d);
      left = 0;
    }
    return new CropDimension(left, top, width, height);
  }

  private RenditionMetadata getWebEnabledRendition() {
    for (Rendition rendition : asset.getRenditions()) {
      if (DEFAULT_WEB_RENDITION_PATTERN.matcher(rendition.getName()).matches()) {
        return new RenditionMetadata(rendition);
      }
    }
    return null;
  }

}
