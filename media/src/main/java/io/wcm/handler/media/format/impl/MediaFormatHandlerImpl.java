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
package io.wcm.handler.media.format.impl;

import io.wcm.handler.media.MediaHandlerConfig;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatCombinedTitleComparator;
import io.wcm.handler.media.format.MediaFormatHandler;
import io.wcm.handler.media.format.MediaFormatRankingComparator;
import io.wcm.handler.media.format.MediaFormatSizeRankingComparator;
import io.wcm.wcm.commons.contenttype.FileExtension;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import com.google.common.collect.ImmutableMap;

/**
 * Media format handling.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
}, adapters = MediaFormatHandler.class)
public final class MediaFormatHandlerImpl implements MediaFormatHandler {

  @Self
  private MediaHandlerConfig mediaHandlerConfig;
  @SlingObject
  private ResourceResolver resourceResolver;

  private String mediaFormatsPath;
  private Map<String, MediaFormat> mediaFormats;

  @PostConstruct
  protected void activate() {
    this.mediaFormatsPath = mediaHandlerConfig.getMediaFormatsPath();
    this.mediaFormats = getMediaFormatsMap();
  }

  private Map<String, MediaFormat> getMediaFormatsMap() {
    if (StringUtils.isEmpty(this.mediaFormatsPath)) {
      return ImmutableMap.of();
    }

    Resource parent = resourceResolver.getResource(this.mediaFormatsPath);
    if (parent == null) {
      return ImmutableMap.of();
    }

    Map<String, MediaFormat> map = new HashMap<String, MediaFormat>();
    for (Resource resource : parent.getChildren()) {
      MediaFormat mediaFormat = new MediaFormat(resource);
      map.put(mediaFormat.getPath(), mediaFormat);
    }
    return ImmutableMap.copyOf(map);
  }


  /**
   * Get media formats defined by a CMS application that is responsible for the given media library path.
   * @return Media formats sorted by combined title
   */
  @Override
  public SortedSet<MediaFormat> getMediaFormats() {
    return getMediaFormats(new MediaFormatCombinedTitleComparator());
  }

  /**
   * Get media formats defined by a CMS application that is responsible for the given media library path.
   * @param comparator Comparator for set
   * @return Media formats
   */
  @Override
  public SortedSet<MediaFormat> getMediaFormats(Comparator<MediaFormat> comparator) {
    SortedSet<MediaFormat> set = new TreeSet<>(comparator);
    set.addAll(mediaFormats.values());
    return set;
  }

  /**
   * Get media format by its path.
   * @param path Media format definition path or media format name.
   *          If only a name (without leading "/") is given the path is constructed by added the media formats path prefix.
   * @return Media format instance or null if invalid
   */
  @Override
  public MediaFormat getMediaFormat(String path) {
    String fullPath = path;
    if (!StringUtils.startsWith(fullPath, "/") && !StringUtils.isEmpty(mediaFormatsPath)) {
      fullPath = mediaFormatsPath + "/" + fullPath;
    }
    return mediaFormats.get(fullPath);
  }

  /**
   * Get list of media formats that have the same (or bigger) resolution as the requested media format
   * and (nearly) the same aspect ratio.
   * @param mediaFormatRequested Requested media format
   * @param filterRenditionGroup Only check media formats of the same rendition group.
   * @return Matching media formats, sorted by size (biggest first), ranking, name
   */
  @Override
  public SortedSet<MediaFormat> getSameBiggerMediaFormats(MediaFormat mediaFormatRequested, boolean filterRenditionGroup) {
    SortedSet<MediaFormat> matchingFormats = new TreeSet<>(new MediaFormatSizeRankingComparator());

    // if filter by rendition group is enabled, but the requested media format does not define one,
    // use only the requested format
    if (filterRenditionGroup && StringUtils.isEmpty(mediaFormatRequested.getRenditionGroup())) {
      matchingFormats.add(mediaFormatRequested);
    }
    else {
      for (MediaFormat mediaFormat : getMediaFormats()) {

        // if filter by rendition group is enabled, check only media formats of same rendition group
        if (!filterRenditionGroup
            || StringUtils.equals(mediaFormat.getRenditionGroup(), mediaFormatRequested.getRenditionGroup())) {

          // check if size matched (image size is same or bigger)
          if (isRenditionMatchSizeSameBigger(mediaFormat, mediaFormatRequested)) {

            // if media formats have ratios, check ratio (with tolerance)
            // otherwise add to list anyway, it *can* contain matching media items
            if (isRenditionMatchRatio(mediaFormat, mediaFormatRequested)
                || !mediaFormat.hasRatio() || !mediaFormatRequested.hasRatio()) {

              // check for supported file extension
              if (isRenditionMatchExtension(mediaFormat)) {
                matchingFormats.add(mediaFormat);
              }
            }

          }

        }

      }
    }

    return matchingFormats;
  }

  /**
   * Get list of possible media formats that can be rendered from the given media format, i.e. same size or smaller
   * and (nearly) the same aspect ratio.
   * @param mediaFormatRequested Available media format
   * @param filterRenditionGroup Only check media formats of the same rendition group.
   * @return Matching media formats, sorted by size (biggest first), ranking, name
   */
  @Override
  public SortedSet<MediaFormat> getSameSmallerMediaFormats(MediaFormat mediaFormatRequested, boolean filterRenditionGroup) {
    SortedSet<MediaFormat> matchingFormats = new TreeSet<>(new MediaFormatSizeRankingComparator());

    // if filter by rendition group is enabled, but the requested media format does not define one,
    // use only the requested format
    if (filterRenditionGroup && StringUtils.isEmpty(mediaFormatRequested.getRenditionGroup())) {
      matchingFormats.add(mediaFormatRequested);
    }
    else {
      for (MediaFormat mediaFormat : getMediaFormats()) {

        // if filter by rendition group is enabled, check only media formats of same rendition group
        if (!filterRenditionGroup
            || StringUtils.equals(mediaFormat.getRenditionGroup(), mediaFormatRequested.getRenditionGroup())) {

          // check if size matched (image size is same or smaller)
          if (isRenditionMatchSizeSameSmaller(mediaFormat, mediaFormatRequested)) {

            // if media formats have ratios, check ratio (with tolerance)
            // otherwise add to list anyway, it *can* contain matching media items
            if (isRenditionMatchRatio(mediaFormat, mediaFormatRequested)
                || !mediaFormat.hasRatio() || !mediaFormatRequested.hasRatio()) {

              // check for supported file extension
              if (isRenditionMatchExtension(mediaFormat)) {
                matchingFormats.add(mediaFormat);
              }
            }

          }

        }

      }
    }

    return matchingFormats;
  }

  /**
   * Checks if the ratio of the given media format matches with the ratio of the requested one (with tolerance).
   * @param mediaFormat Media format
   * @param mediaFormatRequested Requested media format
   * @return true if ratio matches
   */
  private boolean isRenditionMatchRatio(MediaFormat mediaFormat, MediaFormat mediaFormatRequested) {
    if (!mediaFormat.hasRatio() || !mediaFormatRequested.hasRatio()) {
      return false;
    }
    double ratioRequested = mediaFormatRequested.getRatio();
    double ratio = mediaFormat.getRatio();
    return (ratio > ratioRequested - RATIO_TOLERANCE) && (ratio < ratioRequested + RATIO_TOLERANCE);
  }

  /**
   * Checks if the given media format size is same size or bigger than the requested one.
   * @param mediaFormat Media format
   * @param mediaFormatRequested Requested media format
   * @return true if media format is same size or bigger
   */
  private boolean isRenditionMatchSizeSameBigger(MediaFormat mediaFormat, MediaFormat mediaFormatRequested) {
    long widthRequested = mediaFormatRequested.getEffectiveWidthMin();
    long heightRequested = mediaFormatRequested.getEffectiveHeightMin();

    long widthMax = mediaFormat.getEffectiveWidthMax();
    long heightMax = mediaFormat.getEffectiveHeightMax();

    return ((widthMax >= widthRequested) || (widthMax == 0))
        && ((heightMax >= heightRequested) || (heightMax == 0));
  }

  /**
   * Checks if the given media format size is same size or smaller than the requested one.
   * @param mediaFormat Media format
   * @param mediaFormatRequested Requested media format
   * @return true if media format is same size or smaller
   */
  private boolean isRenditionMatchSizeSameSmaller(MediaFormat mediaFormat, MediaFormat mediaFormatRequested) {
    long widthRequested = mediaFormatRequested.getEffectiveWidthMin();
    long heightRequested = mediaFormatRequested.getEffectiveHeightMin();

    long widthMin = mediaFormat.getEffectiveWidthMin();
    long heightMin = mediaFormat.getEffectiveHeightMin();

    return widthMin <= widthRequested && heightMin <= heightRequested;
  }

  /**
   * Checks if one of the extensions of the given media format are supported for renditions.
   * @param mediaFormat Media format
   * @return true if supported extension found
   */
  private boolean isRenditionMatchExtension(MediaFormat mediaFormat) {
    for (String extension : mediaFormat.getExtension()) {
      if (FileExtension.isImage(extension)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Detect matching media format.
   * @param extension File extension
   * @param fileSize File size
   * @param width Image width (or 0 if not image)
   * @param height Image height (or 0 if not image)
   * @return Media format or null if no matching media format found
   */
  @Override
  public MediaFormat detectMediaFormat(String extension, long fileSize, long width, long height) {
    SortedSet<MediaFormat> matchingFormats = detectMediaFormats(extension, fileSize, width, height);
    return !matchingFormats.isEmpty() ? matchingFormats.first() : null;
  }

  /**
   * Detect all matching media formats.
   * @param extension File extension
   * @param fileSize File size
   * @param width Image width (or 0 if not image)
   * @param height Image height (or 0 if not image)
   * @return Matching media formats sorted by their ranking or an empty list if no matching format was found
   */
  @Override
  public SortedSet<MediaFormat> detectMediaFormats(String extension, long fileSize, long width, long height) {

    // sort media formats by ranking
    SortedSet<MediaFormat> matchingFormats = new TreeSet<>(new MediaFormatRankingComparator());

    for (MediaFormat mediaFormat : getMediaFormats()) {

      // skip media formats with negative ranking
      if (mediaFormat.getRanking() < 0) {
        continue;
      }

      // check extension
      boolean extensionMatch = false;
      if (mediaFormat.getExtension() != null) {
        for (String ext : mediaFormat.getExtension()) {
          if (StringUtils.equalsIgnoreCase(ext, extension)) {
            extensionMatch = true;
            break;
          }
        }
      }
      else {
        extensionMatch = true;
      }

      // check file size
      boolean fileSizeMatch = false;
      if (mediaFormat.getFileSizeMax() > 0) {
        fileSizeMatch = (fileSize <= mediaFormat.getFileSizeMax());
      }
      else {
        fileSizeMatch = true;
      }

      // width/height match
      boolean dimensionMatch = false;
      if (width > 0 && height > 0) {
        dimensionMatch = (mediaFormat.getEffectiveWidthMin() == 0 || width >= mediaFormat.getEffectiveWidthMin())
            && (mediaFormat.getEffectiveWidthMax() == 0 || width <= mediaFormat.getEffectiveWidthMax())
            && (mediaFormat.getEffectiveHeightMin() == 0 || height >= mediaFormat.getEffectiveHeightMin())
            && (mediaFormat.getEffectiveHeightMax() == 0 || height <= mediaFormat.getEffectiveHeightMax());
      }
      else {
        dimensionMatch = true;
      }

      boolean ratioMatch = false;
      if (mediaFormat.hasRatio() && width > 0 && height > 0) {
        double formatRatio = mediaFormat.getRatio();
        double ratio = (double)width / height;
        ratioMatch = (ratio > formatRatio - RATIO_TOLERANCE) && (ratio < formatRatio + RATIO_TOLERANCE);
      }
      else {
        ratioMatch = true;
      }

      if (extensionMatch && fileSizeMatch && dimensionMatch && ratioMatch) {
        matchingFormats.add(mediaFormat);
      }
    }

    return matchingFormats;
  }

}
