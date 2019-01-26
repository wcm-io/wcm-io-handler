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

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.CropDimension;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Extended rendition handler supporting cropping and rotating of images.
 */
public class CropRotateRenditionHandler extends DefaultRenditionHandler {

  // supported rotation values
  private static final int ROTATE_90 = 90;
  private static final int ROTATE_180 = 180;
  private static final int ROTATE_270 = 270;

  private static final Pattern DEFAULT_WEB_RENDITION_PATTERN = Pattern.compile("^cq5dam\\.web\\..*$");
  private final CropDimension cropDimension;
  private final Integer rotation;
  private final String assetFileExtension;

  /**
   * @param asset DAM asset
   * @param cropDimension Crop dimension
   */
  CropRotateRenditionHandler(Asset asset, CropDimension cropDimension, Integer rotation) {
    super(asset);
    this.cropDimension = cropDimension;
    this.rotation = (rotation != null && isValidRotation(rotation)) ? rotation : null;
    assetFileExtension = StringUtils.substringAfterLast(asset.getName(), ".");
  }

  /**
   * Searches for the biggest web enabled rendition and, if exists,
   * adds a {@link VirtualCropRotateRenditionMetadata} to the list.
   * @param candidates
   * @return {@link Set} of {@link RenditionMetadata}
   */
  @Override
  protected Set<RenditionMetadata> postProcessCandidates(Set<RenditionMetadata> candidates) {
    NavigableSet<RenditionMetadata> processedCandidates = rotateSourceRenditions(candidates);
    if (cropDimension != null) {
      VirtualCropRotateRenditionMetadata cropRendition = getCropRendition(processedCandidates);
      if (cropRendition != null) {
        processedCandidates.add(cropRendition);
      }
    }
    return processedCandidates;
  }

  /**
   * Rotates all source renditions if configured.
   * @param candidates Candidate renditions
   * @return Virtual-rotated and sorted candidate renditions
   */
  private NavigableSet<RenditionMetadata> rotateSourceRenditions(Set<RenditionMetadata> candidates) {
    if (rotation == null) {
      return new TreeSet<>(candidates);
    }
    return candidates.stream()
        .map(rendition -> new VirtualCropRotateRenditionMetadata(rendition.getRendition(),
            rotateMapWidth(rendition), rotateMapHeight(rendition), null, rotation))
        .collect(Collectors.toCollection(TreeSet::new));
  }

  /**
   * Searches for the biggest web-enabled rendition that matches the crop dimensions width and height or is bigger.
   * @param candidates
   * @return Rendition or null if no match found
   */
  private VirtualCropRotateRenditionMetadata getCropRendition(NavigableSet<RenditionMetadata> candidates) {
    Iterator<RenditionMetadata> descendingIterator = candidates.descendingIterator();
    while (descendingIterator.hasNext()) {
      RenditionMetadata rendition = descendingIterator.next();
      if (DEFAULT_WEB_RENDITION_PATTERN.matcher(rendition.getRendition().getName()).matches()) {
        RenditionMetadata sourceRendition = new RenditionMetadata(rendition.getRendition());
        boolean isImage = FileExtension.isImage(assetFileExtension);
        long sourceRenditionWidth = rotateMapWidth(sourceRendition);
        long sourceRenditionHeight = rotateMapHeight(sourceRendition);
        if (isImage
            && sourceRenditionWidth >= cropDimension.getRight()
            && sourceRenditionHeight >= cropDimension.getBottom()) {
          // found biggest virtual rendition for cropped image
          return new VirtualCropRotateRenditionMetadata(sourceRendition.getRendition(),
              cropDimension.getWidth(), cropDimension.getHeight(), cropDimension, rotation);
        }
      }
    }
    return null;
  }

  /**
   * Swaps width with height if rotated 90° clock-wise or counter clock-wise
   * @param rendition Rendition
   * @return Width
   */
  private long rotateMapWidth(RenditionMetadata rendition) {
    if (rotation != null && (rotation == ROTATE_90 || rotation == ROTATE_270)) {
      return rendition.getHeight();
    }
    else {
      return rendition.getWidth();
    }
  }

  /**
   * Swaps height with width if rotated 90° clock-wise or counter clock-wise
   * @param rendition Rendition
   * @return Height
   */
  private long rotateMapHeight(RenditionMetadata rendition) {
    if (rotation != null && (rotation == ROTATE_90 || rotation == ROTATE_270)) {
      return rendition.getWidth();
    }
    else {
      return rendition.getHeight();
    }
  }

  /**
   * @param rotation Rotation value
   * @return true if the value is a supported image rotation operation.
   */
  public static boolean isValidRotation(int rotation) {
    return rotation == ROTATE_90
        || rotation == ROTATE_180
        || rotation == ROTATE_270;
  }

}
