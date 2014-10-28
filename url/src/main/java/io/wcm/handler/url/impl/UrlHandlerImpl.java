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
package io.wcm.handler.url.impl;

import io.wcm.handler.url.UrlBuilder;
import io.wcm.handler.url.UrlHandler;
import io.wcm.handler.url.UrlMode;
import io.wcm.handler.url.spi.UrlHandlerConfig;
import io.wcm.sling.commons.request.RequestParam;
import io.wcm.sling.models.annotations.AemObject;

import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.settings.SlingSettingsService;

import com.day.cq.wcm.api.Page;

/**
 * Default implementation of a {@link UrlHandler}
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
}, adapters = UrlHandler.class)
public final class UrlHandlerImpl implements UrlHandler {

  @Self
  private Adaptable self;
  @Self
  private UrlHandlerConfig urlHandlerConfig;
  @SlingObject
  private ResourceResolver resolver;
  @OSGiService
  private SlingSettingsService slingSettings;

  // optional injections (only available if called inside a request)
  @SlingObject(optional = true)
  private SlingHttpServletRequest request;
  @AemObject(optional = true)
  private Page currentPage;

  @Override
  public UrlBuilder get(String path) {
    return new UrlBuilderImpl(path, this);
  }

  @Override
  public String rewritePathToContext(final String path) {
    if (currentPage != null) {
      return rewritePathToContext(path, currentPage.getPath());
    }
    else {
      return path;
    }
  }

  @Override
  public String rewritePathToContext(final String path, final String contextPath) {
    if (StringUtils.isEmpty(path) || StringUtils.isEmpty(contextPath)) {
      return path;
    }

    // split up paths
    String[] contextPathParts = StringUtils.split(contextPath, "/");
    String[] pathParts = StringUtils.split(path, "/");

    // check if both paths are valid - return unchanged path if not
    int siteRootLevelContextPath = urlHandlerConfig.getSiteRootLevel(contextPath);
    int siteRootLevelPath = urlHandlerConfig.getSiteRootLevel(path);
    if ((contextPathParts.length <= siteRootLevelContextPath)
        || (pathParts.length <= siteRootLevelPath)
        || !StringUtils.equals(contextPathParts[0], "content")
        || !StringUtils.equals(pathParts[0], "content")) {
      return path;
    }

    // rewrite path to current context
    StringBuilder rewrittenPath = new StringBuilder();
    for (int i = 0; i <= siteRootLevelContextPath; i++) {
      rewrittenPath.append('/').append(contextPathParts[i]);
    }
    for (int i = siteRootLevelPath + 1; i < pathParts.length; i++) {
      rewrittenPath.append('/').append(pathParts[i]);
    }
    return rewrittenPath.toString();
  }

  String externalizeLinkUrl(final String url, final Page targetPage, final UrlMode urlMode) {

    // check for empty url
    if (StringUtils.isEmpty(url)) {
      return null;
    }

    // do not externalize urls again that are already externalized
    if (Externalizer.isExternalized(url)) {
      return url;
    }

    // apply sling mapping, namespace mangling and add webapp context path if required
    String externalizedUrl = Externalizer.externalizeUrl(url, resolver, request);

    // add link URL prefix (scheme/hostname or integrator placeholder) if required
    String linkUrlPrefix = getLinkUrlPrefix(urlMode, targetPage);
    externalizedUrl = StringUtils.defaultString(linkUrlPrefix) + externalizedUrl; //NOPMD

    return externalizedUrl;
  }

  private String getLinkUrlPrefix(UrlMode urlMode, Page targetPage) {
    UrlMode mode = ObjectUtils.defaultIfNull(urlMode, urlHandlerConfig.getDefaultUrlMode());
    return mode.getLinkUrlPrefix(self, slingSettings.getRunModes(), currentPage, targetPage);
  }

  String externalizeResourceUrl(String url, UrlMode urlMode) {

    // check for empty path
    if (StringUtils.isEmpty(url)) {
      return null;
    }

    // do not externalize urls again that are already externalized
    if (Externalizer.isExternalized(url)) {
      return url;
    }

    // check if path that should be externalized points to another site than the current site
    // if this is the case, apply externalization without sling mapping
    boolean slingMappingRequired = false;
    if (currentPage != null) {
      String currentSiteRoot = getRootPath(currentPage.getPath(), urlHandlerConfig.getSiteRootLevel(currentPage.getPath()));
      String pathSiteRoot = getRootPath(url, urlHandlerConfig.getSiteRootLevel(url));
      slingMappingRequired = StringUtils.equals(currentSiteRoot, pathSiteRoot);
    }
    String externalizedUrl;
    if (slingMappingRequired) {
      // apply sling mapping, namespace mangling and add webapp context path if required
      externalizedUrl = Externalizer.externalizeUrl(url, resolver, request);
    }
    else {
      // apply namespace mangling and add webapp context path if required, do *not* apply sling mapping
      externalizedUrl = Externalizer.externalizeUrlWithoutMapping(url, request);
    }

    // add resource URL prefix (scheme/hostname or integrator placeholder) if required
    String resourceUrlPrefix = getResourceUrlPrefix(urlMode);
    externalizedUrl = StringUtils.defaultString(resourceUrlPrefix) + externalizedUrl; //NOPMD

    return externalizedUrl;
  }

  private String getResourceUrlPrefix(UrlMode urlMode) {
    UrlMode mode = ObjectUtils.defaultIfNull(urlMode, urlHandlerConfig.getDefaultUrlMode());
    return mode.getResourceUrlPrefix(self, slingSettings.getRunModes(), currentPage);
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

  String buildUrl(String path, String selector, String extension, String suffix) { //NOPMD
    if (StringUtils.isBlank(path)) {
      return null;
    }

    // Extension url part
    StringBuilder extensionPart = new StringBuilder();
    if (StringUtils.isNotBlank(extension)) {
      extensionPart.append('.').append(extension);
    }

    // Selector url part
    StringBuilder selectorPart = new StringBuilder();
    if (StringUtils.isNotBlank(selector)) {
      // prepend delimiter to selector if required
      if (!StringUtils.startsWith(selector, ".")) {
        selectorPart.append('.');
      }
      selectorPart.append(selector);
    }

    // Suffix part
    StringBuilder suffixPart = new StringBuilder();
    if (StringUtils.isNotBlank(suffix)) {
      // prepend delimiter to suffix if required and add extension
      if (!StringUtils.startsWith(suffix, "/")) {
        suffixPart = suffixPart.append("/");
      }
      suffixPart.append(suffix);

      // if suffix does not contain a file extension add main file extension
      if (!StringUtils.contains(suffix, ".")) {
        suffixPart.append(extensionPart);
      }

      // add a ".suffix" selector to avoid overlapping of filenames between suffixed and non-suffixed versions of the same page in the dispatcher cache
      selectorPart.append('.').append(UrlHandler.SELECTOR_SUFFIX);
    }

    // build externalized url
    return path + selectorPart.toString() + extensionPart.toString() + suffixPart.toString();
  }

  String appendQueryString(String url, String queryString, Set<String> inheritableParameterNames) {
    if (StringUtils.isEmpty(url)) {
      return url;
    }

    // split url from existing query parameters
    StringBuilder urlBuilder = new StringBuilder();
    StringBuilder queryParams = new StringBuilder();
    int separatorPos = url.indexOf('?');
    if (separatorPos >= 0) {
      queryParams.append(url.substring(separatorPos + 1));
      urlBuilder.append(url.substring(0, separatorPos));
    }
    else {
      urlBuilder.append(url);
    }

    // append new query parameters
    if (StringUtils.isNotBlank(queryString)) {
      if (queryParams.length() > 0) {
        queryParams.append('&');
      }
      queryParams.append(queryString);
    }

    // inherit query parameters from current request (only if the parameter is not already included in the params list)
    if (inheritableParameterNames != null && request != null) {
      for (String parameterName : inheritableParameterNames) {
        if (queryParams.indexOf(parameterName + "=") == -1) {
          String[] values = RequestParam.getMultiple(request, parameterName);
          if (values != null) {
            for (String value : values) {
              if (StringUtils.isNotEmpty(value)) {
                if (queryParams.length() > 0) {
                  queryParams.append('&');
                }
                queryParams.append(parameterName);
                queryParams.append('=');
                queryParams.append(value);
              }
            }
          }
        }
      }
    }

    // build complete url
    if (queryParams.length() > 0) {
      urlBuilder.append('?');
      urlBuilder.append(queryParams);
    }
    return urlBuilder.toString();
  }

  String setFragment(String url, String fragment) {
    if (StringUtils.isEmpty(url)) {
      return url;
    }

    // strip off anchor if already present
    StringBuilder urlBuilder;
    int index = url.indexOf('#');
    if (index >= 0) {
      urlBuilder = new StringBuilder(url.substring(0, index));
    }
    else {
      urlBuilder = new StringBuilder(url);
    }

    // prepend "#" for anchor if not present
    if (StringUtils.isNotBlank(fragment)) {
      if (!StringUtils.startsWith(fragment, "#")) {
        urlBuilder.append('#');
      }
      urlBuilder.append(fragment);
    }

    return urlBuilder.toString();
  }

}
