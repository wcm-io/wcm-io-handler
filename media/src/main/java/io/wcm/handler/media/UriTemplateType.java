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
package io.wcm.handler.media;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Defines the type of URI template to generate.
 */
@ProviderType
public enum UriTemplateType {

  /**
   * URI template for building an asset rendition with <code>{width}</code> and <code>{height}</code> parameter,
   * cropping the original image if required to it's central part to fit into the given box.
   * The image is never scaled up.
   */
  CROP_CENTER,

  /**
   * URI template for building an asset rendition with <code>{width}</code> parameter,
   * resizing the image to the given width.
   * The image is never scaled up.
   */
  SCALE_WIDTH,

  /**
   * URI template for building an asset rendition with <code>{width}</code> parameter,
   * resizing the image to the given height.
   * The image is never scaled up.
   */
  SCALE_HEIGHT

}
