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

import io.wcm.handler.commons.jcr.JcrBinary;
import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatHandler;
import io.wcm.handler.media.impl.ImageFileServlet;
import io.wcm.handler.media.impl.MediaFileServlet;
import io.wcm.handler.url.UrlHandler;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.caching.ModificationDate;
import io.wcm.wcm.commons.contenttype.FileExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.image.Layer;

/**
 * {@link Rendition} implementation for inline media objects stored in a node in a content page.
 */
class InlineRendition extends SlingAdaptable implements Rendition {

  private final Adaptable adaptable;
  private final Resource resource;
  private final Media media;
  private final MediaArgs mediaArgs;
  private final String fileName;
  private final Dimension imageDimension;
  private final String url;
  private MediaFormat resolvedMediaFormat;

  /**
   * Special dimension instance that marks "scaling is required but not possible"
   */
  private static final Dimension SCALING_NOT_POSSIBLE_DIMENSION = new Dimension(-1, -1);

  /**
   * @param resource Binary resource
   * @param media Media metadata
   * @param mediaArgs Media args
   * @param fileName File name
   */
  InlineRendition(Resource resource, Media media, MediaArgs mediaArgs, String fileName, Adaptable adaptable) {
    this.resource = resource;
    this.media = media;
    this.mediaArgs = mediaArgs;
    this.adaptable = adaptable;

    // detect image dimension
    String processedFileName = fileName;

    // check if scaling is possible
    String fileExtension = StringUtils.substringAfterLast(processedFileName, ".");
    boolean isImage = FileExtension.isImage(fileExtension);

    Dimension dimension = null;
    Dimension scaledDimension = null;
    if (isImage) {
      // get dimension from image binary
      dimension = getImageDimension();

      // check if scaling is required
      scaledDimension = getScaledDimension(dimension);
      if (scaledDimension != null && !scaledDimension.equals(SCALING_NOT_POSSIBLE_DIMENSION)) {
        // overwrite image dimension of {@link Rendition} instance with scaled dimensions
        dimension = scaledDimension;
        // change extension to JPEG because scaling always produces JPEG images
        processedFileName = StringUtils.substringBeforeLast(processedFileName, ".") + "." + FileExtension.JPEG;
      }
    }
    this.fileName = processedFileName;
    this.imageDimension = dimension;

    // build media url (it is null if no rendition is available for the given media args)
    this.url = buildMediaUrl(scaledDimension);

    // set first media format as resolved format - because only the first is supported
    if (url != null && mediaArgs.getMediaFormats() != null && mediaArgs.getMediaFormats().length > 0) {
      this.resolvedMediaFormat = mediaArgs.getMediaFormats()[0];
    }

  }

  /**
   * Gets the dimension of the uploaded image (if the binary is an image file at all).
   * @return Dimension
   */
  private Dimension getImageDimension() {
    Dimension dimension = null;

    // check for cropping dimension
    if (this.media.getCropDimension() != null) {
      dimension = this.media.getCropDimension();
    }
    else {
      // if binary is image try to calculcate dimensions by loading it into a layer
      Layer layer = this.resource.adaptTo(Layer.class);
      if (layer != null) {
        dimension = new Dimension(layer.getWidth(), layer.getHeight());
      }
    }

    return dimension;
  }

  /**
   * Checks if the current binary is an image and has to be scaled. In this case the destination dimension is returned.
   * @return Scaled destination or null if no scaling is required. If a destination object with both
   *         width and height set to -1 is returned, a scaling is required but not possible with the given source
   *         object.
   */
  private Dimension getScaledDimension(Dimension originalDimension) {

    // check if image has to be rescaled
    Dimension requestedDimension = getRequestedDimension();
    boolean scaleWidth = (requestedDimension.getWidth() > 0
        && requestedDimension.getWidth() != originalDimension.getWidth());
    boolean scaleHeight = (requestedDimension.getHeight() > 0
        && requestedDimension.getHeight() != originalDimension.getHeight());
    if (scaleWidth || scaleHeight) {
      long requestedWidth = requestedDimension.getWidth();
      long requestedHeight = requestedDimension.getHeight();

      // calculate missing width/height from ration if not specified
      double imageRatio = (double)originalDimension.getWidth() / (double)originalDimension.getHeight();
      if (requestedWidth == 0 && requestedHeight > 0) {
        requestedWidth = (int)Math.round(requestedHeight * imageRatio);
      }
      else if (requestedWidth > 0 && requestedHeight == 0) {
        requestedHeight = (int)Math.round(requestedWidth / imageRatio);
      }

      // calculate requested ratio
      double requestedRatio = (double)requestedWidth / (double)requestedHeight;

      // if ratio does not match, or requested width/height is larger than the original image scaling is not possible
      if ((imageRatio > requestedRatio + MediaFormatHandler.RATIO_TOLERANCE)
          || (imageRatio < requestedRatio - MediaFormatHandler.RATIO_TOLERANCE)
          || (originalDimension.getWidth() < requestedWidth)
          || (originalDimension.getHeight() < requestedHeight)) {
        return SCALING_NOT_POSSIBLE_DIMENSION;
      }
      else {
        return new Dimension(requestedWidth, requestedHeight);
      }

    }

    return null;
  }

