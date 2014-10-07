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
 * Defines a media source supported by {@link MediaHandler}
 */
public interface MediaSource {

  /**
   * @return Media source ID
   */
  String getId();

  /**
   * @return Name of the property in which the primary media reference is stored
   */
  String getPrimaryMediaRefProperty();

  /**
   * Checks whether a media reference can be handled by this media source
   * @param mediaReference Media reference
   * @return true if this media source can handle the given media reference
   */
  boolean accepts(MediaReference mediaReference);

  /**
   * Checks whether a media reference string can be handled by this media source
   * @param mediaRef Media reference string
   * @return true if this media source can handle the given media reference
   */
  boolean accepts(String mediaRef);

  /**
   * Resolves a media reference
   * @param mediaMetadata Media metadata
   * @return Resolved media metadata. Never null.
   */
  MediaMetadata resolveMedia(MediaMetadata mediaMetadata);

  /**
   * Create a ExtJS drop area for given HTML element to enable drag&drop of media library items
   * from content finder to this element.
   * @param element Html element
   * @param mediaReference Media reference to detect media args and property names
   */
  void enableMediaDrop(HtmlElement<?> element, MediaReference mediaReference);

}
