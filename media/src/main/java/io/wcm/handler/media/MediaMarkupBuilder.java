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

import io.wcm.handler.commons.dom.HtmlElement;

/**
 * Builds XHTML markup for media element
 */
public interface MediaMarkupBuilder {

  /**
   * Dummy image
   */
  String DUMMY_IMAGE = "/apps/wcm-io/handler/media/docroot/resources/img/media-dummy.png";

  /**
   * Try to render dummy image with least with this dimension.
   */
  int DUMMY_MIN_DIMENSION = 30;

  /**
   * Checks whether this builder can generate markup for the given media.
   * @param mediaMetadata Media metadata
   * @return true if this markup builder can handle the given media
   */
  boolean accepts(MediaMetadata mediaMetadata);

  /**
   * Build media element markup
   * @param mediaMetadata Media metadata
   * @return Media element or null if media is invalid
   */
  HtmlElement<?> build(MediaMetadata mediaMetadata);

  /**
   * Checks if the given HTML element is valid.
   * It is treated as invalid if it is null, or if it is a simple IMG element containing the dummy image.
   * @param element Media markup element.
   * @return true if media element is invalid
   */
  boolean isValidMedia(HtmlElement<?> element);

}
