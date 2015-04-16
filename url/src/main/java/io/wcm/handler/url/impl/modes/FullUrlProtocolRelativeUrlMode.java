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

import io.wcm.handler.url.integrator.IntegratorHandler;
import io.wcm.handler.url.integrator.IntegratorPlaceholder;
import io.wcm.handler.url.spi.UrlHandlerConfig;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.util.RunMode;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;

import com.day.cq.wcm.api.Page;

/**
 * Enforce the generation of a full URL with hostname and "//" as protocol (protocol-relative mode).
 * Using "//" instead of "http://" or "https://" results in using the same protocol as the current request
 * in the browser.
 */
public final class FullUrlProtocolRelativeUrlMode extends AbstractUrlMode {

  private final boolean forcePublish;

  /**
   * @param forcePublish Force to select publish URLs even on author instance
   */
  public FullUrlProtocolRelativeUrlMode(boolean forcePublish) {
    this.forcePublish = forcePublish;
  }

  @Override
  public String getId() {
    return "FULL_URL_PROTOCOLRELATIVE";
  }

  @Override
  public String getLinkUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage, Page targetPage) {

    // if integrator template mode with placeholders is active return link url placeholder
    IntegratorHandler integratorHandler = AdaptTo.notNull(adaptable, IntegratorHandler.class);
    if (integratorHandler.isIntegratorTemplateMode()
        && integratorHandler.getIntegratorMode().isUseUrlPlaceholders()) {
      UrlHandlerConfig urlHandlerConfig = AdaptTo.notNull(adaptable, UrlHandlerConfig.class);
      if (targetPage != null && urlHandlerConfig.isSecure(targetPage)) {
        return IntegratorPlaceholder.URL_CONTENT_SECURE;
      }
      else {
        return IntegratorPlaceholder.URL_CONTENT;
      }
    }

    UrlConfig config = getUrlConfigForTarget(adaptable, targetPage);

    // in author mode return author site url
    if (!forcePublish && RunMode.isAuthor(runModes) && config.hasSiteUrlAuthor()) {
      return config.getSiteUrlAuthor();
    }

    // return siteUrl in protocol-relative mode
    return convertToProtocolRelative(config.getSiteUrl());
  }

  @Override
  public String getResourceUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage, Resource targetResource) {

    // if integrator template mode with placeholders is active return resource url placeholder
    IntegratorHandler integratorHandler = AdaptTo.notNull(adaptable, IntegratorHandler.class);
    if (integratorHandler.isIntegratorTemplateMode()
        && integratorHandler.getIntegratorMode().isUseUrlPlaceholders()) {
      return IntegratorPlaceholder.URL_CONTENT_PROXY;
    }

    UrlConfig config = getUrlConfigForTarget(adaptable, targetResource);

    // in author mode return author site url
    if (!forcePublish && RunMode.isAuthor(runModes) && config.hasSiteUrlAuthor()) {
      return config.getSiteUrlAuthor();
    }

    // return secure or non-secure site url
    return convertToProtocolRelative(config.getSiteUrl());
  }

  /**
   * Strips of protocol from given URL (if any protocol is included)
   * @param pPrefix Prefix with protocol
   * @return Prefix without protocol (protocol-relative mode)
   */
  private String convertToProtocolRelative(String pPrefix) {
    if (StringUtils.isEmpty(pPrefix)) {
      return null;
    }
    int index = StringUtils.indexOf(pPrefix, "://");
    if (index >= 0) {
      return pPrefix.substring(index + 1);
    }
    else {
      return pPrefix;
    }
  }

}
