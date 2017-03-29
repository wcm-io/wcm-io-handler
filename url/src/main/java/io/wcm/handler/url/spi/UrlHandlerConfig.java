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

import java.util.Collection;

import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.annotation.versioning.ConsumerType;

import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;

import io.wcm.handler.commons.spisupport.SpiMatcher;
import io.wcm.handler.url.UrlMode;
import io.wcm.handler.url.UrlModes;
import io.wcm.handler.url.integrator.IntegratorMode;

/**
 * {@link UrlHandlerConfig} OSGi services provide application-specific configuration for URL handling.
 * Via the {@link SpiMatcher} methods it can be controlled if this configuration applies to all or only certain
 * resources.
 */
@ConsumerType
public abstract class UrlHandlerConfig implements SpiMatcher {

  /**
   * Returns the absolute path level where the root page of the site is located.
   * @param contextPath Context path that is assumed to be inside the site context.
   * @param resolver Resource resolver
   * @return Root level or 0 if it could not be detected
   */
  // TODO: pass in resource insetad of path + resolver?
  public int getSiteRootLevel(String contextPath, ResourceResolver resolver) {
    // TODO: sensible default for site root level
    return 0;
  }

  /**
   * Detects if a page has to be accessed in secure mode
   * @param page Page Page
   * @return true if secure mode is required
   */
  public boolean isSecure(Page page) {
    // TODO: sensible default for secure detection
    return false;
  }

  /**
   * Detects if page is a integrator page and contains application redirect link information
   * @param page Page
   * @return true if Page is a integrator page
   */
  public boolean isIntegrator(Page page) {
    // not supported by default
    return false;
  }

  /**
   * @return Default URL mode that is used if no URL mode is specified
   */
  public UrlMode getDefaultUrlMode() {
    return UrlModes.DEFAULT;
  }

  /**
   * @return Supported integrator modes
   */
  public Collection<IntegratorMode> getIntegratorModes() {
    // not supported by default
    return ImmutableList.of();
  }

}
