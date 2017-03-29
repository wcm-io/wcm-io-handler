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
package io.wcm.handler.commons.caservice;

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * OSGi service interfaces or classes may additionally extend or implement this interface
 * if they can be looked up "context-aware", that means multiple implementations with a certain
 * service ranking exist and each of them is asked at lookup if the given resource matches.
 */
@ConsumerType
public interface ContextAwareService {

  /**
   * Evaluates if the given resource can be handled by this implementation or not.
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
