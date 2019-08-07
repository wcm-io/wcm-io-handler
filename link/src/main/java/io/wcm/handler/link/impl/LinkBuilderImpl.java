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

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;

import com.day.cq.wcm.api.Page;

import io.wcm.handler.commons.dom.Anchor;
import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkArgs;
import io.wcm.handler.link.LinkBuilder;
import io.wcm.handler.link.LinkRequest;
import io.wcm.handler.url.UrlMode;

/**
 * Default implementation or {@link LinkBuilder}.
 */
final class LinkBuilderImpl implements LinkBuilder {

  private final LinkHandlerImpl linkHandler;

  private final Resource resource;
  private final Page page;
  private final String reference;
  private LinkArgs linkArgs = new LinkArgs();

  LinkBuilderImpl(Resource resource, LinkHandlerImpl linkHandler) {
    this.resource = resource;
    this.page = null;
    this.reference = null;
    this.linkHandler = linkHandler;
  }

  LinkBuilderImpl(Page page, LinkHandlerImpl linkHandler) {
    this.resource = null;
    this.page = page;
    this.reference = null;
    this.linkHandler = linkHandler;
  }

  LinkBuilderImpl(String reference, LinkHandlerImpl linkHandler) {
    this.resource = null;
    this.page = null;
    this.reference = reference;
    this.linkHandler = linkHandler;
  }

  LinkBuilderImpl(LinkRequest linkRequest, LinkHandlerImpl linkHandler) {
    if (linkRequest == null) {
      throw new IllegalArgumentException("Link request is null.");
    }
    this.resource = linkRequest.getResource();
    this.page = linkRequest.getPage();
    this.reference = linkRequest.getReference();
    this.linkHandler = linkHandler;
    // clone link args to make sure the original object is not modified
    this.linkArgs = linkRequest.getLinkArgs().clone();
  }

  @Override
  public @NotNull LinkBuilder args(LinkArgs value) {
    if (value == null) {
      throw new IllegalArgumentException("LinkArgs is null.");
    }
    // clone link args to make sure the original object is not modified
    this.linkArgs = value.clone();
    return this;
  }

  @Override
  public @NotNull LinkBuilder urlMode(UrlMode value) {
    this.linkArgs.urlMode(value);
    return this;
  }

  @Override
  public @NotNull LinkBuilder dummyLink(boolean value) {
    this.linkArgs.dummyLink(value);
    return this;
  }

  @Override
  public @NotNull LinkBuilder dummyLinkUrl(String value) {
    this.linkArgs.dummyLinkUrl(value);
    return this;
  }

  @Override
  public @NotNull LinkBuilder selectors(String value) {
    this.linkArgs.selectors(value);
    return this;
  }

  @Override
  public @NotNull LinkBuilder extension(String value) {
    this.linkArgs.extension(value);
    return this;
  }

  @Override
  public @NotNull LinkBuilder suffix(String value) {
    this.linkArgs.suffix(value);
    return this;
  }

  @Override
  public @NotNull LinkBuilder queryString(String value) {
    this.linkArgs.queryString(value);
    return this;
  }

  @Override
  public @NotNull LinkBuilder fragment(String value) {
    this.linkArgs.fragment(value);
    return this;
  }

  @Override
  public @NotNull Link build() {
    LinkRequest request = new LinkRequest(this.resource, this.page, this.reference, this.linkArgs);
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
