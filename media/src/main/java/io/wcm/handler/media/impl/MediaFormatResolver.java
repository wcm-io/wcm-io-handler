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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaArgs.ImageSizes;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatBuilder;
import io.wcm.handler.media.format.MediaFormatHandler;

/**
 * Resolves media formats before starting the media handler processing.
 */
final class MediaFormatResolver {

  private final MediaFormatHandler mediaFormatHandler;

  private static final Logger log = LoggerFactory.getLogger(MediaHandlerImpl.class);

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
    return resolveByNames(mediaArgs)
        && addResponsiveImageMediaFormats(mediaArgs);
  }

  /**
   * Resolve media format names to media formats so all downstream logic has only to handle the resolved media formats.
   * If resolving fails an exception is thrown.
   * @param mediaArgs Media args
   * @return true if resolution was successful.
   */
  private boolean resolveByNames(MediaArgs mediaArgs) {
    // resolved media formats already set? done.
    if (mediaArgs.getMediaFormats() != null) {
      return true;
    }
    // no media format names present? done.
    if (mediaArgs.getMediaFormatNames() == null) {
      return true;
    }
    String[] mediaFormatNames = mediaArgs.getMediaFormatNames();
    MediaFormat[] mediaFormats = new MediaFormat[mediaFormatNames.length];
    boolean resolutionSuccessful = true;
    for (int i = 0; i < mediaFormatNames.length; i++) {
      mediaFormats[i] = mediaFormatHandler.getMediaFormat(mediaFormatNames[i]);
      if (mediaFormats[i] == null) {
        log.warn("Media format name '" + mediaFormatNames[i] + "' is invalid.");
        resolutionSuccessful = false;
      }
    }
    mediaArgs.mediaFormats(mediaFormats);
    mediaArgs.mediaFormatNames((String[])null);
    return resolutionSuccessful;
  }

  /**
   * Add on-the-fly generated media formats if required for responsive image handling
   * via image sizes or picture sources.
   * @param mediaArgs Media args
   * @return true if resolution was successful
   */
  private boolean addResponsiveImageMediaFormats(MediaArgs mediaArgs) {
    Map<String, MediaFormat> additionalMediaFormats = new LinkedHashMap<>();

    // check if additional on-the-fly generated media formats needs to be added for responsive image handling
    if (!resolveForImageSizes(mediaArgs, additionalMediaFormats)) {
      return false;
    }
    if (!resolveForResponsivePictureSources(mediaArgs, additionalMediaFormats)) {
      return false;
    }

    // if additional media formats where found add them to the media format list in media args
    if (!additionalMediaFormats.isEmpty()) {
      List<MediaFormat> allMediaFormats = new ArrayList<>();
      if (mediaArgs.getMediaFormats() != null) {
        allMediaFormats.addAll(Arrays.asList(mediaArgs.getMediaFormats()));
      }
      allMediaFormats.addAll(additionalMediaFormats.values());
      mediaArgs.mediaFormats(allMediaFormats.toArray(new MediaFormat[allMediaFormats.size()]));
    }

    return true;
  }

  private boolean resolveForImageSizes(MediaArgs mediaArgs, Map<String, MediaFormat> additionalMediaFormats) {
    ImageSizes imageSizes = mediaArgs.getImageSizes();
    if (imageSizes == null) {
      return true;
    }
    MediaFormat primaryMediaFormat = getFirstMediaFormatWithRatio(mediaArgs);
    if (primaryMediaFormat == null) {
      log.warn("No media format with ratio given - unable to fulfill resolve image sizes.");
      return false;
    }
    generateMediaFormatsForWidths(additionalMediaFormats, primaryMediaFormat, imageSizes.getWidths());
    return true;
  }

  private MediaFormat getFirstMediaFormatWithRatio(MediaArgs mediaArgs) {
    if (mediaArgs.getMediaFormats() != null) {
      for (MediaFormat mediaFormat : mediaArgs.getMediaFormats()) {
        if (mediaFormat.hasRatio()) {
          return mediaFormat;
        }
      }
    }
    return null;
  }

  private boolean resolveForResponsivePictureSources(MediaArgs mediaArgs, Map<String, MediaFormat> additionalMediaFormats) {
    PictureSource[] pictureSources = mediaArgs.getPictureSources();
    if (pictureSources == null || pictureSources.length == 0) {
      return true;
    }
    for (PictureSource pictureSource : pictureSources) {
      generateMediaFormatsForWidths(additionalMediaFormats, pictureSource.getMediaFormat(), pictureSource.getWidths());
    }
    return true;
  }

  private void generateMediaFormatsForWidths(Map<String, MediaFormat> additionalMediaFormats,
      MediaFormat mediaFormat, long... widths) {
    for (long width : widths) {
      MediaFormat widthMediaFormat = MediaFormatBuilder.create(mediaFormat.getName() + MEDIAFORMAT_NAME_SEPARATOR + width)
          .label(mediaFormat.getLabel())
          .extensions(mediaFormat.getExtensions())
          .ratio(mediaFormat.getRatio())
          .width(width)
          .build();
      additionalMediaFormats.put(widthMediaFormat.getName(), widthMediaFormat);
    }
  }

}
