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
package io.wcm.handler.media.impl.ipeconfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatBuilder;
import io.wcm.handler.media.format.MediaFormatHandler;
import io.wcm.handler.media.format.Ratio;

/**
 * Helper class to build list of media formats for cropping in the AEM image editor
 * based on media formats of the current request/the application.
 */
public class CroppingRatios {

  /**
   * Special media format name to also allow "free cropping" without constraints.
   */
  public static final MediaFormat MEDIAFORMAT_FREE_CROP = MediaFormatBuilder.create("__FREE_CROP__")
      .label("Free Hand")
      .build();

  private final MediaFormatHandler mediaFormatHandler;

  /**
   * @param mediaFormatHandler Media format handler
   */
  public CroppingRatios(@NotNull MediaFormatHandler mediaFormatHandler) {
    this.mediaFormatHandler = mediaFormatHandler;
  }

  /**
   * Get media formats that should be offered as cropping ratios when using the AEM image editor.
   * This eliminates duplicates (with same ratio) and media formats not suited for cropping.
   * @param mediaRequest Media request
   * @return Media format names
   */
  public @NotNull Set<String> getMediaFormatsForCropping(@NotNull MediaRequest mediaRequest) {

    // if the current request has media formats, use them directly
    MediaFormat[] requestMediaFormats = mediaRequest.getMediaArgs().getMediaFormats();
    if (requestMediaFormats != null) {
      return toNameSet(Arrays.stream(mediaRequest.getMediaArgs().getMediaFormats()));
    }

    // otherwise get all image media formats with ratios, filter out duplicates with same ratios
    // including the special free cropping ratio option
    return toNameSet(getUniqueRatios().stream());
  }

  private Set<MediaFormat> getUniqueRatios() {
    Set<MediaFormat> all = mediaFormatHandler.getMediaFormats();
    Set<MediaFormat> result = new HashSet<>();

    // add special free cropping action
    result.add(MEDIAFORMAT_FREE_CROP);

    for (MediaFormat mediaFormat : all) {

      // skip non-image and non-ratio media formats
      if (!(mediaFormat.isImage() && mediaFormat.hasRatio())) {
        continue;
      }

      // check if a media format with same ratio already exists
      // replace existing media format if the new one is bigger
      // prefer media formats without fixed dimension
      MediaFormat existingMediaFormat = getExistingMediaFormatWithRatio(result, mediaFormat.getRatio());
      if (existingMediaFormat != null) {
        if ((hasDimension(existingMediaFormat) && !hasDimension(mediaFormat))
            || isBigger(mediaFormat, existingMediaFormat)) {
          result.remove(existingMediaFormat);
          result.add(mediaFormat);
        }
      }
      else {
        result.add(mediaFormat);
      }
    }

    return result;
  }

  private @Nullable MediaFormat getExistingMediaFormatWithRatio(Set<MediaFormat> all, double ratio) {
    return all.stream()
        .filter(mediaFormat -> Ratio.matches(mediaFormat.getRatio(), ratio))
        .findFirst().orElse(null);
  }

  private boolean isBigger(@NotNull MediaFormat mediaFormat1, @NotNull MediaFormat mediaFormat2) {
    return mediaFormat1.getEffectiveMinWidth() > mediaFormat2.getEffectiveMinWidth()
        && mediaFormat2.getEffectiveMinWidth() > 0;
  }

  private boolean hasDimension(@NotNull MediaFormat mediaFormat) {
    return (mediaFormat.getEffectiveMinWidth() > 0 || mediaFormat.getEffectiveMaxWidth() > 0)
        && (mediaFormat.getEffectiveMinHeight() > 0 || mediaFormat.getEffectiveMaxHeight() > 0);
  }

  @SuppressWarnings("null")
  private @NotNull Set<String> toNameSet(@NotNull Stream<MediaFormat> mediaFormats) {
    return mediaFormats
        .map(MediaFormat::getName)
        .collect(Collectors.toSet());
  }

}
