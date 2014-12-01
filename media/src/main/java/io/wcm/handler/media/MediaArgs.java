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
import io.wcm.handler.media.markup.DragDropSupport;
import io.wcm.handler.url.UrlMode;
import io.wcm.wcm.commons.util.ToStringStyle;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Holds parameters to influence the media resolving process.
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
  private boolean dummyImage = true;
  private String dummyImageUrl;
  private DragDropSupport dragDropSupport = DragDropSupport.AUTO;
  private ValueMap properties;

  /**
   * Default constructor
   */
  public MediaArgs() {
    // default constructor
  }

  /**
   * @param mediaFormats Media formats
   */
  public MediaArgs(MediaFormat... mediaFormats) {
    mediaFormats(mediaFormats);
  }

  /**
   * @param mediaFormatNames Media format names
   */
  public MediaArgs(String... mediaFormatNames) {
    mediaFormatNames(mediaFormatNames);
  }

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
  public MediaArgs mediaFormats(MediaFormat... values) {
    this.mediaFormats = values;
    return this;
  }

  /**
   * Sets list of media formats to resolve to.
   * Additionally {@link #isMediaFormatsMandatory()} is set to true.
   * @param values Media formats
   * @return this
   */
  public MediaArgs mandatoryMediaFormats(MediaFormat... values) {
    mediaFormats(values);
    mediaFormatsMandatory(true);
    return this;
  }

  /**
   * Sets a single media format to resolve to.
   * @param value Media format
   * @return this
   */
  public MediaArgs mediaFormat(MediaFormat value) {
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
  public MediaArgs mediaFormatsMandatory(boolean value) {
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
  public MediaArgs mediaFormatNames(String... values) {
    this.mediaFormatNames = values;
    return this;
  }

  /**
   * Sets list of media formats to resolve to.
   * Additionally {@link #isMediaFormatsMandatory()} is set to true.
   * @param values Media format names.
   * @return this
   */
  public MediaArgs mandatoryMediaFormatNames(String... values) {
    mediaFormatNames(values);
    mediaFormatsMandatory(true);
    return this;
  }

  /**
   * Sets a single media format to resolve to.
   * @param value Media format name
   * @return this
   */
  public MediaArgs mediaFormatName(String value) {
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
  public MediaArgs fileExtensions(String... values) {
    this.fileExtensions = values;
    return this;
  }

  /**
   * @param value File extension
   * @return this
   */
  public MediaArgs fileExtension(String value) {
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
  public MediaArgs urlMode(UrlMode value) {
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
  public MediaArgs fixedWidth(long value) {
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
  public MediaArgs fixedHeight(long value) {
    this.fixedHeight = value;
    return this;
  }

  /**
   * Use fixed dimensions instead of width from media format or original image
   * @param widthValue Fixed width
   * @param heightValue Fixed height
   * @return this
   */
  public MediaArgs fixedDimension(long widthValue, long heightValue) {
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
  public MediaArgs forceDownload(boolean value) {
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
  public MediaArgs altText(String value) {
    this.altText = value;
    return this;
  }

  /**
   * @return If set to true, media handler never returns a dummy image. Otherwise this can happen in edit mode.
   */
  public boolean isDummyImage() {
    return this.dummyImage;
  }

  /**
   * @param value If set to false, media handler never returns a dummy image. Otherwise this can happen in edit mode.
   * @return this
   */
  public MediaArgs dummyImage(boolean value) {
    this.dummyImage = value;
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
  public MediaArgs dummyImageUrl(String value) {
    this.dummyImageUrl = value;
    return this;
  }

  /**
   * Drag&amp;Drop support for media builder.
   * @return Drag&amp;Drop support
   */
  public DragDropSupport getDragDropSupport() {
    return this.dragDropSupport;
  }

  /**
   * Drag&amp;Drop support for media builder.
   * @param value Drag&amp;Drop support
   * @return this
   */
  public MediaArgs dragDropSupport(DragDropSupport value) {
    if (value == null) {
      throw new IllegalArgumentException("No null value allowed for drag&drop support.");
    }
    this.dragDropSupport = value;
    return this;
  }

  /**
   * Custom properties that my be used by application-specific markup builders or processors.
   * @param map Property map. Is merged with properties already set.
   * @return this
   */
  public MediaArgs properties(Map<String, Object> map) {
    if (map == null) {
      throw new IllegalArgumentException("Map argument must not be null.");
    }
    getProperties().putAll(map);
    return this;
  }

  /**
   * Custom properties that my be used by application-specific markup builders or processors.
   * @param key Property key
   * @param value Property value
   * @return this
   */
  public MediaArgs property(String key, Object value) {
    if (key == null) {
      throw new IllegalArgumentException("Key argument must not be null.");
    }
    getProperties().put(key, value);
    return this;
  }

  /**
   * Custom properties that my be used by application-specific markup builders or processors.
   * @return Value map
   */
  public ValueMap getProperties() {
    if (this.properties == null) {
      this.properties = new ValueMapDecorator(new HashMap<String, Object>());
    }
    return this.properties;
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
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_OMIT_NULL_STYLE);
  }

  /**
   * Custom clone-method for {@link MediaArgs}
   * @return the cloned {@link MediaArgs}
   */
  // CHECKSTYLE:OFF
  @Override
  public MediaArgs clone() { //NOPMD
    // CHECKSTYLE:ON
    MediaArgs clone = new MediaArgs();

    clone.mediaFormats = ArrayUtils.clone(this.mediaFormats);
    clone.mediaFormatNames = ArrayUtils.clone(this.mediaFormatNames);
    clone.mediaFormatsMandatory = this.mediaFormatsMandatory;
    clone.fileExtensions = ArrayUtils.clone(this.fileExtensions);
    clone.urlMode = this.urlMode;
    clone.fixedWidth = this.fixedWidth;
    clone.fixedHeight = this.fixedHeight;
    clone.forceDownload = this.forceDownload;
    clone.altText = this.altText;
    clone.dummyImage = this.dummyImage;
    clone.dummyImageUrl = this.dummyImageUrl;
    clone.dragDropSupport = this.dragDropSupport;
    if (this.properties != null) {
      clone.properties = new ValueMapDecorator(new HashMap<String, Object>(this.properties));
    }

    return clone;
  }

}
