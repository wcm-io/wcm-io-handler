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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaFileType;

/**
 * Media format.
 */
@ProviderType
public final class MediaFormat implements Comparable<MediaFormat> {

  private final String name;
  private final String label;
  private final String description;
  private final long width;
  private final long minWidth;
  private final long maxWidth;
  private final long height;
  private final long minHeight;
  private final long maxHeight;
  private final double ratio;
  private final double ratioWidth;
  private final double ratioHeight;
  private final long fileSizeMax;
  private final String[] extensions;
  private final String renditionGroup;
  private final boolean download;
  private final boolean internal;
  private final int ranking;
  private final ValueMap properties;
  private String ratioDisplayString;
  private String combinedTitle;

  //CHECKSTYLE:OFF
  MediaFormat(String name, String label, String description,
      long width, long minWidth, long maxWidth, long height, long minHeight, long maxHeight,
      double ratio, double ratioWidth, double ratioHeight, long fileSizeMax, String[] extensions,
      String renditionGroup, boolean download, boolean internal, int ranking, ValueMap properties) {
    this.name = name;
    this.label = label;
    this.description = description;
    this.width = width;
    this.minWidth = minWidth;
    this.maxWidth = maxWidth;
    this.height = height;
    this.minHeight = minHeight;
    this.maxHeight = maxHeight;
    this.ratio = ratio;
    this.ratioWidth = ratioWidth;
    this.ratioHeight = ratioHeight;
    this.fileSizeMax = fileSizeMax;
    this.extensions = extensions;
    this.renditionGroup = renditionGroup;
    this.download = download;
    this.internal = internal;
    this.ranking = ranking;
    this.properties = properties;
  }
  //CHECKSTYLE:ON

  /**
   * @return Media format name
   */
  @JsonProperty("mediaFormat")
  public @NotNull String getName() {
    return this.name;
  }

  /**
   * @return Media format label
   */
  @JsonIgnore
  public @NotNull String getLabel() {
    return StringUtils.defaultString(this.label, this.name);
  }

  /**
   * @return Media format description
   */
  @JsonIgnore
  public @Nullable String getDescription() {
    return this.description;
  }

  /**
   * @return Image width (px)
   */
  @JsonIgnore
  public long getWidth() {
    return this.width;
  }

  /**
   * @return Min. image width (px)
   */
  @JsonIgnore
  public long getMinWidth() {
    return this.minWidth;
  }

  /**
   * @return Max. image width (px)
   */
  @JsonIgnore
  public long getMaxWidth() {
    return this.maxWidth;
  }

  /**
   * @return Image height (px)
   */
  @JsonIgnore
  public long getHeight() {
    return this.height;
  }

  /**
   * @return Min. image height (px)
   */
  @JsonIgnore
  public long getMinHeight() {
    return this.minHeight;
  }

  /**
   * @return Max. image height (px)
   */
  @JsonIgnore
  public long getMaxHeight() {
    return this.maxHeight;
  }

  /**
   * @return Ration width (px)
   * @deprecated Use {@link #getRatioWidthAsDouble()}
   */
  @Deprecated
  @JsonIgnore
  public long getRatioWidth() {
    return Math.round(this.ratioWidth);
  }

  /**
   * @return Ration height (px)
   * @deprecated Use {@link #getRatioHeightAsDouble()}
   */
  @Deprecated
  @JsonIgnore
  public long getRatioHeight() {
    return Math.round(this.ratioHeight);
  }

  /**
   * @return Ration width (px)
   */
  @JsonIgnore
  public double getRatioWidthAsDouble() {
    return this.ratioWidth;
  }

  /**
   * @return Ration height (px)
   */
  @JsonIgnore
  public double getRatioHeightAsDouble() {
    return this.ratioHeight;
  }

  /**
   * Returns the ratio defined in the media format definition.
   * If no ratio is defined an the media format has a fixed with/height it is calculated automatically.
   * Otherwise 0 is returned.
   * @return Ratio
   */
  @JsonIgnore
  public double getRatio() {

    // get ratio from media format definition
    if (this.ratio > 0) {
      return this.ratio;
    }

    // get ratio from media format definition calculated from ratio sample/display values
    if (this.ratioWidth > 0 && this.ratioHeight > 0) {
      return Ratio.get(this.ratioWidth, this.ratioHeight);
    }

    // otherwise calculate ratio
    if (isFixedDimension() && this.width > 0 && this.height > 0) {
      return Ratio.get(this.width, this.height);
    }

    return 0d;
  }

  /**
   * Return display string for defined ratio.
   * @return Display string or null if media format has no ratio.
   */
  @JsonIgnore
  public String getRatioDisplayString() {
    if (!hasRatio()) {
      return null;
    }

    if (ratioDisplayString == null) {
      ratioDisplayString = buildratioDisplayString(this);
    }
    return ratioDisplayString;
  }

