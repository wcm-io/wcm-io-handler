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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.LinkRequest;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.spi.LinkType;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaHandler;

/**
 * Default implementation of {@link io.wcm.handler.link.spi.LinkType} for media links.
 * Media links are links to media items from media sources
 * that implement the {@link io.wcm.handler.media.spi.MediaSource} interface.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ProviderType
public final class MediaLinkType extends LinkType {

  /**
   * Default root folder für DAM
   */
  private static final String DEFAULT_DAM_ROOT = "/content/dam/";

  /**
   * Link type ID
   */
  public static final @NotNull String ID = "media";

  @Self
  private MediaHandler mediaHandler;

  /**
   * @return Link type ID (is stored as identifier in repository)
   */
  @Override
  public @NotNull String getId() {
    return ID;
  }

  @Override
  public @NotNull String getLabel() {
    return "Asset";
  }

  @Override
  public String getPrimaryLinkRefProperty() {
    return LinkNameConstants.PN_LINK_MEDIA_REF;
  }

  @Override
  public @Nullable String getEditComponentResourceType() {
    return "wcm-io/handler/link/components/granite/form/linktype/media";
  }

  @Override
  public boolean accepts(@NotNull String linkRef) {
    // accept as media link if the ref is inside default media subtrees
    return MediaLinkType.isDefaultMediaContentPath(linkRef);
  }

  @SuppressWarnings("null")
  @Override
  public @NotNull Link resolveLink(@NotNull Link link) {
    LinkRequest linkRequest = link.getLinkRequest();
    ValueMap props = linkRequest.getResourceProperties();

    // get properties
    String mediaRef = props.get(LinkNameConstants.PN_LINK_MEDIA_REF, link.getLinkRequest().getReference());
    boolean isDownload = props.get(LinkNameConstants.PN_LINK_MEDIA_DOWNLOAD, false);

    MediaArgs mediaArgs = new MediaArgs()
        // only allow linking to "download" media formats
        .download(true)
        .contentDispositionAttachment(isDownload)
        .urlMode(linkRequest.getLinkArgs().getUrlMode());

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
   * @param path Resource path. Can be a non-existing path, but the path should be located somewhere within the
   *          applications content paths to make sure the handler configuration looked up via context-aware services
   *          is the expected one.
   * @param mediaRef Media asset reference
   * @return Synthetic link resource
   */
  public static @NotNull Resource getSyntheticLinkResource(@NotNull ResourceResolver resourceResolver,
      @NotNull String path, @NotNull String mediaRef) {
    Map<String, Object> map = new HashMap<>();
    map.put(LinkNameConstants.PN_LINK_TYPE, ID);
    map.put(LinkNameConstants.PN_LINK_MEDIA_REF, mediaRef);
    return new SyntheticLinkResource(resourceResolver, path, map);
  }

  /**
   * Get synthetic link resource for this link type.
   * @param resourceResolver Resource resolver
   * @param mediaRef Media asset reference
   * @return Synthetic link resource
   * @deprecated Please use {@link #getSyntheticLinkResource(ResourceResolver, String, String)}
   */
  @Deprecated
  public static @NotNull Resource getSyntheticLinkResource(@NotNull ResourceResolver resourceResolver, @NotNull String mediaRef) {
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
