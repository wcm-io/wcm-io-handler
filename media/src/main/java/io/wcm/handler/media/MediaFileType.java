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
package io.wcm.handler.media;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.google.common.collect.ImmutableSet;

/**
 * File types supported by Media Handler.
 */
@ProviderType
public enum MediaFileType {

  /**
   * JPEG
   */
  JPEG("jpg", "jpeg"),

  /**
   * PNG
   */
  PNG("png"),

  /**
   * GIF
   */
  GIF("gif"),

  /**
   * TIFF
   */
  TIFF("tif", "tiff"),

  /**
   * Flash
   */
  SWF("swf");

  private final Set<String> extensions;

  MediaFileType(String... extensions) {
    this.extensions = ImmutableSet.copyOf(extensions);
  }

  /**
   * @return File extensions
   */
  public Set<String> getExtensions() {
    return extensions;
  }

  /**
   * All file types that are supported by the Media Handler for rendering as image.
   */
  private static final EnumSet<MediaFileType> IMAGE_FILE_TYPES = EnumSet.of(
      GIF,
      JPEG,
      PNG,
      TIFF);

  /**
   * All file types that are supported by the browser for direct display.
   */
  private static final EnumSet<MediaFileType> BROWSER_IMAGE_FILE_TYPES = EnumSet.of(
      GIF,
      JPEG,
      PNG);

  /**
   * All file types that will be displayed as Flash.
   */
  private static final EnumSet<MediaFileType> FLASH_FILE_TYPES = EnumSet.of(
      SWF);

  /**
   * Check if the given file extension is supported by the Media Handler for rendering as image.
   * @param fileExtension File extension
   * @return true if image
   */
  @SuppressWarnings("null")
  public static boolean isImage(@Nullable String fileExtension) {
    return isExtension(IMAGE_FILE_TYPES, fileExtension);
  }

  /**
   * @return Image file extensions supported by the Media Handler for rendering as image.
   */
  public static @NotNull Set<String> getImageFileExtensions() {
    return getFileExtensions(IMAGE_FILE_TYPES);
  }

  /**
   * Check if the given file extension is supported for direct display in a browser.
   * @param fileExtension File extension
   * @return true if image is supported in browsers
   */
  @SuppressWarnings("null")
  public static boolean isBrowserImage(@Nullable String fileExtension) {
    return isExtension(BROWSER_IMAGE_FILE_TYPES, fileExtension);
  }

  /**
   * @return Image file extensions supported for direct display in a browser.
   */
  public static @NotNull Set<String> getBrowserImageFileExtensions() {
    return getFileExtensions(BROWSER_IMAGE_FILE_TYPES);
  }

  /**
   * Check if the given file extension is an flash.
   * @param fileExtension File extension
   * @return true if flash
   */
  @SuppressWarnings("null")
  public static boolean isFlash(@Nullable String fileExtension) {
    return isExtension(FLASH_FILE_TYPES, fileExtension);
  }

  /**
   * @return Flash file extensions
   */
  public static @NotNull Set<String> getFlashFileExtensions() {
    return getFileExtensions(FLASH_FILE_TYPES);
  }

  private static boolean isExtension(@NotNull EnumSet<MediaFileType> fileTypes, @NotNull String fileExtension) {
    if (StringUtils.isEmpty(fileExtension)) {
      return false;
    }
    return fileTypes.stream()
        .filter(type -> type.getExtensions().contains(StringUtils.lowerCase(fileExtension)))
        .findFirst().isPresent();
  }

  private static Set<String> getFileExtensions(@NotNull EnumSet<MediaFileType> fileTypes) {
    return fileTypes.stream()
        .flatMap(type -> type.getExtensions().stream())
        .collect(Collectors.toSet());
  }

}
