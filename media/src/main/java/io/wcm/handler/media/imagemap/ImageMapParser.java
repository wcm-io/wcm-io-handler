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

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Parses image maps produced by the AEM image editor.
 * <p>
 * The interface is implemented by a Sling Model. You can adapt from
 * {@link org.apache.sling.api.SlingHttpServletRequest} or {@link org.apache.sling.api.resource.Resource} to get a
 * context-specific handler instance.
 * </p>
 * @param <T> Link result type
 */
@ProviderType
public interface ImageMapParser<T> {

  /**
   * Parses a map area string to {@link ImageMapArea} objects.
   * @param mapString Map string
   * @return List of areas or null if none found
   */
  @Nullable
  List<ImageMapArea<T>> parseMap(@Nullable String mapString);

}
