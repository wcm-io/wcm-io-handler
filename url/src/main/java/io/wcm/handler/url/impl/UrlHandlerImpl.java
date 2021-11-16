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

import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.jetbrains.annotations.NotNull;

import com.day.cq.wcm.api.Page;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.handler.url.UrlBuilder;
import io.wcm.handler.url.UrlHandler;
import io.wcm.handler.url.UrlMode;
import io.wcm.handler.url.impl.clientlib.ClientlibProxyRewriter;
import io.wcm.handler.url.spi.UrlHandlerConfig;
import io.wcm.sling.commons.request.RequestParam;
import io.wcm.sling.models.annotations.AemObject;
import io.wcm.wcm.commons.instancetype.InstanceTypeService;
import io.wcm.wcm.commons.util.Path;

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
  private InstanceTypeService instanceTypeService;
  @OSGiService
  private ClientlibProxyRewriter clientlibProxyRewriter;

  // optional injections (only available if called inside a request)
  @SlingObject(injectionStrategy = InjectionStrategy.OPTIONAL)
  private SlingHttpServletRequest request;
  @AemObject(injectionStrategy = InjectionStrategy.OPTIONAL)
  private Page currentPage;

  @Override
  public @NotNull UrlBuilder get(@NotNull String path) {
    return new UrlBuilderImpl(path, this);
  }

  @Override
  public @NotNull UrlBuilder get(@NotNull Resource resource) {
    return new UrlBuilderImpl(resource, this);
  }

  @Override
  public @NotNull UrlBuilder get(@NotNull Page page) {
    return new UrlBuilderImpl(page, this);
  }

  @Override
  @SuppressWarnings({ "null", "unused" })
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public String rewritePathToContext(@NotNull final Resource resource) {
    if (resource == null) {
      return null;
    }
    if (currentPage != null) {
      return rewritePathToContext(resource, currentPage.adaptTo(Resource.class));
    }
    else {
      return resource.getPath();
    }
  }

  @Override
  @SuppressWarnings({ "null", "unused" })
  public String rewritePathToContext(@NotNull final Resource resource, @NotNull final Resource contextResource) {
    if (resource == null) {
      return null;
    }
    if (contextResource == null) {
      return resource.getPath();
    }

    // split up paths
    String[] contextPathParts = StringUtils.split(Path.getOriginalPath(contextResource.getPath(), resolver), "/");
    String[] pathParts = StringUtils.split(Path.getOriginalPath(resource.getPath(), resolver), "/");

    // check if both paths are valid - return unchanged path if not
    int siteRootLevelContextPath = urlHandlerConfig.getSiteRootLevel(contextResource);
    int siteRootLevelPath = urlHandlerConfig.getSiteRootLevel(resource);
    if ((contextPathParts.length <= siteRootLevelContextPath)
        || (pathParts.length <= siteRootLevelPath)
        || !StringUtils.equals(contextPathParts[0], "content")
        || !StringUtils.equals(pathParts[0], "content")) {
      return resource.getPath();
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

    String externalizedUrl;
    if (urlHandlerConfig.isHostProvidedBySlingMapping()) {
      // apply sling mapping with host
      externalizedUrl = Externalizer.externalizeUrlWithHost(url, resolver, request);
    }
    else {
      // apply sling mapping, namespace mangling and add webapp context path if required
      externalizedUrl = Externalizer.externalizeUrl(url, resolver, request);
    }
    if (externalizedUrl != null && !Externalizer.isExternalized(externalizedUrl)) {
      // add link URL prefix (scheme/hostname or integrator placeholder) if required
      String linkUrlPrefix = getLinkUrlPrefix(urlMode, targetPage);
      externalizedUrl = StringUtils.defaultString(linkUrlPrefix) + externalizedUrl; //NOPMD
    }
    return externalizedUrl;
  }

  @SuppressWarnings("null")
  private String getLinkUrlPrefix(UrlMode urlMode, Page targetPage) {
    UrlMode mode = ObjectUtils.defaultIfNull(urlMode, urlHandlerConfig.getDefaultUrlMode());
    return mode.getLinkUrlPrefix(self, instanceTypeService.getRunModes(), currentPage, targetPage);
  }

  String externalizeResourceUrl(final String url, final Resource targetResource, final UrlMode urlMode) {

    // check for empty path
    if (StringUtils.isEmpty(url)) {
      return null;
    }

    // do not externalize URLs again that are already externalized
    // do not externalize URLs that do not start with "/" (they are no content paths in that case)
    if (Externalizer.isExternalized(url) || !Externalizer.isExternalizable(url)) {
      return url;
    }

    // try to resolve the target resource from url/path if it was not given initially (only below /content)
    Resource resource = targetResource;
    if (resource == null && StringUtils.startsWith(url, "/content/")) {
      resource = resolver.resolve(url); // accept NonExistingResource as well
    }

    // check for reference to static resource from proxied client library
    String externalizedUrl = clientlibProxyRewriter.rewriteStaticResourcePath(url);

    if (urlHandlerConfig.isHostProvidedBySlingMapping()) {
      // apply sling mapping with host
      externalizedUrl = Externalizer.externalizeUrlWithHost(externalizedUrl, resolver, request);
    }
    else {
      // apply sling mapping when externalizing URLs
      externalizedUrl = Externalizer.externalizeUrl(externalizedUrl, resolver, request);
    }
    if (externalizedUrl != null && !Externalizer.isExternalized(externalizedUrl)) {
      // add resource URL prefix (scheme/hostname or integrator placeholder) if required
      String resourceUrlPrefix = getResourceUrlPrefix(urlMode, resource);
      externalizedUrl = StringUtils.defaultString(resourceUrlPrefix) + externalizedUrl; //NOPMD
    }
    return externalizedUrl;
  }

  @SuppressWarnings("null")
  private String getResourceUrlPrefix(UrlMode urlMode, Resource targetResource) {
    UrlMode mode = ObjectUtils.defaultIfNull(urlMode, urlHandlerConfig.getDefaultUrlMode());
    return mode.getResourceUrlPrefix(self, instanceTypeService.getRunModes(), currentPage, targetResource);
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
