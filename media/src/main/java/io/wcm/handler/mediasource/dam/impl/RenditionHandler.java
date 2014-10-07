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
package io.wcm.handler.mediasource.dam.impl;

import io.wcm.handler.media.MediaArgsType;

/**
 * Handles resolving DAM renditions and resizing for media handler.
 */
interface RenditionHandler {

  /**
   * Get rendition (probably virtual) for given media arguments.
   * @param mediaArgs Media arguments
   * @return Rendition or null if none is matching
   */
  RenditionMetadata getRendition(MediaArgsType mediaArgs);

}
