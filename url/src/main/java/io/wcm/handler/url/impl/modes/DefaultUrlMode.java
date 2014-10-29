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
import io.wcm.handler.url.spi.UrlHandlerConfig;
import io.wcm.sling.commons.adapter.AdaptTo;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;

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
  public String getResourceUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage, Resource targetResource) {
    // in integrator template mode or if resource is from another site default to full URL mode, otherwise to no-hostname mode
    IntegratorHandler integratorHandler = AdaptTo.notNull(adaptable, IntegratorHandler.class);
    if (integratorHandler.isIntegratorTemplateMode()
        || linksToOtherDomain(adaptable, currentPage, targetResource)) {
      return UrlModes.FULL_URL.getResourceUrlPrefix(adaptable, runModes, currentPage, targetResource);
    }
    return UrlModes.NO_HOSTNAME.getResourceUrlPrefix(adaptable, runModes, currentPage, targetResource);
  }

  /**
   * Checks if the target resource is located outsite the current site, and if for this other
   * resource context a valid url configuration with a specific hostname exists.
   * @param adaptable Adaptable
   * @param currentPage Current page (may be null)
   * @param targetResource Target resource (may be null)
   * @return true if the target resources is located in another site/context with separate url configuration
   */
  private boolean linksToOtherDomain(Adaptable adaptable, Page currentPage, Resource targetResource) {
    if (currentPage == null || targetResource == null) {
      return false;
    }

    UrlHandlerConfig urlHandlerConfig = AdaptTo.notNull(adaptable, UrlHandlerConfig.class);
    String currentPath = currentPage.getPath();
    String targetPath = targetResource.getPath();
    String currentSiteRoot = getRootPath(currentPath, urlHandlerConfig.getSiteRootLevel(currentPath));
    String pathSiteRoot = getRootPath(targetPath, urlHandlerConfig.getSiteRootLevel(targetPath));
    boolean notInCurrentSite = !StringUtils.equals(currentSiteRoot, pathSiteRoot);

    if (notInCurrentSite) {
      UrlConfig targetUrlConfig = new UrlConfig(targetResource);
      return targetUrlConfig.isValid();
    }
    else {
      return false;
    }
  }

  /**
   * Gets site root level path of a site.
   * @param path Path of page within the site
   * @param rootLevel Level of root page
   * @return Site root path for the site. The path is not checked for validness.
   */
  private String getRootPath(String path, int rootLevel) {
    String rootPath = Text.getAbsoluteParent(path, rootLevel);

    // strip off everything after first "." - root path may be passed with selectors/extension which is not relevant
    if (StringUtils.contains(rootPath, ".")) {
      rootPath = StringUtils.substringBefore(rootPath, ".");
    }

    return rootPath;
  }

}
