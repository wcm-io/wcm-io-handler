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

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkBuilder;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.LinkRequest;
import io.wcm.handler.link.spi.LinkHandlerConfig;
import io.wcm.handler.link.spi.LinkMarkupBuilder;
import io.wcm.handler.link.spi.LinkProcessor;
import io.wcm.handler.link.spi.LinkType;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.models.annotations.AemObject;

import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.day.cq.wcm.api.Page;

/**
 * Default implementation of a {@link LinkHandler}
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
}, adapters = LinkHandler.class)
public final class LinkHandlerImpl implements LinkHandler {

  @Self
  private Adaptable adaptable;
  @Self
  private LinkHandlerConfig linkHandlerConfig;
  @AemObject
  private Page currentPage;

  @Override
  public LinkBuilder get(Resource resource) {
    return new LinkBuilderImpl(resource, this);
  }

  @Override
  public LinkBuilder get(Page page) {
    return new LinkBuilderImpl(page, this);
  }

  @Override
  public LinkBuilder get(LinkRequest linkRequest) {
    return new LinkBuilderImpl(linkRequest, this);
  }

  /**
   * Resolves the link
   * @param linkRequest Link request
   * @return Link metadata (never null)
   */
  Link processRequest(LinkRequest linkRequest) {

    // detect link type - first accepting wins
    LinkType linkType = null;
    List<Class<? extends LinkType>> linkTypes = linkHandlerConfig.getLinkTypes();
    if (linkTypes == null || linkTypes.size() == 0) {
      throw new RuntimeException("No link types defined.");
    }
    for (Class<? extends LinkType> candidateLinkTypeClass : linkTypes) {
      LinkType candidateLinkType = AdaptTo.notNull(adaptable, candidateLinkTypeClass);
      if (candidateLinkType.accepts(linkRequest)) {
        linkType = candidateLinkType;
        break;
      }
    }
    Link link = new Link(linkType, linkRequest);

    // preprocess link before resolving
    List<Class<? extends LinkProcessor>> linkPreProcessors = linkHandlerConfig.getPreProcessors();
    if (linkPreProcessors != null) {
      for (Class<? extends LinkProcessor> processorClass : linkPreProcessors) {
        LinkProcessor processor = AdaptTo.notNull(adaptable, processorClass);
        link = processor.process(link);
        if (link == null) {
          throw new RuntimeException("LinkPreProcessor '" + processor + "' returned null, page '" + currentPage.getPath() + "'.");
        }
      }
    }

    // resolve link
    if (linkType != null) {
      link = linkType.resolveLink(link);
      if (link == null) {
        throw new RuntimeException("LinkType '" + linkType + "' returned null, page '" + currentPage.getPath() + "'.");
      }
    }

    // generate markup (if markup builder is available) - first accepting wins
    List<Class<? extends LinkMarkupBuilder>> linkMarkupBuilders = linkHandlerConfig.getMarkupBuilders();
    if (linkMarkupBuilders != null) {
      for (Class<? extends LinkMarkupBuilder> linkMarkupBuilderClass : linkMarkupBuilders) {
        LinkMarkupBuilder linkMarkupBuilder = AdaptTo.notNull(adaptable, linkMarkupBuilderClass);
        if (linkMarkupBuilder.accepts(link)) {
          link.setAnchor(linkMarkupBuilder.build(link));
          break;
        }
      }
    }

    // postprocess link after resolving
    List<Class<? extends LinkProcessor>> linkPostProcessors = linkHandlerConfig.getPostProcessors();
    if (linkPostProcessors != null) {
      for (Class<? extends LinkProcessor> processorClass : linkPostProcessors) {
        LinkProcessor processor = AdaptTo.notNull(adaptable, processorClass);
        link = processor.process(link);
        if (link == null) {
          throw new RuntimeException("LinkPostProcessor '" + processor + "' returned null, page '" + currentPage.getPath() + "'.");
        }
      }
    }

    return link;
  }

}
