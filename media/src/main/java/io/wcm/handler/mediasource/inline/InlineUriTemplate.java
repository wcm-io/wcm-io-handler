/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.handler.mediasource.inline;

import static io.wcm.handler.media.MediaNameConstants.URI_TEMPLATE_PLACEHOLDER_HEIGHT;
import static io.wcm.handler.media.MediaNameConstants.URI_TEMPLATE_PLACEHOLDER_WIDTH;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.media.impl.ImageFileServlet;
import io.wcm.handler.media.impl.JcrBinary;
import io.wcm.handler.media.impl.MediaFileServlet;
import io.wcm.handler.url.UrlHandler;
import io.wcm.sling.commons.adapter.AdaptTo;

class InlineUriTemplate implements UriTemplate {

  private final String uriTemplate;
  private final UriTemplateType type;
  private final long width;
  private final long height;

  InlineUriTemplate(@NotNull UriTemplateType type, long width, long height,
      @NotNull Resource resource, @NotNull String fileName, @NotNull MediaArgs mediaArgs, @NotNull Adaptable adaptable) {
    this.uriTemplate = buildUriTemplate(type, resource, fileName, mediaArgs, adaptable);
    this.type = type;
    this.width = width;
    this.height = height;
  }

  private static String buildUriTemplate(@NotNull UriTemplateType type, @NotNull Resource resource,
      @NotNull String fileName, @NotNull MediaArgs mediaArgs, @NotNull Adaptable adaptable) {
    String resourcePath = resource.getPath();

    // if parent resource is a nt:file resource, use this one as path for scaled image
    Resource parentResource = resource.getParent();
    if (parentResource != null && JcrBinary.isNtFile(parentResource)) {
      resourcePath = parentResource.getPath();
    }

    // URL to render scaled image via {@link InlineRenditionServlet}
    final long DUMMY_WIDTH = 999991;
    final long DUMMY_HEIGHT = 999992;
    String path = resourcePath
        + "." + ImageFileServlet.buildSelectorString(DUMMY_WIDTH, DUMMY_HEIGHT, null, null, false)
        + "." + MediaFileServlet.EXTENSION + "/"
        // replace extension based on the format supported by ImageFileServlet for rendering for this rendition
        + ImageFileServlet.getImageFileName(fileName, mediaArgs.getEnforceOutputFileExtension());

    // build externalized URL
    UrlHandler urlHandler = AdaptTo.notNull(adaptable, UrlHandler.class);
    String url = urlHandler.get(path).urlMode(mediaArgs.getUrlMode()).buildExternalResourceUrl(resource);

    switch (type) {
      case CROP_CENTER:
        url = StringUtils.replace(url, Long.toString(DUMMY_WIDTH), URI_TEMPLATE_PLACEHOLDER_WIDTH);
        url = StringUtils.replace(url, Long.toString(DUMMY_HEIGHT), URI_TEMPLATE_PLACEHOLDER_HEIGHT);
        break;
      case SCALE_WIDTH:
        url = StringUtils.replace(url, Long.toString(DUMMY_WIDTH), URI_TEMPLATE_PLACEHOLDER_WIDTH);
        url = StringUtils.replace(url, Long.toString(DUMMY_HEIGHT), "0");
        break;
      case SCALE_HEIGHT:
        url = StringUtils.replace(url, Long.toString(DUMMY_WIDTH), "0");
        url = StringUtils.replace(url, Long.toString(DUMMY_HEIGHT), URI_TEMPLATE_PLACEHOLDER_HEIGHT);
        break;
      default:
        throw new IllegalArgumentException("Unsupported type: " + type);
    }
    return url;
  }

  @Override
  public String getUriTemplate() {
    return uriTemplate;
  }

  @Override
  public UriTemplateType getType() {
    return type;
  }

  @Override
  public long getMaxWidth() {
    return width;
  }

  @Override
  public long getMaxHeight() {
    return height;
  }

  @Override
  public String toString() {
    return uriTemplate;
  }

}
