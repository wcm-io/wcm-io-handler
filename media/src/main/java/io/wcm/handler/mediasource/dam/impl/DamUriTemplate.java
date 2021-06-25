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
package io.wcm.handler.mediasource.dam.impl;

import static io.wcm.handler.media.MediaNameConstants.URI_TEMPLATE_PLACEHOLDER_HEIGHT;
import static io.wcm.handler.media.MediaNameConstants.URI_TEMPLATE_PLACEHOLDER_WIDTH;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.media.impl.ImageFileServlet;
import io.wcm.handler.media.impl.MediaFileServlet;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.DynamicMediaPath;
import io.wcm.handler.url.UrlHandler;
import io.wcm.sling.commons.adapter.AdaptTo;

class DamUriTemplate implements UriTemplate {

  private final String uriTemplate;
  private final UriTemplateType type;
  private final Dimension dimension;

  DamUriTemplate(@NotNull UriTemplateType type, @NotNull Dimension dimension,
      @NotNull DamContext damContext, @NotNull MediaArgs mediaArgs) {
    this.uriTemplate = buildUriTemplate(type, damContext, mediaArgs);
    this.type = type;
    this.dimension = dimension;
  }

  private static String buildUriTemplate(@NotNull UriTemplateType type, @NotNull DamContext damContext,
      @NotNull MediaArgs mediaArgs) {
    String url = null;
    if (!mediaArgs.isDynamicMediaDisabled() && damContext.isDynamicMediaEnabled() && damContext.isDynamicMediaAsset()) {
      // if DM is enabled: try to get rendition URL from dynamic media
      String productionAssetUrl = damContext.getDynamicMediaServerUrl();
      if (productionAssetUrl != null) {
        switch (type) {
          case CROP_CENTER:
            url = productionAssetUrl + DynamicMediaPath.buildImage(damContext)
                + "?wid=" + URI_TEMPLATE_PLACEHOLDER_WIDTH + "&hei=" + URI_TEMPLATE_PLACEHOLDER_HEIGHT + "&fit=crop";
            break;
          case SCALE_WIDTH:
            url = productionAssetUrl + DynamicMediaPath.buildImage(damContext)
                + "?wid=" + URI_TEMPLATE_PLACEHOLDER_WIDTH;
            break;
          case SCALE_HEIGHT:
            url = productionAssetUrl + DynamicMediaPath.buildImage(damContext)
                + "?hei=" + URI_TEMPLATE_PLACEHOLDER_HEIGHT;
            break;
          default:
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
      }
    }
    if (url == null) {
      // Render renditions in AEM: build externalized URL
      final long DUMMY_WIDTH = 999991;
      final long DUMMY_HEIGHT = 999992;

      String mediaPath = RenditionMetadata.buildMediaPath(damContext.getAsset().getOriginal().getPath() + "." + ImageFileServlet.SELECTOR
          + "." + DUMMY_WIDTH + "." + DUMMY_HEIGHT
          + "." + MediaFileServlet.EXTENSION,
          ImageFileServlet.getImageFileName(damContext.getAsset().getName(), mediaArgs.getEnforceOutputFileExtension()));
      UrlHandler urlHandler = AdaptTo.notNull(damContext, UrlHandler.class);
      url = urlHandler.get(mediaPath).urlMode(mediaArgs.getUrlMode())
          .buildExternalResourceUrl(damContext.getAsset().adaptTo(Resource.class));

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
    return dimension.getWidth();
  }

  @Override
  public long getMaxHeight() {
    return dimension.getHeight();
  }

  @Override
  public String toString() {
    return uriTemplate;
  }

}
