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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.handler.commons.dom.Anchor;
import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.spi.LinkMarkupBuilder;

/**
 * Very basic implementation of {@link LinkMarkupBuilder}
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ProviderType
public final class SimpleLinkMarkupBuilder implements LinkMarkupBuilder {

  @Override
  public boolean accepts(@NotNull Link link) {
    return link.isValid() && StringUtils.isNotEmpty(link.getUrl());
  }

  @Override
  public Anchor build(@NotNull Link link) {
    ValueMap props = link.getLinkRequest().getResourceProperties();

    // build anchor
    Anchor anchor = new Anchor(link.getUrl());

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
