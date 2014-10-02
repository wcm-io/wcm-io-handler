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

import io.wcm.handler.media.format.MediaFormatHandler;
import io.wcm.handler.media.impl.MediaFileServlet;
import io.wcm.wcm.commons.contenttype.FileExtension;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.Rendition;
import com.day.image.Layer;

/**
 * Wrapper class for rendition metadata retrieved from DAM rendition filenames.
 */
class RenditionMetadata extends SlingAdaptable implements Comparable<RenditionMetadata> {

  private final Rendition rendition;
  private final String fileName;
  private final String fileExtension;
  private final int width;
  private final int height;

  /**
   * @param rendition DAM rendition
   */
  public RenditionMetadata(Rendition rendition) {
    this.rendition = rendition;

    // check if rendition is original image
    boolean isOriginal = isOriginalRendition(rendition);
    Asset asset = rendition.getAsset();

    // get filename and extension
    String renditionName = rendition.getName();
    if (isOriginal) {
      renditionName = asset.getName();
    }
    this.fileName = renditionName;
    this.fileExtension = StringUtils.substringAfterLast(renditionName, ".");

    // get image width/height
    int imageWidth = 0;
    int imageHeight = 0;
    if (isOriginal) {
      // get width/height from metadata for original renditions
      try {
        imageWidth = Integer.parseInt(StringUtils.defaultString(asset.getMetadataValue(DamConstants.TIFF_IMAGEWIDTH), "0"));
      }
      catch (NumberFormatException ex) {
        // ignore
      }
      if (imageWidth == 0) {
        try {
          imageWidth = Integer.parseInt(StringUtils.defaultString(asset.getMetadataValue(DamConstants.EXIF_PIXELXDIMENSION), "0"));
        }
        catch (NumberFormatException ex) {
          // ignore
        }
      }
      try {
        imageHeight = Integer.parseInt(StringUtils.defaultString(asset.getMetadataValue(DamConstants.TIFF_IMAGELENGTH), "0"));
      }
      catch (NumberFormatException ex) {
        // ignore
      }
      if (imageHeight == 0) {
        try {
          imageHeight = Integer.parseInt(StringUtils.defaultString(asset.getMetadataValue(DamConstants.EXIF_PIXELYDIMENSION), "0"));
        }
        catch (NumberFormatException ex) {
          // ignore
        }
      }
    }
    else if (FileExtension.isImage(this.fileExtension)) {
      // otherwise get from rendition metadata written by {@link DamRenditionMetadataService}
      String metadataPath = JcrConstants.JCR_CONTENT + "/" + DamRenditionMetadataService.NN_RENDITIONS_METADATA + "/" + rendition.getName();
      Resource metadataResource = asset.adaptTo(Resource.class).getChild(metadataPath);
      if (metadataResource != null) {
        ValueMap props = metadataResource.getValueMap();
        imageWidth = props.get(DamRenditionMetadataService.PN_IMAGE_WIDTH, 0);
        imageHeight = props.get(DamRenditionMetadataService.PN_IMAGE_HEIGHT, 0);
      }
    }
    this.width = imageWidth;
    this.height = imageHeight;
  }

  /**
   * @param value DAM rendition
   * @return true if rendition is the original file that was uploaded initially
   */
  private boolean isOriginalRendition(Rendition value) {
    return StringUtils.equals(value.getName(), DamConstants.ORIGINAL_FILE);
  }

  /**
   * @return DAM rendition
   */
  public Rendition getRendition() {
    return this.rendition;
  }

  /**
   * @return File name
   */
  public String getFileName() {
    return this.fileName;
  }

  /**
   * @return File extension
   */
  public String getFileExtension() {
    return this.fileExtension;
  }

  /**
   * @return Image width
   */
  public int getWidth() {
    return this.width;
  }

  /**
   * @return Image height
   */
  public int getHeight() {
    return this.height;
  }

