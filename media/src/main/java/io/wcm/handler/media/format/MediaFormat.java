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
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.commons.jcr.JcrConstants;

/**
 * Media format.
 */
public final class MediaFormat {

  /**
   * Media format title
   */
  public static final String PN_TITLE = JcrConstants.JCR_TITLE;

  /**
   * Media format description
   */
  public static final String PN_DESCRIPTION = JcrConstants.JCR_DESCRIPTION;

  /**
   * Image width (px)
   */
  public static final String PN_WIDTH = "width";

  /**
   * Min. image width (px)
   */
  public static final String PN_WIDTH_MIN = "widthMin";

  /**
   * Max. image width (px)
   */
  public static final String PN_WIDTH_MAX = "widthMax";

  /**
   * Image height (px)
   */
  public static final String PN_HEIGHT = "height";

  /**
   * Min. image height (px)
   */
  public static final String PN_HEIGHT_MIN = "heightMin";

  /**
   * Max. image height (px)
   */
  public static final String PN_HEIGHT_MAX = "heightMax";

  /**
   * Ratio (width/height)
   */
  public static final String PN_RATIO = "ratio";

  /**
   * Ratio width sample value (is used for calculating the ratio together with ratioHeight, and for display)
   */
  public static final String PN_RATIO_WIDTH = "ratioWidth";

  /**
   * Ratio height sample value (is used for calculating the ratio together with ratioWidth, and for display)
   */
  public static final String PN_RATIO_HEIGHT = "ratioHeight";

  /**
   * Max. file size (bytes)
   */
  public static final String PN_FILESIZE_MAX = "fileSizeMax";

  /**
   * Allowed file extensions
   */
  public static final String PN_EXTENSION = "extension";

  /**
   * Rendition group id
   */
  public static final String PN_RENDITIONGROUP = "renditionGroup";

  /**
   * For internal use only (not displayed for user)
   */
  public static final String PN_INTERNAL = "internal";

  /**
   * Ranking for auto-detection
   */
  public static final String PN_RANKING = "ranking";

  private final String path;
  private final ValueMap props;
  private String combinedTitle;

  /**
   * @param resource Resource with media format definition from repository
   */
  public MediaFormat(Resource resource) {
    path = resource.getPath();
    props = resource.getValueMap();
  }

  /**
   * @return Media format path (unique identifier)
   */
  public String getPath() {
    return path;
  }

  /**
   * @return Media format title
   */
  public String getTitle() {
    return props.get(PN_TITLE, String.class);
  }

  /**
   * @return Media format description
   */
  public String getDescription() {
    return props.get(PN_DESCRIPTION, String.class);
  }

  /**
   * @return Image width (px)
   */
  public long getWidth() {
    return props.get(PN_WIDTH, 0L);
  }

  /**
   * @return Min. image width (px)
   */
  public long getWidthMin() {
    return props.get(PN_WIDTH_MIN, 0L);
  }

  /**
   * @return Max. image width (px)
   */
  public long getWidthMax() {
    return props.get(PN_WIDTH_MAX, 0L);
  }

  /**
   * @return Image height (px)
   */
  public long getHeight() {
    return props.get(PN_HEIGHT, 0L);
  }

  /**
   * @return Min. image height (px)
   */
  public long getHeightMin() {
    return props.get(PN_HEIGHT_MIN, 0L);
  }

  /**
   * @return Max. image height (px)
   */
  public long getHeightMax() {
    return props.get(PN_HEIGHT_MAX, 0L);
  }

  /**
   * @return Ration width (px)
   */
  public long getRatioWidth() {
    return props.get(PN_RATIO_WIDTH, 0L);
  }

  /**
   * @return Ration height (px)
   */
  public long getRatioHeight() {
    return props.get(PN_RATIO_HEIGHT, 0L);
  }

