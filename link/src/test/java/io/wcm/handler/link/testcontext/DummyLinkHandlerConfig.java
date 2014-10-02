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
package io.wcm.handler.link.testcontext;

import io.wcm.config.api.annotations.Application;
import io.wcm.handler.link.AbstractLinkHandlerConfig;
import io.wcm.handler.link.LinkHandlerConfig;
import io.wcm.handler.link.LinkMarkupBuilder;
import io.wcm.handler.link.LinkType;
import io.wcm.handler.link.markup.SimpleLinkMarkupBuilder;
import io.wcm.handler.link.type.ExternalLinkType;
import io.wcm.handler.link.type.InternalLinkType;
import io.wcm.handler.link.type.MediaLinkType;
import io.wcm.handler.url.UrlParams;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;

/**
 * Dummy link configuration
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
}, adapters = LinkHandlerConfig.class)
@Application(UrlParams.APPLICATION_ID)
public class DummyLinkHandlerConfig extends AbstractLinkHandlerConfig {

  private static final List<Class<? extends LinkType>> LINK_TYPES = ImmutableList.<Class<? extends LinkType>>of(
      InternalLinkType.class,
      ExternalLinkType.class,
      MediaLinkType.class
      );

  private static final List<Class<? extends LinkMarkupBuilder>> LINK_MARKUP_BUILDERS = ImmutableList.<Class<? extends LinkMarkupBuilder>>of(
      SimpleLinkMarkupBuilder.class
      );

  @Override
  public List<Class<? extends LinkType>> getLinkTypes() {
    return LINK_TYPES;
  }

  @Override
  public List<Class<? extends LinkMarkupBuilder>> getLinkMarkupBuilders() {
    return LINK_MARKUP_BUILDERS;
  }


  @Override
  public boolean isValidLinkTarget(Page page) {

    // check for non-linkable templates
    String templatePath = page.getProperties().get(NameConstants.PN_TEMPLATE, String.class);
    if (StringUtils.equals(templatePath, DummyAppTemplate.STRUCTURE_ELEMENT.getTemplatePath())) {
      return false;
    }

    return super.isValidLinkTarget(page);
  }

  @Override
  public boolean isRedirect(Page page) {
    String templatePath = page.getProperties().get(NameConstants.PN_TEMPLATE, String.class);
    return StringUtils.equals(templatePath, DummyAppTemplate.REDIRECT.getTemplatePath());
  }

}
