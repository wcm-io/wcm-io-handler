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
package io.wcm.handler.url.impl.modes;

import io.wcm.handler.url.UrlModes;
import io.wcm.handler.url.integrator.IntegratorHandler;
import io.wcm.sling.commons.adapter.AdaptTo;

import java.util.Set;

import org.apache.sling.api.adapter.Adaptable;

import com.day.cq.wcm.api.Page;

/**
 * Default mode: Does generate a full externalized URL only if both siteUrl and siteUrlSecure parameter
 * are set in context-specific configuration. If not set, only URLs without hostname are generated.
 * If the target is an internal content page, siteUrl or siteUrlSecure is chosen automatically depending on the secure
 * state of the page.
 */
public final class DefaultUrlMode extends AbstractUrlMode {

  @Override
  public String getId() {
    return "DEFAULT";
  }

  @Override
  public String getLinkUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage, Page targetPage) {
    // default to full url for content URLs
    return UrlModes.FULL_URL.getLinkUrlPrefix(adaptable, runModes, currentPage, targetPage);
  }

  @Override
  public String getResourceUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage) {
    // in integrator template mode default to full URL mode, otherwise to no-hostname mode
    IntegratorHandler integratorHandler = AdaptTo.notNull(adaptable, IntegratorHandler.class);
    if (integratorHandler.isIntegratorTemplateMode()) {
      return UrlModes.FULL_URL.getResourceUrlPrefix(adaptable, runModes, currentPage);
    }
    return UrlModes.NO_HOSTNAME.getResourceUrlPrefix(adaptable, runModes, currentPage);
  }

}
