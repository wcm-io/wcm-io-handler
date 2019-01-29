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

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Represents a media item that is referenced via a {@link MediaRequest} and resolved via {@link MediaHandler}.
 * It cannot be rendered directly, but contains references to renditions depending on {@link MediaArgs}.
 */
@ProviderType
public interface Asset extends Adaptable {

  /**
   * @return Title of media item
   */
  @Nullable
  String getTitle();

  /**
   * @return Alternative text for media item
   */
  @Nullable
  String getAltText();

  /**
   * @return Description for this media item
   */
  @Nullable
  String getDescription();

  /**
   * Internal path pointing to media item, if it is stored in the JCR repository.
   * @return Repository path
   */
  @NotNull
  String getPath();

  /**
   * @return Properties of media item
   */
  @NotNull
  ValueMap getProperties();

  /**
   * Get the default rendition without specifying and media args.
   * @return {@link Rendition} instance or null if no rendition exists
   */
  @Nullable
  Rendition getDefaultRendition();

  /**
   * Get the first rendition that matches the given media args.
   * @param mediaArgs Media args to filter specific media formats or extensions.
   * @return {@link Rendition} for the first matching rendition or null if no match found.
   */
  @Nullable
  Rendition getRendition(@NotNull MediaArgs mediaArgs);

  /**
   * Get the first image rendition that matches the given media args.
   * @param mediaArgs Media args to filter specific media formats or extensions.
   * @return {@link Rendition} for the first matching rendition or null if no match found.
   */
  @Nullable
  Rendition getImageRendition(@NotNull MediaArgs mediaArgs);

  /**
   * Get the first flash rendition that matches the given media args.
   * @param mediaArgs Media args to filter specific media formats or extensions.
   * @return {@link Rendition} for the first matching rendition or null if no match found.
   * @deprecated Flash support is deprecated
   */
  @Deprecated
  @Nullable
  Rendition getFlashRendition(@NotNull MediaArgs mediaArgs);

  /**
   * Get the first download rendition that matches the given media args.
   * @param mediaArgs Media args to filter specific media formats or extensions.
   * @return {@link Rendition} for the first matching rendition or null if no match found.
   */
  @Nullable
  Rendition getDownloadRendition(@NotNull MediaArgs mediaArgs);

}
