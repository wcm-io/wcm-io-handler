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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.apache.sling.models.annotations.injectorspecific.Self;
import org.osgi.annotation.versioning.ConsumerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.spi.LinkProcessor;
import io.wcm.handler.link.type.InternalLinkType;
import io.wcm.handler.url.UrlHandler;

/**
 * Linkhandler post processor to inherit URL parameters to internal links.
 */
@ConsumerType
public abstract class AbstractInternalLinkInheritUrlParamLinkPostProcessor implements LinkProcessor {

  @Self
  private UrlHandler urlHandler;

  private final Set<String> inheritUrlParameterNames;

  private final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * @param inheritUrlParameterNames Custom list of inheritable URL parameter names.
   */
  protected AbstractInternalLinkInheritUrlParamLinkPostProcessor(Set<String> inheritUrlParameterNames) {
    this.inheritUrlParameterNames = inheritUrlParameterNames;
  }

  @Override
  public final Link process(Link link) {

    if (link.isValid() && link.getLinkType().getId() == InternalLinkType.ID) {
      String url = link.getUrl();
      try {
        URI uri = new URI(url);
        String path = uri.getPath();
        if (uri.getScheme() != null && uri.getHost() != null) {
          path = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() == -1 ? "" : (":" + uri.getPort())) + path;
        }
        url = urlHandler.get(path)
            .queryString(uri.getRawQuery(), this.inheritUrlParameterNames)
            .fragment(uri.getFragment())
            .build();
        link.setUrl(url);
        if (link.getAnchor() != null) {
          link.getAnchor().setAttribute("href", url);
        }
      }
      catch (URISyntaxException ex) {
        log.warn("Skipping post-processing or URL: " + url, ex);
      }
    }

    return link;
  }

}
