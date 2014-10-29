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
package io.wcm.handler.url;

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;

/**
 * Rewrites and builds URLs for links to content pages and resources.
 * <p>
 * The interface is implemented by a Sling Model. You can adapt from
 * {@link org.apache.sling.api.SlingHttpServletRequest} or {@link org.apache.sling.api.resource.Resource} to get a
 * context-specific handler instance.
 * </p>
 */
@ProviderType
public interface UrlHandler {

  /**
   * Selector that is always added if a Sling-URL contains a suffix (to avoid files and directories with same name in
   * dispatcher cache)
   */
  String SELECTOR_SUFFIX = "suffix";

  /**
   * Builds and optionally externalizes an URL using a builder pattern.
   * @param path Path to start URL building with
   * @return URL builder which allows to chain further optional parameters before building the URL string.
   */
  UrlBuilder get(String path);

  /**
   * Builds and optionally externalizes an URL using a builder pattern.
   * @param resource Resource, URL building is started with its path
   * @return URL builder which allows to chain further optional parameters before building the URL string.
   */
  UrlBuilder get(Resource resource);

  /**
   * Builds and optionally externalizes an URL using a builder pattern.
   * @param page Page Page, URL building is started with its path
   * @return URL builder which allows to chain further optional parameters before building the URL string.
   */
  UrlBuilder get(Page page);

  /**
   * Rewrites given path to current site or context.
   * The site root path is replaced with the one from current site
   * This is useful if a link to an internal page points to a page outside the site (e.g. because the page containing
   * the link was copied from the other site or inherited). When the AEM built-in rewrite logic was not applied the link
   * would be invalid. This methods rewrites the link path to the current site to try to resolve it there.
   * @param path Path to rewrite
   * @return Rewritten path
   */
  String rewritePathToContext(String path);

  /**
   * Rewrites given path to given site or context.
   * The site root path is replaced with the one from current site.
   * This is useful if a link to an internal page points to a page outside the site (e.g. because the page containing
   * the link was copied from the other site or inherited). When the AEM built-in rewrite logic was not applied the link
   * would be invalid. This methods rewrites the link path to the current site to try to resolve it there.
   * @param path Path to rewrite
   * @param contextPath Context path
   * @return Rewritten path
   */
  String rewritePathToContext(String path, String contextPath);

}
