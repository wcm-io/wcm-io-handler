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
package io.wcm.handler.url.spi;

import io.wcm.handler.url.UrlMode;
import io.wcm.handler.url.integrator.IntegratorMode;

import java.util.Collection;

import aQute.bnd.annotation.ConsumerType;

import com.day.cq.wcm.api.Page;

/**
 * Provides application-specific configuration information required for URL handling.
 * This interface has to be implemented by a Sling @Model class, optional with @Application annotation,
 * the adaptables should be {@link org.apache.sling.api.resource.Resource} and
 * {@link org.apache.sling.api.SlingHttpServletRequest}.
 */
@ConsumerType
public interface UrlHandlerConfig {

  /**
   * Returns the absolute path level where the root page of the site is located.
   * @param contextPath Context path that is assumed to be inside the site context.
   * @return Root level or 0 if it could not be detected
   */
  int getSiteRootLevel(String contextPath);

  /**
   * Detects if a page has to be accessed in secure mode
   * @param page Page Page
   * @return true if secure mode is required
   */
  boolean isSecure(Page page);

  /**
   * Detects if page is a integrator page and contains application redirect link information
   * @param page Page
   * @return true if Page is a integrator page
   */
  boolean isIntegrator(Page page);

  /**
   * @return Default URL mode that is used if no URL mode is specified
   */
  UrlMode getDefaultUrlMode();

  /**
   * @return Supported integrator modes
   */
  Collection<IntegratorMode> getIntegratorModes();

}
