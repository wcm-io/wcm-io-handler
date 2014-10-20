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

import io.wcm.handler.media.Dimension;
import io.wcm.wcm.commons.contenttype.FileExtension;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Media format.
 */
@ProviderType
public final class MediaFormat implements Comparable<MediaFormat> {

  private final String name;
  private final String applicationId;
  private final String label;
  private final String description;
  private final long width;
  private final long minWidth;
  private final long maxWidth;
  private final long height;
  private final long minHeight;
  private final long maxHeight;
  private final double ratio;
  private final long ratioWidth;
  private final long ratioHeight;
  private final long fileSizeMax;
  private final String[] extensions;
  private final String renditionGroup;
  private final boolean internal;
  private final int ranking;
  private final ValueMap properties;

  private final String key;
  private String combinedTitle;

  //CHECKSTYLE:OFF
  MediaFormat(String name, String applicationId, String label, String description,
      long width, long minWidth, long maxWidth, long height, long minHeight, long maxHeight,
      double ratio, long ratioWidth, long ratioHeight, long fileSizeMax, String[] extensions,
      String renditionGroup, boolean internal, int ranking, ValueMap properties) {
    this.name = name;
    this.applicationId = applicationId;
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
    this.internal = internal;
    this.ranking = ranking;
    this.properties = properties;

    this.key = applicationId + ":" + name;
  }
  //CHECKSTYLE:ON

  /**
   * @return Media format name
   */
  public String getName() {
    return this.name;
  }

  /**
   * @return Application Id
   */
  public String getApplicationId() {
    return this.applicationId;
  }

  /**
   * @return Media format label
   */
  public String getLabel() {
    return StringUtils.defaultString(this.label, this.name);
  }

  /**
   * @return Media format description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * @return Image width (px)
   */
  public long getWidth() {
    return this.width;
  }

  /**
   * @return Min. image width (px)
   */
  public long getMinWidth() {
    return this.minWidth;
  }

  /**
   * @return Max. image width (px)
   */
  public long getMaxWidth() {
    return this.maxWidth;
  }

  /**
   * @return Image height (px)
   */
  public long getHeight() {
    return this.height;
  }

  /**
   * @return Min. image height (px)
   */
  public long getMinHeight() {
    return this.minHeight;
  }

  /**
   * @return Max. image height (px)
   */
  public long getMaxHeight() {
    return this.maxHeight;
  }

  /**
   * @return Ration width (px)
   */
  public long getRatioWidth() {
    return this.ratioWidth;
  }

  /**
   * @return Ration height (px)
   */
  public long getRatioHeight() {
    return this.ratioHeight;
  }

  /**
   * Returns the ratio defined in the media format definition.
   * If no ratio is defined an the media format has a fixed with/height it is calcluated automatically.
   * Otherwise 0 is returned.
   * @return Ratio
   */
  public double getRatio() {

    // get ratio from media format definition
    if (this.ratio > 0) {
      return this.ratio;
    }

    // get ratio from media format definition calculated from ratio sample/display values
    if (this.ratioWidth > 0 && this.ratioHeight > 0) {
      return (double)this.ratioWidth / (double)this.ratioHeight;
    }

    // otherwise calculate ratio
    if (isFixedDimension() && this.width > 0 && this.height > 0) {
      return (double)this.width / (double)this.height;
    }

    return 0d;
  }

  /**
   * Return display string for defined ratio.
   * @return Display string or null if media format has no ratio.
   */
  public String getRatioDisplayString() {
    if (isFixedDimension() || !hasRatio()) {
      return null;
    }

    if (this.ratioWidth > 0 && this.ratioHeight > 0) {
      return ratioWidth + ":" + ratioHeight;
    }

    return "R" + Double.toString(getRatio());
  }

  /**
   * @return true if the mediaformat has ratio (calcuated for fixed dimensions or defined in media format)
   */
  public boolean hasRatio() {
    return getRatio() > 0;
  }

  /**
   * @return Max. file size (bytes)
   */
  public long getFileSizeMax() {
    return this.fileSizeMax;
  }

  /**
   * @return Allowed file extensions
   */
  public String[] getExtensions() {
    return this.extensions;
  }

  /**
   * @return Rendition group id
   */
  public String getRenditionGroup() {
    return this.renditionGroup;
  }

  /**
   * @return For internal use only (not displayed for user)
   */
  public boolean isInternal() {
    return this.internal;
  }

  /**
   * @return Ranking for auto-detection
   */
  public long getRanking() {
    return this.ranking;
  }

  /**
   * @return Whether the format allows at least one image extension
   */
  public boolean isImage() {
    for (String extension : getExtensions()) {
      if (FileExtension.isImage(extension)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the media format has a fixed width defined, and no min/max constraints.
   * @return If the media format has a fixed dimension.
   */
  public boolean isFixedWidth() {
    return getWidth() > 0 && getMinWidth() == 0 && getMaxWidth() == 0;
  }

  /**
   * Checks if the media format has a fixed height defined, and no min/max constraints.
   * @return If the media format has a fixed dimension.
   */
  public boolean isFixedHeight() {
    return getHeight() > 0 && getMinHeight() == 0 && getMaxHeight() == 0;
  }

  /**
   * Checks if the media format has a fixed width and height defined, and no min/max constraints.
   * @return If the media format has a fixed dimension.
   */
  public boolean isFixedDimension() {
    return isFixedWidth() && isFixedHeight();
  }

  /**
   * @return Effective min. image width (px). Takes widthMin and width into account.
   */
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
          sbRestrictions.append(widthMin);
        }
        else {
          sbRestrictions.append(widthMin);
          sbRestrictions.append("..");
          sbRestrictions.append(widthMax);
        }
        sbRestrictions.append('x');
        if (heightMin == heightMax) {
          sbRestrictions.append(heightMin);
        }
        else {
          sbRestrictions.append(heightMin);
          sbRestrictions.append("..");
          sbRestrictions.append(heightMax);
        }
        sbRestrictions.append("px");
        extParts.add(sbRestrictions.toString());
      }

      // ratio
      if (hasRatio()) {
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
      return key.equals(other.key);
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public int compareTo(MediaFormat o) {
    return this.key.compareTo(o.key);
  }

}
