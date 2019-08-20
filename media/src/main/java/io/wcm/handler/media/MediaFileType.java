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

import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * File types supported by Media Handler.
 */
@ProviderType
public enum MediaFileType {

  /**
   * JPEG
   */
  JPEG(new String[] { ContentType.JPEG }, new String[] { FileExtension.JPEG, "jpeg" }),

  /**
   * PNG
   */
  PNG(new String[] { ContentType.PNG }, new String[] { FileExtension.PNG }),

  /**
   * GIF
   */
  GIF(new String[] { ContentType.GIF }, new String[] { FileExtension.GIF }),

  /**
   * TIFF
   */
  TIFF(new String[] { ContentType.TIFF }, new String[] { FileExtension.TIFF, "tiff" }),

  /**
   * SVG
   */
  SVG(new String[] { ContentType.SVG }, new String[] { FileExtension.SVG }),

  /**
   * Flash
   * @deprecated Flash support is deprecated
   */
  @Deprecated
  SWF(new String[] { ContentType.SWF }, new String[] { FileExtension.SWF });

  private final Set<String> contentTypes;
  private final Set<String> extensions;

  MediaFileType(@NotNull String @NotNull [] contentTypes, @NotNull String @NotNull [] extensions) {
    this.contentTypes = ImmutableSet.copyOf(contentTypes);
    this.extensions = ImmutableSet.copyOf(extensions);
  }

  /**
   * @return Content types
   */
  public Set<String> getContentTypes() {
    return this.contentTypes;
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
      TIFF,
      SVG);

  /**
   * All file types that are supported by the browser for direct display.
   */
  private static final EnumSet<MediaFileType> BROWSER_IMAGE_FILE_TYPES = EnumSet.of(
      GIF,
      JPEG,
      PNG,
      SVG);

  /**
   * All file types that are vector formats and can be scaled by the browser.
   */
  private static final EnumSet<MediaFileType> VECTOR_IMAGE_FILE_TYPES = EnumSet.of(
      SVG);

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
   * @return Image content types supported by the Media Handler for rendering as image.
   */
  public static @NotNull Set<String> getImageContentTypes() {
    return getContentTypes(IMAGE_FILE_TYPES);
  }

  /**
   * Check if the given file extension is supported for direct display in a browser.
   * @param fileExtension File extension
   * @return true if image is supported in browsers
   */
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
   * @return Image content types supported for direct display in a browser.
   */
  public static @NotNull Set<String> getBrowserImageContentTypes() {
    return getContentTypes(BROWSER_IMAGE_FILE_TYPES);
  }

  /**
   * Check if the given file extension is a vector image file extension.
   * @param fileExtension File extension
   * @return true if image is a vector image.
   */
  public static boolean isVectorImage(@Nullable String fileExtension) {
    return isExtension(VECTOR_IMAGE_FILE_TYPES, fileExtension);
  }

  /**
   * @return Image file extensions that are vector images.
   */
  public static @NotNull Set<String> getVectorImageFileExtensions() {
    return getFileExtensions(VECTOR_IMAGE_FILE_TYPES);
  }

  /**
   * @return Image content types that are vector images.
   */
  public static @NotNull Set<String> getVectorImageContentTypes() {
    return getContentTypes(VECTOR_IMAGE_FILE_TYPES);
  }

  /**
   * Check if the given file extension is an flash.
   * @param fileExtension File extension
   * @return true if flash
   * @deprecated Flash support is deprecated
   */
  @Deprecated
  public static boolean isFlash(@Nullable String fileExtension) {
    return isExtension(FLASH_FILE_TYPES, fileExtension);
  }

  /**
   * @return Flash file extensions
   * @deprecated Flash support is deprecated
   */
  @Deprecated
  public static @NotNull Set<String> getFlashFileExtensions() {
    return getFileExtensions(FLASH_FILE_TYPES);
  }

  /**
   * @return Flash content types
   * @deprecated Flash support is deprecated
   */
  @Deprecated
  public static @NotNull Set<String> getFlashContentTypes() {
    return getContentTypes(FLASH_FILE_TYPES);
  }

  private static Set<String> getContentTypes(@NotNull EnumSet<MediaFileType> fileTypes) {
    return fileTypes.stream()
        .flatMap(type -> type.getContentTypes().stream())
        .collect(Collectors.toSet());
  }

  private static boolean isExtension(@NotNull EnumSet<MediaFileType> fileTypes, @Nullable String fileExtension) {
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
