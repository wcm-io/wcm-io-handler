/* Copyright (c) pro!vision GmbH. All rights reserved. */
package io.wcm.handler.url.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMMode;
import com.day.text.Text;

import io.wcm.handler.url.spi.UrlHandlerConfig;
import io.wcm.sling.models.annotations.AemObject;

/**
 * Model for detecting site root pages.
 */
@ProviderType
@Model(adaptables = SlingHttpServletRequest.class)
public final class SiteRoot {

  private Page siteRootPage;

  @AemObject
  private Page currentPage;
  @AemObject
  private PageManager pageManager;
  @AemObject
  private WCMMode wcmMode;
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
    return getRootPath(page.getPath());
  }

  /**
   * Gets site root level path of a site.
   * @param path Path of page within the site
   * @return Site root path for the site. The path is not checked for validness.
   */
  public String getRootPath(String path) {
    int rootLevel = urlHandlerConfig.getSiteRootLevel(path);
    if (rootLevel > 0) {
      return Text.getAbsoluteParent(path, rootLevel);
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
   * Gets the site root page of the given path
   * @param path
   * @return Site root page
   */
  public Page getRootPage(String path) {
    return pageManager.getPage(getRootPath(path));
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
    if (!relativePath.startsWith("/")) {
      path += "/";
    }
    path += relativePath;
    return pageManager.getPage(path);
  }

  /**
   * @param page Page
   * @return true if given page is the site root page
   */
  public boolean isRootPage(Page page) {
    return StringUtils.equals(page.getPath(), getRootPath());
  }

}
