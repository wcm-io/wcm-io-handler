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
package io.wcm.handler.media;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.Ratio;
import io.wcm.wcm.commons.caching.ModificationDateProvider;

/**
 * Represents a rendition contained in a {@link Asset} which can be rendered.
 * A rendition can be pointing to a binary file stored in the repository, or a virtual rendition that is
 * rendered on the fly if required.
 */
@ProviderType
public interface Rendition extends Adaptable, ModificationDateProvider {

  /**
   * Get externalized URL pointing to the rendition.
   * @return Rendition URL
   */
  @Nullable
  String getUrl();

  /**
   * Returns the internal path of the rendition if stored within the JCR repository. If the rendition is a virtual
   * rendition the path points to the binary from which the virtual rendition is derived from.
   * @return Repository path
   */
  @Nullable
  String getPath();

  /**
   * @return File name of the renditions source binary
   */
  @Nullable
  String getFileName();

  /**
   * @return File extension of the renditions source binary
   */
  @Nullable
  String getFileExtension();

  /**
   * @return File size of the rendition in bytes (or -1 if it is unknown).
   */
  long getFileSize();

  /**
   * @return Mime type of the renditions source binary.
   */
  @Nullable
  String getMimeType();

  /**
   * @return Media format that matches with the resolved rendition. Null if no media format was specified for resolving.
   */
  @Nullable
  MediaFormat getMediaFormat();

  /**
   * @return Properties of rendition
   */
  @NotNull
  ValueMap getProperties();

  /**
   * @return true if the rendition is a web image file format that can be displayed and resized.
   */
  boolean isImage();

  /**
   * @return true if the rendition has a flash movie.
   * @deprecated Flash support is deprecated
   */
  @Deprecated
  boolean isFlash();

  /**
   * @return true if the rendition is not and image nor a flash movie.
   */
  boolean isDownload();

  /**
   * Gets the width of this rendition. If it is a virtual rendition the virtual height is returned.
   * @return Height in pixels. Returns 0 if no height information is available.
   */
  long getWidth();

  /**
   * Gets the height of this rendition. If it is a virtual rendition the virtual height is returned.
   * @return Height in pixels. Returns 0 if no height information is available.
   */
  long getHeight();

  /**
   * Gets the ratio of the image (width / height).
   * @return Ratio. Returns 0 if no width or height information is available.
   */
  default double getRatio() {
    long width = getWidth();
    long height = getHeight();
    if (width > 0 && height > 0) {
      return Ratio.get(width, height);
    }
    return 0d;
  }

}
