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
package io.wcm.handler.mediasource.inline;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;

import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaFileType;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.wcm.commons.util.ToStringStyle;

/**
 * {@link Asset} implementation for inline media objects stored in a node in a content page.
 */
class InlineAsset extends SlingAdaptable implements Asset {

  private final Adaptable adaptable;
  private final Resource resource;
  private final Media media;
  private final MediaArgs defaultMediaArgs;
  private final MediaHandlerConfig mediaHandlerConfig;
  private final String fileName;

  /**
   * @param resource Binary resource
   * @param media Media metadata
   * @param fileName File name
   */
  InlineAsset(Resource resource, Media media, MediaHandlerConfig mediaHandlerConfig,
      String fileName, Adaptable adaptable) {
    this.resource = resource;
    this.media = media;
    this.mediaHandlerConfig = mediaHandlerConfig;
    this.defaultMediaArgs = media.getMediaRequest().getMediaArgs();
    this.fileName = fileName;
    this.adaptable = adaptable;
  }

  @Override
  public String getTitle() {
    return this.fileName;
  }

  @Override
  public String getAltText() {
    if (defaultMediaArgs.isDecorative()) {
      return "";
    }
    else {
      return defaultMediaArgs.getAltText();
    }
  }

  @Override
  public String getDescription() {
    // not supported
    return null;
  }

  @Override
  public @NotNull String getPath() {
    return this.resource.getPath();
  }

  @Override
  public @NotNull ValueMap getProperties() {
    return this.resource.getValueMap();
  }

  @Override
  public Rendition getDefaultRendition() {
    return getRendition(this.defaultMediaArgs);
  }

  @Override
  public Rendition getRendition(@NotNull MediaArgs mediaArgs) {
    Rendition rendition = getInlineRendition(mediaArgs);

    // check if rendition is valid - otherwise return null
    if (StringUtils.isEmpty(rendition.getUrl())) {
      rendition = null;
    }

    return rendition;
  }

  @Override
  public Rendition getImageRendition(@NotNull MediaArgs mediaArgs) {
    Rendition rendition = getRendition(mediaArgs);
    if (rendition != null && rendition.isImage()) {
      return rendition;
    }
    else {
      return null;
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public Rendition getFlashRendition(@NotNull MediaArgs mediaArgs) {
    Rendition rendition = getRendition(mediaArgs);
    if (rendition != null && rendition.isFlash()) {
      return rendition;
    }
    else {
      return null;
    }
  }

  @Override
  public Rendition getDownloadRendition(@NotNull MediaArgs mediaArgs) {
    Rendition rendition = getRendition(mediaArgs);
    if (rendition != null && rendition.isDownload()) {
      return rendition;
    }
    else {
      return null;
    }
  }

  /**
   * Get inline rendition instance.
   * @param mediaArgs Media args
   * @return Inline rendition instance (may be invalid rendition)
   */
  private Rendition getInlineRendition(MediaArgs mediaArgs) {
    return new InlineRendition(this.resource, this.media, mediaArgs, this.mediaHandlerConfig,
        this.fileName, this.adaptable);
  }

  @Override
  @SuppressWarnings({ "unchecked", "null" })
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (type == Resource.class) {
      return (AdapterType)this.resource;
    }
    return super.adaptTo(type);
  }

  @Override
  public @NotNull UriTemplate getUriTemplate(@NotNull UriTemplateType type) {
    String extension = FilenameUtils.getExtension(fileName);
    if (!MediaFileType.isImage(extension) || MediaFileType.isVectorImage(extension)) {
      throw new UnsupportedOperationException("Unable to build URI template for this asset type: " + getPath());
    }
    Rendition originalRendition = getInlineRendition(new MediaArgs());
    return new InlineUriTemplate(type, originalRendition.getWidth(), originalRendition.getHeight(),
        this.resource, fileName, defaultMediaArgs, adaptable);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_OMIT_NULL_STYLE);
  }

}
