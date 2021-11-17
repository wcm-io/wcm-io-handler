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
package io.wcm.handler.media.impl;

import static io.wcm.handler.media.MediaNameConstants.MEDIAFORMAT_PROP_PARENT_MEDIA_FORMAT;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaArgs.ImageSizes;
import io.wcm.handler.media.MediaArgs.MediaFormatOption;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaArgs.WidthOption;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatBuilder;
import io.wcm.handler.media.format.MediaFormatHandler;

/**
 * Resolves media formats before starting the media handler processing.
 */
final class MediaFormatResolver {

  private final MediaFormatHandler mediaFormatHandler;

  private static final Logger log = LoggerFactory.getLogger(MediaFormatResolver.class);

  static final String MEDIAFORMAT_NAME_SEPARATOR = "___";

  MediaFormatResolver(MediaFormatHandler mediaFormatHandler) {
    this.mediaFormatHandler = mediaFormatHandler;
  }

  /**
   * Resolve media format names and responsive media formats.
   * @param mediaArgs Media args
   * @return true if resolution was successful.
   */
  public boolean resolve(MediaArgs mediaArgs) {
    return resolveMediaFormatOptionsByNames(mediaArgs)
        && resolvePictureSourcesByNames(mediaArgs)
        && addResponsiveImageMediaFormats(mediaArgs);
  }

  /**
   * Resolve media format names to media formats in media options so all downstream logic has only to handle the
   * resolved media formats.
   * @param mediaArgs Media args
   * @return true if resolution was successful.
   */
  private boolean resolveMediaFormatOptionsByNames(MediaArgs mediaArgs) {
    MediaFormatOption[] mediaFormatOptions = mediaArgs.getMediaFormatOptions();
    if (mediaFormatOptions == null) {
      return true;
    }

    // resolve media format options that have only a name set
    boolean resolutionSuccessful = true;
    for (int i = 0; i < mediaFormatOptions.length; i++) {
      MediaFormatOption option = mediaFormatOptions[i];
      String mediaFormatName = option.getMediaFormatName();
      if (option.getMediaFormat() == null && mediaFormatName != null) {
        MediaFormat mediaFormat = mediaFormatHandler.getMediaFormat(mediaFormatName);
        if (mediaFormat == null) {
          log.warn("Media format name '{}' is invalid.", option.getMediaFormatName());
          resolutionSuccessful = false;
        }
        mediaFormatOptions[i] = new MediaFormatOption(mediaFormat, option.isMandatory());
      }
    }
    mediaArgs.mediaFormatOptions(mediaFormatOptions);

    return resolutionSuccessful;
  }

  /**
   * Resolve media format names to media formats in picture sources so all downstream logic has only to handle the
   * resolved media formats.
   * @param mediaArgs Media args
   * @return true if resolution was successful.
   */
  @SuppressWarnings("null")
  private boolean resolvePictureSourcesByNames(MediaArgs mediaArgs) {
    PictureSource[] pictureSources = mediaArgs.getPictureSources();
    if (pictureSources == null) {
      return true;
    }

    // resolve media format options that have only a name set
    boolean resolutionSuccessful = true;
    for (int i = 0; i < pictureSources.length; i++) {
      PictureSource pictureSource = pictureSources[i];
      String mediaFormatName = pictureSource.getMediaFormatName();
      if (pictureSource.getMediaFormat() == null && mediaFormatName != null) {
        MediaFormat mediaFormat = mediaFormatHandler.getMediaFormat(mediaFormatName);
        if (mediaFormat == null) {
          log.warn("Media format name '{}' is invalid.", pictureSource.getMediaFormatName());
          resolutionSuccessful = false;
        }
        else {
          pictureSources[i] = new PictureSource(mediaFormat)
              .media(pictureSource.getMedia())
              .sizes(pictureSource.getSizes())
              .widthOptions(pictureSource.getWidthOptions());
        }
      }
    }
    mediaArgs.pictureSources(pictureSources);

    return resolutionSuccessful;
  }

  /**
   * Add on-the-fly generated media formats if required for responsive image handling
   * via image sizes or picture sources.
   * @param mediaArgs Media args
   * @return true if resolution was successful
   */
  private boolean addResponsiveImageMediaFormats(MediaArgs mediaArgs) {
    Map<String, MediaFormatOption> additionalMediaFormats = new LinkedHashMap<>();

    // check if additional on-the-fly generated media formats needs to be added for responsive image handling
    if (!resolveForImageSizes(mediaArgs, additionalMediaFormats)) {
      return false;
    }
    if (!resolveForResponsivePictureSources(mediaArgs, additionalMediaFormats)) {
      return false;
    }

    // if additional media formats where found add them to the media format list in media args
    if (!additionalMediaFormats.isEmpty()) {
      List<MediaFormatOption> allMediaFormats = new ArrayList<>();
      if (mediaArgs.getMediaFormatOptions() != null) {
        allMediaFormats.addAll(Arrays.asList(mediaArgs.getMediaFormatOptions()));
      }
      allMediaFormats.addAll(additionalMediaFormats.values());
      mediaArgs.mediaFormatOptions(allMediaFormats.toArray(new MediaFormatOption[0]));
    }

    return true;
  }

  private boolean resolveForImageSizes(MediaArgs mediaArgs, Map<String, MediaFormatOption> additionalMediaFormats) {
    ImageSizes imageSizes = mediaArgs.getImageSizes();
    if (imageSizes == null) {
      return true;
    }

    final MediaFormat[] mediaFormats = mediaArgs.getMediaFormats();
    if (isEmpty(mediaFormats)) {
      log.warn("No media format with ratio given - unable to fulfill resolve image sizes.");
      return false;
    }

    Arrays.stream(mediaFormats)
        .filter(Objects::nonNull)
        .forEach(mediaFormat -> generateMediaFormatsForWidths(additionalMediaFormats, mediaFormat, true, imageSizes.getWidthOptions()));
    return true;
  }

  private boolean resolveForResponsivePictureSources(MediaArgs mediaArgs, Map<String, MediaFormatOption> additionalMediaFormats) {
    PictureSource[] pictureSources = mediaArgs.getPictureSources();
    if (pictureSources == null || pictureSources.length == 0) {
      return true;
    }
    for (PictureSource pictureSource : pictureSources) {
      generateMediaFormatsForWidths(additionalMediaFormats, pictureSource.getMediaFormat(), false, pictureSource.getWidthOptions());
    }
    return true;
  }

  private void generateMediaFormatsForWidths(@NotNull Map<String, MediaFormatOption> additionalMediaFormats,
      @Nullable MediaFormat mediaFormat, boolean setParent, @NotNull WidthOption @Nullable... widthOptions) {
    if (mediaFormat == null || widthOptions == null) {
      return;
    }
    for (WidthOption widthOption : widthOptions) {
      MediaFormat widthMediaFormat = MediaFormatBuilder.create(
          mediaFormat.getName() + MEDIAFORMAT_NAME_SEPARATOR + widthOption.getWidth())
          .label(mediaFormat.getLabel())
          .extensions(mediaFormat.getExtensions())
          .ratio(mediaFormat.getRatio())
          .width(widthOption.getWidth())
          .property(MEDIAFORMAT_PROP_PARENT_MEDIA_FORMAT, setParent ? mediaFormat : null)
          .build();
      additionalMediaFormats.put(widthMediaFormat.getName(), new MediaFormatOption(widthMediaFormat, widthOption.isMandatory()));
    }
  }

}
