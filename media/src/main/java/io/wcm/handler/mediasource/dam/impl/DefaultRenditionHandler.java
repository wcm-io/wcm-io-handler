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

import static io.wcm.handler.media.format.impl.MediaFormatSupport.getRequestedFileExtensions;
import static io.wcm.handler.media.format.impl.MediaFormatSupport.visitMediaFormats;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.google.common.collect.ImmutableSet;

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaFileType;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatHandler;
import io.wcm.handler.media.format.Ratio;
import io.wcm.handler.media.format.impl.MediaFormatVisitor;
import io.wcm.handler.mediasource.dam.AssetRendition;

/**
 * Handles resolving DAM renditions and resizing for media handler.
 */
class DefaultRenditionHandler implements RenditionHandler {

  private Set<RenditionMetadata> renditions;
  private final RenditionMetadata originalRendition;
  private final DamContext damContext;

  /**
   * @param damContext DAM context objects
   */
  DefaultRenditionHandler(DamContext damContext) {
    this.damContext = damContext;

    Rendition damOriginalRendition = damContext.getAsset().getOriginal();
    originalRendition = damOriginalRendition != null ? new RenditionMetadata(damOriginalRendition) : null;
  }

  protected RenditionMetadata getOriginalRendition() {
    return this.originalRendition;
  }

  /**
   * @return All renditions that are available for this asset
   */
  Set<RenditionMetadata> getAvailableRenditions(MediaArgs mediaArgs) {
    if (this.renditions == null) {
      // gather rendition infos of all renditions and sort them by size (smallest or virtual crop rendition first)
      Set<RenditionMetadata> candidates = new TreeSet<>();
      for (Rendition rendition : damContext.getAsset().getRenditions()) {
        addRendition(candidates, rendition, mediaArgs);
      }
      candidates = postProcessCandidates(candidates, mediaArgs);
      this.renditions = ImmutableSet.<RenditionMetadata>copyOf(candidates);
    }
    return this.renditions;
  }

  /**
   * Provides an option to post process the list of candidates. Can be overridden in subclasses
   * @param candidates Candidates
   * @param mediaArgs Media args
   * @return {@link Set} of {@link RenditionMetadata}
   */
  protected Set<RenditionMetadata> postProcessCandidates(Set<RenditionMetadata> candidates, MediaArgs mediaArgs) {
    return candidates;
  }

  /**
   * adds rendition to the list of candidates, if it should be available for resolving
   * @param candidates Candidates
   * @param rendition Rendition
   */
  private void addRendition(Set<RenditionMetadata> candidates, Rendition rendition, MediaArgs mediaArgs) {
    // ignore AEM-generated thumbnail renditions unless allowed via mediaargs
    if (!mediaArgs.isIncludeAssetThumbnails() && AssetRendition.isThumbnailRendition(rendition)) {
      return;
    }
    // ignore AEM-generated web renditions unless allowed via mediaargs
    boolean isIncludeAssetWebRenditions = mediaArgs.isIncludeAssetWebRenditions() != null
        ? mediaArgs.isIncludeAssetWebRenditions()
        : true;
    if (!isIncludeAssetWebRenditions && AssetRendition.isWebRendition(rendition)) {
      return;
    }
    RenditionMetadata renditionMetadata = createRenditionMetadata(rendition);
    candidates.add(renditionMetadata);
  }

  /**
   * Create rendition metadata for given rendition. May be overridden by subclasses.
   * @param rendition Rendition
   * @return Rendition metadata
   */
  protected RenditionMetadata createRenditionMetadata(Rendition rendition) {
    return new RenditionMetadata(rendition);
  }

