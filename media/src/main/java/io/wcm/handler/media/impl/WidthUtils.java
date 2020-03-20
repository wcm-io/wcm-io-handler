/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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
package io.wcm.handler.media.impl;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.handler.media.MediaArgs.WidthOption;

/**
 * Helper methods for parsing strings with responsive widths (which can be optional).
 */
public final class WidthUtils {

  // example values:
  // 800,1024,2048
  // 800,1024?,2048?   <- last two are optional
  static final Pattern WIDTHS_PATTERN = Pattern.compile("^\\s*\\d+\\??\\s*(,\\s*\\d+\\??\\s*)*$");

  private WidthUtils() {
    // static methods only
  }

  /**
   * Parse width string.
   * @param widths Width string
   * @return Width options
   */
  @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
  public static @NotNull WidthOption @Nullable [] parseWidths(@Nullable String widths) {
    if (StringUtils.isBlank(widths)) {
      return null;
    }
    if (!WIDTHS_PATTERN.matcher(widths).matches()) {
      return null;
    }
    String[] widthItems = StringUtils.split(widths, ",");
    return Arrays.stream(widthItems)
        .map(StringUtils::trim)
        .map(WidthUtils::toWidthOption)
        .toArray(size -> new WidthOption[size]);
  }

  private static @NotNull WidthOption toWidthOption(String width) {
    boolean optional = StringUtils.endsWith(width, "?");
    String widthValue;
    if (optional) {
      widthValue = StringUtils.substringBefore(width, "?");
    }
    else {
      widthValue = width;
    }
    return new WidthOption(NumberUtils.toLong(widthValue), !optional);
  }

}
