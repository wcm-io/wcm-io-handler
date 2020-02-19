package io.wcm.handler.media.impl;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.MediaArgs;

public final class WidthUtils {

  static final Pattern WIDTHS_PATTERN = Pattern.compile("^\\s*\\d+\\??\\s*(,\\s*\\d+\\??\\s*)*$");

  public static @NotNull MediaArgs.WidthOption @Nullable [] parseWidths(@Nullable String widths) {
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
        .sorted((w1, w2) -> Long.compare(w2.getWidth(), w1.getWidth()))
        .toArray(size -> new MediaArgs.WidthOption[size]);
  }

  private static @NotNull MediaArgs.WidthOption toWidthOption(String width) {
    boolean optional = StringUtils.endsWith(width, "?");
    String widthValue;
    if (optional) {
      widthValue = StringUtils.substringBefore(width, "?");
    }
    else {
      widthValue = width;
    }
    return new MediaArgs.WidthOption(NumberUtils.toLong(widthValue), !optional);
  }
}
