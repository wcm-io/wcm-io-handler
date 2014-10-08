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
package io.wcm.handler.media;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.spi.MediaSource;

/**
 * Holds information about a media reference processed and resolved by {@link MediaHandler}
 */
public final class MediaMetadata {

  private final MediaReference originalMediaReference;
  private final MediaReference mediaReference;
  private final MediaSource mediaSource;
  private HtmlElement<?> media;
  private String mediaUrl;
  private MediaItem mediaItem;
  private Rendition rendition;
  private Rendition fallbackRendition;
  private CropDimension cropDimension;
  private MediaInvalidReason mediaInvalidReason;

  /**
   * @param originalMediaReference Original media reference
   * @param mediaReference Processed media reference
   * @param mediaSource Media source
   */
  public MediaMetadata(MediaReference originalMediaReference, MediaReference mediaReference, MediaSource mediaSource) {
    this.originalMediaReference = originalMediaReference;
    this.mediaReference = mediaReference;
    this.mediaSource = mediaSource;
  }

  /**
   * @return Original media reference
   */
  public MediaReference getOriginalMediaReference() {
    return this.originalMediaReference;
  }

  /**
   * @return Processed media reference
   */
  public MediaReference getMediaReference() {
    return this.mediaReference;
  }

  /**
   * @return Media source
   */
  public MediaSource getMediaSource() {
    return this.mediaSource;
  }

  /**
   * @return Html element
   */
  public HtmlElement<?> getMedia() {
    return this.media;
  }

  /**
   * @return Media HTML element serialized to string. Returns null if media element is null.
   */
  public String getMediaMarkup() {
    if (this.media == null) {
      return null;
    }
    return this.media.toString();
  }

  /**
   * @param media Html element
   */
  public void setMedia(HtmlElement<?> media) {
    this.media = media;
  }

  /**
   * @return Media URL
   */
  public String getMediaUrl() {
    return this.mediaUrl;
  }

  /**
   * @param mediaUrl Media URL
   */
  public void setMediaUrl(String mediaUrl) {
    this.mediaUrl = mediaUrl;
  }

  /**
   * Get media item info that was resolved during media handler processing
   * @return Media item
   */
  public MediaItem getMediaItem() {
    return this.mediaItem;
  }

  /**
   * Set media item that was resolved during media handler processing
   * @param mediaItem Media item
   */
  public void setMediaItem(MediaItem mediaItem) {
    this.mediaItem = mediaItem;
  }

  /**
   * Get rendition that was resolved during media handler processing
   * @return Rendition
   */
  public Rendition getRendition() {
    return this.rendition;
  }

  /**
   * Set rendition that was resolved during media handler processing
   * @param rendition Rendition
   */
  public void setRendition(Rendition rendition) {
    this.rendition = rendition;
  }

  /**
   * Get fallback rendition info that was resolved during media handler processing
   * @return Fallback rendition
   */
  public Rendition getFallbackRendition() {
    return this.fallbackRendition;
  }

  /**
   * Set fallback rendition info that was resolved during media handler processing
   * @param fallbackRendition Fallback rendition
   */
  public void setFallbackRendition(Rendition fallbackRendition) {
    this.fallbackRendition = fallbackRendition;
  }

  /**
   * @return Crop dimensions (optional)
   */
  public CropDimension getCropDimension() {
    return this.cropDimension;
  }

  /**
   * @param cropDimension Crop dimensions (optional)
   */
  public void setCropDimension(CropDimension cropDimension) {
    this.cropDimension = cropDimension;
  }

  /**
   * @return true if link is valid and was resolved successfully
   */
  public boolean isValid() {
    return getMediaUrl() != null;
  }

  /**
   * @return Reason why the requested media could not be resolved and is invalid
   */
  public MediaInvalidReason getMediaInvalidReason() {
    return this.mediaInvalidReason;
  }

  /**
   * @param mediaInvalidReason Reason why the requested media could not be resolved and is invalid
   */
  public void setMediaInvalidReason(MediaInvalidReason mediaInvalidReason) {
    this.mediaInvalidReason = mediaInvalidReason;
  }

  @Override
  public String toString() {
    return this.mediaUrl;
  }

}