  /**
   * Get all renditions that match the requested list of file extension.
   * @param fileExtensions List of file extensions
   * @return Matching renditions
   */
  private Set<RenditionMetadata> getRendtionsMatchingFileExtensions(String[] fileExtensions, MediaArgs mediaArgs) {

    // if no file extension restriction get all renditions
    Set<RenditionMetadata> allRenditions = getAvailableRenditions(mediaArgs);
    if (fileExtensions == null || fileExtensions.length == 0) {
      return allRenditions;
    }

    // otherwise return those with matching extensions
    Set<RenditionMetadata> matchingRenditions = new TreeSet<>();
    for (RenditionMetadata rendition : allRenditions) {
      for (String fileExtension : fileExtensions) {
        if (StringUtils.equalsIgnoreCase(fileExtension, rendition.getFileExtension())) {
          matchingRenditions.add(rendition);
          break;
        }
      }
    }
    return matchingRenditions;
  }

  /**
   * Get rendition (probably virtual) for given media arguments.
   * @param mediaArgs Media arguments
   * @return Rendition or null if none is matching
   */
  @Override
  public RenditionMetadata getRendition(MediaArgs mediaArgs) {

    // get list of file extensions requested
    String[] requestedFileExtensions = getRequestedFileExtensions(mediaArgs);

    // if the array is null file extensions constraints are applied, but do not match to each other
    // - no rendition can fulfill these constraints
    if (requestedFileExtensions == null) {
      return null;
    }

    // check if a specific media size is requested
    boolean isSizeMatchingRequest = isSizeMatchingRequest(mediaArgs, requestedFileExtensions);

    // get rendition candidates matching for file extensions
    Set<RenditionMetadata> candidates = getRendtionsMatchingFileExtensions(requestedFileExtensions, mediaArgs);

    // if request does not contain any size restrictions return original image or first by filename matching rendition
    if (!isSizeMatchingRequest) {
      return getOriginalOrFirstRendition(candidates);
    }

    // original rendition is a image - check for matching rendition or build virtual one
    RenditionMetadata exactMatchRendition = getExactMatchRendition(candidates, mediaArgs);
    if (exactMatchRendition != null && !enforceVirtualRendition(exactMatchRendition, mediaArgs)) {
      return exactMatchRendition;
    }

    // get rendition virtual rendition downscaled from existing one
    RenditionMetadata virtualRendition = getVirtualRendition(candidates, mediaArgs);
    if (virtualRendition != null) {
      return virtualRendition;
    }

    // no match found
    return null;
  }

  private boolean enforceVirtualRendition(RenditionMetadata rendition, MediaArgs mediaArgs) {
    if (rendition.isImage() && !rendition.isVectorImage()) {
      if (damContext.getMediaHandlerConfig().enforceVirtualRenditions()) {
        return true;
      }
      if (mediaArgs.getEnforceOutputFileExtension() != null) {
        return !StringUtils.equalsIgnoreCase(rendition.getFileExtension(), mediaArgs.getEnforceOutputFileExtension());
      }
    }
    return false;
  }

  /**
   * Checks if the media args contain any with/height restriction, that means a rendition matching
   * the given size constraints is requested. Additionally it is checked that at least one image file
   * extension is requested.
   * @param mediaArgs Media arguments
   * @return true if any size restriction was defined.
   */
  private boolean isSizeMatchingRequest(MediaArgs mediaArgs, String[] requestedFileExtensions) {

    // check that at least one image file extension is in the list of requested extensions
    boolean anyImageFileExtension = false;
    for (String fileExtension : requestedFileExtensions) {
      if (MediaFileType.isImage(fileExtension)) {
        anyImageFileExtension = true;
        break;
      }
    }
    if (!anyImageFileExtension && mediaArgs.getFixedWidth() == 0 && mediaArgs.getFixedHeight() == 0) {
      return false;
    }

    // check for size restriction
    if (mediaArgs.getFixedWidth() > 0 || mediaArgs.getFixedHeight() > 0) {
      return true;
    }
    Boolean isSizeMatchingMediaFormat = visitMediaFormats(mediaArgs, new MediaFormatVisitor<Boolean>() {
      @Override
      public @Nullable Boolean visit(@NotNull MediaFormat mediaFormat) {
        // check if any width or ratio restrictions are defined for the media format
        if (mediaFormat.getEffectiveMinWidth() > 0
            || mediaFormat.getEffectiveMaxWidth() > 0
            || mediaFormat.getEffectiveMinHeight() > 0
            || mediaFormat.getEffectiveMaxHeight() > 0
            || mediaFormat.getMinWidthHeight() > 0
            || mediaFormat.getRatio() > 0) {
          return true;
        }
        // alternatively check if responsive image is requested via image sizes or picture sources
        if (mediaArgs.getImageSizes() != null || mediaArgs.getPictureSources() != null) {
          return true;
        }
        return null;
      }
    });
    return isSizeMatchingMediaFormat != null && isSizeMatchingMediaFormat.booleanValue();
  }

