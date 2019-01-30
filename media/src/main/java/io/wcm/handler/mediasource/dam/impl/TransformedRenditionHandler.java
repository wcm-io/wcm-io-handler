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

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.CropDimension;

/**
 * Extended rendition handler supporting cropping and rotating of images.
 */
public class TransformedRenditionHandler extends DefaultRenditionHandler {

  // supported rotation values
  private static final int ROTATE_90 = 90;
  private static final int ROTATE_180 = 180;
  private static final int ROTATE_270 = 270;

  private static final Pattern DEFAULT_WEB_RENDITION_PATTERN = Pattern.compile("^cq5dam\\.web\\..*$");
  private final CropDimension cropDimension;
  private final Integer rotation;

  /**
   * @param asset DAM asset
   * @param cropDimension Crop dimension
   */
  TransformedRenditionHandler(Asset asset, CropDimension cropDimension, Integer rotation) {
    super(asset);
    this.cropDimension = cropDimension;
    this.rotation = (rotation != null && isValidRotation(rotation)) ? rotation : null;
  }

  /**
   * Searches for the biggest web enabled rendition and, if exists,
   * adds a {@link VirtualTransformedRenditionMetadata} to the list.
   * @param candidates
   * @return {@link Set} of {@link RenditionMetadata}
   */
  @Override
  protected Set<RenditionMetadata> postProcessCandidates(Set<RenditionMetadata> candidates) {
    NavigableSet<RenditionMetadata> processedCandidates = new TreeSet<>(candidates);
    if (cropDimension != null) {
      VirtualTransformedRenditionMetadata cropRendition = getCropRendition(processedCandidates);
      if (cropRendition != null) {
        // return only cropped rendition
        processedCandidates.clear();
        processedCandidates.add(cropRendition);
        return processedCandidates;
      }
    }
    return rotateSourceRenditions(processedCandidates);
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
        .map(rendition -> new VirtualTransformedRenditionMetadata(rendition.getRendition(),
            rotateMapWidth(rendition.getWidth(), rendition.getHeight()),
            rotateMapHeight(rendition.getWidth(), rendition.getHeight()),
            null, rotation))
        .collect(Collectors.toCollection(TreeSet::new));
  }

  /**
   * Searches for the biggest web-enabled rendition that matches the crop dimensions width and height or is bigger.
   * @param candidates
   * @return Rendition or null if no match found
   */
  private VirtualTransformedRenditionMetadata getCropRendition(NavigableSet<RenditionMetadata> candidates) {
    RenditionMetadata original = getOriginalRendition();
    if (original == null) {
      return null;
    }
    Double scaleFactor = getCropScaleFactor(candidates);
    CropDimension scaledCropDimension = new CropDimension(
        Math.round(cropDimension.getLeft() * scaleFactor),
        Math.round(cropDimension.getTop() * scaleFactor),
        Math.round(cropDimension.getWidth() * scaleFactor),
        Math.round(cropDimension.getHeight() * scaleFactor));
    return new VirtualTransformedRenditionMetadata(original.getRendition(),
        rotateMapWidth(scaledCropDimension.getWidth(), scaledCropDimension.getHeight()),
        rotateMapHeight(scaledCropDimension.getWidth(), scaledCropDimension.getHeight()),
        scaledCropDimension, rotation);
  }

  /**
   * The cropping coordinates are stored with coordinates relating to the web-enabled rendition. But we want
   * to crop the original image, so we have to scale those values to match the coordinates in the original image.
   * @return Scale factor
   */
  private double getCropScaleFactor(NavigableSet<RenditionMetadata> candidates) {
    RenditionMetadata original = getOriginalRendition();
    RenditionMetadata webEnabled = getWebEnabledRendition(candidates);
    if (original == null || webEnabled == null || original.getWidth() == 0 || webEnabled.getWidth() == 0) {
      return 1d;
    }
    return (double)original.getWidth() / (double)webEnabled.getWidth();
  }

  private RenditionMetadata getWebEnabledRendition(NavigableSet<RenditionMetadata> candidates) {
    Iterator<RenditionMetadata> descendingIterator = candidates.descendingIterator();
    while (descendingIterator.hasNext()) {
      RenditionMetadata rendition = descendingIterator.next();
      if (DEFAULT_WEB_RENDITION_PATTERN.matcher(rendition.getRendition().getName()).matches()) {
        return rendition;
      }
    }
    return null;
  }

  /**
   * Swaps width with height if rotated 90° clock-wise or counter clock-wise
   * @param width Rendition width
   * @param height Rendition height
   * @return Width
   */
  private long rotateMapWidth(long width, long height) {
    if (rotation != null && (rotation == ROTATE_90 || rotation == ROTATE_270)) {
      return height;
    }
    else {
      return width;
    }
  }

  /**
   * Swaps height with width if rotated 90° clock-wise or counter clock-wise
   * @param width Rendition width
   * @param height Rendition height
   * @return Height
   */
  private long rotateMapHeight(long width, long height) {
    if (rotation != null && (rotation == ROTATE_90 || rotation == ROTATE_270)) {
      return width;
    }
    else {
      return height;
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