  /**
   * Returns the ratio defined in the media format definition.
   * If no ratio is defined an the media format has a fixed with/height it is calcluated automatically.
   * Otherwise 0 is returned.
   * @return Ratio
   */
  public double getRatio() {

    // get ratio from media format definition
    double ratio = props.get(PN_RATIO, 0d);
    if (ratio > 0) {
      return ratio;
    }

    // get ratio from media format definition calculated from ratio sample/display values
    long ratioWidth = props.get(PN_RATIO_WIDTH, 0L);
    long ratioHeight = props.get(PN_RATIO_HEIGHT, 0L);
    if (ratioWidth > 0 && ratioHeight > 0) {
      return (double)ratioWidth / (double)ratioHeight;
    }

    // otherwise calcuate ratio
    if (isFixedDimension()) {
      double width = getWidth();
      double height = getHeight();
      if (width > 0 && height > 0) {
        return width / height;
      }
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

    long ratioWidth = props.get(PN_RATIO_WIDTH, 0L);
    long ratioHeight = props.get(PN_RATIO_HEIGHT, 0L);
    if (ratioWidth > 0 && ratioHeight > 0) {
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
    return props.get(PN_FILESIZE_MAX, 0L);
  }

  /**
   * @return Allowed file extensions
   */
  public String[] getExtension() {
    return props.get(PN_EXTENSION, String[].class);
  }

  /**
   * @return Rendition group id
   */
  public String getRenditionGroup() {
    return props.get(PN_RENDITIONGROUP, String.class);
  }

  /**
   * @return For internal use only (not displayed for user)
   */
  public boolean isInternal() {
    return props.get(PN_INTERNAL, false);
  }

  /**
   * @return Ranking for auto-detection
   */
  public long getRanking() {
    return props.get(PN_RANKING, 0L);
  }

  /**
   * @return Whether the format allows at least one image extension
   */
  public boolean isImage() {
    for (String extension : getExtension()) {
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
    return getWidth() > 0 && getWidthMin() == 0 && getWidthMax() == 0;
  }

  /**
   * Checks if the media format has a fixed height defined, and no min/max constraints.
   * @return If the media format has a fixed dimension.
   */
  public boolean isFixedHeight() {
    return getHeight() > 0 && getHeightMin() == 0 && getHeightMax() == 0;
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
  public long getEffectiveWidthMin() {
    long widthMin = getWidthMin();
    if (widthMin == 0) {
      widthMin = getWidth();
    }
    return widthMin;
  }

  /**
   * @return Effective max. image width (px). Takes widthMax and width into account.
   */
  public long getEffectiveWidthMax() {
    long widthMax = getWidthMax();
    if (widthMax == 0) {
      widthMax = getWidth();
    }
    return widthMax;
  }

  /**
   * @return Effective min. image height (px). Takes heightMin and height into account.
   */
  public long getEffectiveHeightMin() {
    long heightMin = getHeightMin();
    if (heightMin == 0) {
      heightMin = getHeight();
    }
    return heightMin;
  }

  /**
   * @return Effective max. image height (px). Takes heightMax and height into account.
   */
  public long getEffectiveHeightMax() {
    long heightMax = getHeightMax();
    if (heightMax == 0) {
      heightMax = getHeight();
    }
    return heightMax;
  }

  /**
   * @return User-friendly combined title of current media format name and dimension.
   */
  protected String getCombinedTitle() {
    if (combinedTitle == null) {
      StringBuilder sb = new StringBuilder();

      sb.append(getTitle());

      // list of display extensions for media format names
      List<String> extParts = new ArrayList<String>();

      // with/height restrictions
      long widthMin = getEffectiveWidthMin();
      long widthMax = getEffectiveWidthMax();
      long heightMin = getEffectiveHeightMin();
      long heightMax = getEffectiveHeightMax();
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
        sbRestrictions.append("x");
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
      StringBuilder extensions = new StringBuilder();
      String[] extensionList = getExtension();
      if (extensionList != null) {
        for (int i = 0; i < extensionList.length && i < MAX_EXTENSIONS; i++) {
          if (i > 0) {
            extensions.append(",");
          }
          extensions.append(extensionList[i]);
        }
        if (extensionList.length > MAX_EXTENSIONS) {
          extensions.append("...");
        }
        if (extensionList.length > 0) {
          extParts.add(extensions.toString());
        }
      }

      // add extended display parts
      if (extParts.size() > 0) {
        sb.append(" (");
        sb.append(StringUtils.join(extParts, "; "));
        sb.append(")");
      }

      combinedTitle = sb.toString();
    }
    return combinedTitle;
  }

  /**
   * Get minimum dimensions for media format. If only with or height is defined the missing dimensions
   * is calculated from the ratio. If no ratio defined either only width or height dimension is returned.
   * If neither width or height are defined null is returned.
   * @return Min. dimensions or null
   */
  public Dimension getMinDimension() {
    int width = (int)getEffectiveWidthMin();
    int height = (int)getEffectiveHeightMin();
    double ratio = getRatio();

    if (width == 0 && height > 0 && ratio > 0) {
      width = (int)Math.round(height * ratio);
    }
    if (width > 0 && height == 0 && ratio > 0) {
      height = (int)Math.round(width / ratio);
    }

    if (width > 0 || height > 0) {
      return new Dimension(width, height);
    }
    else {
      return null;
    }
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
      return path.equals(other.path);
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }

}
