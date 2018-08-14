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
package io.wcm.handler.link.type.helpers;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;

import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.url.UrlHandler;

/**
 * Options to influence the link resolving process from {@link InternalLinkResolver}.
 */
public final class InternalLinkResolverOptions {

  private String primaryLinkRefProperty = LinkNameConstants.PN_LINK_CONTENT_REF;
  private boolean rewritePathToContext = true;
  private boolean useTargetContext;

  /**
   * Primary ink reference property.
   * @return Name of the property in which the primary link reference is stored
   */
  public String getPrimaryLinkRefProperty() {
    return this.primaryLinkRefProperty;
  }

  /**
   * Primary ink reference property.
   * @param value Name of the property in which the primary link reference is stored
   * @return this
   */
  public @NotNull InternalLinkResolverOptions primaryLinkRefProperty(String value) {
    this.primaryLinkRefProperty = value;
    return this;
  }

  /**
   * Rewrite path to context.
   * @return If set to true it is ensured that all links target only pages inside the same inner-most configuration
   *         scope, which is usually the same site/language. All link paths referencing pages outside this content
   *         subtree are rewritten via {@link UrlHandler#rewritePathToContext(Resource)} with the root path of the
   *         inner-most configuration scope/site and then resolved.
   */
  public boolean isRewritePathToContext() {
    return this.rewritePathToContext;
  }

  /**
   * Rewrite path to context.
   * @param value If set to true it is ensured that all links target only pages inside the same inner-most configuration
   *          scope, which is usually the same site/language. All link paths referencing pages outside this content
   *          subtree are rewritten via {@link UrlHandler#rewritePathToContext(Resource)} with the root path of the
   *          inner-most configuration scope/site and then resolved.
   * @return this
   */
  public @NotNull InternalLinkResolverOptions rewritePathToContext(boolean value) {
    this.rewritePathToContext = value;
    return this;
  }


  /**
   * User target context for URL building.
   * @return If set to true an {@link io.wcm.handler.url.UrlHandler} with configuration from the configuration scope
   *         (e.g. site/language) from the target page is used to build the link URL to the internal page, and not the
   *         URL handler of the current resource's configuration scope (site/language). This makes only sense if
   *         additional the flag "rewritePathToContext" is set to false.
   */
  public boolean isUseTargetContext() {
    return this.useTargetContext;
  }

  /**
   * User target context for URL building.
   * @param value If set to true an {@link io.wcm.handler.url.UrlHandler} with configuration from the configuration
   *          scope
   *          (e.g. site/language) from the target page is used to build the link URL to the internal page, and not the
   *          URL handler of the current resource's configuration scope (site/language). This makes only sense if
   *          additional the flag "rewritePathToContext" is set to false.
   * @return this
   */
  public @NotNull InternalLinkResolverOptions useTargetContext(boolean value) {
    this.useTargetContext = value;
    return this;
  }

}
