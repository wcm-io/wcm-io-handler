/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.media.spi.ImageMapLinkResolver;

/**
 * Autodetects and resolves a link URL found by wcm.io Media Handler in a Image Map string.
 */
@Component(service = ImageMapLinkResolver.class, immediate = true)
public class ImageMapLinkResolverImpl implements ImageMapLinkResolver<Link> {

  @Override
  public @Nullable String resolve(@NotNull String linkUrl, @NotNull Resource context) {
    Link link = resolveLink(linkUrl, null, context);
    return getLinkUrl(link);
  }

  @Override
  public @Nullable Link resolveLink(@NotNull String linkUrl, @Nullable String linkWindowTarget, @NotNull Resource context) {
    LinkHandler linkHandler = context.adaptTo(LinkHandler.class);
    if (linkHandler != null) {
      return linkHandler.get(linkUrl)
          .windowTarget(linkWindowTarget)
          .build();
    }
    return null;
  }

  @Override
  public @Nullable String getLinkUrl(@Nullable Link link) {
    if (link != null && link.isValid()) {
      return link.getUrl();
    }
    return null;
  }

}