  /**
   * Get rendition that matches exactly with the given media args requirements.
   * @param candidates Rendition candidates
   * @param mediaArgs Media args
   * @return Rendition or null if none found
   */
  private RenditionMetadata getExactMatchRendition(final Set<RenditionMetadata> candidates, MediaArgs mediaArgs) {
    // check for fixed width and/or height request
    if (mediaArgs.getFixedWidth() > 0 || mediaArgs.getFixedHeight() > 0) {
      for (RenditionMetadata candidate : candidates) {
        if (candidate.matches(mediaArgs.getFixedWidth(), mediaArgs.getFixedHeight())) {
          return candidate;
        }
      }
    }

    // otherwise check for media format restriction
    else if (mediaArgs.getMediaFormats() != null && mediaArgs.getMediaFormats().length > 0) {
      return visitMediaFormats(mediaArgs, new MediaFormatVisitor<RenditionMetadata>() {
        @Override
        public @Nullable RenditionMetadata visit(@NotNull MediaFormat mediaFormat) {
          for (RenditionMetadata candidate : candidates) {
            if (candidate.matches((int)mediaFormat.getEffectiveMinWidth(),
                (int)mediaFormat.getEffectiveMinHeight(),
                (int)mediaFormat.getEffectiveMaxWidth(),
                (int)mediaFormat.getEffectiveMaxHeight(),
                (int)mediaFormat.getMinWidthHeight(),
                mediaFormat.getRatio())) {
              candidate.setMediaFormat(mediaFormat);
              return candidate;
            }
          }
          return null;
        }
      });
    }

    // no restriction - return original or first rendition
    else {
      return getOriginalOrFirstRendition(candidates);
    }

    // none found
    return null;
  }

  /**
   * Returns original rendition - if it is contained in the candidate set. Otherwise first candidate is returned.
   * If a VirtualCropRenditionMetadata is present always the first one is returned.
   * @param candidates Candidates
   * @return Original or first rendition of candidates or null
   */
  private RenditionMetadata getOriginalOrFirstRendition(Set<RenditionMetadata> candidates) {
    if (this.originalRendition != null && candidates.contains(this.originalRendition)) {
      return this.originalRendition;
    }
    else if (!candidates.isEmpty()) {
      return candidates.iterator().next();
    }
    else {
      return null;
    }
  }

  /**
   * Check if a rendition is available from which the required format can be downscaled from and returns
   * a virtual rendition in this case.
   * @param candidates Candidates
   * @param mediaArgs Media args
   * @return Rendition or null
   */
  private RenditionMetadata getVirtualRendition(final Set<RenditionMetadata> candidates, MediaArgs mediaArgs) {

    // get from fixed with/height
    if (mediaArgs.getFixedWidth() > 0 || mediaArgs.getFixedHeight() > 0) {
      long destWidth = mediaArgs.getFixedWidth();
      long destHeight = mediaArgs.getFixedHeight();
      double destRatio = 0;
      if (destWidth > 0 && destHeight > 0) {
        destRatio = Ratio.get(destWidth, destHeight);
      }
      return getVirtualRendition(candidates, destWidth, destHeight, 0, destRatio,
          mediaArgs.getEnforceOutputFileExtension());
    }

    // or from any media format
    return visitMediaFormats(mediaArgs, new MediaFormatVisitor<RenditionMetadata>() {
      @Override
      public @Nullable RenditionMetadata visit(@NotNull MediaFormat mediaFormat) {
        int destWidth = (int)mediaFormat.getEffectiveMinWidth();
        int destHeight = (int)mediaFormat.getEffectiveMinHeight();
        int minWidthHeight = (int)mediaFormat.getMinWidthHeight();
        double destRatio = mediaFormat.getRatio();
        // try to find matching rendition, otherwise check for next media format
        RenditionMetadata rendition = getVirtualRendition(candidates, destWidth, destHeight, minWidthHeight, destRatio,
            mediaArgs.getEnforceOutputFileExtension());
        if (rendition != null) {
          rendition.setMediaFormat(mediaFormat);
        }
        return rendition;
      }
    });
  }