  private static String buildratioDisplayString(MediaFormat mf) {
    String ratioDisplayString = null;

    NumberFormat decimal1Format = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.US));
    if (mf.getRatioWidthAsDouble() > 0 && mf.getRatioHeightAsDouble() > 0) {
      // 1. check for explicit ratio numbers defined for the media format
      ratioDisplayString = decimal1Format.format(mf.getRatioWidthAsDouble())
          + ":" + decimal1Format.format(mf.getRatioHeightAsDouble());
    }
    else {
      // 2. try to guess a nice "human-readable" ratio string
      ratioDisplayString = guessHumanReadableRatioString(mf.getRatio(), decimal1Format);
    }

    if (ratioDisplayString == null) {
      if (mf.isFixedDimension()) {
        // 3. use fixed dimension as ratio
        ratioDisplayString = decimal1Format.format(mf.getWidth())
            + ":" + decimal1Format.format(mf.getHeight());
      }
      else {
        // 4. last resort: disable decimal ratio value
        NumberFormat decimal3Format = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.US));
        ratioDisplayString = "R" + decimal3Format.format(mf.getRatio());
      }
    }

    return ratioDisplayString;
  }

  /**
   * Try to guess a nice human readable ratio string from the given decimal ratio
   * @param ratio Ratio
   * @param numberFormat Number format
   * @return Ratio display string or null if no nice string was found
   */
  private static String guessHumanReadableRatioString(double ratio, NumberFormat numberFormat) {
    for (long width = 1; width <= 50; width++) {
      double height = width / ratio;
      if (isLong(height)) {
        return numberFormat.format(width) + ":" + numberFormat.format(height);
      }
    }
    for (long width = 1; width <= 200; width++) {
      double height = width / 2d / ratio;
      if (isHalfLong(height)) {
        return numberFormat.format(width / 2d) + ":" + numberFormat.format(height);
      }
    }
    return null;
  }

  /**
   * @param value Value
   * @return true if the number ends with .0000 = is a nice integer
   */
  private static boolean isLong(double value) {
    return Math.round(value * 10000d) == Math.round(value) * 10000L;
  }

  /**
   * @param value Value
   * @return true if the number ends with .0000 or .5000 = is a nice integer or a half
   */
  private static boolean isHalfLong(double value) {
    return (Math.round(value * 2d * 10000d) == Math.round(value * 2d) * 10000L);
  }

  /**
   * @return true if the media format has ratio (calculated for fixed dimensions or defined in media format)
   */
  @JsonIgnore
  public boolean hasRatio() {
    return getRatio() > 0;
  }

  /**
   * @return Max. file size (bytes)
   */
  @JsonIgnore
  public long getFileSizeMax() {
    return this.fileSizeMax;
  }

  /**
   * @return Allowed file extensions
   */
  @JsonIgnore
  public String[] getExtensions() {
    return this.extensions != null ? this.extensions.clone() : null;
  }

  /**
   * @return Rendition group id
   */
  @JsonIgnore
  public String getRenditionGroup() {
    return this.renditionGroup;
  }

  /**
   * @return Media assets with this format should be downloaded and not displayed directly
   */
  @JsonIgnore
  public boolean isDownload() {
    return this.download;
  }

  /**
   * @return For internal use only (not displayed for user)
   */
  @JsonIgnore
  public boolean isInternal() {
    return this.internal;
  }

  /**
   * @return Ranking for auto-detection. Lowest value = highest priority.
   */
  @JsonIgnore
  public long getRanking() {
    return this.ranking;
  }

  /**
   * @return Whether the format allows at least one image extension
   */
  @JsonIgnore
  public boolean isImage() {
    for (String extension : getExtensions()) {
      if (MediaFileType.isImage(extension)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the media format has a fixed width defined, and no min/max constraints.
   * @return If the media format has a fixed dimension.
   */
  @JsonIgnore
  public boolean isFixedWidth() {
    return getWidth() > 0 && getMinWidth() == 0 && getMaxWidth() == 0;
  }

  /**
   * Checks if the media format has a fixed height defined, and no min/max constraints.
   * @return If the media format has a fixed dimension.
   */
  @JsonIgnore
  public boolean isFixedHeight() {
    return getHeight() > 0 && getMinHeight() == 0 && getMaxHeight() == 0;
  }

  /**
   * Checks if the media format has a fixed width and height defined, and no min/max constraints.
   * @return If the media format has a fixed dimension.
   */
  @JsonIgnore
  public boolean isFixedDimension() {
    return isFixedWidth() && isFixedHeight();
  }

  /**
   * @return Effective min. image width (px). Takes widthMin and width into account.
   */
  @JsonIgnore
  public long getEffectiveMinWidth() {
    long widthMin = getMinWidth();
    if (widthMin == 0) {
      widthMin = getWidth();
    }
    return widthMin;
  }

  /**
   * @return Effective max. image width (px). Takes widthMax and width into account.
   */
  @JsonIgnore
  public long getEffectiveMaxWidth() {
    long widthMax = getMaxWidth();
    if (widthMax == 0) {
      widthMax = getWidth();
    }
    return widthMax;
  }

  /**
   * @return Effective min. image height (px). Takes heightMin and height into account.
   */
  @JsonIgnore
  public long getEffectiveMinHeight() {
    long heightMin = getMinHeight();
    if (heightMin == 0) {
      heightMin = getHeight();
    }
    return heightMin;
  }

  /**
   * @return Effective max. image height (px). Takes heightMax and height into account.
   */
  @JsonIgnore
  public long getEffectiveMaxHeight() {
    long heightMax = getMaxHeight();
    if (heightMax == 0) {
      heightMax = getHeight();
    }
    return heightMax;
  }

  /**
   * Get minimum dimensions for media format. If only with or height is defined the missing dimensions
   * is calculated from the ratio. If no ratio defined either only width or height dimension is returned.
   * If neither width or height are defined null is returned.
   * @return Min. dimensions or null
   */
  @JsonIgnore
  public Dimension getMinDimension() {
    long effWithMin = getEffectiveMinWidth();
    long effHeightMin = getEffectiveMinHeight();
    double effRatio = getRatio();

    if (effWithMin == 0 && effHeightMin > 0 && effRatio > 0) {
      effWithMin = Math.round(effHeightMin * effRatio);
    }
    if (effWithMin > 0 && effHeightMin == 0 && effRatio > 0) {
      effHeightMin = Math.round(effWithMin / effRatio);
    }

    if (effWithMin > 0 || effHeightMin > 0) {
      return new Dimension(effWithMin, effHeightMin);
    }
    else {
      return null;
    }
  }

  /**
   * @return User-friendly combined title of current media format name and dimension.
   */
  @JsonIgnore
  String getCombinedTitle() {
    if (combinedTitle == null) {
      StringBuilder sb = new StringBuilder();

      sb.append(getLabel());

      List<String> extParts = new ArrayList<>();

      // with/height restrictions
      long widthMin = getEffectiveMinWidth();
      long widthMax = getEffectiveMaxWidth();
      long heightMin = getEffectiveMinHeight();
      long heightMax = getEffectiveMaxHeight();
      if (widthMin > 0 || widthMax > 0 || heightMin > 0 || heightMax > 0) {
        StringBuilder sbRestrictions = new StringBuilder();
        if (widthMin == widthMax) {
          if (widthMin == 0) {
            sbRestrictions.append("?");
          }
          else {
            sbRestrictions.append(widthMin);
          }
        }
        else {
          if (widthMin > 0) {
            sbRestrictions.append(widthMin);
          }
          sbRestrictions.append("..");
          if (widthMax > 0) {
            sbRestrictions.append(widthMax);
          }
        }
        sbRestrictions.append('x');
        if (heightMin == heightMax) {
          if (heightMin == 0) {
            sbRestrictions.append("?");
          }
          else {
            sbRestrictions.append(heightMin);
          }
        }
        else {
          if (heightMin > 0) {
            sbRestrictions.append(heightMin);
          }
          sbRestrictions.append("..");
          if (heightMax > 0) {
            sbRestrictions.append(heightMax);
          }
        }
        sbRestrictions.append("px");
        extParts.add(sbRestrictions.toString());
      }

      // ratio (if label contains a ":" it is assumed a ratio is already contained in the label)
      if (hasRatio() && !StringUtils.contains(getLabel(), ":")) {
        String ratioString = getRatioDisplayString();
        if (StringUtils.isNotEmpty(ratioString)) {
          extParts.add(ratioString);
        }
      }

      // display max. 6 extensions in combined title
      final int MAX_EXTENSIONS = 6;
      StringBuilder extensionsString = new StringBuilder();
      String[] extensionList = getExtensions();
      if (extensionList != null) {
        for (int i = 0; i < extensionList.length && i < MAX_EXTENSIONS; i++) {
          if (i > 0) {
            extensionsString.append(',');
          }
          extensionsString.append(extensionList[i]);
        }
        if (extensionList.length > MAX_EXTENSIONS) {
          extensionsString.append("...");
        }
        if (extensionList.length > 0) {
          extParts.add(extensionsString.toString());
        }
      }

      // add extended display parts
      if (!extParts.isEmpty()) {
        sb.append(" (");
        sb.append(StringUtils.join(extParts, "; "));
        sb.append(')');
      }

      combinedTitle = sb.toString();
    }
    return combinedTitle;
  }

  /**
   * @return Custom properties that my be used by application-specific markup builders or processors.
   */
  @JsonIgnore
  public ValueMap getProperties() {
    return this.properties;
  }

  /**
   * @return User-friendly combined title of current media format name and dimension.
   */
  @Override
  public String toString() {
    return getCombinedTitle();
  }

  @Override
  public boolean equals(Object pObj) {
    if (pObj instanceof MediaFormat) {
      MediaFormat other = (MediaFormat)pObj;
      return name.equals(other.name);
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public int compareTo(MediaFormat o) {
    return this.name.compareTo(o.name);
  }

}
