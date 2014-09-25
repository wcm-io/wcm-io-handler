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
package io.wcm.handler.mediasource.dam;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.MediaArgsType;
import io.wcm.handler.media.MediaItem;
import io.wcm.handler.media.MediaMetadata;
import io.wcm.handler.media.Rendition;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;

/**
 * {@link MediaItem} implementation for DAM assets.
 */
class DamMediaItem extends SlingAdaptable implements MediaItem {

  private final Adaptable adaptable;
  private final Asset asset;
  private final CropDimension cropDimension;
  private final MediaArgsType defaultMediaArgs;
  private final ValueMap properties;

  /**
   * @param asset DAM asset
   * @param mediaMetadata Media metadata
   */
  DamMediaItem(Asset asset, MediaMetadata mediaMetadata, Adaptable adaptable) {
    this.asset = asset;
    this.cropDimension = mediaMetadata.getCropDimension();
    this.defaultMediaArgs = mediaMetadata.getMediaReference().getMediaArgs();
    this.properties = new ValueMapDecorator(asset.getMetadata());
    this.adaptable = adaptable;
  }

  @Override
  public String getTitle() {
    // default title is the asset name
    String title = this.asset.getName();

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
  public String getPath() {
    return this.asset.getPath();
  }

  @Override
  public ValueMap getProperties() {
    return this.properties;
  }

  @Override
  public Rendition getDefaultRendition() {
    return getRendition(this.defaultMediaArgs);
  }

  @Override
  public Rendition getRendition(MediaArgsType mediaArgs) {
    Rendition rendition = getDamRendition(mediaArgs);

    // check if rendition is valid - otherwise return null
    if (StringUtils.isEmpty(rendition.getMediaUrl())) {
      rendition = null;
    }

    return rendition;
  }

  @Override
  public Rendition getImageRendition(MediaArgsType mediaArgs) {
    Rendition rendition = getRendition(mediaArgs);
    if (rendition != null && rendition.isImage()) {
      return rendition;
    }
    else {
      return null;
    }
  }

  @Override
  public Rendition getFlashRendition(MediaArgsType mediaArgs) {
    Rendition rendition = getRendition(mediaArgs);
    if (rendition != null && rendition.isFlash()) {
      return rendition;
    }
    else {
      return null;
    }
  }

  @Override
  public Rendition getDownloadRendition(MediaArgsType mediaArgs) {
    Rendition rendition = getRendition(mediaArgs);
    if (rendition != null && rendition.isDownload()) {
      return rendition;
    }
    else {
      return null;
    }
  }

  /**
   * @return DAM asset
   */
  public Asset getDamAsset() {
    return this.asset;
  }

  /**
   * Get DAM rendition instance.
   * @param mediaArgs Media args
   * @return DAM rendition instance (may be invalid rendition)
   */
  protected Rendition getDamRendition(MediaArgsType mediaArgs) {
    return new DamRendition(this.asset, this.cropDimension, mediaArgs, adaptable);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (type == Asset.class) {
      return (AdapterType)this.asset;
    }
    if (type == Resource.class) {
      return (AdapterType)this.asset.adaptTo(Resource.class);
    }
    return super.adaptTo(type);
  }

}
