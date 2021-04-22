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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSortedSet;

import io.wcm.handler.media.MediaFileType;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatHandler;
import io.wcm.handler.media.format.MediaFormatProviderManager;
import io.wcm.handler.media.format.MediaFormatRankingComparator;
import io.wcm.handler.media.format.MediaFormatSizeRankingComparator;
import io.wcm.handler.media.format.Ratio;

/**
 * Media format handling.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
}, adapters = MediaFormatHandler.class)
public final class MediaFormatHandlerImpl implements MediaFormatHandler {

  @SlingObject
  private Resource currentResource;
  @OSGiService
  private MediaFormatProviderManager mediaFormatProviderManager;

  // do not access directly - used for caching. use getMediaFormatsForCurrentResource() and getMediaFormatMap() instead
  private SortedSet<MediaFormat> mediaFormats;
  private Map<String, MediaFormat> mediaFormatMap;

  private SortedSet<MediaFormat> getMediaFormatsForCurrentResource() {
    if (this.mediaFormats == null) {
      this.mediaFormats = mediaFormatProviderManager.getMediaFormats(currentResource);
    }
    return this.mediaFormats;
  }

  private Map<String, MediaFormat> getMediaFormatMap() {
    if (this.mediaFormatMap == null) {
      this.mediaFormatMap = new HashMap<>();
      for (MediaFormat mediaFormat : getMediaFormatsForCurrentResource()) {
        this.mediaFormatMap.put(mediaFormat.getName(), mediaFormat);
      }
    }
    return this.mediaFormatMap;
  }

  /**
   * Resolves media format name to media format object.
   * @param mediaFormatName Media format name
   * @return Media format or null if no match found
   */
  @Override
  public MediaFormat getMediaFormat(@NotNull String mediaFormatName) {
    return getMediaFormatMap().get(mediaFormatName);
  }

  /**
   * Get media formats defined by a CMS application that is responsible for the given media library path.
   * @return Media formats sorted by media format name.
   */
  @Override
  public @NotNull SortedSet<MediaFormat> getMediaFormats() {
    return getMediaFormatsForCurrentResource();
  }

  /**
   * Get media formats defined by a CMS application that is responsible for the given media library path.
   * @param comparator Comparator for set
   * @return Media formats
   */
  @Override
  public @NotNull SortedSet<MediaFormat> getMediaFormats(@NotNull Comparator<MediaFormat> comparator) {
    SortedSet<MediaFormat> set = new TreeSet<>(comparator);
    set.addAll(getMediaFormatsForCurrentResource());
    return ImmutableSortedSet.copyOf(set);
  }

  /**
   * Get list of media formats that have the same (or bigger) resolution as the requested media format
   * and (nearly) the same aspect ratio.
   * @param mediaFormatRequested Requested media format
   * @param filterRenditionGroup Only check media formats of the same rendition group.
   * @return Matching media formats, sorted by size (biggest first), ranking, name
   */
  @Override
  public @NotNull SortedSet<MediaFormat> getSameBiggerMediaFormats(@NotNull MediaFormat mediaFormatRequested, boolean filterRenditionGroup) {
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
          if (isRenditionMatchSizeSameBigger(mediaFormat, mediaFormatRequested)) { //NOPMD

            // if media formats have ratios, check ratio (with tolerance)
            // otherwise add to list anyway, it *can* contain matching media items
            if (Ratio.matches(mediaFormat, mediaFormatRequested) //NOPMD
                || !mediaFormat.hasRatio() || !mediaFormatRequested.hasRatio()) {

              // check for supported file extension
              if (isRenditionMatchExtension(mediaFormat)) { //NOPMD
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
  public @NotNull SortedSet<MediaFormat> getSameSmallerMediaFormats(@NotNull MediaFormat mediaFormatRequested, boolean filterRenditionGroup) {
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
          if (isRenditionMatchSizeSameSmaller(mediaFormat, mediaFormatRequested)) { //NOPMD

            // if media formats have ratios, check ratio (with tolerance)
            // otherwise add to list anyway, it *can* contain matching media items
            if (Ratio.matches(mediaFormat, mediaFormatRequested) //NOPMD
                || !mediaFormat.hasRatio() || !mediaFormatRequested.hasRatio()) {

              // check for supported file extension
              if (isRenditionMatchExtension(mediaFormat)) { //NOPMD
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
   * Checks if the given media format size is same size or bigger than the requested one.
   * @param mediaFormat Media format
   * @param mediaFormatRequested Requested media format
   * @return true if media format is same size or bigger
   */
  private boolean isRenditionMatchSizeSameBigger(MediaFormat mediaFormat, MediaFormat mediaFormatRequested) {
    long widthRequested = getEffectiveMinWidthPreferringMinWidthHeight(mediaFormatRequested);
    long heightRequested = getEffectiveMinHeightPreferringMinWidthHeight(mediaFormatRequested);

    long widthMax = mediaFormat.getEffectiveMaxWidth();
    long heightMax = mediaFormat.getEffectiveMaxHeight();

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
    long widthRequested = getEffectiveMinWidthPreferringMinWidthHeight(mediaFormatRequested);
    long heightRequested = getEffectiveMinHeightPreferringMinWidthHeight(mediaFormatRequested);

    long widthMin = getEffectiveMinWidthPreferringMinWidthHeight(mediaFormat);
    long heightMin = getEffectiveMinHeightPreferringMinWidthHeight(mediaFormat);

    return widthMin <= widthRequested && heightMin <= heightRequested;
  }

  private long getEffectiveMinWidthPreferringMinWidthHeight(MediaFormat mf) {
    if (mf.getMinWidthHeight() > 0) {
      return mf.getMinWidthHeight();
    }
    else {
      return mf.getEffectiveMinWidth();
    }
  }

  private long getEffectiveMinHeightPreferringMinWidthHeight(MediaFormat mf) {
    if (mf.getMinWidthHeight() > 0) {
      return mf.getMinWidthHeight();
    }
    else {
      return mf.getEffectiveMinHeight();
    }
  }

  /**
   * Checks if one of the extensions of the given media format are supported for renditions.
   * @param mediaFormat Media format
   * @return true if supported extension found
   */
  private boolean isRenditionMatchExtension(MediaFormat mediaFormat) {
    for (String extension : mediaFormat.getExtensions()) {
      if (MediaFileType.isImage(extension)) {
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
  public MediaFormat detectMediaFormat(@Nullable String extension, long fileSize, long width, long height) {
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
  public @NotNull SortedSet<MediaFormat> detectMediaFormats(@Nullable String extension, long fileSize, long width, long height) {

    // sort media formats by ranking
    SortedSet<MediaFormat> matchingFormats = new TreeSet<>(new MediaFormatRankingComparator());

    for (MediaFormat mediaFormat : getMediaFormats()) {

      // skip media formats with negative ranking
      if (mediaFormat.getRanking() < 0) {
        continue;
      }

      // check extension
      boolean extensionMatch = false;
      if (mediaFormat.getExtensions() != null) {
        for (String ext : mediaFormat.getExtensions()) {
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
        if (mediaFormat.getMinWidthHeight() > 0) {
          dimensionMatch = (width >= mediaFormat.getMinWidthHeight())
              || (height >= mediaFormat.getMinWidthHeight());
        }
        else {
          dimensionMatch = (mediaFormat.getEffectiveMinWidth() == 0 || width >= mediaFormat.getEffectiveMinWidth())
              && (mediaFormat.getEffectiveMaxWidth() == 0 || width <= mediaFormat.getEffectiveMaxWidth())
              && (mediaFormat.getEffectiveMinHeight() == 0 || height >= mediaFormat.getEffectiveMinHeight())
              && (mediaFormat.getEffectiveMaxHeight() == 0 || height <= mediaFormat.getEffectiveMaxHeight());
        }
      }
      else {
        dimensionMatch = true;
      }

      boolean ratioMatch = false;
      if (mediaFormat.hasRatio() && width > 0 && height > 0) {
        double formatRatio = mediaFormat.getRatio();
        double ratio = (double)width / height;
        ratioMatch = Ratio.matches(ratio, formatRatio);
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
