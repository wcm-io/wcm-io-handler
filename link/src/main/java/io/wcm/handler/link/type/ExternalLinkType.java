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

import io.wcm.handler.link.LinkMetadata;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.SyntheticLinkResource;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;

/**
 * Default implementation of {@link io.wcm.handler.link.LinkType} for external links.
 * External links are links to destinations outside the CMS.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
public final class ExternalLinkType extends AbstractLinkType {

  /**
   * Link type ID
   */
  public static final String ID = "external";

  /**
   * @return Link type ID (is stored as identifier in repository)
   */
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getPrimaryLinkRefProperty() {
    return LinkNameConstants.PN_LINK_EXTERNAL_REF;
  }

  @Override
  public boolean accepts(String linkRef) {
    // accept as external link if the ref contains "://"
    return StringUtils.contains(linkRef, "://");
  }

  @Override
  public LinkMetadata resolveLink(LinkMetadata linkMetadata) {
    ValueMap props = linkMetadata.getLinkReference().getResourceProperties();

    // get external URL from link properties
    String linkUrl = props.get(LinkNameConstants.PN_LINK_EXTERNAL_REF, String.class);

    // check external link url
    if (StringUtils.isBlank(linkUrl)) {
      linkUrl = null;
    }

    // set link url
    linkMetadata.setLinkUrl(linkUrl);

    return linkMetadata;
  }

  /**
   * Get synthetic link resource for this link type.
   * @param resourceResolver Resource resolver
   * @param url Link URL
   * @return Synthetic link resource
   */
  public static Resource getSyntheticLinkResource(ResourceResolver resourceResolver, String url) {
    Map<String, Object> map = new HashMap<>();
    map.put(LinkNameConstants.PN_LINK_TYPE, ID);
    map.put(LinkNameConstants.PN_LINK_EXTERNAL_REF, url);
    return new SyntheticLinkResource(resourceResolver, map);
  }

}
