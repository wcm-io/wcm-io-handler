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

import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

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
@ProviderType
public final class MediaNameConstants {

  private MediaNameConstants() {
    // holds constants only
  }

  /**
   * Media format node type
   */
  public static final @NotNull String NT_MEDIAFORMAT = "wcmio:MediaFormat";

  /**
   * Media source
   */
  public static final @NotNull String PN_MEDIA_SOURCE = "mediaSource";

  /**
   * Default property name for reference to media library item (Legacy wcm.io Handler Standard)
   */
  public static final @NotNull String PN_MEDIA_REF = "mediaRef";

  /**
   * Default property name for reference to media library item (Adobe/Core Component standard)
   */
  public static final @NotNull String PN_MEDIA_REF_STANDARD = "fileReference";

  /**
   * Default property name for cropping parameters (Legacy wcm.io Handler Standard)
   */
  public static final @NotNull String PN_MEDIA_CROP = "mediaCrop";

  /**
   * Default property name for cropping parameters (Adobe/Core Component standard)
   */
  public static final @NotNull String PN_MEDIA_CROP_STANDARD = "imageCrop";

  /**
   * Default property name for rotate parameter (Legacy wcm.io Handler Standard)
   */
  public static final @NotNull String PN_MEDIA_ROTATION = "mediaRotate";

  /**
   * Default property name for rotate parameter (Adobe/Core Component standard)
   */
  public static final @NotNull String PN_MEDIA_ROTATION_STANDARD = "imageRotate";

  /**
   * Default property name for map parameter (Legacy wcm.io Handler Standard)
   */
  public static final @NotNull String PN_MEDIA_MAP = "mediaMap";

  /**
   * Default property name for map parameter (Adobe/Core Component standard)
   */
  public static final @NotNull String PN_MEDIA_MAP_STANDARD = "imageMap";

  /**
   * Default property name for media alt. text (Legacy wcm.io Handler Standard)
   */
  public static final @NotNull String PN_MEDIA_ALTTEXT = "mediaAltText";

  /**
   * Default property name for media alt. text (Adobe/Core Component standard)
   */
  public static final @NotNull String PN_MEDIA_ALTTEXT_STANDARD = "alt";

  /**
   * Default node name for inline media item stored in node within the content page (Legacy wcm.io Handler Standard)
   */
  public static final @NotNull String NN_MEDIA_INLINE = "mediaInline";

  /**
   * Default node name for inline media item stored in node within the content page (Adobe/Core Component standard)
   */
  public static final @NotNull String NN_MEDIA_INLINE_STANDARD = "file";

  /**
   * Default property name for flash variables
   * @deprecated Flash support is deprecated
   */
  @Deprecated
  public static final @NotNull String PN_FLASH_VARS = "flashVars";

  /**
   * CSS class for dummy image
   */
  public static final @NotNull String CSS_DUMMYIMAGE = "wcmio_mediahandler_wcm_dummy";

  /**
   * CSS class for "Added in Diff Mode"
   */
  public static final @NotNull String CSS_DIFF_ADDED = "wcmio_mediahandler_wcm_diff_added";

  /**
   * CSS class for "Updated in Diff Mode"
   */
  public static final @NotNull String CSS_DIFF_UPDATED = "wcmio_mediahandler_wcm_diff_updated";

  /**
   * CSS class for "Removed in Diff Mode"
   */
  public static final @NotNull String CSS_DIFF_REMOVED = "wcmio_mediahandler_wcm_diff_removed";

  /**
   * Property name for responsive breakpoint (mq)
   * @deprecated This is used only for the "legacy mode" of responsive image handling.
   */
  @Deprecated
  public static final @NotNull String PROP_BREAKPOINT = "mq";

  /**
   * Property name for setting additional CSS classes
   */
  public static final @NotNull String PROP_CSS_CLASS = "cssClass";

  /**
   * Enable "auto-cropping" mode for this component by setting to true. Property is to be set on component or in policy.
   */
  public static final @NotNull String PN_COMPONENT_MEDIA_AUTOCROP = "wcmio:mediaCropAuto";

  /**
   * List of media formats accepted by this component. Property is to be set on component or in policy.
   */
  public static final @NotNull String PN_COMPONENT_MEDIA_FORMATS = "wcmio:mediaFormats";

  /**
   * Resolving of all media formats is mandatory. Property is to be set on component or in policy.
   */
  public static final @NotNull String PN_COMPONENT_MEDIA_FORMATS_MANDATORY = "wcmio:mediaFormatsMandatory";

}
