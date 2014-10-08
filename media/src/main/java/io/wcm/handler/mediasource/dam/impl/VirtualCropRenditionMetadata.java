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
import io.wcm.handler.media.impl.ImageFileServlet;
import io.wcm.handler.media.impl.MediaFileServlet;

import java.io.InputStream;

import com.day.cq.dam.api.Rendition;
import com.day.image.Layer;

/**
 * Virtual rendition that is cropping and downscaling from an existing rendition.
 */
class VirtualCropRenditionMetadata extends RenditionMetadata {

  private final long width;
  private final long height;
  private final CropDimension cropDimension;

  public VirtualCropRenditionMetadata(Rendition rendition, long width, long height, CropDimension cropDimension) {
    super(rendition);
    this.width = width;
    this.height = height;
    this.cropDimension = cropDimension;
  }

  @Override
  public String getFileName() {
    // replace extension based on the format supported by ImageFileServlet for rendering for this rendition
    return ImageFileServlet.getImageFileName(super.getFileName());
  }

  @Override
  public long getWidth() {
    return this.width;
  }

  @Override
  public long getHeight() {
    return this.height;
  }

  public CropDimension getCropDimension() {
    return this.cropDimension;
  }

  @Override
  public String getMediaPath(boolean forceDownload) {
    return RenditionMetadata.buildMediaPath(getRendition().getPath() + "." + ImageFileServlet.SELECTOR
        + "." + getWidth() + "." + getHeight()
        + "." + this.cropDimension.getCropString()
        + (forceDownload ? "." + MediaFileServlet.SELECTOR_DOWNLOAD : "")
        + "." + MediaFileServlet.EXTENSION, getFileName());
  }

  @Override
  protected Layer getLayer() {
    Layer layer = super.getLayer();
    if (layer != null) {
      layer.crop(cropDimension.getRectangle());
      if (width <= layer.getWidth() && height <= layer.getHeight()) {
        layer.resize((int)width, (int)height);
      }
    }
    return layer;
  }

  @Override
  protected InputStream getInputStream() {
    // currently not supported for virtual renditions
    return null;
  }

}
