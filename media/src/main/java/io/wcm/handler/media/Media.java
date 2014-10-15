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

import org.osgi.annotation.versioning.ProviderType;

/**
 * Holds information about a media request processed and resolved by {@link MediaHandler}.
 */
@ProviderType
public final class Media {

  private final MediaSource mediaSource;
  private MediaRequest mediaRequest;
  private HtmlElement<?> element;
  private String url;
  private Asset asset;
  private Rendition rendition;
  private Rendition fallbackRendition;
  private CropDimension cropDimension;
  private MediaInvalidReason mediaInvalidReason;

  /**
   * @param mediaSource Media source
   * @param mediaRequest Processed media request
   */
  public Media(MediaSource mediaSource, MediaRequest mediaRequest) {
    this.mediaSource = mediaSource;
    this.mediaRequest = mediaRequest;
  }

  /**
   * @return Media source
   */
  public MediaSource getMediaSource() {
    return this.mediaSource;
  }

  /**
   * @return Media handling request
   */
  public MediaRequest getMediaRequest() {
    return this.mediaRequest;
  }

  /**
   * @param mediaRequest Media handling request
   */
  public void setMediaRequest(MediaRequest mediaRequest) {
    this.mediaRequest = mediaRequest;
  }

  /**
   * @return Html element
   */
  public HtmlElement<?> getElement() {
    return this.element;
  }

  /**
   * @return Media HTML element serialized to string. Returns null if media element is null.
   */
  public String getMarkup() {
    if (this.element == null) {
      return null;
    }
    return this.element.toString();
  }

  /**
   * @param value Html element
   */
  public void setElement(HtmlElement<?> value) {
    this.element = value;
  }

  /**
   * @return Media URL
   */
  public String getUrl() {
    return this.url;
  }

  /**
   * @param value Media URL
   */
  public void setUrl(String value) {
    this.url = value;
  }

  /**
   * Get media item info that was resolved during media handler processing
   * @return Media item
   */
  public Asset getAsset() {
    return this.asset;
  }

  /**
   * Set media item that was resolved during media handler processing
   * @param asset Media item
   */
  public void setAsset(Asset asset) {
    this.asset = asset;
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
    return getUrl() != null;
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
    return this.url;
  }

}
