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
public final class LinkNameConstants {

  private LinkNameConstants() {
    // holds constants only
  }

  /**
   * Link title
   */
  public static final String PN_LINK_TITLE = "linkTitle";

  /**
   * Link type
   */
  public static final String PN_LINK_TYPE = "linkType";

  /**
   * Internal content page path
   */
  public static final String PN_LINK_CONTENT_REF = "linkContentRef";

  /**
   * Internal media library item path
   */
  public static final String PN_LINK_MEDIA_REF = "linkMediaRef";

  /**
   * External URL
   */
  public static final String PN_LINK_EXTERNAL_REF = "linkExternalRef";

  /**
   * Name of anchor
   */
  public static final String PN_LINK_ANCHOR_NAME = "linkAnchorName";

  /**
   * Open media library item with download dialog
   */
  public static final String PN_LINK_QUERY_PARAM = "linkQueryParam";

  /**
   * Window target name
   */
  public static final String PN_LINK_WINDOW_TARGET = "linkWindowTarget";

  /**
   * Window width (px)
   */
  public static final String PN_LINK_WINDOW_WIDTH = "linkWindowWidth";

  /**
   * Window height (px)
   */
  public static final String PN_LINK_WINDOW_HEIGHT = "linkWindowHeight";

  /**
   * Window features
   */
  public static final String PN_LINK_WINDOW_FEATURES = "linkWindowFeatures";

  /**
   * Open media library item with download dialog
   */
  public static final String PN_LINK_MEDIA_DOWNLOAD = "linkMediaDownload";

}
