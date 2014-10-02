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

import io.wcm.handler.media.impl.ImageFileServlet;
import io.wcm.handler.media.impl.MediaFileServlet;

import java.io.InputStream;

import com.day.cq.dam.api.Rendition;
import com.day.image.Layer;

/**
 * Virtual rendition that is downscaling from an existing rendition.
 */
class VirtualRenditionMetadata extends RenditionMetadata {

  private final int width;
  private final int height;

  public VirtualRenditionMetadata(Rendition rendition, int width, int height) {
    super(rendition);
    this.width = width;
    this.height = height;
  }

  @Override
  public String getFileName() {
    // replace extension based on the format supported by ImageFileServlet for rendering for this rendition
    return ImageFileServlet.getImageFileName(super.getFileName());
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  @Override
  public int getHeight() {
    return this.height;
  }

  @Override
  public String getMediaPath(boolean forceDownload) {
    return RenditionMetadata.buildMediaPath(getRendition().getPath() + "." + ImageFileServlet.SELECTOR
        + "." + getWidth() + "." + getHeight()
        + (forceDownload ? "." + MediaFileServlet.SELECTOR_DOWNLOAD : "")
        + "." + MediaFileServlet.EXTENSION, getFileName());
  }

  @Override
  protected Layer getLayer() {
    Layer layer = super.getLayer();
    if (layer != null) {
      layer.resize(getWidth(), getHeight());
    }
    return layer;
  }

  @Override
  protected InputStream getInputStream() {
    // currently not supported for virtual renditions
    return null;
  }

}
