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

import com.day.cq.wcm.api.Page;

/**
 * Default implementation or {@link UrlBuilder}.
 */
final class UrlBuilderImpl implements UrlBuilder {

  private final UrlHandlerImpl urlHandler;

  private final String path;
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
  public String buildExternalLinkUrl(Page targetPage) {
    String url = build();
    return urlHandler.externalizeLinkUrl(url, targetPage, urlMode);
  }

  @Override
  public String buildExternalResourceUrl() {
    String url = build();
    return urlHandler.externalizeResourceUrl(url, urlMode);
  }

}
