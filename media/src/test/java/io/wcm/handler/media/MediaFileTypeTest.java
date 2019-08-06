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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

class MediaFileTypeTest {

  @Test
  void testIsImage() {
    assertTrue(MediaFileType.isImage("gif"));
    assertTrue(MediaFileType.isImage("JPG"));
    assertTrue(MediaFileType.isImage("jpeg"));
    assertTrue(MediaFileType.isImage("tif"));
    assertTrue(MediaFileType.isImage("TIFF"));
    assertFalse(MediaFileType.isImage("pdf"));
    assertFalse(MediaFileType.isImage(null));
  }

  @Test
  void testGetImageFileExtensions() {
    assertEquals(ImmutableSet.of("jpg", "jpeg", "gif", "png", "svg", "tif", "tiff"), MediaFileType.getImageFileExtensions());
  }

  @Test
  void testIsBrowserImage() {
    assertTrue(MediaFileType.isBrowserImage("GIF"));
    assertTrue(MediaFileType.isBrowserImage("jpg"));
    assertTrue(MediaFileType.isBrowserImage("jpeg"));
    assertTrue(MediaFileType.isBrowserImage("SVG"));
    assertFalse(MediaFileType.isBrowserImage("tif"));
    assertFalse(MediaFileType.isBrowserImage("pdf"));
    assertFalse(MediaFileType.isBrowserImage(null));
  }

  @Test
  void testGetVectorImageFileExtensions() {
    assertEquals(ImmutableSet.of("svg"), MediaFileType.getVectorImageFileExtensions());
  }

  @Test
  void testIsVectorImage() {
    assertTrue(MediaFileType.isVectorImage("svg"));
    assertFalse(MediaFileType.isVectorImage("jpg"));
    assertFalse(MediaFileType.isVectorImage("pdf"));
    assertFalse(MediaFileType.isVectorImage(null));
  }

  @Test
  void testGetBrowserImageFileExtensions() {
    assertEquals(ImmutableSet.of("jpg", "jpeg", "gif", "png", "svg"), MediaFileType.getBrowserImageFileExtensions());
  }

  @Test
  @SuppressWarnings("deprecation")
  void testIsFlash() {
    assertTrue(MediaFileType.isFlash("swf"));
    assertFalse(MediaFileType.isFlash("pdf"));
    assertFalse(MediaFileType.isFlash(null));
  }

  @Test
  @SuppressWarnings("deprecation")
  void testGetFlashFileExtensions() {
    assertEquals(ImmutableSet.of("swf"), MediaFileType.getFlashFileExtensions());
  }

}
