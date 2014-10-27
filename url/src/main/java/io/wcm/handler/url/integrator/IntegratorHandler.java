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
package io.wcm.handler.url.integrator;

import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;

/**
 * Manages detection of integrator template context.
 * <p>
 * The interface is implemented by a Sling Model. You can adapt from
 * {@link org.apache.sling.api.SlingHttpServletRequest} or {@link org.apache.sling.api.resource.Resource} to get a
 * context-specific handler instance.
 * </p>
 */
@ProviderType
public interface IntegratorHandler {

  /**
   * Selector for "integrator template" mode.
   */
  String SELECTOR_INTEGRATORTEMPLATE = "integratortemplate";
  /**
   * Selector for "integrator template" secure mode.
   */
  String SELECTOR_INTEGRATORTEMPLATE_SECURE = "integratortemplatesecure";

  /**
   * Checks if current request is in integrator template mode.
   * @return true if in integrator template mode
   */
  boolean isIntegratorTemplateMode();

  /**
   * Checks if current request is in integrator secure template mode.
   * @return true if in integrator template secure mode
   */
  boolean isIntegratorTemplateSecureMode();

  /**
   * Returns selector for integrator template mode.
   * In HTTPS mode the secure selector is returned, otherwise the default selector.
   * HTTPS mode is active if the current page is an integrator page and has simple mode-HTTPs activated, or
   * the secure integrator mode selector is included in the current request.
   * @return Integrator template selector
   */
  String getIntegratorTemplateSelector();

  /**
   * Get integrator mode configured for the current page.
   * @return Integrator mode
   */
  IntegratorMode getIntegratorMode();

  /**
   * Get integrator mode configured for the given page.
   * @param page Page
   * @return Integrator mode
   */
  IntegratorMode getIntegratorMode(Page page);

}
