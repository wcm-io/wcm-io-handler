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
package io.wcm.handler.media.spi;

import io.wcm.handler.media.format.MediaFormat;

import java.util.List;
import java.util.Set;

/**
 * Provides application-specific configuration information required for media handling.
 */
public interface MediaHandlerConfig {

  /**
   * Default value for JPEG quality.
   */
  double DEFAULT_JPEG_QUALITY = 0.98d;

  /**
   * @return Media format names for downloads that are allowed as target for links
   */
  Set<MediaFormat> getDownloadMediaFormats();

  /**
   * @return Supported media sources
   */
  List<Class<? extends MediaSource>> getSources();

  /**
   * @return Available media markup builders
   */
  List<Class<? extends MediaMarkupBuilder>> getMarkupBuilders();

  /**
   * @return List of media metadata pre processors (optional). The processors are applied in list order.
   */
  List<Class<? extends MediaProcessor>> getPreProcessors();

  /**
   * @return List of media metadata post processors (optional). The processors are applied in list order.
   */
  List<Class<? extends MediaProcessor>> getPostProcessors();

  /**
   * Get the default quality for images in this app generated with the Layer API.
   * The meaning of the quality parameter for the different image formats is described in
   * {@link com.day.image.Layer#write(String, double, java.io.OutputStream)}.
   * @param mimeType MIME-type of the output format
   * @return Quality factor
   */
  double getDefaultImageQuality(String mimeType);

}
