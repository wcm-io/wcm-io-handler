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

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ConsumerType;

import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;

import io.wcm.handler.url.UrlMode;
import io.wcm.handler.url.UrlModes;
import io.wcm.handler.url.integrator.IntegratorMode;
import io.wcm.sling.commons.caservice.ContextAwareService;

/**
 * {@link UrlHandlerConfig} OSGi services provide application-specific configuration for URL handling.
 * Via the {@link ContextAwareService} methods it can be controlled if this configuration applies to all or only certain
 * resources.
 */
@ConsumerType
public abstract class UrlHandlerConfig implements ContextAwareService {

  /**
   * Returns the absolute path level where the root page of the site is located.
   * @param contextResource Context resource that is assumed to be inside the site context.
   * @return Root level or 0 if it could not be detected
   */
  public abstract int getSiteRootLevel(Resource contextResource);

  /**
   * Detects if a page has to be accessed in secure mode
   * @param page Page Page
   * @return true if secure mode is required
   */
  public boolean isSecure(Page page) {
    // not supported by default
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
