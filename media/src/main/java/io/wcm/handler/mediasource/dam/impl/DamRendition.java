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

import java.util.Date;
import java.util.List;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaFileExtension;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.url.UrlHandler;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.caching.ModificationDate;

/**
 * {@link Rendition} implementation for DAM asset renditions.
 */
class DamRendition extends SlingAdaptable implements Rendition {

  private final Adaptable adaptable;
  private final MediaArgs mediaArgs;
  private final RenditionMetadata rendition;

  /**
   * @param asset DAM asset
   * @param cropDimension Crop dimension
   * @param mediaArgs Media args
   */
  DamRendition(Asset asset, CropDimension cropDimension, Integer rotation, MediaArgs mediaArgs, Adaptable adaptable) {
    this.mediaArgs = mediaArgs;

    // resolve rendition from DAM assets
    RenditionHandler renditionHandler;
    if (cropDimension != null || rotation != null) {
      renditionHandler = new TransformedRenditionHandler(asset, cropDimension, rotation);
    }
    else {
      renditionHandler = new DefaultRenditionHandler(asset);
    }
    RenditionMetadata resolvedRendition = renditionHandler.getRendition(mediaArgs);

    // if auto-cropping is enabled, and no cropping or rotation parameters set, try to build a
    // transformed rendition with automatically devised cropping parameters
    if (resolvedRendition == null && mediaArgs.isAutoCrop() && !(renditionHandler instanceof TransformedRenditionHandler)) {
      AutoCropping autoCropping = new AutoCropping(asset, mediaArgs);
      List<CropDimension> autoCropDimensions = autoCropping.calculateAutoCropDimensions();
      for (CropDimension autoCropDimension : autoCropDimensions) {
        renditionHandler = new TransformedRenditionHandler(asset, autoCropDimension, null);
        resolvedRendition = renditionHandler.getRendition(mediaArgs);
        if (resolvedRendition != null) {
          break;
        }
      }
    }
    this.rendition = resolvedRendition;

    this.adaptable = adaptable;
  }

  @Override
  public String getUrl() {
    if (this.rendition != null) {
      // build externalized URL
      UrlHandler urlHandler = AdaptTo.notNull(adaptable, UrlHandler.class);
      String mediaPath = this.rendition.getMediaPath(this.mediaArgs.isContentDispositionAttachment());
      return urlHandler.get(mediaPath).urlMode(this.mediaArgs.getUrlMode())
          .buildExternalResourceUrl(this.rendition.adaptTo(Resource.class));
    }
    else {
      return null;
    }
  }

  @Override
  public String getPath() {
    if (this.rendition != null) {
      return this.rendition.getRendition().getPath();
    }
    else {
      return null;
    }
  }

  @Override
  public String getFileName() {
    if (this.rendition != null) {
      return this.rendition.getFileName();
    }
    else {
      return null;
    }
  }

  @Override
  public String getFileExtension() {
    if (this.rendition != null) {
      return this.rendition.getFileExtension();
    }
    else {
      return null;
    }
  }

  @Override
  public long getFileSize() {
    if (this.rendition != null) {
      return this.rendition.getFileSize();
    }
    else {
      return 0L;
    }
  }

  @Override
  public String getMimeType() {
    if (this.rendition != null) {
      return this.rendition.getMimeType();
    }
    else {
      return null;
    }
  }

  @Override
  public Date getModificationDate() {
    if (this.rendition != null) {
      return ModificationDate.get(this.rendition.getRendition().adaptTo(Resource.class));
    }
    else {
      return null;
    }
  }

  @Override
  public MediaFormat getMediaFormat() {
    if (this.rendition != null) {
      return this.rendition.getMediaFormat();
    }
    else {
      return null;
    }
  }

  @Override
  @SuppressWarnings("null")
  public @NotNull ValueMap getProperties() {
    if (this.rendition != null) {
      return this.rendition.getRendition().adaptTo(Resource.class).getValueMap();
    }
    else {
      return ValueMap.EMPTY;
    }
  }

  @Override
  public boolean isImage() {
    return MediaFileExtension.isImage(getFileExtension());
  }

  @Override
  public boolean isFlash() {
    return MediaFileExtension.isFlash(getFileExtension());
  }

  @Override
  public boolean isDownload() {
    return !isImage() && !isFlash();
  }

  @Override
  public long getWidth() {
    if (this.rendition != null) {
      return this.rendition.getWidth();
    }
    else {
      return 0;
    }
  }

  @Override
  public long getHeight() {
    if (this.rendition != null) {
      return this.rendition.getHeight();
    }
    else {
      return 0;
    }
  }

  @Override
  @SuppressWarnings("null")
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (this.rendition != null) {
      AdapterType result = this.rendition.adaptTo(type);
      if (result != null) {
        return result;
      }
    }
    return super.adaptTo(type);
  }

  @Override
  public String toString() {
    if (rendition != null) {
      return rendition.toString();
    }
    return super.toString();
  }

}
