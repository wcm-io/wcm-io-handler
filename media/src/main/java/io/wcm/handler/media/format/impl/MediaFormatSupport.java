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
package io.wcm.handler.media.format.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.format.MediaFormat;

/**
 * Helper methods for matching media formats.
 */
public final class MediaFormatSupport {

  private MediaFormatSupport() {
    // static methods only
  }

  /**
   * Get merged list of file extensions from both media formats and media args.
   * @param mediaArgs Media args
   * @return Array of file extensions.
   *         Returns empty array if all file extensions are allowed.
   *         Returns null if different file extensions are requested in media formats and media args
   *         and the file extension filtering is not fulfillable.
   */
  public static @Nullable String[] getRequestedFileExtensions(@NotNull MediaArgs mediaArgs) {
    // get file extension defined in media args
    Set<String> mediaArgsFileExtensions = new HashSet<String>();
    if (mediaArgs.getFileExtensions() != null && mediaArgs.getFileExtensions().length > 0) {
      mediaArgsFileExtensions.addAll(ImmutableList.copyOf(mediaArgs.getFileExtensions()));
    }

    // get file extensions from media formats
    final Set<String> mediaFormatFileExtensions = new HashSet<String>();
    visitMediaFormats(mediaArgs, new MediaFormatVisitor<Object>() {
      @Override
      public @Nullable Object visit(@NotNull MediaFormat mediaFormat) {
        if (mediaFormat.getExtensions() != null && mediaFormat.getExtensions().length > 0) {
          mediaFormatFileExtensions.addAll(ImmutableList.copyOf(mediaFormat.getExtensions()));
        }
        return null;
      }
    });

    // if extensions are defined both in mediaargs and media formats use intersection of both
    final String[] fileExtensions;
    if (!mediaArgsFileExtensions.isEmpty() && !mediaFormatFileExtensions.isEmpty()) {
      Collection<String> intersection = Sets.intersection(mediaArgsFileExtensions, mediaFormatFileExtensions);
      if (intersection.isEmpty()) {
        // not intersected file extensions - return null to singal no valid file extension request
        fileExtensions = null;
      }
      else {
        fileExtensions = intersection.toArray(new String[0]);
      }
    }
    else if (!mediaArgsFileExtensions.isEmpty()) {
      fileExtensions = mediaArgsFileExtensions.toArray(new String[0]);
    }
    else {
      fileExtensions = mediaFormatFileExtensions.toArray(new String[0]);
    }

    return fileExtensions;
  }


  /**
   * Iterate over all media formats defined in media args. Ignores invalid media formats.
   * If the media format visitor returns a value that is not null, iteration is stopped and the value is returned from
   * this method.
   * @param mediaArgs Media args
   * @param mediaFormatVisitor Media format visitor
   * @return Return value form media format visitor, if any returned a value that is not null
   */
  @SuppressWarnings("null")
  public static @Nullable <T> T visitMediaFormats(@NotNull MediaArgs mediaArgs, @NotNull MediaFormatVisitor<T> mediaFormatVisitor) {
    MediaFormat[] mediaFormats = mediaArgs.getMediaFormats();
    if (mediaFormats != null) {
      for (MediaFormat mediaFormat : mediaFormats) {
        T returnValue = mediaFormatVisitor.visit(mediaFormat);
        if (returnValue != null) {
          return returnValue;
        }
      }
    }
    return null;
  }

}
