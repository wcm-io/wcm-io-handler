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

import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.url.UrlHandler;

/**
 * Options to influence the link resolving process from {@link InternalLinkResolver}.
 */
public final class InternalLinkResolverOptions {

  private String primaryLinkRefProperty = LinkNameConstants.PN_LINK_CONTENT_REF;
  private boolean rewritePathToContext = true;

  /**
   * @return Name of the property in which the primary link reference is stored
   */
  public String getPrimaryLinkRefProperty() {
    return this.primaryLinkRefProperty;
  }

  /**
   * @param value Name of the property in which the primary link reference is stored
   * @return this
   */
  public InternalLinkResolverOptions primaryLinkRefProperty(String value) {
    this.primaryLinkRefProperty = value;
    return this;
  }

  /**
   * @return If set to true it is ensured that all links target only pages inside the same inner-most configuration
   *         scope, which is usually the same site/language. All link paths referencing pages outside this content
   *         subtree are rewritten via {@link UrlHandler#rewritePathToContext(String)} with the root path of the
   *         inner-most configuration scope/site and then resolved.
   */
  public boolean isRewritePathToContext() {
    return this.rewritePathToContext;
  }

  /**
   * @param value If set to true it is ensured that all links target only pages inside the same inner-most configuration
   *          scope, which is usually the same site/language. All link paths referencing pages outside this content
   *          subtree are rewritten via {@link UrlHandler#rewritePathToContext(String)} with the root path of the
   *          inner-most configuration scope/site and then resolved.
   * @return this
   */
  public InternalLinkResolverOptions rewritePathToContext(boolean value) {
    this.rewritePathToContext = value;
    return this;
  }

}
