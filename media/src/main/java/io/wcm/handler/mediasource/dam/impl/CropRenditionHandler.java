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
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;

/**
 * Handles resolving DAM renditions and resizing for media handler.
 */
class CropRenditionHandler extends DefaultRenditionHandler {

  private static final Pattern DEFAULT_WEB_RENDITION_PATTERN = Pattern.compile("^cq5dam\\.web\\.1280\\.1280\\..*$");
  private final CropDimension cropDimension;

  /**
   * @param asset DAM asset
   * @param cropDimension Crop dimension
   */
  public CropRenditionHandler(Asset asset, CropDimension cropDimension) {
    super(asset);
    this.cropDimension = cropDimension;
  }

  @Override
  protected void addRendition(Set<RenditionMetadata> candidates, Rendition rendition) {
    if (DEFAULT_WEB_RENDITION_PATTERN.matcher(rendition.getName()).matches()) {
      RenditionMetadata sourceRendition = new RenditionMetadata(rendition);
      String originalFileExtension = StringUtils.substringAfterLast(asset.getName(), ".");
      boolean isImage = FileExtension.isImage(originalFileExtension);
      if (isImage
          && sourceRendition.getWidth() >= cropDimension.getRight()
          && sourceRendition.getHeight() >= cropDimension.getBottom()) {
        // add virtual rendition for cropped image
        candidates.add(new VirtualCropRenditionMetadata(sourceRendition.getRendition(),
            cropDimension.getWidth(), cropDimension.getHeight(), cropDimension));
      }
    }
    else {
      super.addRendition(candidates, rendition);
    }


  }
}
