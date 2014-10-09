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
package io.wcm.handler.link.markup;

import io.wcm.handler.commons.dom.Anchor;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.spi.LinkMarkupBuilder;
import io.wcm.sling.models.annotations.AemObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;

import com.day.cq.wcm.api.WCMMode;

/**
 * Very basic implementation of {@link LinkMarkupBuilder}
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
public final class SimpleLinkMarkupBuilder implements LinkMarkupBuilder {

  @AemObject
  private WCMMode wcmMode;

  @Override
  public boolean accepts(Link link) {
    // can generate markup for all links - even if link url is null
    return true;
  }

  @Override
  public Anchor build(Link link) {
    ValueMap props = link.getLinkRequest().getResourceProperties();

    // If link reference is invalid - special handling in edit mode:
    // use invalid content reference for anchor to allow CQ default handling to mark link as invalid
    String linkUrl = link.getUrl();
    if (link.isLinkReferenceInvalid() && wcmMode == WCMMode.EDIT) {
      linkUrl = LinkHandler.INVALID_LINK;
    }

    // build no anchor if link url is null and not in edit mode
    if (linkUrl == null) {
      return null;
    }

    // build anchor
    Anchor anchor = new Anchor(linkUrl);

    // window target
    String target = props.get(LinkNameConstants.PN_LINK_WINDOW_TARGET, String.class);
    if (StringUtils.isNotEmpty(target) && !"_self".equals(target)) {
      anchor.setTarget(target);
    }

    // all other link reference properties like popup windows settings, user tracking
    // have to be handled by project-specific implementations of LinkMarkupBuilder

    return anchor;
  }

}
