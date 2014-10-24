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
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.spi.MediaHandlerConfig;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Default implementation of {@link io.wcm.handler.link.spi.LinkType} for media links.
 * Media links are links to media items from media sources
 * that implement the {@link io.wcm.handler.media.spi.MediaSource} interface.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ProviderType
public final class MediaLinkType extends AbstractLinkType {

  /**
   * Default root folder für DAM
   */
  private static final String DEFAULT_DAM_ROOT = "/content/dam/";

  /**
   * Link type ID
   */
  public static final String ID = "media";

  @Self
  private MediaHandlerConfig mediaHandlerConfig;
  @Self
  private MediaHandler mediaHandler;

  /**
   * @return Link type ID (is stored as identifier in repository)
   */
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getPrimaryLinkRefProperty() {
    return LinkNameConstants.PN_LINK_MEDIA_REF;
  }

  @Override
  public boolean accepts(String linkRef) {
    // accept as media link if the ref is inside default media subtrees
    return MediaLinkType.isDefaultMediaContentPath(linkRef);
  }

  @Override
  public Link resolveLink(Link link) {
    LinkRequest linkRequest = link.getLinkRequest();
    ValueMap props = linkRequest.getResourceProperties();

    // get properties
    String mediaRef = props.get(LinkNameConstants.PN_LINK_MEDIA_REF, String.class);
    boolean isDownload = props.get(LinkNameConstants.PN_LINK_MEDIA_DOWNLOAD, false);

    // only allow linking to "download" medialib formats
    MediaFormat[] downloadMediaFormats = null;
    if (mediaHandlerConfig.getDownloadMediaFormats() != null) {
      downloadMediaFormats = mediaHandlerConfig.getDownloadMediaFormats().toArray(
          new MediaFormat[mediaHandlerConfig.getDownloadMediaFormats().size()]);
    }
    MediaArgs mediaArgs = new MediaArgs(downloadMediaFormats)
    .forceDownload(isDownload)
    .urlMode(linkRequest.getUrlMode());

    // resolve media library reference
    Media media = mediaHandler.get(mediaRef, mediaArgs).build();

    if (media != null) {
      // set resovled media references information in link metadata
      link.setUrl(media.getUrl());
      link.setTargetAsset(media.getAsset());
      link.setTargetRendition(media.getRendition());
    }

    // mark link as invalid if a reference was set that could not be resolved
    if (link.getUrl() == null && StringUtils.isNotEmpty(mediaRef)) {
      link.setLinkReferenceInvalid(true);
    }

    return link;
  }

  /**
   * @param path Content path
   * @return true if Path is located below DAM default root folders.
   */
  public static boolean isDefaultMediaContentPath(String path) {
    return StringUtils.startsWith(path, DEFAULT_DAM_ROOT);
  }

  /**
   * Get synthetic link resource for this link type.
   * @param resourceResolver Resource resolver
   * @param mediaRef Media asset reference
   * @return Synthetic link resource
   */
  public static Resource getSyntheticLinkResource(ResourceResolver resourceResolver, String mediaRef) {
    Map<String, Object> map = new HashMap<>();
    map.put(LinkNameConstants.PN_LINK_TYPE, ID);
    map.put(LinkNameConstants.PN_LINK_MEDIA_REF, mediaRef);
    return new SyntheticLinkResource(resourceResolver, map);
  }

  @Override
  public String toString() {
    return ID;
  }

}
