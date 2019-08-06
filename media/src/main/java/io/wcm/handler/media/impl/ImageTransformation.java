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
package io.wcm.handler.media.impl;

import com.drew.lang.annotations.Nullable;

/**
 * Image transformation parameters and helper methods.
 */
public final class ImageTransformation {

  /**
   * Rotate 90°
   */
  public static final int ROTATE_90 = 90;

  /**
   * Rotate 180°
   */
  public static final int ROTATE_180 = 180;

  /**
   * Rotate 270°
   */
  public static final int ROTATE_270 = 270;

  private ImageTransformation() {
    // static methods only
  }

  /**
   * @param rotation Rotation value
   * @return true if the value is a supported image rotation operation.
   */
  public static boolean isValidRotation(int rotation) {
    return rotation == ROTATE_90
        || rotation == ROTATE_180
        || rotation == ROTATE_270;
  }

  /**
   * Swaps width with height if rotated 90° clock-wise or counter clock-wise
   * @param width Rendition width
   * @param height Rendition height
   * @param rotation Rotation
   * @return Width
   */
  public static long rotateMapWidth(long width, long height, @Nullable Integer rotation) {
    if (rotation != null && (rotation == ROTATE_90 || rotation == ROTATE_270)) {
      return height;
    }
    else {
      return width;
    }
  }

  /**
   * Swaps height with width if rotated 90° clock-wise or counter clock-wise
   * @param width Rendition width
   * @param height Rendition height
   * @param rotation Rotation
   * @return Height
   */
  public static long rotateMapHeight(long width, long height, @Nullable Integer rotation) {
    if (rotation != null && (rotation == ROTATE_90 || rotation == ROTATE_270)) {
      return width;
    }
    else {
      return height;
    }
  }

}
