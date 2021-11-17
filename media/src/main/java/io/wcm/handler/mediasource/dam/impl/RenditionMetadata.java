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

import static com.day.cq.dam.api.DamConstants.ORIGINAL_FILE;

import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.dam.api.Rendition;
import com.day.image.Layer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaFileType;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.Ratio;
import io.wcm.handler.media.impl.ImageFileServlet;
import io.wcm.handler.media.impl.MediaFileServlet;
import io.wcm.handler.mediasource.dam.AssetRendition;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.DynamicMediaPath;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Wrapper class for rendition metadata retrieved from DAM rendition filenames.
 */
class RenditionMetadata extends SlingAdaptable implements Comparable<RenditionMetadata> {

  private final Rendition rendition;
  private final String fileName;
  private final String fileExtension;
  private final long width;
  private final long height;
  private final boolean isImage;
  private final boolean isVectorImage;
  private MediaFormat mediaFormat;

  /**
   * @param rendition DAM rendition
   */
  RenditionMetadata(Rendition rendition) {
    this.rendition = rendition;

    // get filename and extension
    this.fileName = AssetRendition.getFilename(rendition);
    this.fileExtension = FilenameUtils.getExtension(this.fileName);
    this.isImage = MediaFileType.isImage(this.fileExtension);
    this.isVectorImage = MediaFileType.isVectorImage(this.fileExtension);

    // get image width/height
    Dimension dimension = AssetRendition.getDimension(rendition);
    if (dimension != null) {
      this.width = dimension.getWidth();
      this.height = dimension.getHeight();
    }
    else {
      this.width = 0;
      this.height = 0;
    }
  }

  /**
   * @param value DAM rendition
   * @return true if rendition is the original file that was uploaded initially
   */
  private boolean isOriginalRendition(Rendition value) {
    return StringUtils.equals(value.getName(), ORIGINAL_FILE);
  }

  /**
   * @return True if rendition is an image
   */
  public boolean isImage() {
    return this.isImage;
  }