  /**
   * Build media URL for this rendition - either "native" URL to repository or virtual url to rescaled version.
   * @return Media URL - null if no rendition is available
   */
  private String buildMediaUrl(Dimension scaledDimension) {

    // check for file extension filtering
    if (!isMatchingFileExtension()) {
      return null;
    }

    // check if image has to be rescaled
    if (scaledDimension != null) {

      // check if scaling is not possible
      if (scaledDimension.equals(SCALING_NOT_POSSIBLE_DIMENSION)) {
        return null;
      }

      // otherwise generate scaled image URL
      return buildScaledMediaUrl(scaledDimension, this.media.getCropDimension());
    }

    // if no scaling but cropping required builid scaled image URL
    if (this.media.getCropDimension() != null) {
      return buildScaledMediaUrl(this.media.getCropDimension(), this.media.getCropDimension());
    }

    if (mediaArgs.isForceDownload()) {
      // if not scaling and no cropping required but special content disposition headers required build download url
      return buildDownloadMediaUrl();
    }
    else {
      // if not scaling and no cropping required build native media URL
      return buildNativeMediaUrl();
    }
  }

  /**
   * Builds "native" URL that returns the binary data directly from the repository.
   * @return Media URL
   */
  private String buildNativeMediaUrl() {
    String path = null;

    Resource parentResource = this.resource.getParent();
    if (JcrBinary.isNtFile(parentResource)) {
      // if parent resource is nt:file and its node name equals the detected filename, directly use the nt:file node path
      if (StringUtils.equals(parentResource.getName(), getFileName())) {
        path = parentResource.getPath();
      }
      // otherwise use nt:file node path with custom filename
      else {
        path = parentResource.getPath() + "./" + getFileName();
      }
    }
    else {
      // nt:resource node does not have a nt:file parent, use its path directly
      path = this.resource.getPath() + "./" + getFileName();
    }

    // build externalized URL
    UrlHandler urlHandler = AdaptTo.notNull(this.adaptable, UrlHandler.class);
    return urlHandler.get(path).urlMode(this.mediaArgs.getUrlMode()).buildExternalResourceUrl();
  }

  /**
   * Builds URL to rescaled version of the binary image.
   * @return Media URL
   */
  private String buildScaledMediaUrl(Dimension dimension, CropDimension cropDimension) {
    String resourcePath = this.resource.getPath();

    // if parent resource is a nt:file resource, use this one as path for scaled image
    Resource parentResource = this.resource.getParent();
    if (JcrBinary.isNtFile(parentResource)) {
      resourcePath = parentResource.getPath();
    }

    // URL to render scaled image via {@link InlineRenditionServlet}
    String path = resourcePath + "." + ImageFileServlet.SELECTOR
        + "." + dimension.getWidth() + "." + dimension.getHeight()
        + (cropDimension != null ? "." + cropDimension.getCropString() : "")
        + (this.mediaArgs.isForceDownload() ? "." + MediaFileServlet.SELECTOR_DOWNLOAD : "")
        + "." + MediaFileServlet.EXTENSION + "/"
        // replace extension based on the format supported by ImageFileServlet for rendering for this rendition
        + ImageFileServlet.getImageFileName(getFileName());

    // build externalized URL
    UrlHandler urlHandler = AdaptTo.notNull(this.adaptable, UrlHandler.class);
    return urlHandler.get(path).urlMode(this.mediaArgs.getUrlMode()).buildExternalResourceUrl();
  }

