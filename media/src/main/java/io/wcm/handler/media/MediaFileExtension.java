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

import static io.wcm.wcm.commons.contenttype.FileExtension.GIF;
import static io.wcm.wcm.commons.contenttype.FileExtension.JPEG;
import static io.wcm.wcm.commons.contenttype.FileExtension.PNG;
import static io.wcm.wcm.commons.contenttype.FileExtension.SWF;
import static io.wcm.wcm.commons.contenttype.FileExtension.TIFF;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.google.common.collect.ImmutableSet;

/**
 * File extensions supported by Media Handler.
 */
@ProviderType
public final class MediaFileExtension {

  private MediaFileExtension() {
    // constants only
  }

  /**
   * All file extension that are supported by the Media Handler for rendering as image.
   */
  private static final Set<String> IMAGE_FILE_EXTENSIONS = ImmutableSet.of(
      GIF,
      JPEG,
      "jpeg", // alternative JEPG extension
      PNG,
      TIFF,
      "tiff" // alternative TIFF extension
      );

  /**
   * All file extension that are supported by the browser for direct display.
   */
  private static final Set<String> BROWSER_IMAGE_FILE_EXTENSIONS = ImmutableSet.of(
      GIF,
      JPEG,
      "jpeg", // alternative JEPG extension
      PNG);

  /**
   * All file extensions that will be displayed as Flash.
   */
  private static final Set<String> FLASH_FILE_EXTENSIONS = ImmutableSet.of(
      SWF
      );

  /**
   * Check if the given file extension is supported by the Media Handler for rendering as image.
   * @param fileExtension File extension
   * @return true if image
   */
  @SuppressWarnings("null")
  public static boolean isImage(@Nullable String fileExtension) {
    if (StringUtils.isEmpty(fileExtension)) {
      return false;
    }
    return IMAGE_FILE_EXTENSIONS.contains(fileExtension.toLowerCase());
  }

  /**
   * @return Image file extensions supported by the Media Handler for rendering as image.
   */
  public static @NotNull Set<String> getImageFileExtensions() {
    return IMAGE_FILE_EXTENSIONS;
  }

  /**
   * Check if the given file extension is supported for direct display in a browser.
   * @param fileExtension File extension
   * @return true if image is supported in browsers
   */
  @SuppressWarnings("null")
  public static boolean isBrowserImage(@Nullable String fileExtension) {
    if (StringUtils.isEmpty(fileExtension)) {
      return false;
    }
    return BROWSER_IMAGE_FILE_EXTENSIONS.contains(fileExtension.toLowerCase());
  }

  /**
   * @return Image file extensions supported for direct display in a browser.
   */
  public static @NotNull Set<String> getBrowserImageFileExtensions() {
    return BROWSER_IMAGE_FILE_EXTENSIONS;
  }

  /**
   * Check if the given file extension is an flash.
   * @param fileExtension File extension
   * @return true if flash
   */
  @SuppressWarnings("null")
  public static boolean isFlash(@Nullable String fileExtension) {
    if (StringUtils.isEmpty(fileExtension)) {
      return false;
    }
    return FLASH_FILE_EXTENSIONS.contains(fileExtension.toLowerCase());
  }

  /**
   * @return Flash file extensions
   */
  public static @NotNull Set<String> getFlashFileExtensions() {
    return FLASH_FILE_EXTENSIONS;
  }

}
