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
package io.wcm.handler.url;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Detects the site root based on Context-Aware configuration context paths.
 */
@ProviderType
public interface SiteRootDetector {

  /**
   * Returns the absolute path level where the root page of the site is located.
   * This is the context path of the "inner-most" context-aware configuration context.
   * @param contextResource Context resource that is assumed to be inside the site context.
   * @return Root level or -1 if it could not be detected
   */
  int getSiteRootLevel(@Nullable Resource contextResource);

}
