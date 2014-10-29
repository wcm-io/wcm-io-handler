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
import io.wcm.handler.url.UrlMode;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import com.day.cq.wcm.api.Page;

/**
 * Default implementation or {@link UrlBuilder}.
 */
final class UrlBuilderImpl implements UrlBuilder {

  private final UrlHandlerImpl urlHandler;
  private final String path;
  private final Resource resource;
  private final Page page;

  private String selectors;
  private String extension;
  private String suffix;
  private String queryString;
  private Set<String> inheritableParameterNames;
  private String fragment;
  private UrlMode urlMode;

  /**
   * @param path Path for URL (without any hostname, scheme, extension, suffix etc.)
   * @param urlHandler Url handler instance
   */
  public UrlBuilderImpl(String path, UrlHandlerImpl urlHandler) {
    this.path = path;
    this.resource = null;
    this.page = null;
    this.urlHandler = urlHandler;
  }

  /**
   * @param resource Resource
   * @param urlHandler Url handler instance
   */
  public UrlBuilderImpl(Resource resource, UrlHandlerImpl urlHandler) {
    this.path = resource != null ? resource.getPath() : null;
    this.resource = resource;
    this.page = null;
    this.urlHandler = urlHandler;
  }

  /**
   * @param page Page
   * @param urlHandler Url handler instance
   */
  public UrlBuilderImpl(Page page, UrlHandlerImpl urlHandler) {
    this.path = page != null ? page.getPath() : null;
    this.resource = null;
    this.page = page;
    this.urlHandler = urlHandler;
  }

  @Override
  public UrlBuilder selectors(String value) {
    this.selectors = value;
    return this;
  }

  @Override
  public UrlBuilder extension(String value) {
    this.extension = value;
    return this;
  }

  @Override
  public UrlBuilder suffix(String value) {
    this.suffix = value;
    return this;
  }

  @Override
  public UrlBuilder queryString(String value) {
    this.queryString = value;
    this.inheritableParameterNames = null;
    return this;
  }

  @Override
  public UrlBuilder queryString(String value, Set<String> inheritableParamNames) {
    this.queryString = value;
    this.inheritableParameterNames = inheritableParamNames;
    return this;
  }

  @Override
  public UrlBuilder fragment(String value) {
    this.fragment = value;
    return this;
  }

  @Override
  public UrlBuilder urlMode(UrlMode value) {
    this.urlMode = value;
    return this;
  }

  @Override
  public String build() {
    String url = urlHandler.buildUrl(path, selectors, extension, suffix);
    if (StringUtils.isNotEmpty(queryString) || inheritableParameterNames != null) {
      url = urlHandler.appendQueryString(url, queryString, inheritableParameterNames);
    }
    if (StringUtils.isNotEmpty(fragment)) {
      url = urlHandler.setFragment(url, fragment);
    }
    return url;
  }

  @Override
  public String buildExternalLinkUrl() {
    return buildExternalLinkUrl(null);
  }

  @Override
  public String buildExternalLinkUrl(Page targetPage) {
    Page targetPageToUse = targetPage;
    if (targetPageToUse == null) {
      targetPageToUse = page;
    }
    if (targetPageToUse == null && resource != null) {
      targetPageToUse = resource.adaptTo(Page.class);
    }
    String url = build();
    return urlHandler.externalizeLinkUrl(url, targetPageToUse, urlMode);
  }

  @Override
  public String buildExternalResourceUrl() {
    return buildExternalResourceUrl(null);
  }

  @Override
  public String buildExternalResourceUrl(Resource targetResource) {
    Resource targetResourceToUse = targetResource;
    if (targetResourceToUse == null) {
      targetResourceToUse = resource;
    }
    if (targetResourceToUse == null && page != null) {
      targetResourceToUse = page.adaptTo(Resource.class);
    }
    String url = build();
    return urlHandler.externalizeResourceUrl(url, targetResourceToUse, urlMode);
  }

}
