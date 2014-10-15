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
package io.wcm.handler.media;

/**
 * Reasons while a media request could not be resolved.
 */
public enum MediaInvalidReason {

  /**
   * The media request was empty.
   */
  MEDIA_REFERENCE_MISSING,

  /**
   * Media item not found: that means the media request path is invalid or the user has no read access.
   */
  MEDIA_REFERENCE_INVALID,

  /**
   * No matching rendition: The media item exists, but no rendition matches for the requested media args.
   */
  NO_MATCHING_RENDITION,

  /**
   * Not enough matching renditions: The media item exists and some renditions could be resolved, but not for all
   * mandatory media formats requested.
   */
  NOT_ENOUGH_MATCHING_RENDITIONS,

  /**
   * No media source found for handling the given (or empty) media request
   */
  NO_MEDIA_SOURCE

}
