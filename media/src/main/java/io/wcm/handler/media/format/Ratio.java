/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import static io.wcm.handler.media.format.MediaFormatHandler.RATIO_TOLERANCE;

import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.handler.media.Dimension;

/**
 * Compare ratios with tolerance.
 */
@ProviderType
public final class Ratio {

  private Ratio() {
    // static methods only
  }

  /**
   * Checks if both given ratio values are the same within the bounds of the {@link MediaFormatHandler#RATIO_TOLERANCE}.
   * @param first First ratio
   * @param second Second ratio
   * @return If ratio matches.
   */
  public static boolean matches(double first, double second) {
    return (first > second - RATIO_TOLERANCE) && (first < second + RATIO_TOLERANCE);
  }

  /**
   * Checks if the ratios of both given media formats are the same within the bounds of the
   * {@link MediaFormatHandler#RATIO_TOLERANCE}.
   * If one or both of the media formats do not have a ratio set, the method returns false.
   * @param first First ratio
   * @param second Second ratio
   * @return If ratio matches.
   */
  public static boolean matches(@NotNull MediaFormat first, @NotNull MediaFormat second) {
    if (!(first.hasRatio() && second.hasRatio())) {
      return false;
    }
    return matches(first.getRatio(), second.getRatio());
  }

  /**
   * Get ratio from width/height.
   * @param width Width
   * @param height Height
   * @return Ratio (returns 0 when ratio is invalid)
   */
  public static double get(double width, double height) {
    if (width <= 0) {
      return 0d;
    }
    if (height <= 0) {
      return 0d;
    }
    return width / height;
  }

  /**
   * Get ratio from width/height.
   * @param width Width
   * @param height Height
   * @return Ratio (returns 0 when ratio is invalid)
   */
  public static double get(long width, long height) {
    return Ratio.get((double)width, (double)height);
  }

  /**
   * Get ratio from width/height.
   * @param dimension Dimension
   * @return Ratio (returns 0 when ratio is invalid)
   */
  public static double get(@NotNull Dimension dimension) {
    return get(dimension.getWidth(), dimension.getHeight());
  }

}
