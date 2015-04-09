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
package io.wcm.handler.url.suffix;

import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Interface for strategies that decide which parts from the current request's suffix should be kept when constructing
 * new links with the {@link SuffixBuilder}
 */
@ConsumerType
public interface SuffixStateKeepingStrategy {

  /**
   * Implement this method to return those suffix parts that should be kept at the beginning of the suffix to construct
   * to keep the page's state
   * @param request Current request
   * @return a list of suffix parts
   */
  List<String> getSuffixPartsToKeep(SlingHttpServletRequest request);

}
