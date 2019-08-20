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
package io.wcm.handler.link;

import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Names used for link handling.
 * <p>
 * Conventions:
 * </p>
 * <ul>
 * <li>NT_ prefix stands for "node type"</li>
 * <li>NN_ prefix stands for "node name"</li>
 * <li>PN_ prefix stands for "property name"</li>
 * <li>RP_ prefix stands for "request property"</li>
 * <li>RA_ prefix stands for "request attribute"</li>
 * </ul>
 */
@ProviderType
public final class LinkNameConstants {

  private LinkNameConstants() {
    // holds constants only
  }

  /**
   * Link title
   */
  public static final @NotNull String PN_LINK_TITLE = "linkTitle";

  /**
   * Link type
   */
  public static final @NotNull String PN_LINK_TYPE = "linkType";

  /**
   * Internal content page path
   */
  public static final @NotNull String PN_LINK_CONTENT_REF = "linkContentRef";

  /**
   * Internal content page path (other scope/site)
   */
  public static final @NotNull String PN_LINK_CROSSCONTEXT_CONTENT_REF = "linkCrossContextContentRef";

  /**
   * Internal media library item path
   */
  public static final @NotNull String PN_LINK_MEDIA_REF = "linkMediaRef";

  /**
   * External URL
   */
  public static final @NotNull String PN_LINK_EXTERNAL_REF = "linkExternalRef";

  /**
   * Name of fragment
   */
  public static final @NotNull String PN_LINK_FRAGMENT = "linkFragment";

  /**
   * Open media library item with download dialog
   */
  public static final @NotNull String PN_LINK_QUERY_PARAM = "linkQueryParam";

  /**
   * Window target name
   */
  public static final @NotNull String PN_LINK_WINDOW_TARGET = "linkWindowTarget";

  /**
   * Window width (px)
   */
  public static final @NotNull String PN_LINK_WINDOW_WIDTH = "linkWindowWidth";

  /**
   * Window height (px)
   */
  public static final @NotNull String PN_LINK_WINDOW_HEIGHT = "linkWindowHeight";

  /**
   * Window features
   */
  public static final @NotNull String PN_LINK_WINDOW_FEATURES = "linkWindowFeatures";

  /**
   * Open media library item with download dialog
   */
  public static final @NotNull String PN_LINK_MEDIA_DOWNLOAD = "linkMediaDownload";

  /**
   * Defines a "fallback" property name that is used to load link target information from a single property
   * instead of the link type + link type depending property name. This property is used for migration
   * from components that do not support Link Handler. It is only used for reading, and never written back to.
   * When opened and saved in the link dialog, the property is removed and instead the dedicated properties are used.
   * <p>
   * Property is to be set on component or in policy.
   * </p>
   */
  public static final @NotNull String PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY = "wcmio:linkTargetUrlFallbackProperty";

}
