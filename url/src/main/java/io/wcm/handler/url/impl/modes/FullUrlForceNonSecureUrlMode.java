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

import io.wcm.config.api.Configuration;
import io.wcm.handler.url.UrlParams;
import io.wcm.handler.url.integrator.IntegratorHandler;
import io.wcm.handler.url.integrator.IntegratorPlaceholder;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.util.RunMode;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.Adaptable;

import com.day.cq.wcm.api.Page;

/**
 * Enforce the generation of a full URL with protocol and hostname and non-secure mode.
 */
public final class FullUrlForceNonSecureUrlMode extends AbstractUrlMode {

  @Override
  public String getId() {
    return "FULL_URL_FORCENONSECURE";
  }

  @Override
  public String getLinkUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage, Page targetPage) {

    // if integrator template mode with placeholders is active return link url placeholder
    IntegratorHandler integratorHandler = AdaptTo.notNull(adaptable, IntegratorHandler.class);
    if (integratorHandler.isIntegratorTemplateMode()
        && integratorHandler.getIntegratorMode().isUseUrlPlaceholders()) {
      return IntegratorPlaceholder.URL_CONTENT;
    }

    Configuration config = adaptable.adaptTo(Configuration.class);
    if (config == null) {
      return null;
    }

    // in author mode return author site url
    if (RunMode.isAuthor(runModes)) {
      String siteUrlAuthor = config.get(UrlParams.SITE_URL_AUTHOR);
      if (StringUtils.isNotEmpty(siteUrlAuthor)) {
        return siteUrlAuthor;
      }
    }

    // return non-secure site url
    return config.get(UrlParams.SITE_URL);
  }

  @Override
  public String getResourceUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage) {

    // if integrator template mode with placeholders is active return resource url placeholder
    IntegratorHandler integratorHandler = AdaptTo.notNull(adaptable, IntegratorHandler.class);
    if (integratorHandler.isIntegratorTemplateMode()
        && integratorHandler.getIntegratorMode().isUseUrlPlaceholders()) {
      return IntegratorPlaceholder.URL_CONTENT_PROXY;
    }

    Configuration config = adaptable.adaptTo(Configuration.class);
    if (config == null) {
      return null;
    }

    // in author mode return author site url
    if (RunMode.isAuthor(runModes)) {
      String siteUrlAuthor = config.get(UrlParams.SITE_URL_AUTHOR);
      if (StringUtils.isNotEmpty(siteUrlAuthor)) {
        return siteUrlAuthor;
      }
    }

    // return non-secure site url
    return config.get(UrlParams.SITE_URL);
  }

}
