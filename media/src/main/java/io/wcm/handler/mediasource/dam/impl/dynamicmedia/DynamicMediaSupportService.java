/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.handler.mediasource.dam.impl.dynamicmedia;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.url.UrlMode;

/**
 * Read image profiles stored in /conf resources.
 * Image profiles are usually stored at /conf/global/settings/dam/adminui-extension/imageprofile.
 */
public interface DynamicMediaSupportService {

  /**
   * @return Whether dynamic media is enabled on this AEM instance
   */
  boolean isDynamicMediaEnabled();

  /**
   * @return Whether a transparent fallback to Media Handler-based rendering of renditions is allowed
   *         if the appropriate Dynamic Media metadata is not preset for an asset.
   */
  boolean isAemFallbackDisabled();

  /**
   * @return Reply image size limit as configured in dynamic media.
   */
  @NotNull
  Dimension getImageSizeLimit();

  /**
   * Get image profile.
   * @param profilePath Full profile path
   * @return Profile or null if no profile found
   */
  @Nullable
  ImageProfile getImageProfile(@NotNull String profilePath);

  /**
   * Get image profile for given asset.
   * @param asset DAM asset
   * @return Profile or null if no profile found
   */
  @Nullable
  ImageProfile getImageProfileForAsset(@NotNull Asset asset);

  /**
   * Get scene7 host/URL prefix for publish environment.
   * @param asset DAM asset
   * @return Protocol and hostname of scene7 host or null.
   *         If author preview mode is enabled, returns empty string.
   */
  @Nullable
  String getDynamicMediaServerUrl(@NotNull Asset asset, @Nullable UrlMode urlMode);

}
