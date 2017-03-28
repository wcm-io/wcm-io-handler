/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.handler.commons.spisupport;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Service that resolves the best-matching SPI implementation.
 */
@ProviderType
public interface SpiResolver {

  /**
   * Resolves the best-matching SPI implementation for the given resource.
   * Only SPI implementation that report a match via {@link SpiMatcher} are considered as candidates -
   * if multiple candidates exist the implementation with the highest service ranking is returned.
   * It is recommended that for each SPI interface a default service with lowest service ranking (Integer.MIN_VALUE)
   * is registered which accepts all resources.
   * @param spiInterface SPI interface
   * @param adaptable Adaptable which is either a {@link Resource} or {@link SlingHttpServletRequest}.
   *          A resource instances is used directly for matching, in case of request the associated resource is used.
   *          May be null if no context is available.
   * @param <T> SPI interface class
   * @return SPI implementation or null if no matching found.
   */
  @CheckForNull
  <T extends SpiMatcher> T resolve(@Nonnull Class<T> spiInterface, @CheckForNull Adaptable adaptable);

}