  /**
   * Builds URL to rescaled version of the binary image.
   * @return Media URL
   */
  private String buildDownloadMediaUrl() {
    String resourcePath = this.resource.getPath();

    // if parent resource is a nt:file resource, use this one as path for scaled image
    Resource parentResource = this.resource.getParent();
    if (JcrBinary.isNtFile(parentResource)) {
      resourcePath = parentResource.getPath();
    }

    // URL to render scaled image via {@link InlineRenditionServlet}
    String path = resourcePath + "." + MediaFileServlet.SELECTOR
        + "." + MediaFileServlet.SELECTOR_DOWNLOAD
        + "." + MediaFileServlet.EXTENSION + "/" + getFileName();

    // build externalized URL
    UrlHandler urlHandler = AdaptTo.notNull(this.adaptable, UrlHandler.class);
    return urlHandler.get(path).urlMode(this.mediaArgs.getUrlMode()).buildExternalResourceUrl();
  }

  /**
   * Checks if the file extension of the current binary matches with the requested extensions from the media args.
   * @return true if file extension matches
   */
  private boolean isMatchingFileExtension() {
    String[] fileExtensions = mediaArgs.getFileExtensions();
    if (fileExtensions == null || fileExtensions.length == 0) {
      return true;
    }
    for (String fileExtension : fileExtensions) {
      if (StringUtils.equalsIgnoreCase(fileExtension, getFileExtension())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Requested dimensions either from media format or fixed dimensions from media args.
   * @return Requested dimensions
   */
  private Dimension getRequestedDimension() {

    // check for fixed dimensions from media args
    if (mediaArgs.getFixedWidth() > 0 || mediaArgs.getFixedHeight() > 0) {
      return new Dimension(mediaArgs.getFixedWidth(), mediaArgs.getFixedHeight());
    }

    // check for dimensions from mediaformat (evaluate only first media format)
    MediaFormat[] mediaFormats = mediaArgs.getMediaFormats();
    if (mediaFormats != null && mediaFormats.length > 0) {
      Dimension dimension = mediaFormats[0].getMinDimension();
      if (dimension != null) {
        return dimension;
      }
    }

    // fallback to 0/0 - no specific dimension requested
    return new Dimension(0, 0);
  }

  @Override
  public String getUrl() {
    return this.url;
  }

  @Override
  public String getPath() {
    return this.resource.getPath();
  }

  @Override
  public String getFileName() {
    return this.fileName;
  }

  @Override
  public String getFileExtension() {
    return StringUtils.substringAfterLast(this.fileName, ".");
  }

  @Override
  public long getFileSize() {
    Node node = this.resource.adaptTo(Node.class);
    if (node != null) {
      try {
        Property data = node.getProperty(JcrConstants.JCR_DATA);
        return data.getBinary().getSize();
      }
      catch (RepositoryException ex) {
        throw new RuntimeException("Unable to detect binary file size for " + this.resource.getPath(), ex);
      }
    }
    else {
      // fallback to Sling API if JCR node is not present (inefficient - but this should happen only in unit tests)
      try {
        InputStream is = this.resource.getValueMap().get(JcrConstants.JCR_DATA, InputStream.class);
        return IOUtils.toByteArray(is).length;
      }
      catch (IOException ex) {
        throw new RuntimeException("Unable to detect binary file size for " + this.resource.getPath(), ex);
      }
    }
  }

  @Override
  public Date getModificationDate() {
    return ModificationDate.get(this.resource);
  }

  @Override
  public MediaFormat getMediaFormat() {
    return resolvedMediaFormat;
  }

  @Override
  public ValueMap getProperties() {
    return this.resource.getValueMap();
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
    if (this.imageDimension != null) {
      return this.imageDimension.getWidth();
    }
    else {
      return 0;
    }
  }

  @Override
  public long getHeight() {
    if (this.imageDimension != null) {
      return this.imageDimension.getHeight();
    }
    else {
      return 0;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (type == Resource.class) {
      return (AdapterType)this.resource;
    }
    else if (type == Layer.class && isImage()) {
      return (AdapterType)this.resource.adaptTo(Layer.class);
    }
    else if (type == InputStream.class) {
      return (AdapterType)this.resource.adaptTo(InputStream.class);
    }
    return super.adaptTo(type);
  }

}
