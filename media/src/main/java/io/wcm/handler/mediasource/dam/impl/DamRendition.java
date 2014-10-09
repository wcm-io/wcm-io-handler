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

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.args.MediaArgsType;
import io.wcm.handler.url.UrlHandler;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.caching.ModificationDate;
import io.wcm.wcm.commons.contenttype.FileExtension;

import java.util.Date;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.dam.api.Asset;

/**
 * {@link Rendition} implementation for DAM asset renditions.
 */
class DamRendition extends SlingAdaptable implements Rendition {

  private final Adaptable adaptable;
  private final MediaArgsType mediaArgs;
  private final RenditionMetadata rendition;

  /**
   * @param asset DAM asset
   * @param cropDimension Crop dimension
   * @param mediaArgs Media args
   */
  DamRendition(Asset asset, CropDimension cropDimension, MediaArgsType mediaArgs, Adaptable adaptable) {
    this.mediaArgs = mediaArgs;

    // resolve rendition from DAM assets
    RenditionHandler renditionHandler;
    if (cropDimension != null) {
      renditionHandler = new CropRenditionHandler(asset, cropDimension);
    }
    else {
      renditionHandler = new DefaultRenditionHandler(asset);
    }
    this.rendition = renditionHandler.getRendition(mediaArgs);

    this.adaptable = adaptable;
  }

  @Override
  public String getUrl() {
    if (this.rendition != null) {
      // build externalized URL
      UrlHandler urlHandler = AdaptTo.notNull(adaptable, UrlHandler.class);
      String mediaPath = this.rendition.getMediaPath(this.mediaArgs.isForceDownload());
      return urlHandler.get(mediaPath).urlMode(this.mediaArgs.getUrlMode()).buildExternalResourceUrl();
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
      return this.rendition.getRendition().getSize();
    }
    else {
      return 0L;
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
  public String getMediaFormat() {
    // not supported
    return null;
  }

  @Override
  public ValueMap getProperties() {
    if (this.rendition != null) {
      return this.rendition.getRendition().adaptTo(Resource.class).getValueMap();
    }
    else {
      return null;
    }
  }

  @Override
  public boolean isImage() {
    return FileExtension.isImage(getFileExtension());
  }

  @Override
  public boolean isFlash() {
    return FileExtension.isFlash(getFileExtension());
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
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (this.rendition != null) {
      AdapterType result = this.rendition.adaptTo(type);
      if (result != null) {
        return result;
      }
    }
    return super.adaptTo(type);
  }

}
