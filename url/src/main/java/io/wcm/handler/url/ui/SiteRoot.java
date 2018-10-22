/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.handler.url.ui;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import io.wcm.handler.url.spi.UrlHandlerConfig;
import io.wcm.sling.models.annotations.AemObject;
import io.wcm.wcm.commons.util.Path;

/**
 * Model for detecting site root pages.
 */
@ProviderType
@Model(adaptables = {SlingHttpServletRequest.class, Resource.class})
public final class SiteRoot {

  private Page siteRootPage;

  @AemObject(injectionStrategy = InjectionStrategy.OPTIONAL)
  private Page currentPage;
  @AemObject
  private PageManager pageManager;
  @Self
  private UrlHandlerConfig urlHandlerConfig;

  /**
   * Gets site root level path of a site.
   * @param page CQ Page of site
   * @return Site root path for the site. The path is not checked for validness.
   */
  public String getRootPath(Page page) {
    if (page == null) {
      return null;
    }
    return getRootPath(page.adaptTo(Resource.class));
  }

  /**
   * Gets site root level path of a site.
   * @param resource Resource within the site
   * @return Site root path for the site. The path is not checked for validness.
   */
  public String getRootPath(Resource resource) {
    int rootLevel = urlHandlerConfig.getSiteRootLevel(resource);
    if (rootLevel > 0) {
      return Path.getAbsoluteParent(resource.getPath(), rootLevel, resource.getResourceResolver());
    }
    return null;
  }

  /**
   * Gets site root level path of the current site.
   * @return Site root path for the current site. The path is not checked for validness.
   */
  public String getRootPath() {
    return getRootPath(currentPage);
  }

  /**
   * Gets site root page of the current site.
   * @return Site root page for the current site.
   */
  public Page getRootPage() {
    if (siteRootPage == null) {
      siteRootPage = pageManager.getPage(getRootPath());
    }
    return siteRootPage;
  }

  /**
   * Get page relative to site root.
   * @param relativePath Path relative to site root
   * @return Page instance or null if not found
   */
  public Page getRelativePage(String relativePath) {
    String path = getRootPath();
    if (path == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder(path);
    if (!relativePath.startsWith("/")) {
      sb.append("/");
    }
    sb.append(relativePath);
    return pageManager.getPage(sb.toString());
  }

  /**
   * @param page Page
   * @return true if given page is the site root page
   */
  public boolean isRootPage(Page page) {
    return StringUtils.equals(page.getPath(), getRootPath());
  }

}
