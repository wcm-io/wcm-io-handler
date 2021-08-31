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
package io.wcm.handler.media.spi;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Resolves link URLs found in Image Map definitions.
 * To be implemented by an OSGi service provided by wcm.io Link Handler.
 * @param <T> Link result type
 */
@ConsumerType
public interface ImageMapLinkResolver<T> {

  /**
   * Resolve link URL.
   * @param linkUrl Link URL
   * @param context Context resource where the image map is defined
   * @return Resolved link URL or null.
   */
  @Nullable
  String resolve(@NotNull String linkUrl, @NotNull Resource context);

  /**
   * Resolve link.
   * @param linkUrl Link URL
   * @param linkWindowTarget Link window target
   * @param context Context resource where the image map is defined
   * @return Resolved link object (may be invalid)
   */
  @Nullable
  default T resolveLink(@NotNull String linkUrl, @Nullable String linkWindowTarget, @NotNull Resource context) {
    return null;
  }

  /**
   * Get Link URL from link Object.
   * @param link Link object
   * @return Resolved link URL or null
   */
  @Nullable
  default String getLinkUrl(@Nullable T link) {
    return null;
  }

}
