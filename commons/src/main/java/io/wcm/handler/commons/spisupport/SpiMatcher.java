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

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * This interface can be extended by SPI interfaces that allow matching multiple implementation.
 * It should never be implemented directly.
 * TODO: more generic name and place for this?
 */
@ConsumerType
public interface SpiMatcher {

  /**
   * Evaluates if the given resources should be handled to this SPI implementation or not.
   * It is important that this check is implemented as efficient as possible (e.g. by only checking the resource
   * path against a precompiled regular expression), because this method is called quite often.
   * @param resource Resource - never null unless {@link #supportsNullResource()} returns true.
   * @return true if the resource matches
   */
  boolean matches(Resource resource);

  /**
   * Implementations may signal that they are also interested being called when no resource context exists.
   * @return true if the {@link #matches(Resource)} method should also be called when no resource context exists.
   */
  default boolean supportsNullResource() {
    return false;
  }

}
