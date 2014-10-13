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

import io.wcm.handler.media.args.MediaArgsType;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.ValueMap;

import aQute.bnd.annotation.ProviderType;

/**
 * Represents a media item that is referenced via a {@link MediaRequest} and resolved via {@link MediaHandler}.
 * It cannot be rendered directly, but contains references to renditions depending on {@link MediaArgsType}.
 */
@ProviderType
public interface Asset extends Adaptable {

  /**
   * @return Title of media item
   */
  String getTitle();

  /**
   * @return Alternative text for media item
   */
  String getAltText();

  /**
   * @return Description for this media item
   */
  String getDescription();

  /**
   * Internal path pointing to media item, if it is stored in the JCR repository.
   * @return Repository path
   */
  String getPath();

  /**
   * @return Properties of media item
   */
  ValueMap getProperties();

  /**
   * Get the default rendition without specifying and media args.
   * @return {@link Rendition} instance or null if no rendition exists
   */
  Rendition getDefaultRendition();

  /**
   * Get the first rendition that matches the given media args.
   * @param mediaArgs Media args to filter specific media formats or extensions.
   * @return {@link Rendition} for the first matching rendition or null if not match found.
   */
  Rendition getRendition(MediaArgsType mediaArgs);

  /**
   * Get the first image rendition that matches the given media args.
   * @param mediaArgs Media args to filter specific media formats or extensions.
   * @return {@link Rendition} for the first matching rendition or null if not match found.
   */
  Rendition getImageRendition(MediaArgsType mediaArgs);

  /**
   * Get the first flash rendition that matches the given media args.
   * @param mediaArgs Media args to filter specific media formats or extensions.
   * @return {@link Rendition} for the first matching rendition or null if not match found.
   */
  Rendition getFlashRendition(MediaArgsType mediaArgs);

  /**
   * Get the first download rendition that matches the given media args.
   * @param mediaArgs Media args to filter specific media formats or extensions.
   * @return {@link Rendition} for the first matching rendition or null if not match found.
   */
  Rendition getDownloadRendition(MediaArgsType mediaArgs);

}
