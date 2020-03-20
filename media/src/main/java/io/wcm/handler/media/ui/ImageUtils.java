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
package io.wcm.handler.media.ui;

import java.util.Arrays;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaBuilder;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatHandler;

final class ImageUtils {

  private static final Logger LOG = LoggerFactory.getLogger(ImageUtils.class);

  private ImageUtils() {
    // private constructor as this is a utils class
  }

  /**
   * Applies picture sources to the given {@link MediaBuilder}.
   *
   * @param mediaFormatHandler {@link MediaFormatHandler} to fetch media formats from
   * @param builder            {@link MediaBuilder} to apply the picture sources to
   * @param mediaFormatNames   media formats for the picture source elements
   * @param medias             media expressions for the picture source elements
   * @param widths             widths for the picture source elements
   */
  public static void applyPictureSources(MediaFormatHandler mediaFormatHandler, MediaBuilder builder,
      String[] mediaFormatNames, String[] medias, String[] widths) {
    for (int i = 0; i < mediaFormatNames.length && i < medias.length && i < widths.length; i++) {
      MediaFormat mediaFormat = mediaFormatHandler.getMediaFormat(mediaFormatNames[i]);
      if (mediaFormat != null) {
        String media = medias[i];
        long[] widthsArray = toWidthsArray(widths[i]);
        if (widthsArray.length > 0) {

          PictureSource pictureSource = new PictureSource(mediaFormat);
          pictureSource.widths(widthsArray);
          if (StringUtils.isNotBlank(media)) {
            pictureSource.media(media);
          }
          builder.pictureSource(pictureSource);
        }
      }
      else {
        LOG.warn("Ignoring invalid media format: {}", mediaFormatNames[i]);
      }
    }
  }

  /**
   * Convert widths string to long array and ignore invalid numbers, sort values descending.
   *
   * @param widths Widths string
   * @return Widths array
   */
  public static long[] toWidthsArray(String widths) {
    if (StringUtils.isBlank(widths)) {
      return new long[0];
    }
    return Arrays.stream(StringUtils.split(widths, ","))
        .map(NumberUtils::toLong)
        .filter(width -> width > 0)
        .sorted((l1, l2) -> Long.compare(l2, l1))
        .mapToLong(Long::longValue)
        .toArray();
  }

  /**
   * Convert width options string to MediaArgs.WidthOption array and ignore invalid numbers and invalid format, sort
   * values descending.
   * @param widthOptions Width options string
   * @return Widths array which is empty in case given widthOptions is blank
   */
  public static MediaArgs.WidthOption[] toWidthOptionArray(String widthOptions) {
    if (StringUtils.isBlank(widthOptions)) {
      return new MediaArgs.WidthOption[0];
    }
    return Arrays.stream(StringUtils.split(widthOptions, ","))
        .filter(StringUtils::isNotBlank)
        .map(widthOption -> StringUtils.split(widthOption, ":"))
        .filter(widthOptionArray -> widthOptionArray.length == 2)
        .map(widthOptionArray -> new MediaArgs.WidthOption(NumberUtils.toLong(widthOptionArray[0]), BooleanUtils.toBoolean(widthOptionArray[1])))
        .filter(widthOption -> widthOption.getWidth() > 0)
        .sorted((wo1, wo2) -> Long.compare(wo2.getWidth(), wo1.getWidth()))
        .toArray(MediaArgs.WidthOption[]::new);
  }

}
