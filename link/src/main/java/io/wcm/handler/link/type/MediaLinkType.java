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
import io.wcm.handler.link.LinkReference;
import io.wcm.handler.media.MediaArgsType;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaHandlerConfig;
import io.wcm.handler.media.MediaMetadata;
import io.wcm.handler.media.args.MediaArgs;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

/**
 * Default implementation of {@link io.wcm.handler.link.LinkType} for media links.
 * Media links are links to media items from media sources
 * that implement the {@link io.wcm.handler.media.MediaSource} interface.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
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
  public LinkMetadata resolveLink(LinkMetadata linkMetadata) {
    LinkReference linkReference = linkMetadata.getLinkReference();
    ValueMap props = linkReference.getResourceProperties();

    // get properties
    String mediaRef = props.get(LinkNameConstants.PN_LINK_MEDIA_REF, String.class);
    boolean isDownload = props.get(LinkNameConstants.PN_LINK_MEDIA_DOWNLOAD, false);

    // only allow linking to "download" medialib formats
    MediaArgsType mediaArgs = MediaArgs.mediaFormats(mediaHandlerConfig.getDownloadMediaFormats());
    mediaArgs.setForceDownload(isDownload);
    mediaArgs.setUrlMode(linkReference.getLinkArgs().getUrlMode());

    // resolve media library reference
    MediaMetadata mediaMetadata = mediaHandler.getMediaMetadata(mediaRef, mediaArgs);

    if (mediaMetadata != null) {
      // set resovled media references information in link metadata
      linkMetadata.setLinkUrl(mediaMetadata.getMediaUrl());
      linkMetadata.setTargetMediaItem(mediaMetadata.getMediaItem());
      linkMetadata.setTargetRendition(mediaMetadata.getRendition());
    }

    if (linkMetadata.getLinkUrl() == null) {
      // mark link as invalid if a reference was set that could not be resolved
      if (StringUtils.isNotEmpty(mediaRef)) {
        linkMetadata.setLinkReferenceInvalid(true);
      }
    }

    return linkMetadata;
  }

  /**
   * @param path Content path
   * @return true if Path is located below DAM default root folders.
   */
  public static boolean isDefaultMediaContentPath(String path) {
    return StringUtils.startsWith(path, DEFAULT_DAM_ROOT);
  }

}
