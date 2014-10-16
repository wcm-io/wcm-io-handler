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

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.url.UrlMode;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Default implementation of media arguments
 */
@ProviderType
public final class MediaArgs implements Cloneable {

  private MediaFormat[] mediaFormats;
  private String[] mediaFormatNames;
  private boolean mediaFormatsMandatory;
  private String[] fileExtensions;
  private UrlMode urlMode;
  private long fixedWidth;
  private long fixedHeight;
  private boolean forceDownload;
  private String altText;
  private boolean noDummyImage;
  private String dummyImageUrl;

  /**
   * Returns list of media formats to resolve to. If {@link #isMediaFormatsMandatory()} is false,
   * the first rendition that matches any of the given media format is returned. If it is set to true,
   * for each media format given a rendition has to be resolved and returned. If not all renditions
   * could be resolved the media is marked as invalid (but the partial resolved renditions are returned anyway).
   * @return Media formats
   */
  public MediaFormat[] getMediaFormats() {
    return this.mediaFormats;
  }

  /**
   * Sets list of media formats to resolve to.
   * @param values Media formats
   * @return this
   */
  public MediaArgs setMediaFormats(MediaFormat... values) {
    this.mediaFormats = values;
    return this;
  }

  /**
   * Sets a single media format to resolve to.
   * @param value Media format
   * @return this
   */
  public MediaArgs setMediaFormat(MediaFormat value) {
    if (value == null) {
      this.mediaFormats = null;
    }
    else {
      this.mediaFormats = new MediaFormat[] {
          value
      };
    }
    return this;
  }

  /**
   * If set to true, media handler never returns a dummy image. Otherwise this can happen in edit mode.
   * @return Resolving of all media formats is mandatory.
   */
  public boolean isMediaFormatsMandatory() {
    return this.mediaFormatsMandatory;
  }

  /**
   * If set to true, media handler never returns a dummy image. Otherwise this can happen
   * in edit mode.
   * @param value Resolving of all media formats is mandatory.
   * @return this
   */
  public MediaArgs setMediaFormatsMandatory(boolean value) {
    this.mediaFormatsMandatory = value;
    return this;
  }

  /**
   * Returns list of media formats to resolve to. See {@link #getMediaFormatNames()} for details.
   * @return Media format names
   */
  public String[] getMediaFormatNames() {
    return this.mediaFormatNames;
  }

  /**
   * Sets list of media formats to resolve to.
   * @param values Media format names.
   * @return this
   */
  public MediaArgs setMediaFormatNames(String... values) {
    this.mediaFormatNames = values;
    return this;
  }

  /**
   * Sets a single media format to resolve to.
   * @param value Media format name
   * @return this
   */
  public MediaArgs setMediaFormatName(String value) {
    if (value == null) {
      this.mediaFormatNames = null;
    }
    else {
      this.mediaFormatNames = new String[] {
          value
      };
    }
    return this;
  }

  /**
   * @return File extensions
   */
  public String[] getFileExtensions() {
    return this.fileExtensions;
  }

  /**
   * @param values File extensions
   * @return this
   */
  public MediaArgs setFileExtensions(String... values) {
    this.fileExtensions = values;
    return this;
  }

  /**
   * @param value File extension
   * @return this
   */
  public MediaArgs setFileExtension(String value) {
    if (value == null) {
      this.fileExtensions = null;
    }
    else {
      this.fileExtensions = new String[] {
          value
      };
    }
    return this;
  }

  /**
   * @return URL mode
   */
  public UrlMode getUrlMode() {
    return this.urlMode;
  }

  /**
   * @param value URS mode
   * @return this
   */
  public MediaArgs setUrlMode(UrlMode value) {
    this.urlMode = value;
    return this;
  }

  /**
   * Use fixed width instead of width from media format or original image
   * @return Fixed width
   */
  public long getFixedWidth() {
    return this.fixedWidth;
  }

  /**
   * Use fixed width instead of width from media format or original image
   * @param value Fixed width
   * @return this
   */
  public MediaArgs setFixedWidth(long value) {
    this.fixedWidth = value;
    return this;
  }

  /**
   * Use fixed height instead of width from media format or original image
   * @return Fixed height
   */
  public long getFixedHeight() {
    return this.fixedHeight;
  }

  /**
   * Use fixed height instead of width from media format or original image
   * @param value Fixed height
   * @return this
   */
  public MediaArgs setFixedHeight(long value) {
    this.fixedHeight = value;
    return this;
  }

  /**
   * Use fixed dimensions instead of width from media format or original image
   * @param widthValue Fixed width
   * @param heightValue Fixed height
   * @return this
   */
  public MediaArgs setFixedDimensions(long widthValue, long heightValue) {
    this.fixedWidth = widthValue;
    this.fixedHeight = heightValue;
    return this;
  }

  /**
   * @return Whether to set a "Content-Disposition" header for forcing a "Save as" dialog on the client
   */
  public boolean isForceDownload() {
    return this.forceDownload;
  }

