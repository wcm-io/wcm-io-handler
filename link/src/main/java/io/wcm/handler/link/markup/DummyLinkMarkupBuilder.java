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
import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.spi.LinkMarkupBuilder;
import io.wcm.sling.models.annotations.AemObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.WCMMode;

/**
 * Very basic implementation of {@link LinkMarkupBuilder}
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ProviderType
public final class DummyLinkMarkupBuilder implements LinkMarkupBuilder {

  @AemObject(optional = true)
  private WCMMode wcmMode;

  @Override
  public boolean accepts(Link link) {
    return (!link.isValid() || link.getUrl() == null)
        && wcmMode == WCMMode.EDIT
        && link.getLinkRequest().isDummyLink();
  }

  @Override
  public Anchor build(Link link) {
    // build anchor
    String url = StringUtils.defaultString(link.getLinkRequest().getDummyLinkUrl(), LinkHandler.INVALID_LINK);
    return new Anchor(url);
  }

}
