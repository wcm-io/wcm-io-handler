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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.handler.commons.dom.Anchor;
import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkArgs;
import io.wcm.handler.link.LinkBuilder;
import io.wcm.handler.link.LinkComponentPropertyResolver;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.LinkRequest;
import io.wcm.handler.url.UrlMode;
import io.wcm.wcm.commons.component.ComponentPropertyResolverFactory;

/**
 * Default implementation or {@link LinkBuilder}.
 */
final class LinkBuilderImpl implements LinkBuilder {

  private final LinkHandlerImpl linkHandler;

  private final Resource resource;
  private final Page page;
  private final String reference;
  private LinkArgs linkArgs = new LinkArgs();

  private static final Logger log = LoggerFactory.getLogger(LinkBuilderImpl.class);

  LinkBuilderImpl(@Nullable Resource resource, @NotNull LinkHandlerImpl linkHandler,
      @Nullable ComponentPropertyResolverFactory componentPropertyResolverFactory) {
    this.resource = resource;
    this.page = null;
    this.reference = null;
    this.linkHandler = linkHandler;

    if (resource != null) {
      // resolve default settings from content policies and component properties
      try (LinkComponentPropertyResolver resolver = getLinkComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
        linkArgs.linkTargetUrlFallbackProperty(resolver.getLinkTargetUrlFallbackProperty());
        linkArgs.linkTargetWindowTargetFallbackProperty(resolver.getLinkTargetWindowTargetFallbackProperty());
      }
      catch (Exception ex) {
        log.warn("Error closing component property resolver.", ex);
      }

      // get window target from resource
      linkArgs.windowTarget(getWindowTargetFromResource(resource, linkArgs));
    }
  }

  @SuppressWarnings("deprecation")
  private static LinkComponentPropertyResolver getLinkComponentPropertyResolver(@NotNull Resource resource,
      @Nullable ComponentPropertyResolverFactory componentPropertyResolverFactory) {
    if (componentPropertyResolverFactory != null) {
      return new LinkComponentPropertyResolver(resource, componentPropertyResolverFactory);
    }
    else {
      // fallback mode if ComponentPropertyResolverFactory is not available
      return new LinkComponentPropertyResolver(resource);
    }
  }

  private static String getWindowTargetFromResource(@NotNull Resource resource, @NotNull LinkArgs linkArgs) {
    ValueMap props = resource.getValueMap();
    String windowTarget = null;

    // check if a link target URL is set in the fallback property
    String[] linkTargetWindowTargetFallbackProperty = linkArgs.getLinkTargetWindowTargetFallbackProperty();
    if (linkTargetWindowTargetFallbackProperty != null) {
      for (String propertyName : linkTargetWindowTargetFallbackProperty) {
        windowTarget = props.get(propertyName, String.class);
        if (StringUtils.isNotBlank(windowTarget)) {
          break;
        }
      }
    }

    // read from resource with default property name
    if (StringUtils.isBlank(windowTarget)) {
      windowTarget = props.get(LinkNameConstants.PN_LINK_WINDOW_TARGET, String.class);
    }
    return windowTarget;
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
  @SuppressWarnings({ "null", "unused" })
  public @NotNull LinkBuilder args(@NotNull LinkArgs value) {
    if (value == null) {
      throw new IllegalArgumentException("LinkArgs is null.");
    }
    // clone link args to make sure the original object is not modified
    this.linkArgs = value.clone();
    return this;
  }

  @Override
  public @NotNull LinkBuilder urlMode(@Nullable UrlMode value) {
    this.linkArgs.urlMode(value);
    return this;
  }

  @Override
  public @NotNull LinkBuilder dummyLink(boolean value) {
    this.linkArgs.dummyLink(value);
    return this;
  }

  @Override
  public @NotNull LinkBuilder dummyLinkUrl(@Nullable String value) {
    this.linkArgs.dummyLinkUrl(value);
    return this;
  }

  @Override
  public @NotNull LinkBuilder selectors(@Nullable String value) {
    this.linkArgs.selectors(value);
    return this;
  }

  @Override
  public @NotNull LinkBuilder extension(@Nullable String value) {
    this.linkArgs.extension(value);
    return this;
  }

  @Override
  public @NotNull LinkBuilder suffix(@Nullable String value) {
    this.linkArgs.suffix(value);
    return this;
  }

  @Override
  public @NotNull LinkBuilder queryString(@Nullable String value) {
    this.linkArgs.queryString(value);
    return this;
  }

  @Override
  public @NotNull LinkBuilder fragment(@Nullable String value) {
    this.linkArgs.fragment(value);
    return this;
  }

  @Override
  public @NotNull LinkBuilder windowTarget(@Nullable String value) {
    this.linkArgs.windowTarget(value);
    return this;
  }

  @Override
  @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
  public @NotNull LinkBuilder linkTargetUrlFallbackProperty(@NotNull String @Nullable... propertyNames) {
    this.linkArgs.linkTargetUrlFallbackProperty(propertyNames);
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
