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

import io.wcm.handler.commons.dom.Anchor;
import io.wcm.handler.link.LinkArgsType;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.LinkHandlerConfig;
import io.wcm.handler.link.LinkMarkupBuilder;
import io.wcm.handler.link.LinkMetadata;
import io.wcm.handler.link.LinkMetadataProcessor;
import io.wcm.handler.link.LinkReference;
import io.wcm.handler.link.LinkType;
import io.wcm.handler.link.args.LinkArgs;
import io.wcm.handler.url.UrlMode;
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
  public Anchor getAnchor(Resource resource) {
    return getAnchor(new LinkReference(resource));
  }

  @Override
  public Anchor getAnchor(Resource resource, String selectors) {
    return getAnchor(new LinkReference(resource, new LinkArgs(selectors)));
  }

  @Override
  public Anchor getAnchor(Resource resource, String selectors, String extension) {
    return getAnchor(new LinkReference(resource, new LinkArgs(selectors, extension)));
  }

  @Override
  public Anchor getAnchor(Resource resource, String selectors, String extension, String suffix) {
    return getAnchor(new LinkReference(resource, new LinkArgs(selectors, extension, suffix)));
  }

  @Override
  public Anchor getAnchor(Resource resource, LinkArgsType linkArgs) {
    return getAnchor(new LinkReference(resource, linkArgs));
  }

  @Override
  public Anchor getAnchor(Page page) {
    return getAnchor(new LinkReference(page));
  }

  @Override
  public Anchor getAnchor(Page page, String selectors) {
    return getAnchor(new LinkReference(page, new LinkArgs(selectors)));
  }

  @Override
  public Anchor getAnchor(Page page, String selectors, String extension) {
    return getAnchor(new LinkReference(page, new LinkArgs(selectors, extension)));
  }

  @Override
  public Anchor getAnchor(Page page, String selectors, String extension, String suffix) {
    return getAnchor(new LinkReference(page, new LinkArgs(selectors, extension, suffix)));
  }

  @Override
  public Anchor getAnchor(Page page, LinkArgsType linkArgs) {
    return getAnchor(new LinkReference(page, linkArgs));
  }

  @Override
  public Anchor getAnchor(LinkReference linkReference) {
    return getLinkMetadata(linkReference).getAnchor();
  }

  @Override
  public String getLinkUrl(Resource resource) {
    return getLinkUrl(new LinkReference(resource));
  }

  @Override
  public String getLinkUrl(Resource resource, String selectors) {
    return getLinkUrl(new LinkReference(resource, new LinkArgs(selectors)));
  }

  @Override
  public String getLinkUrl(Resource resource, String selectors, String extension) {
    return getLinkUrl(new LinkReference(resource, new LinkArgs(selectors, extension)));
  }

  @Override
  public String getLinkUrl(Resource resource, String selectors, String extension, String suffix) {
    return getLinkUrl(new LinkReference(resource, new LinkArgs(selectors, extension, suffix)));
  }

  @Override
  public String getLinkUrl(Resource resource, String selectors, String extension, String suffix, UrlMode urlMode) {
    LinkReference linkReference = new LinkReference(resource,
        new LinkArgs(selectors, extension, suffix).setUrlMode(urlMode));
    return getLinkUrl(linkReference);
  }

  @Override
  public String getLinkUrl(Resource resource, LinkArgsType linkArgs) {
    return getLinkUrl(new LinkReference(resource, linkArgs));
  }

  @Override
  public String getLinkUrl(Page page) {
    return getLinkUrl(new LinkReference(page));
  }

  @Override
  public String getLinkUrl(Page page, String selectors) {
    return getLinkUrl(new LinkReference(page, new LinkArgs(selectors)));
  }

  @Override
  public String getLinkUrl(Page page, String selectors, String extension) {
    return getLinkUrl(new LinkReference(page, new LinkArgs(selectors, extension)));
  }

  @Override
  public String getLinkUrl(Page page, String selectors, String extension, String suffix) {
    return getLinkUrl(new LinkReference(page, new LinkArgs(selectors, extension, suffix)));
  }

  @Override
  public String getLinkUrl(Page page, String selectors, String extension, String suffix, UrlMode urlMode) {
    LinkReference linkReference = new LinkReference(page,
        new LinkArgs(selectors, extension, suffix).setUrlMode(urlMode));
    return getLinkUrl(linkReference);
  }

  @Override
  public String getLinkUrl(Page page, LinkArgsType linkArgs) {
    return getLinkUrl(new LinkReference(page, linkArgs));
  }

  @Override
  public String getLinkUrl(LinkReference linkReference) {
    return getLinkMetadata(linkReference).getLinkUrl();
  }

  @Override
  public LinkMetadata getLinkMetadata(Resource resource) {
    return getLinkMetadata(new LinkReference(resource));
  }

  @Override
  public LinkMetadata getLinkMetadata(Page page) {
    return getLinkMetadata(new LinkReference(page));
  }

  @Override
  public LinkMetadata getLinkMetadata(LinkReference linkReference) {
    return processLinkReference(linkReference);
  }

  /**
   * Resolves the link
   * @param linkReference Describes the link reference
   * @return Link metadata (never null)
   */
  protected LinkMetadata processLinkReference(LinkReference linkReference) {
    LinkReference orignalLinkReference = linkReference;

    // clone link reference to allow modifications on further processing without changing input parameters
    LinkReference clonedLinkReference;
    try {
      clonedLinkReference = (LinkReference)orignalLinkReference.clone();
    }
    catch (CloneNotSupportedException ex) {
      throw new RuntimeException("Unable to clone LinkReference instance.", ex);
    }

    // make sure linkargs is not null
    if (clonedLinkReference.getLinkArgs() == null) {
      clonedLinkReference.setLinkArgs(new LinkArgs());
    }

    // detect link type - first accepting wins
    LinkType linkType = null;
    List<Class<? extends LinkType>> linkTypes = linkHandlerConfig.getLinkTypes();
    if (linkTypes == null || linkTypes.size() == 0) {
      throw new RuntimeException("No link types defined.");
    }
    for (Class<? extends LinkType> candidateLinkTypeClass : linkTypes) {
      LinkType candidateLinkType = AdaptTo.notNull(adaptable, candidateLinkTypeClass);
      if (candidateLinkType.accepts(clonedLinkReference)) {
        linkType = candidateLinkType;
        break;
      }
    }
    LinkMetadata linkMetadata = new LinkMetadata(orignalLinkReference, clonedLinkReference, linkType);

    // preprocess link before resolving
    List<Class<? extends LinkMetadataProcessor>> linkPreProcessors = linkHandlerConfig.getLinkMetadataPreProcessors();
    if (linkPreProcessors != null) {
      for (Class<? extends LinkMetadataProcessor> processorClass : linkPreProcessors) {
        LinkMetadataProcessor processor = AdaptTo.notNull(adaptable, processorClass);
        linkMetadata = processor.process(linkMetadata);
        if (linkMetadata == null) {
          throw new RuntimeException("LinkMetadataPreProcessor '" + processor + "' returned null, page '" + currentPage.getPath() + "'.");
        }
      }
    }

    // resolve link
    if (linkType != null) {
      linkMetadata = linkType.resolveLink(linkMetadata);
      if (linkMetadata == null) {
        throw new RuntimeException("LinkType '" + linkType + "' returned null, page '" + currentPage.getPath() + "'.");
      }
    }

    // generate markup (if markup builder is available) - first accepting wins
    List<Class<? extends LinkMarkupBuilder>> linkMarkupBuilders = linkHandlerConfig.getLinkMarkupBuilders();
    if (linkMarkupBuilders != null) {
      for (Class<? extends LinkMarkupBuilder> linkMarkupBuilderClass : linkMarkupBuilders) {
        LinkMarkupBuilder linkMarkupBuilder = AdaptTo.notNull(adaptable, linkMarkupBuilderClass);
        if (linkMarkupBuilder.accepts(linkMetadata)) {
          linkMetadata.setAnchor(linkMarkupBuilder.build(linkMetadata));
          break;
        }
      }
    }

    // postprocess link after resolving
    List<Class<? extends LinkMetadataProcessor>> linkPostProcessors = linkHandlerConfig.getLinkMetadataPostProcessors();
    if (linkPostProcessors != null) {
      for (Class<? extends LinkMetadataProcessor> processorClass : linkPostProcessors) {
        LinkMetadataProcessor processor = AdaptTo.notNull(adaptable, processorClass);
        linkMetadata = processor.process(linkMetadata);
        if (linkMetadata == null) {
          throw new RuntimeException("LinkMetadataPostProcessor '" + processor + "' returned null, page '" + currentPage.getPath() + "'.");
        }
      }
    }

    return linkMetadata;
  }

}
