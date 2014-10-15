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

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.url.UrlMode;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Default implementation of media arguments
 */
@ProviderType
public final class MediaArgs extends AbstractMediaArgs<MediaArgs> {

  /**
   * Shortcut for building {@link MediaArgs} with media format
   * @param mediaFormat Media format
   * @return Media args
   */
  public static MediaArgs mediaFormat(MediaFormat mediaFormat) {
    return new MediaArgs().setMediaFormat(mediaFormat);
  }

  /**
   * Shortcut for building {@link MediaArgs} with media format
   * @param mediaFormats Media format
   * @return Media args
   */
  public static MediaArgs mediaFormats(MediaFormat... mediaFormats) {
    return new MediaArgs().setMediaFormats(mediaFormats);
  }

  /**
   * Shortcut for building {@link MediaArgs} with media format
   * @param mediaFormatName Media format name
   * @return Media args
   */
  public static MediaArgs mediaFormat(String mediaFormatName) {
    return new MediaArgs().setMediaFormatName(mediaFormatName);
  }

  /**
   * Shortcut for building {@link MediaArgs} with media format
   * @param mediaFormatNames Media format names
   * @return Media args
   */
  public static MediaArgs mediaFormats(String... mediaFormatNames) {
    return new MediaArgs().setMediaFormatNames(mediaFormatNames);
  }

  /**
   * Shortcut for building {@link MediaArgs} with URL mode.
   * @param urlMode URL mode
   * @return Media args
   */
  public static MediaArgs urlMode(UrlMode urlMode) {
    return new MediaArgs().setUrlMode(urlMode);
  }

  /**
   * Shortcut for building {@link MediaArgs} with URL mode.
   * @param fixedWidth Fixed width
   * @param fixedHeight Fixed height
   * @return Media args
   */
  public static MediaArgs fixedDimension(int fixedWidth, int fixedHeight) {
    return new MediaArgs().setFixedDimensions(fixedWidth, fixedHeight);
  }

}
