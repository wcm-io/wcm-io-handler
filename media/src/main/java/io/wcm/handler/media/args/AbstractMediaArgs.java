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
package io.wcm.handler.media.args;

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.url.UrlMode;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Abstract value object for passing multiple optional arguments to {@link io.wcm.handler.media.MediaHandler} class
 * for filtering media library item renditions and controlling markup rendering.
 * @param <T> Subclass implementation to support "builder pattern" with correct return type
 */
@SuppressWarnings("unchecked")
public abstract class AbstractMediaArgs<T extends MediaArgsType> implements MediaArgsType<T> {

  private MediaFormat[] mediaFormats;
  private String[] mediaFormatNames;
  private String[] fileExtensions;
  private UrlMode urlMode;
  private int fixedWidth;
  private int fixedHeight;
  private boolean forceDownload;
  private String altText;
  private boolean noDummyImage;
  private String dummyImageUrl;

  @Override
  public final MediaFormat[] getMediaFormats() {
    return this.mediaFormats;
  }

  @Override
  public final T setMediaFormats(MediaFormat... values) {
    this.mediaFormats = values;
    return (T)this;
  }

  @Override
  public final T setMediaFormat(MediaFormat value) {
    if (value == null) {
      this.mediaFormats = null;
    }
    else {
      this.mediaFormats = new MediaFormat[] {
          value
      };
    }
    return (T)this;
  }

  @Override
  public final String[] getMediaFormatNames() {
    return this.mediaFormatNames;
  }

  @Override
  public final T setMediaFormatNames(String... values) {
    this.mediaFormatNames = values;
    return (T)this;
  }

  @Override
  public final T setMediaFormatName(String value) {
    if (value == null) {
      this.mediaFormatNames = null;
    }
    else {
      this.mediaFormatNames = new String[] {
          value
      };
    }
    return (T)this;
  }

  @Override
  public final String[] getFileExtensions() {
    return this.fileExtensions;
  }

  @Override
  public final T setFileExtensions(String... values) {
    this.fileExtensions = values;
    return (T)this;
  }

  @Override
  public final T setFileExtension(String value) {
    this.fileExtensions = new String[] {
        value
    };
    return (T)this;
  }

  @Override
  public final UrlMode getUrlMode() {
    return this.urlMode;
  }

  @Override
  public final T setUrlMode(UrlMode value) {
    this.urlMode = value;
    return (T)this;
  }

  @Override
  public final int getFixedWidth() {
    return this.fixedWidth;
  }

  @Override
  public final T setFixedWidth(int value) {
    this.fixedWidth = value;
    return (T)this;
  }

  @Override
  public final int getFixedHeight() {
    return this.fixedHeight;
  }

  @Override
  public final T setFixedHeight(int value) {
    this.fixedHeight = value;
    return (T)this;
  }

  @Override
  public final T setFixedDimensions(int widthValue, int heightValue) {
    this.fixedWidth = widthValue;
    this.fixedHeight = heightValue;
    return (T)this;
  }

  @Override
  public final boolean isForceDownload() {
    return this.forceDownload;
  }

  @Override
  public final T setForceDownload(boolean value) {
    this.forceDownload = value;
    return (T)this;
  }

  @Override
  public final String getAltText() {
    return this.altText;
  }

  @Override
  public final T setAltText(String value) {
    this.altText = value;
    return (T)this;
  }

  @Override
  public final boolean isNoDummyImage() {
    return this.noDummyImage;
  }

  @Override
  public final T setNoDummyImage(boolean value) {
    this.noDummyImage = value;
    return (T)this;
  }

  @Override
  public final String getDummyImageUrl() {
    return this.dummyImageUrl;
  }

  @Override
  public final T setDummyImageUrl(String value) {
    this.dummyImageUrl = value;
    return (T)this;
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

  @Override
  public T clone() throws CloneNotSupportedException {
    T clone = (T)super.clone();

    // explicitly copy all array fields (primitive properties are properly cloned by super.clone()
    clone.setMediaFormats(ArrayUtils.clone(this.mediaFormats));
    clone.setFileExtensions(ArrayUtils.clone(this.fileExtensions));

    return clone;
  }

}
