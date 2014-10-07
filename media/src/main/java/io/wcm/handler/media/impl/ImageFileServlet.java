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
package io.wcm.handler.media.impl;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.MediaHandlerConfig;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.image.Layer;

/**
 * Stream resized or cropped image from binary data stored in a nt:file or nt:resource node.
 * Optional support for Content-Disposition header ("download_attachment").
 */
@SlingServlet(
    resourceTypes = {
        JcrConstants.NT_FILE, JcrConstants.NT_RESOURCE
    },
    selectors = ImageFileServlet.SELECTOR,
    extensions = MediaFileServlet.EXTENSION,
    methods = HttpConstants.METHOD_GET)
public final class ImageFileServlet extends AbstractMediaFileServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Selector
   */
  public static final String SELECTOR = "image_file";

  @Override
  protected byte[] getBinaryData(Resource resource, SlingHttpServletRequest request) throws IOException {
    // get media app config
    MediaHandlerConfig config = AdaptTo.notNull(request, MediaHandlerConfig.class);

    // check for image scaling parameters
    int width = 0;
    int height = 0;
    String[] selectors = request.getRequestPathInfo().getSelectors();
    if (selectors.length >= 3) {
      width = NumberUtils.toInt(selectors[1]);
      height = NumberUtils.toInt(selectors[2]);
    }
    if (width <= 0 || height <= 0) {
      return null;
    }

    // check for cropping parameter
    CropDimension cropDimension = null;
    if (selectors.length >= 4) {
      String cropString = selectors[3];
      try {
        cropDimension = CropDimension.fromCropString(cropString);
      }
      catch (IllegalArgumentException ex) {
        // ignore
      }
    }

    // if resizing requested rescale via layer
    Layer layer = resource.adaptTo(Layer.class);
    if (layer == null) {
      return null;
    }

    // if required: crop image
    if (cropDimension != null) {
      layer.crop(cropDimension.getRectangle());
    }

    // resize layer
    if (width <= layer.getWidth() && height <= layer.getHeight()) {
      layer.resize(width, height);
    }

    // stream to byte array
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    String contentType = getContentType(resource, request);
    layer.write(contentType, config.getDefaultImageQuality(contentType), bos);
    bos.flush();
    return bos.toByteArray();
  }

  @Override
  protected String getContentType(Resource resource, SlingHttpServletRequest request) {

    // get filename from suffix to get extension
    String fileName = request.getRequestPathInfo().getSuffix();
    if (StringUtils.isNotEmpty(fileName)) {
      // if extension is PNG use PNG content type, otherwise fallback to JPEG
      String fileExtension = StringUtils.substringAfterLast(fileName, ".");
      if (StringUtils.equalsIgnoreCase(fileExtension, FileExtension.PNG)) {
        return ContentType.PNG;
      }
    }

    // for rendered images use JPEG mime type as default fallback
    return ContentType.JPEG;
  }

  /**
   * Get image filename to be used for the URL with file extension matching the image format which is produced by this
   * servlet.
   * @param pOriginalFilename Original filename of the image to render.
   * @return Filename to be used for URL.
   */
  public static String getImageFileName(String pOriginalFilename) {
    String namePart = StringUtils.substringBeforeLast(pOriginalFilename, ".");
    String extensionPart = StringUtils.substringAfterLast(pOriginalFilename, ".");

    // use PNG format if original image is PNG, otherwise always use JPEG
    if (StringUtils.equalsIgnoreCase(extensionPart, FileExtension.PNG)) {
      extensionPart = FileExtension.PNG;
    }
    else {
      extensionPart = FileExtension.JPEG;
    }
    return namePart + "." + extensionPart;
  }

}
