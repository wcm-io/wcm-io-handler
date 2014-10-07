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
import io.wcm.wcm.commons.contenttype.FileExtension;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.Adaptable;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;

/**
 * Handles resolving DAM renditions and resizing for media handler.
 */
class CropRenditionHandler extends DefaultRenditionHandler implements RenditionHandler {

  private static final Pattern DEFAULT_WEB_RENDITION_PATTERN = Pattern.compile("^cq5dam\\.web\\.1280\\.1280\\..*$");

  private final Asset asset;
  private final CropDimension cropDimension;
  private final Set<RenditionMetadata> renditions;
  private final RenditionMetadata originalRendition;

  /**
   * @param asset DAM asset
   * @param cropDimension Crop dimension
   */
  public CropRenditionHandler(Asset asset, CropDimension cropDimension, Adaptable adaptable) {
    super(asset, adaptable);
    this.asset = asset;
    this.cropDimension = cropDimension;

    // get original rendition
    this.originalRendition = new RenditionMetadata(asset.getOriginal());

    // check if default web rendition exists - use this one, otherwise the original
    RenditionMetadata sourceRendition = null;
    for (Rendition rendition : asset.getRenditions()) {
      if (DEFAULT_WEB_RENDITION_PATTERN.matcher(rendition.getName()).matches()) {
        sourceRendition = new RenditionMetadata(rendition);
        if (sourceRendition.getWidth() == 0 || sourceRendition.getHeight() == 0) {
          sourceRendition = null;
        }
        break;
      }
    }
    if (sourceRendition == null) {
      sourceRendition = this.originalRendition;
    }

    // check if original rendition is an image
    String originalFileExtension = StringUtils.substringAfterLast(this.asset.getName(), ".");
    boolean isImage = FileExtension.isImage(originalFileExtension);

    // check if original image is not an image because otherwise cropping is not possible
    this.renditions = new TreeSet<RenditionMetadata>();
    if (isImage) {
      // check if original image is big enough because otherwise cropping is not possible
      if (sourceRendition.getWidth() >= this.cropDimension.getRight()
          && sourceRendition.getHeight() >= this.cropDimension.getBottom()) {
        // add virtual rendition for cropped image
        this.renditions.add(new VirtualCropRenditionMetadata(sourceRendition.getRendition(),
            this.cropDimension.getWidth(), this.cropDimension.getHeight(), this.cropDimension));
      }
    }

    // always add original rendition
    this.renditions.add(this.originalRendition);
  }

  /**
   * @return All renditions that are available for this asset
   */
  @Override
  protected Set<RenditionMetadata> getAvailableRenditions() {
    return this.renditions;
  }

}
