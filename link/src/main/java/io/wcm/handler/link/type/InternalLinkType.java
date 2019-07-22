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
package io.wcm.handler.link.type;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.LinkRequest;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.spi.LinkType;
import io.wcm.handler.link.type.helpers.InternalLinkResolver;
import io.wcm.handler.link.type.helpers.InternalLinkResolverOptions;
import io.wcm.handler.url.UrlHandler;

/**
 * Default implementation of {@link io.wcm.handler.link.spi.LinkType} for internal links.
 * Internal links are links to content pages inside the CMS.
 * <p>
 * This link type ensures all links target only pages inside the same inner-most configuration scope, which is usually
 * the same site/language. All link paths referencing pages outside this content subtree are rewritten via
 * {@link UrlHandler#rewritePathToContext(Resource)} with the root path of the inner-most configuration scope/site and
 * then resolved.
 * </p>
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ProviderType
public final class InternalLinkType extends LinkType {

  /**
   * Link type ID
   */
  public static final @NotNull String ID = "internal";

  private final @NotNull InternalLinkResolverOptions resolverOptions = new InternalLinkResolverOptions()
      .primaryLinkRefProperty(getPrimaryLinkRefProperty())
      .rewritePathToContext(true)
      .useTargetContext(false);

  @Self
  private InternalLinkResolver internalLinkResolver;

  /**
   * @return Link type ID (is stored as identifier in repository)
   */
  @Override
  public @NotNull String getId() {
    return ID;
  }

  @Override
  public @NotNull String getLabel() {
    return "Internal (same site)";
  }

  @Override
  public String getPrimaryLinkRefProperty() {
    return LinkNameConstants.PN_LINK_CONTENT_REF;
  }

  @Override
  public boolean accepts(@NotNull String linkRef) {
    // accept as internal link if the ref starts with "/content/"
    return StringUtils.startsWith(linkRef, "/content/")
        && !MediaLinkType.isDefaultMediaContentPath(linkRef);
  }

  @Override
  public boolean accepts(@NotNull LinkRequest linkRequest) {
    if (internalLinkResolver.acceptPage(linkRequest.getPage(), resolverOptions)) {
      // support direct links to pages
      return true;
    }
    // check for matching link type ID in link resource
    return super.accepts(linkRequest);
  }

  @Override
  public @NotNull Link resolveLink(@NotNull Link link) {
    return internalLinkResolver.resolveLink(link, resolverOptions);
  }

  /**
   * Get synthetic link resource for this link type.
   * @param resourceResolver Resource resolver
   * @param path Resource path. Can be a non-existing path, but the path should be located somewhere within the
   *          applications content paths to make sure the handler configuration looked up via context-aware services
   *          is the expected one.
   * @param pageRef Path to target page
   * @return Synthetic link resource
   */
  public static @NotNull Resource getSyntheticLinkResource(@NotNull ResourceResolver resourceResolver,
      @NotNull String path, @NotNull String pageRef) {
    Map<String, Object> map = new HashMap<>();
    map.put(LinkNameConstants.PN_LINK_TYPE, ID);
    map.put(LinkNameConstants.PN_LINK_CONTENT_REF, pageRef);
    return new SyntheticLinkResource(resourceResolver, path, map);
  }

  /**
   * Get synthetic link resource for this link type.
   * @param resourceResolver Resource resolver
   * @param pageRef Path to target page
   * @return Synthetic link resource
   * @deprecated Please use {@link #getSyntheticLinkResource(ResourceResolver, String, String)}
   */
  @Deprecated
  public static @NotNull Resource getSyntheticLinkResource(@NotNull ResourceResolver resourceResolver, @NotNull String pageRef) {
    Map<String, Object> map = new HashMap<>();
    map.put(LinkNameConstants.PN_LINK_TYPE, ID);
    map.put(LinkNameConstants.PN_LINK_CONTENT_REF, pageRef);
    return new SyntheticLinkResource(resourceResolver, map);
  }

  @Override
  public String toString() {
    return ID;
  }

}
