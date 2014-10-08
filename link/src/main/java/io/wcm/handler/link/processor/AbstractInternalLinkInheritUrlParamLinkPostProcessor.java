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
package io.wcm.handler.link.processor;

import io.wcm.handler.link.LinkMetadata;
import io.wcm.handler.link.spi.LinkMetadataProcessor;
import io.wcm.handler.link.type.InternalLinkType;
import io.wcm.handler.url.UrlHandler;

import java.util.Set;

import org.apache.sling.models.annotations.injectorspecific.Self;

/**
 * Linkhandler postprocessor to inherit URL parametres to internal links.
 */
public abstract class AbstractInternalLinkInheritUrlParamLinkPostProcessor implements LinkMetadataProcessor {

  @Self
  private UrlHandler urlHandler;

  private final Set<String> inheritUrlParameterNames;

  /**
   * @param inheritUrlParameterNames Custom list of inheritable URL parameter names.
   */
  protected AbstractInternalLinkInheritUrlParamLinkPostProcessor(Set<String> inheritUrlParameterNames) {
    this.inheritUrlParameterNames = inheritUrlParameterNames;
  }

  @Override
  public final LinkMetadata process(LinkMetadata linkMetadata) {

    if (linkMetadata.isValid() && linkMetadata.getLinkType().getId() == InternalLinkType.ID) {
      String url = linkMetadata.getLinkUrl();
      url = urlHandler.url(url).queryString(null, this.inheritUrlParameterNames).build();
      linkMetadata.setLinkUrl(url);
      if (linkMetadata.getAnchor() != null) {
        linkMetadata.getAnchor().setAttribute("href", url);
      }
    }

    return linkMetadata;
  }

}