  /**
   * @param value Whether to set a "Content-Disposition" header for forcing a "Save as" dialog on the client
   * @return this
   */
  public MediaArgs setForceDownload(boolean value) {
    this.forceDownload = value;
    return this;
  }

  /**
   * @return The custom alternative text that is to be used instead of the one defined in the the media lib item
   */
  public String getAltText() {
    return this.altText;
  }

  /**
   * Allows to specify a custom alternative text that is to be used instead of the one defined in the the media lib item
   * @param value Custom alternative text. If null or empty, the default alt text from media library is used.
   * @return this
   */
  public MediaArgs setAltText(String value) {
    this.altText = value;
    return this;
  }

  /**
   * @return If set to true, media handler never returns a dummy image. Otherwise this can happen in edit mode.
   */
  public boolean isNoDummyImage() {
    return this.noDummyImage;
  }

  /**
   * @param value If set to true, media handler never returns a dummy image. Otherwise this can happen in edit mode.
   * @return this
   */
  public MediaArgs setNoDummyImage(boolean value) {
    this.noDummyImage = value;
    return this;
  }

  /**
   * @return Url of custom dummy image. If null default dummy image is used.
   */
  public String getDummyImageUrl() {
    return this.dummyImageUrl;
  }

  /**
   * @param value Url of custom dummy image. If null default dummy image is used.
   * @return this
   */
  public MediaArgs setDummyImageUrl(String value) {
    this.dummyImageUrl = value;
    return this;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  /**
   * Custom clone-method for {@link MediaArgs}
   * @return the cloned {@link MediaArgs}
   */
  @Override
  public MediaArgs clone() {
    try {
      MediaArgs clone = (MediaArgs)super.clone();

      // explicitly copy all array fields (primitive properties are properly cloned by super.clone()
      clone.setMediaFormats(ArrayUtils.clone(this.mediaFormats));
      clone.setMediaFormatNames(ArrayUtils.clone(this.mediaFormatNames));
      clone.setFileExtensions(ArrayUtils.clone(this.fileExtensions));

      return clone;
    }
    catch (CloneNotSupportedException ex) {
      throw new RuntimeException("Clone failed.", ex);
    }
  }

  /**
   * Shortcut for building {@link MediaArgs} with media format.
   * @param mediaFormat Media format
   * @return Media args
   */
  public static MediaArgs mediaFormat(MediaFormat mediaFormat) {
    return new MediaArgs().setMediaFormat(mediaFormat);
  }

  /**
   * Shortcut for building {@link MediaArgs} with media formats.
   * @param mediaFormats Media formats
   * @return Media args
   */
  public static MediaArgs mediaFormats(MediaFormat... mediaFormats) {
    return new MediaArgs().setMediaFormats(mediaFormats);
  }

  /**
   * Shortcut for building {@link MediaArgs} with media formats.
   * Additionally {@link #isMediaFormatsMandatory()} is set to true.
   * @param mediaFormats Media format
   * @return Media args
   */
  public static MediaArgs mandatoryMediaFormats(MediaFormat... mediaFormats) {
    return new MediaArgs().setMediaFormats(mediaFormats).setMediaFormatsMandatory(true);
  }

  /**
   * Shortcut for building {@link MediaArgs} with media format.
   * @param mediaFormatName Media format name
   * @return Media args
   */
  public static MediaArgs mediaFormat(String mediaFormatName) {
    return new MediaArgs().setMediaFormatName(mediaFormatName);
  }

  /**
   * Shortcut for building {@link MediaArgs} with media formats.
   * @param mediaFormatNames Media format names
   * @return Media args
   */
  public static MediaArgs mediaFormats(String... mediaFormatNames) {
    return new MediaArgs().setMediaFormatNames(mediaFormatNames);
  }

  /**
   * Shortcut for building {@link MediaArgs} with media formats.
   * Additionally {@link #isMediaFormatsMandatory()} is set to true.
   * @param mediaFormatNames Media format names
   * @return Media args
   */
  public static MediaArgs mandatoryMediaFormats(String... mediaFormatNames) {
    return new MediaArgs().setMediaFormatNames(mediaFormatNames).setMediaFormatsMandatory(true);
  }

  /**
   * Shortcut for building {@link MediaArgs} with URL mode.
   * @param urlMode URL mode
   * @return Media args
   */
  public static MediaArgs urlMode(UrlMode urlMode) {
    return new MediaArgs().setUrlMode(urlMode);
  }

  /**
   * Shortcut for building {@link MediaArgs} with URL mode.
   * @param fixedWidth Fixed width
   * @param fixedHeight Fixed height
   * @return Media args
   */
  public static MediaArgs fixedDimension(int fixedWidth, int fixedHeight) {
    return new MediaArgs().setFixedDimensions(fixedWidth, fixedHeight);
  }

}
