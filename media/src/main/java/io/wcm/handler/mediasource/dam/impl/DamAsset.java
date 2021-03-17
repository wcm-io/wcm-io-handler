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
package io.wcm.handler.mediasource.dam.impl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.jetbrains.annotations.NotNull;

import com.day.cq.dam.api.DamConstants;

import io.wcm.handler.media.Asset;
import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaFileType;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.mediasource.dam.AssetRendition;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.DynamicMediaSupportService;

/**
 * {@link Asset} implementation for DAM assets.
 */
public final class DamAsset extends SlingAdaptable implements Asset {

  private final DamContext damContext;
  private final CropDimension cropDimension;
  private final Integer rotation;
  private final MediaArgs defaultMediaArgs;
  private final ValueMap properties;

  /**
   * @param media Media metadata
   * @param damAsset DAM asset
   * @param dynamicMediaSupportService Dynamic media support service
   * @param adaptable Adaptable from current context
   */
  public DamAsset(Media media, com.day.cq.dam.api.Asset damAsset,
      DynamicMediaSupportService dynamicMediaSupportService, Adaptable adaptable) {
    this.damContext = new DamContext(damAsset, dynamicMediaSupportService, adaptable);
    this.cropDimension = media.getCropDimension();
    this.rotation = media.getRotation();
    this.defaultMediaArgs = media.getMediaRequest().getMediaArgs();
    this.properties = new ValueMapDecorator(damAsset.getMetadata());
  }

  @Override
  public String getTitle() {
    // default title is the asset name
    String title = damContext.getAsset().getName();

    Object titleObj = this.properties.get(DamConstants.DC_TITLE);
    if (titleObj != null) {
      // it might happen that the adobe xmp lib creates an array, e.g. if the asset file already has a title attribute
      if (titleObj instanceof Object[]) {
        Object[] titleArray = (Object[])titleObj;
        if (titleArray.length > 0) {
          title = StringUtils.defaultString(titleArray[0].toString(), title);
        }
      }
      else {
        title = titleObj.toString();
      }
    }

    return title;
  }

  @Override
  public String getAltText() {
    return StringUtils.defaultString(this.defaultMediaArgs.getAltText(), getTitle());
  }

  @Override
  public String getDescription() {
    return this.properties.get(DamConstants.DC_DESCRIPTION, String.class);
  }

  @Override
  public @NotNull String getPath() {
    return this.damContext.getAsset().getPath();
  }

  @Override
  public @NotNull ValueMap getProperties() {
    return this.properties;
  }

  @Override
  public Rendition getDefaultRendition() {
    return getRendition(this.defaultMediaArgs);
  }

  @Override
  public Rendition getRendition(@NotNull MediaArgs mediaArgs) {
    Rendition rendition = getDamRendition(mediaArgs);

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
   * Get DAM rendition instance.
   * @param mediaArgs Media args
   * @return DAM rendition instance (may be invalid rendition)
   */
  protected Rendition getDamRendition(MediaArgs mediaArgs) {
    return new DamRendition(this.cropDimension, this.rotation, mediaArgs, damContext);
  }

  @Override
  @SuppressWarnings({ "unchecked", "null" })
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (type == com.day.cq.dam.api.Asset.class) {
      return (AdapterType)this.damContext.getAsset();
    }
    if (type == Resource.class) {
      return (AdapterType)this.damContext.getAsset().adaptTo(Resource.class);
    }
    return super.adaptTo(type);
  }

  @Override
  public @NotNull UriTemplate getUriTemplate(@NotNull UriTemplateType type) {
    String extension = FilenameUtils.getExtension(damContext.getAsset().getName());
    if (!MediaFileType.isImage(extension) || MediaFileType.isVectorImage(extension)) {
      throw new UnsupportedOperationException("Unable to build URI template for this asset type: " + getPath());
    }
    com.day.cq.dam.api.Rendition original = damContext.getAsset().getOriginal();
    Dimension dimension = AssetRendition.getDimension(original);
    if (dimension == null) {
      throw new IllegalArgumentException("Unable to get dimension for original rendition of asset: " + getPath());
    }
    return new DamUriTemplate(type, dimension, damContext, defaultMediaArgs);
  }

}