  /**
   * @return True if rendition is a vector image
   */
  public boolean isVectorImage() {
    return this.isVectorImage;
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
  public String getFileName(boolean contentDispositionAttachment) {
    if (MediaFileType.isBrowserImage(getFileExtension())
        || !MediaFileType.isImage(getFileExtension())
        || contentDispositionAttachment
        || isVectorImage()) {
      return this.fileName;
    }
    else {
      return ImageFileServlet.getImageFileName(this.fileName);
    }
  }

  /**
   * @return File size
   */
  public long getFileSize() {
    return this.rendition.getSize();
  }

  /**
   * @return File extension
   */
  public String getFileExtension() {
    return this.fileExtension;
  }

  /**
   * @return Mime type
   */
  public String getMimeType() {
    return this.rendition.getMimeType();
  }

  /**
   * @return Image width
   */
  public long getWidth() {
    return this.width;
  }

  /**
   * @return Image height
   */
  public long getHeight() {
    return this.height;
  }

  /**
   * @return Media format that matches with the resolved rendition. Null if no media format was specified for resolving.
   */
  public MediaFormat getMediaFormat() {
    return this.mediaFormat;
  }

  /**
   * @param mediaFormat Media format that matches with the resolved rendition. Null if no media format was specified for
   *          resolving.
   */
  public void setMediaFormat(MediaFormat mediaFormat) {
    this.mediaFormat = mediaFormat;
  }

  /**
   * @param contentDispositionAttachment Force content disposition download header.
   * @return Media path (not externalized)
   */
  public @NotNull String getMediaPath(boolean contentDispositionAttachment) {
    if (contentDispositionAttachment) {
      return RenditionMetadata.buildMediaPath(getRendition().getPath() + "." + MediaFileServlet.SELECTOR
          + "." + MediaFileServlet.SELECTOR_DOWNLOAD
          + "." + MediaFileServlet.EXTENSION, getFileName(contentDispositionAttachment));
    }
    else if (MediaFileType.isBrowserImage(getFileExtension()) || !MediaFileType.isImage(getFileExtension())) {
      // use "deep URL" to reference rendition directly
      // do not use Asset URL for original rendition because it creates conflicts for dispatcher cache (filename vs. directory for asset resource name)
      return RenditionMetadata.buildMediaPath(this.rendition.getPath() + ".", getFileName(contentDispositionAttachment));
    }
    else {
      // image rendition uses a file extension that cannot be displayed in browser directly - render via ImageFileServlet
      return RenditionMetadata.buildMediaPath(getRendition().getPath() + "." + ImageFileServlet.SELECTOR
          + "." + getWidth() + "." + getHeight()
          + "." + MediaFileServlet.EXTENSION, getFileName(contentDispositionAttachment));
    }
  }

  /**
   * Returns a dynamic media path part (object and further URL modifiers, the string after "/is/image/").
   * @param contentDispositionAttachment Force content disposition download header.
   * @param damContext DAM context
   * @return Dynamic media path part or null if dynamic media not supported for this rendition
   */
  public @Nullable String getDynamicMediaPath(boolean contentDispositionAttachment, DamContext damContext) {
    if (contentDispositionAttachment) {
      // serve static content from dynamic media for content disposition attachment
      return DynamicMediaPath.buildContent(damContext, true);
    }
    else if ((MediaFileType.isBrowserImage(getFileExtension())
        && (MediaFileType.isVectorImage(getFileExtension()) || StringUtils.equals(getFileExtension(), FileExtension.GIF)))
        || !MediaFileType.isImage(getFileExtension())) {
      // serve non-image requests or Vector/GIF images as static content from dynamic media
      // (vector can be scaled in browser directly, GIF may be animated which is not supported by dynamic media)
      return DynamicMediaPath.buildContent(damContext, false);
    }
    else {
      // display original rendition with original file size - or image rendition that uses a file extension
      // that cannot be displayed in browser directly - render via dynamic media
      return DynamicMediaPath.buildImage(damContext, getWidth(), getHeight());
    }
  }

  /**
   * Checks if this rendition matches the given width/height.
   * @param checkWidth Width
   * @param checkHeight Height
   * @return true if matches
   */
  public boolean matches(long checkWidth, long checkHeight) {
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
   * @param minWidthHeight Min. width/height (the longest edge)
   * @param ratio Ratio
   * @return true if matches
   */
  public boolean matches(long minWidth, long minHeight, long maxWidth, long maxHeight, long minWidthHeight, double ratio) {
    if (minWidthHeight > 0 && (getWidth() < minWidthHeight && getHeight() < minWidthHeight)) {
      return false;
    }
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
      double renditionRatio = Ratio.get(getWidth(), getHeight());
      if (!Ratio.matches(renditionRatio, ratio)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(this.rendition.getPath())
        .hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    RenditionMetadata other = (RenditionMetadata)obj;
    return new EqualsBuilder()
        .append(this.rendition.getPath(), other.rendition.getPath())
        .build();
  }

  @Override
  public int compareTo(RenditionMetadata obj) {
    // always prefer the virtual rendition
    boolean thisIsVirtualRendition = this instanceof VirtualTransformedRenditionMetadata;
    boolean otherIsVirtualRendition = obj instanceof VirtualTransformedRenditionMetadata;
    if (thisIsVirtualRendition && !otherIsVirtualRendition) {
      return -2;
    }
    else if (otherIsVirtualRendition && !thisIsVirtualRendition) {
      return 2;
    }

    // always prefer original rendition
    boolean thisIsOriginalRendition = isOriginalRendition(getRendition());
    boolean otherIsOriginalRendition = isOriginalRendition(obj.getRendition());
    if (thisIsOriginalRendition && !otherIsOriginalRendition) {
      return -1;
    }
    else if (otherIsOriginalRendition && !thisIsOriginalRendition) {
      return 1;
    }

    // order by width, height, rendition path
    Long thisWidth = getWidth();
    Long otherWidth = obj.getWidth();
    if (thisWidth.equals(otherWidth)) {
      Long thisHeight = getHeight();
      Long otherHeight = obj.getHeight();
      if (thisHeight.equals(otherHeight)) {
        String thisPath = getRendition().getPath();
        String otherPath = obj.getRendition().getPath();
        if (!StringUtils.equals(thisPath, otherPath)) {
          // same with/height - compare paths as last resort
          return thisPath.compareTo(otherPath);
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

  @SuppressWarnings("null")
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected Layer getLayer() {
    if (MediaFileType.isImage(getFileExtension())) {
      return this.rendition.adaptTo(Resource.class).adaptTo(Layer.class);
    }
    else {
      return null;
    }
  }

  @SuppressWarnings("null")
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected InputStream getInputStream() {
    return this.rendition.adaptTo(Resource.class).adaptTo(InputStream.class);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName())
        .append(": ")
        .append(this.rendition.getPath());
    if (this.width > 0 || this.height > 0) {
      sb.append(" (").append(Long.toString(this.width)).append("x").append(Long.toString(this.height)).append(")");
    }
    return sb.toString();
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
    if (type == Rendition.class) {
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
