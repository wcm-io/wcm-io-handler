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
package io.wcm.handler.media.format;

import io.wcm.config.spi.ApplicationProvider;

import java.util.regex.Pattern;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Fluent API for building media format definitions.
 */
@ProviderType
public final class MediaFormatBuilder {

  private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-\\_]+$");

  private String name;
  private String applicationId;
  private String label;
  private String description;
  private long width;
  private long minWidth;
  private long maxWidth;
  private long height;
  private long minHeight;
  private long maxHeight;
  private double ratio;
  private long ratioWidth;
  private long ratioHeight;
  private long fileSizeMax;
  private String[] extensions;
  private String renditionGroup;
  private boolean internal;
  private int ranking;

  private MediaFormatBuilder() {
    // private constructor
  }

  /**
   * Create a new media format builder.
   * @return Media format builder
   */
  public static MediaFormatBuilder create() {
    return new MediaFormatBuilder();
  }

  /**
   * Create a new media format builder.
   * @param name Media format name. Only characters, numbers, hyphen and underline are allowed.
   * @return Media foramt builder
   */
  public static MediaFormatBuilder create(String name, String applicationId) {
    return new MediaFormatBuilder()
    .name(name)
    .applicationId(applicationId);
  }

  /**
   * @param value Media format name. Only characters, numbers, hyphen and underline are allowed.
   * @return this
   */
  public MediaFormatBuilder name(String value) {
    if (value == null || !NAME_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid name: " + value);
    }
    this.name = value;
    return this;
  }

  /**
   * @param value Media format name. Only characters, numbers, hyphen and underline are allowed.
   * @return this
   */
  public MediaFormatBuilder applicationId(String value) {
    if (value == null || !ApplicationProvider.APPLICATION_ID_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid applicaiton id: " + value);
    }
    this.applicationId = value;
    return this;
  }

  /**
   * @param value Label for displaying to user
   * @return this
   */
  public MediaFormatBuilder label(String value) {
    this.label = value;
    return this;
  }

  /**
   * @param value Description for displaying to user
   * @return this
   */
  public MediaFormatBuilder description(String value) {
    this.description = value;
    return this;
  }

  /**
   * @param value Fixed image width (px)
   * @return this
   */
  public MediaFormatBuilder width(long value) {
    this.width = value;
    return this;
  }

  /**
   * @param value Image width min (px)
   * @return this
   */
  public MediaFormatBuilder minWidth(long value) {
    this.minWidth = value;
    return this;
  }

  /**
   * @param value Image width max (px)
   * @return this
   */
  public MediaFormatBuilder maxWidth(long value) {
    this.maxWidth = value;
    return this;
  }

  /**
   * @param min Image width min (px)
   * @param max Image width max (px)
   * @return this
   */
  public MediaFormatBuilder width(long min, long max) {
    this.minWidth = min;
    this.maxWidth = max;
    return this;
  }

  /**
   * @param value Fixed image height (px)
   * @return this
   */
  public MediaFormatBuilder height(long value) {
    this.height = value;
    return this;
  }

  /**
   * @param value Image height min (px)
   * @return this
   */
  public MediaFormatBuilder minHeight(long value) {
    this.minHeight = value;
    return this;
  }

  /**
   * @param value Image height max (px)
   * @return this
   */
  public MediaFormatBuilder maxHeight(long value) {
    this.maxHeight = value;
    return this;
  }

  /**
   * @param min Image height min (px)
   * @param max Image height max (px)
   * @return this
   */
  public MediaFormatBuilder height(long min, long max) {
    this.minHeight = min;
    this.maxHeight = max;
    return this;
  }

  /**
   * @param widthValue Fixed image width (px)
   * @param heightValue Fixed image height (px)
   * @return this
   */
  public MediaFormatBuilder fixedDimension(long widthValue, long heightValue) {
    this.width = widthValue;
    this.height = heightValue;
    return this;
  }

  /**
   * @param value Ratio (width/height)
   * @return this
   */
  public MediaFormatBuilder ratio(double value) {
    this.ratio = value;
    return this;
  }

  /**
   * @param widthValue Ratio width sample value (is used for calculating the ratio together with ratioHeight, and for
   *          display)
   * @param heightValue Ratio height sample value (is used for calculating the ratio together with ratioWidth, and for
   *          display)
   * @return this
   */
  public MediaFormatBuilder ratio(long widthValue, long heightValue) {
    this.ratioWidth = widthValue;
    this.ratioHeight = heightValue;
    return this;
  }

  /**
   * @param value Max. file size (bytes)
   * @return this
   */
  public MediaFormatBuilder fileSizeMax(long value) {
    this.fileSizeMax = value;
    return this;
  }

  /**
   * @param value Allowed file extensions
   * @return this
   */
  public MediaFormatBuilder extensions(String... value) {
    this.extensions = value;
    return this;
  }

  /**
   * @param value Rendition group id
   * @return this
   */
  public MediaFormatBuilder renditionGroup(String value) {
    this.renditionGroup = value;
    return this;
  }

  /**
   * @param value For internal use only (not displayed for user)
   * @return this
   */
  public MediaFormatBuilder internal(boolean value) {
    this.internal = value;
    return this;
  }

  /**
   * @param value Ranking for controlling priority in auto-detection
   * @return this
   */
  public MediaFormatBuilder ranking(int value) {
    this.ranking = value;
    return this;
  }

  /**
   * Builds the media format definition.
   * @return Media format definition
   */
  public MediaFormat build() {
    if (this.name == null) {
      throw new IllegalArgumentException("Name is missing.");
    }
    if (this.applicationId == null) {
      throw new IllegalArgumentException("Application id is missing.");
    }
    return new MediaFormat(
        name,
        applicationId,
        label,
        description,
        width,
        minWidth,
        maxWidth,
        height,
        minHeight,
        maxHeight,
        ratio,
        ratioWidth,
        ratioHeight,
        fileSizeMax,
        extensions == null ? new String[0] : extensions,
            renditionGroup,
            internal,
            ranking);
  }

}
