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
 * Names used for media handling.
 * <p>
 * Conventions:
 * </p>
 * <ul>
 * <li>NT_ prefix stands for "node type"</li>
 * <li>NN_ prefix stands for "node name"</li>
 * <li>PN_ prefix stands for "property name"</li>
 * <li>RP_ prefix stands for "request property"</li>
 * <li>RA_ prefix stands for "request attribute"</li>
 * <li>CSS_ prefix stands for "CSS class"</li>
 * </ul>
 */
public final class MediaNameConstants {

  private MediaNameConstants() {
    // holds constants only
  }

  /**
   * Media format node type
   */
  public static final String NT_MEDIAFORMAT = "wcmio:MediaFormat";

  /**
   * Media source
   */
  public static final String PN_MEDIA_SOURCE = "mediaSource";

  /**
   * Default property name for reference to media library item
   */
  public static final String PN_MEDIA_REF = "mediaRef";

  /**
   * Default property name for cropping paraemters
   */
  public static final String PN_MEDIA_CROP = "mediaCrop";

  /**
   * Default node name for inline media item stored in node within the content page
   */
  public static final String NN_MEDIA_INLINE = "mediaInline";

  /**
   * Default property name for media alt. text
   */
  public static final String PN_MEDIA_ALTTEXT = "mediaAltText";

  /**
   * Default property name for flash variables
   */
  public static final String PN_FLASH_VARS = "flashVars";

  /**
   * CSS class for dummy image
   */
  public static final String CSS_DUMMYIMAGE = "wcmio_mediahandler_wcm_dummy";

  /**
   * CSS class for "Added in Diff Mode"
   */
  public static final String CSS_DIFF_ADDED = "wcmio_mediahandler_wcm_diff_added";

  /**
   * CSS class for "Updated in Diff Mode"
   */
  public static final String CSS_DIFF_UPDATED = "wcmio_mediahandler_wcm_diff_updated";

  /**
   * CSS class for "Removed in Diff Mode"
   */
  public static final String CSS_DIFF_REMOVED = "wcmio_mediahandler_wcm_diff_removed";

}
