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

import io.wcm.wcm.commons.util.ToStringStyle;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Dimension with width and height as integer.
 * This class is used instead of {@link java.awt.Dimension} because the latter converts the dimensions to double.
 */
@ProviderType
public class Dimension {

  private final long width;
  private final long height;

  /**
   * @param width Width in pixels
   * @param height Height in pixels
   */
  public Dimension(long width, long height) {
    this.width = width;
    this.height = height;
  }

  /**
   * @return Width in pixels
   */
  public final long getWidth() {
    return this.width;
  }

  /**
   * @return Height in pixels
   */
  public final long getHeight() {
    return this.height;
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

}