  /**
   * Check if a rendition is available from which the required format can be downscaled from and returns
   * a virtual rendition in this case.
   * @param candidates Candidates
   * @param destWidth Destination width
   * @param destHeight Destination height
   * @param minWidthHeight Min. width/height (longest edge)
   * @param destRatio Destination ratio
   * @param enforceOutputFileExtension Enforce output file extension
   * @return Rendition or null
   */
  private RenditionMetadata getVirtualRendition(Set<RenditionMetadata> candidates,
      long destWidth, long destHeight, long minWidthHeight, double destRatio,
      String enforceOutputFileExtension) {

    // if ratio is defined get first rendition with matching ratio and same or bigger size
    if (destRatio > 0) {
      for (RenditionMetadata candidate : candidates) {
        if (candidate.matches(destWidth, destHeight, 0, 0, minWidthHeight, destRatio)) {
          return getVirtualRendition(candidate, destWidth, destHeight, destRatio, enforceOutputFileExtension);
        }
      }
    }
    // otherwise get first rendition which is same or bigger in width and height
    else {
      for (RenditionMetadata candidate : candidates) {
        if (candidate.matches(destWidth, destHeight, 0, 0, minWidthHeight, 0d)) {
          return getVirtualRendition(candidate, destWidth, destHeight, 0d, enforceOutputFileExtension);
        }
      }
    }

    // none found
    return null;
  }

  /**
   * Get virtual rendition for given width/height/ratio.
   * @param rendition Rendition
   * @param widthValue Width
   * @param heightValue Height
   * @param ratioValue Ratio
   * @param enforceOutputFileExtension Enforce output file extension
   * @return Rendition or null
   */
  private RenditionMetadata getVirtualRendition(RenditionMetadata rendition, long widthValue, long heightValue,
      double ratioValue, String enforceOutputFileExtension) {

    long width = widthValue;
    long height = heightValue;
    double ratio = ratioValue;

    // if ratio is missing: calculate from given rendition
    if (ratio < MediaFormatHandler.RATIO_TOLERANCE) {
      ratio = Ratio.get(rendition.getWidth(), rendition.getHeight());
    }

    // if height is missing - calculate from width
    if (height == 0 && width > 0) {
      height = (int)Math.round(width / ratio);
    }

    // if width is missing - calculate from height
    if (width == 0 && height > 0) {
      width = (int)Math.round(height * ratio);
    }

    // return virtual rendition
    if (width > 0 && height > 0) {
      if (rendition instanceof VirtualTransformedRenditionMetadata) {
        VirtualTransformedRenditionMetadata cropRendition = (VirtualTransformedRenditionMetadata)rendition;
        return new VirtualTransformedRenditionMetadata(cropRendition.getRendition(), width, height, enforceOutputFileExtension,
            cropRendition.getCropDimension(), cropRendition.getRotation());
      }
      else {
        return new VirtualRenditionMetadata(rendition.getRendition(), width, height, enforceOutputFileExtension);
      }
    }
    else {
      return null;
    }
  }

  protected Asset getAsset() {
    return damContext.getAsset();
  }

}
