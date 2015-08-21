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

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.LinkRequest;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.type.helpers.InternalLinkResolver;
import io.wcm.handler.link.type.helpers.InternalLinkResolverOptions;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Implementation of {@link io.wcm.handler.link.spi.LinkType} for internal links with supports
 * links between different configuration scopes (which normally relates to different sites/languages).
 * Internal links are links to content pages inside the CMS.
 * <p>
 * This link type ensures that links that are referenced from other configuration scopes (sites/languages) are resolved
 * using the URL handler configuration of the target scope, e.g. with the Site URL from the other site.
 * </p>
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ProviderType
public final class InternalCrossScopeLinkType extends AbstractLinkType {

  /**
   * Link type ID
   */
  public static final String ID = "internalCrossScope";

  private final InternalLinkResolverOptions resolverOptions = new InternalLinkResolverOptions()
  .primaryLinkRefProperty(getPrimaryLinkRefProperty())
  .rewritePathToContext(false)
  .useTargetContext(true);

  @Self
  private InternalLinkResolver internalLinkResolver;

  /**
   * @return Link type ID (is stored as identifier in repository)
   */
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getPrimaryLinkRefProperty() {
    return LinkNameConstants.PN_LINK_CONTENT_REF;
  }

  @Override
  public boolean accepts(String linkRef) {
    // accept as internal link if the ref starts with "/content/"
    return StringUtils.startsWith(linkRef, "/content/")
        && !MediaLinkType.isDefaultMediaContentPath(linkRef);
  }

  @Override
  public boolean accepts(LinkRequest linkRequest) {
    if (internalLinkResolver.acceptPage(linkRequest.getPage(), resolverOptions)) {
      // support direct links to pages
      return true;
    }
    // check for matching link type ID in link resource
    return super.accepts(linkRequest);
  }

  @Override
  public Link resolveLink(Link link) {
    return internalLinkResolver.resolveLink(link, resolverOptions);
  }

  /**
   * Get synthetic link resource for this link type.
   * @param resourceResolver Resource resolver
   * @param pageRef Path to target page
   * @return Synthetic link resource
   */
  public static Resource getSyntheticLinkResource(ResourceResolver resourceResolver, String pageRef) {
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
