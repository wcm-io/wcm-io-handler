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

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.day.cq.dam.api.Asset;

/**
 * Handles resolving DAM renditions and resizing for media handler.
 */
class CropRenditionHandler extends DefaultRenditionHandler {

  private static final Pattern DEFAULT_WEB_RENDITION_PATTERN = Pattern.compile("^cq5dam\\.web\\..*$");
  private final CropDimension cropDimension;
  private final String assetFileExtension;

  /**
   * @param asset DAM asset
   * @param cropDimension Crop dimension
   */
  public CropRenditionHandler(Asset asset, CropDimension cropDimension) {
    super(asset);
    this.cropDimension = cropDimension;
    assetFileExtension = StringUtils.substringAfterLast(asset.getName(), ".");
  }

  /**
   * Searches for the biggest web enabled rendition and, if exists, adds a {@link VirtualCropRenditionMetadata} to the
   * list
   * @param candidates
   * @return {@link Set} of {@link RenditionMetadata}
   */
  @Override
  protected Set<RenditionMetadata> postProcessCandidates(Set<RenditionMetadata> candidates) {
    TreeSet<RenditionMetadata> processedCandidates = new TreeSet<>(candidates);
    Iterator<RenditionMetadata> descendingIterator = processedCandidates.descendingIterator();
    VirtualCropRenditionMetadata cropRendition = null;
    while (descendingIterator.hasNext()) {
      RenditionMetadata rendition = descendingIterator.next();
      if (DEFAULT_WEB_RENDITION_PATTERN.matcher(rendition.getRendition().getName()).matches()) {
        RenditionMetadata sourceRendition = new RenditionMetadata(rendition.getRendition());
        boolean isImage = FileExtension.isImage(assetFileExtension);
        if (isImage
            && sourceRendition.getWidth() >= cropDimension.getRight()
            && sourceRendition.getHeight() >= cropDimension.getBottom()) {
          // found biggest virtual rendition for cropped image
          cropRendition = new VirtualCropRenditionMetadata(sourceRendition.getRendition(),
              cropDimension.getWidth(), cropDimension.getHeight(), cropDimension);
          break;
        }
      }
    }
    if (cropRendition != null) {
      processedCandidates.add(cropRendition);
    }
    return processedCandidates;
  }

}
