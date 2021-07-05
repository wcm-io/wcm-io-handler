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
package io.wcm.handler.media.imagemap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Represents an image map area defined by the AEM image editor.
 * @param <T> Link result type
 */
@ProviderType
public interface ImageMapArea<T> {

  /**
   * Returns the value for the {@code shape} attribute of the image map area.
   * @return the image map area shape
   */
  @NotNull
  String getShape();

  /**
   * Returns the value for the {@code coords} attribute of the image map area.
   * @return the image map area coordinates
   */
  @NotNull
  String getCoordinates();

  /**
   * Returns the value for a relative unit representation of the {@code coords} attribute of the image map area.
   * @return the image map area coordinates expressed in relative units
   */
  @Nullable
  String getRelativeCoordinates();

  /**
   * Returns the value for the {@code href} attribute of the image map area.
   * @return the image map area link href
   */
  @NotNull
  String getLinkUrl();

  /**
   * Returns the value for the {@code target} attribute of the image map area.
   * @return the image map area link target
   */
  @Nullable
  String getLinkWindowTarget();

  /**
   * Returns the value for the {@code alt} attribute of the image map area.
   * @return the image map area's alternative text
   */
  @Nullable
  String getAltText();

  /**
   * Returns link represent as link object
   * @return Link object (may be invalid)
   */
  @Nullable
  T getLink();

}
