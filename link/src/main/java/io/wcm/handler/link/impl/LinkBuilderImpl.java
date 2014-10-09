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
package io.wcm.handler.link.impl;

import io.wcm.handler.commons.dom.Anchor;
import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkBuilder;
import io.wcm.handler.link.LinkRequest;
import io.wcm.handler.url.UrlMode;

import org.apache.sling.api.resource.Resource;

import com.day.cq.wcm.api.Page;

/**
 * Default implementation or {@link LinkBuilder}.
 */
final class LinkBuilderImpl implements LinkBuilder {

  private final LinkHandlerImpl linkHandler;

  private final Resource resource;
  private final Page page;

  private String selectors;
  private String extension;
  private String suffix;
  private String queryString;
  private String fragement;
  private UrlMode urlMode;

  public LinkBuilderImpl(Resource resource, LinkHandlerImpl linkHandler) {
    this.resource = resource;
    this.page = null;
    this.linkHandler = linkHandler;
  }

  public LinkBuilderImpl(Page page, LinkHandlerImpl linkHandler) {
    this.resource = null;
    this.page = page;
    this.linkHandler = linkHandler;
  }

  public LinkBuilderImpl(LinkRequest linkRequest, LinkHandlerImpl linkHandler) {
    this.resource = linkRequest.getResource();
    this.page = linkRequest.getPage();
    this.selectors = linkRequest.getSelectors();
    this.extension = linkRequest.getExtension();
    this.suffix = linkRequest.getSuffix();
    this.queryString = linkRequest.getQueryString();
    this.urlMode = linkRequest.getUrlMode();
    this.linkHandler = linkHandler;
  }

  @Override
  public LinkBuilder selectors(String value) {
    this.selectors = value;
    return this;
  }

  @Override
  public LinkBuilder extension(String value) {
    this.extension = value;
    return this;
  }

  @Override
  public LinkBuilder suffix(String value) {
    this.suffix = value;
    return this;
  }

  @Override
  public LinkBuilder queryString(String value) {
    this.queryString = value;
    return this;
  }

  @Override
  public LinkBuilder fragment(String value) {
    this.fragement = value;
    return this;
  }

  @Override
  public LinkBuilder urlMode(UrlMode value) {
    this.urlMode = value;
    return this;
  }

  @Override
  public Link build() {
    LinkRequest request = new LinkRequest(this.resource, this.page, this.urlMode, this.selectors,
        this.extension, this.suffix, this.queryString, this.fragement);
    return linkHandler.processRequest(request);
  }

  @Override
  public String buildMarkup() {
    return build().getMarkup();
  }

  @Override
  public Anchor buildAnchor() {
    return build().getAnchor();
  }

  @Override
  public String buildUrl() {
    return build().getUrl();
  }


}
