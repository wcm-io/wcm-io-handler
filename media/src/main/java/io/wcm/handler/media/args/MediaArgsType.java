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
package io.wcm.handler.media.args;

import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.url.UrlMode;

/**
 * Interface for value object for passing multiple optional arguments to {@link MediaHandler} class
 * for filtering media library item renditions and controlling markup rendering.
 * @param <T> Subclass implementation to support "builder pattern" with correct return type
 */
public interface MediaArgsType<T extends MediaArgsType> extends Cloneable {

  /**
   * @return Absolute media format paths or relative (without media format parent path)
   */
  MediaFormat[] getMediaFormats();

  /**
   * @param mediaFormats Absolute media format paths or relative (without media format parent path)
   * @return this
   */
  T setMediaFormats(MediaFormat... mediaFormats);

  /**
   * @param mediaFormat Absolute media format path or relative (without media format parent path)
   * @return this
   */
  T setMediaFormat(MediaFormat mediaFormat);

  /**
   * @return Absolute media format paths or relative (without media format parent path)
   */
  String[] getMediaFormatNames();

  /**
   * @param mediaFormatNames Absolute media format paths or relative (without media format parent path)
   * @return this
   */
  T setMediaFormatNames(String... mediaFormatNames);

  /**
   * @param mediaFormatName Absolute media format path or relative (without media format parent path)
   * @return this
   */
  T setMediaFormatName(String mediaFormatName);

  /**
   * @return File extensions
   */
  String[] getFileExtensions();

  /**
   * @param fileExtensions File extensions
   * @return this
   */
  T setFileExtensions(String... fileExtensions);

  /**
   * @param fileExtension File extension
   * @return this
   */
  T setFileExtension(String fileExtension);

  /**
   * @return URL mode
   */
  UrlMode getUrlMode();

  /**
   * @param urlMode URS mode
   * @return this
   */
  T setUrlMode(UrlMode urlMode);

  /**
   * Use fixed width instead of width from media format or original image
   * @return Fixed width
   */
  long getFixedWidth();

  /**
   * Use fixed width instead of width from media format or original image
   * @param fixedWidth Fixed width
   * @return this
   */
  T setFixedWidth(long fixedWidth);

  /**
   * Use fixed height instead of width from media format or original image
   * @return Fixed height
   */
  long getFixedHeight();

  /**
   * Use fixed height instead of width from media format or original image
   * @param fixedHeight Fixed height
   * @return this
   */
  T setFixedHeight(long fixedHeight);

  /**
   * Use fixed dimensions instead of width from media format or original image
   * @param fixedWidth Fixed width
   * @param fixedHeight Fixed height
   * @return this
   */
  T setFixedDimensions(long fixedWidth, long fixedHeight);

  /**
   * @return Whether to set a "Content-Disposition" header for forcing a "Save as" dialog on the client
   */
  boolean isForceDownload();

  /**
   * @param forceDownload Whether to set a "Content-Disposition" header for forcing a "Save as" dialog on the client
   * @return this
   */
  T setForceDownload(boolean forceDownload);

  /**
   * @return The custom alternative text that is to be used instead of the one defined in the the media lib item
   */
  String getAltText();

  /**
   * Allows to specify a custom alternative text that is to be used instead of the one defined in the the media lib item
   * @param altTextOverride Custom alternative text. If null or empty, the default alt text from media library is used.
   * @return this
   */
  T setAltText(String altTextOverride);

  /**
   * @return If set to true, media handler never returns a dummy image. Otherwise this can happen in edit mode.
   */
  boolean isNoDummyImage();

  /**
   * @param noDummyImage If set to true, media handler never returns a dummy image. Otherwise this can happen in edit mode.
   * @return this
   */
  T setNoDummyImage(boolean noDummyImage);

  /**
   * @return Url of custom dummy image. If null default dummy image is used.
   */
  String getDummyImageUrl();

  /**
   * @param dummyImageUrl Url of custom dummy image. If null default dummy image is used.
   * @return this
   */
  T setDummyImageUrl(String dummyImageUrl);

  /**
   * custom clone-method for {@link MediaArgsType}
   * @return the cloned {@link MediaArgsType}
   * @throws CloneNotSupportedException
   */
  Object clone() throws CloneNotSupportedException;

}
