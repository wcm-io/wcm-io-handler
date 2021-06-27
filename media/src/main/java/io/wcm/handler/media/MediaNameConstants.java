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
   * Default property name for forcing reading alt. text from DAM asset description (Legacy wcm.io Handler Standard)
   */
  public static final @NotNull String PN_MEDIA_FORCE_ALTTEXT_FROM_ASSET = "mediaForceAltValueFromAsset";

  /**
   * Default property name for forcing reading alt. text from DAM asset description (Adobe/Core Component standard)
   */
  public static final @NotNull String PN_MEDIA_FORCE_ALTTEXT_FROM_ASSET_STANDARD = "altValueFromDAM";

  /**
   * Default property name for marking image as "decorative" - requiring no alt. text (Legacy wcm.io Handler Standard)
   */
  public static final @NotNull String PN_MEDIA_IS_DECORATIVE = "mediaIsDecorative";

  /**
   * Default property name for marking image as "decorative" - requiring no alt. text (Adobe/Core Component standard)
   */
  public static final @NotNull String PN_MEDIA_IS_DECORATIVE_STANDARD = "isDecorative";

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
   * Enable "auto-cropping" mode for this component by setting to true.
   * <p>
   * Property is to be set on component or in policy.
   * </p>
   */
  public static final @NotNull String PN_COMPONENT_MEDIA_AUTOCROP = "wcmio:mediaCropAuto";

  /**
   * List of media format names accepted by this component.
   * <p>
   * Property is to be set on component or in policy.
   * </p>
   */
  public static final @NotNull String PN_COMPONENT_MEDIA_FORMATS = "wcmio:mediaFormats";

  /**
   * Resolving of all media formats is mandatory. This can be a single boolean, or a boolean array
   * where each entry matches with the media format name defined in {@link #PN_COMPONENT_MEDIA_FORMATS}.
   * <p>
   * Property is to be set on component or in policy.
   * </p>
   */
  public static final @NotNull String PN_COMPONENT_MEDIA_FORMATS_MANDATORY = "wcmio:mediaFormatsMandatory";

  /**
   * List of media format names that are mandatory. The list of names is merged with the list
   * of names defined in {@link #PN_COMPONENT_MEDIA_FORMATS}, but all formats defined in
   * this property ad defined as mandatory.
   * <p>
   * Property is to be set on component or in policy.
   * </p>
   */
  public static final @NotNull String PN_COMPONENT_MEDIA_FORMATS_MANDATORY_NAMES = "wcmio:mediaFormatsMandatoryNames";

  /**
   * If multiple responsive image settings are defined, this property defines which gets active.
   * Possible values: <code>none</code>, <code>imageSizes</code>, <code>pictureSources</code>
   * <p>
   * Property is to be set on component or in policy.
   * </p>
   */
  public static final @NotNull String PN_COMPONENT_MEDIA_RESPONSIVE_TYPE = "wcmio:mediaResponsiveType";

  /**
   * Defines "image sizes" responsive image setting.
   * Contains properties <code>sizes</code>, <code>widths</code>.
   * <p>
   * Child node is to be defined on component or in policy.
   * </p>
   */
  public static final @NotNull String NN_COMPONENT_MEDIA_RESPONSIVEIMAGE_SIZES = "wcmio:mediaResponsiveImageSizes";

  /**
   * Defines "picture sources" responsive image setting.
   * Contains child nodes for each source definition with properties <code>mediaFormat</code>, <code>media</code>,
   * <code>widths</code>.
   * <p>
   * Child node is to be defined on component or in policy.
   * </p>
   */
  public static final @NotNull String NN_COMPONENT_MEDIA_RESPONSIVEPICTURE_SOURCES = "wcmio:mediaResponsivePictureSources";

  /**
   * Defines "image sizes" responsive image setting.
   * Contains properties <code>sizes</code>, <code>widths</code>.
   * <p>
   * Child node is to be defined on component or in policy.
   * </p>
   * @deprecated Please use {@link #NN_COMPONENT_MEDIA_RESPONSIVEIMAGE_SIZES}
   */
  @Deprecated
  public static final @NotNull String NN_COMPONENT_MEDIA_RESPONSIVE_IMAGE_SIZES = "wcmio:mediaRepsonsiveImageSizes";

  /**
   * Defines "picture sources" responsive image setting.
   * Contains child nodes for each source definition with properties <code>mediaFormat</code>, <code>media</code>,
   * <code>widths</code>.
   * <p>
   * Child node is to be defined on component or in policy.
   * </p>
   * @deprecated Please use {@link #NN_COMPONENT_MEDIA_RESPONSIVEPICTURE_SOURCES}
   */
  @Deprecated
  public static final @NotNull String NN_COMPONENT_MEDIA_RESPONSIVE_PICTURE_SOURCES = "wcmio:mediaRepsonsivePictureSources";

  /**
   * Media format property name for the parent media format. Parent media format is the original media format that
   * is used to generate a width-based sub-media-format for responsive images.
   */
  public static final String MEDIAFORMAT_PROP_PARENT_MEDIA_FORMAT = "parentMediaFormat";

  /**
   * URI template placeholder for width.
   */
  public static final String URI_TEMPLATE_PLACEHOLDER_WIDTH = "{width}";

  /**
   * URI template placeholder for height.
   */
  public static final String URI_TEMPLATE_PLACEHOLDER_HEIGHT = "{height}";

  /**
   * URI template placeholder for width.
   * @deprecated Please use {@link #URI_TEMPLATE_PLACEHOLDER_WIDTH}
   */
  @Deprecated
  public static final String URI_TEMPLATE_PLACEHOLDER_WITH = URI_TEMPLATE_PLACEHOLDER_WIDTH;

}
