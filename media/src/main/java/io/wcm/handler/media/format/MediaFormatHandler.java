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
package io.wcm.handler.media.format;

import java.util.Comparator;
import java.util.SortedSet;

/**
 * Media format handling.
 */
public interface MediaFormatHandler {

  /**
   * When comparing the ratio's of different media formats for automatic rendition generation, use this
   * value as tolerance barrier when comparing ratios of different media formats.
   */
  double RATIO_TOLERANCE = 0.05d;

  /**
   * Get media formats defined by a CMS application that is responsible for the given media library path.
   * @return Media formats sorted by combined title
   */
  SortedSet<MediaFormat> getMediaFormats();

  /**
   * Get media formats defined by a CMS application that is responsible for the given media library path.
   * @param comparator Comparator for set
   * @return Media formats
   */
  SortedSet<MediaFormat> getMediaFormats(Comparator<MediaFormat> comparator);

  /**
   * Get media format by its path.
   * @param path Media format definition path or media format name.
   *          If only a name (without leading "/") is given the path is constructed by added the media formats path prefix.
   * @return Media format instance or null if invalid
   */
  MediaFormat getMediaFormat(String path);

  /**
   * Get list of media formats that have the same (or bigger) resolution as the requested media format
   * and (nearly) the same aspect ratio.
   * @param mediaFormatRequested Requested media format
   * @param filterRenditionGroup Only check media formats of the same rendition group.
   * @return Matching media formats, sorted by size (biggest first), ranking, name
   */
  SortedSet<MediaFormat> getSameBiggerMediaFormats(MediaFormat mediaFormatRequested, boolean filterRenditionGroup);

  /**
   * Get list of possible media formats that can be rendered from the given media format, i.e. same size or smaller
   * and (nearly) the same aspect ratio.
   * @param mediaFormatRequested Available media format
   * @param filterRenditionGroup Only check media formats of the same rendition group.
   * @return Matching media formats, sorted by size (biggest first), ranking, name
   */
  SortedSet<MediaFormat> getSameSmallerMediaFormats(MediaFormat mediaFormatRequested, boolean filterRenditionGroup);

  /**
   * Detect matching media format.
   * @param extension File extension
   * @param fileSize File size
   * @param width Image width (or 0 if not image)
   * @param height Image height (or 0 if not image)
   * @return Media format or null if no matching media format found
   */
  MediaFormat detectMediaFormat(String extension, long fileSize, long width, long height);
  /**
   * Detect all matching media formats.
   * @param extension File extension
   * @param fileSize File size
   * @param width Image width (or 0 if not image)
   * @param height Image height (or 0 if not image)
   * @return Matching media formats sorted by their ranking or an empty list if no matching format was found
   */
  SortedSet<MediaFormat> detectMediaFormats(String extension, long fileSize, long width, long height);

}