  /**
   * @param forceDownload Force content disposition download header.
   * @return Media path (not externalized)
   */
  public String getMediaPath(boolean forceDownload) {
    if (forceDownload) {
      return RenditionMetadata.buildMediaPath(getRendition().getPath() + "." + MediaFileServlet.SELECTOR
          + "." + MediaFileServlet.SELECTOR_DOWNLOAD
          + "." + MediaFileServlet.EXTENSION, getFileName());
    }
    else {
      return RenditionMetadata.buildMediaPath(this.rendition.getPath() + ".", getFileName());
    }
  }

  /**
   * Checks if this rendition matches the given width/height.
   * @param checkWidth Width
   * @param checkHeight Height
   * @return true if matches
   */
  public boolean matches(int checkWidth, int checkHeight) {
    if (checkWidth != 0 && checkWidth != getWidth()) {
      return false;
    }
    if (checkHeight != 0 && checkHeight != getHeight()) {
      return false;
    }
    return true;
  }

  /**
   * Checks if this rendition matches the given width/height/ration restrictions.
   * @param minWidth Min. width
   * @param minHeight Min. height
   * @param maxWidth Max. width
   * @param maxHeight Max. height
   * @param ratio Ratio
   * @return true if matches
   */
  public boolean matches(int minWidth, int minHeight, int maxWidth, int maxHeight, double ratio) {
    if (minWidth > 0 && getWidth() < minWidth) {
      return false;
    }
    if (minHeight > 0 && getHeight() < minHeight) {
      return false;
    }
    if (maxWidth > 0 && getWidth() > maxWidth) {
      return false;
    }
    if (maxHeight > 0 && getHeight() > maxHeight) {
      return false;
    }
    if (ratio > 0) {
      double renditionRatio = (double)getWidth() / (double)getHeight();
      if (renditionRatio > ratio + MediaFormatHandler.RATIO_TOLERANCE
          || renditionRatio < ratio - MediaFormatHandler.RATIO_TOLERANCE) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return this.rendition.getPath().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RenditionMetadata) {
      return StringUtils.equals(this.getRendition().getPath(), ((RenditionMetadata)obj).getRendition().getPath());
    }
    else {
      return false;
    }
  }

  @Override
  public int compareTo(RenditionMetadata obj) {
    // order by width, height, rendition path
    Integer thisWidth = getWidth();
    Integer otherWidth = obj.getWidth();
    if (thisWidth.equals(otherWidth)) {
      Integer thisHeight = getHeight();
      Integer otherHeight = obj.getHeight();
      if (thisHeight.equals(otherHeight)) {
        String thisPath = getRendition().getPath();
        String otherPath = obj.getRendition().getPath();
        if (!StringUtils.equals(thisPath, otherPath)) {
          // same with/height - prefer original rendition
          if (isOriginalRendition(getRendition())) {
            return -1;
          }
          else if (isOriginalRendition(obj.getRendition())) {
            return 1;
          }
          else {
            return thisPath.compareTo(otherPath);
          }
        }
        else {
          return 0;
        }
      }
      else {
        return thisHeight.compareTo(otherHeight);
      }
    }
    else {
      return thisWidth.compareTo(otherWidth);
    }
  }

  protected Layer getLayer() {
    if (FileExtension.isImage(getFileExtension())) {
      return this.rendition.adaptTo(Resource.class).adaptTo(Layer.class);
    }
    else {
      return null;
    }
  }

  protected InputStream getInputStream() {
    return this.rendition.adaptTo(Resource.class).adaptTo(InputStream.class);
  }

  @Override
  public String toString() {
    return this.rendition.getPath() + " (" + this.width + "x" + this.height + ")";
  }

  /**
   * Build media path and suffix. The suffix is url-encoded.
   * @param mediaPath Media path
   * @param suffix Suffix
   * @return Media path and suffix
   */
  static String buildMediaPath(String mediaPath, String suffix) {
    return mediaPath + "/" + suffix;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (type == com.day.cq.dam.api.Rendition.class) {
      return (AdapterType)this.rendition;
    }
    if (type == Resource.class) {
      return (AdapterType)this.rendition.adaptTo(Resource.class);
    }
    else if (type == Layer.class) {
      return (AdapterType)getLayer();
    }
    else if (type == InputStream.class) {
      return (AdapterType)getInputStream();
    }
    return super.adaptTo(type);
  }

}
