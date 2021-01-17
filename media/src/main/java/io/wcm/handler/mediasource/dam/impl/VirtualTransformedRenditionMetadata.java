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

import java.io.InputStream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.dam.api.Rendition;
import com.day.image.Layer;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.impl.ImageFileServlet;
import io.wcm.handler.media.impl.MediaFileServlet;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.DynamicMediaPath;

/**
 * Virtual rendition that is cropping and/or rotating and downscaling from an existing rendition.
 */
class VirtualTransformedRenditionMetadata extends RenditionMetadata {

  private final long width;
  private final long height;
  private final CropDimension cropDimension;
  private final Integer rotation;

  VirtualTransformedRenditionMetadata(Rendition rendition, long width, long height,
      CropDimension cropDimension, Integer rotation) {
    super(rendition);
    this.width = width;
    this.height = height;
    this.cropDimension = cropDimension;
    this.rotation = rotation;
  }

  @Override
  public String getFileName(boolean contentDispositionAttachment) {
    // replace extension based on the format supported by ImageFileServlet for rendering for this rendition
    return ImageFileServlet.getImageFileName(super.getFileName(contentDispositionAttachment));
  }

  @Override
  public long getFileSize() {
    // no size for virutal renditions
    return 0L;
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

  public Integer getRotation() {
    return this.rotation;
  }

  @Override
  public @NotNull String getMediaPath(boolean contentDispositionAttachment) {
    return RenditionMetadata.buildMediaPath(getRendition().getPath()
        + "." + ImageFileServlet.buildSelectorString(getWidth(), getHeight(),
            this.cropDimension, this.rotation, contentDispositionAttachment)
        + "." + MediaFileServlet.EXTENSION, getFileName(contentDispositionAttachment));
  }

  @Override
  public @Nullable String getDynamicMediaPath(boolean contentDispositionAttachment, DamContext damContext) {
    if (contentDispositionAttachment) {
      // do not use dynamic media for request forced with content disposition attachment
      return null;
    }
    else {
      // render virtual rendition with dynamic media
      return DynamicMediaPath.build(damContext, getWidth(), getHeight(), this.cropDimension, this.rotation);
    }
  }

  @Override
  protected Layer getLayer() {
    Layer layer = super.getLayer();
    if (layer != null) {
      if (cropDimension != null) {
        layer.crop(cropDimension.getRectangle());
        if (width <= layer.getWidth() && height <= layer.getHeight()) {
          layer.resize((int)width, (int)height);
        }
      }
      if (rotation != null) {
        layer.rotate(rotation);
      }
    }
    return layer;
  }

  @Override
  protected InputStream getInputStream() {
    // currently not supported for virtual renditions
    return null;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(this.getRendition().getPath())
        .append(width)
        .append(height)
        .append(cropDimension)
        .append(rotation)
        .hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    VirtualTransformedRenditionMetadata other = (VirtualTransformedRenditionMetadata)obj;
    return new EqualsBuilder()
        .append(this.getRendition().getPath(), other.getRendition().getPath())
        .append(this.width, other.width)
        .append(this.height, other.height)
        .append(this.cropDimension, other.cropDimension)
        .append(this.rotation, other.rotation)
        .build();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(super.toString());
    sb.append(" -> ").append(Long.toString(this.width)).append("x").append(Long.toString(this.height));
    if (cropDimension != null) {
      sb.append(", ").append(cropDimension.toString());
    }
    if (rotation != null) {
      sb.append(", rotation:").append(Integer.toString(rotation));
    }
    return sb.toString();
  }

}
