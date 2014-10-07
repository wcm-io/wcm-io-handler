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

import io.wcm.handler.media.markup.EditPlaceholderMediaMarkupBuilder;
import io.wcm.handler.media.markup.SimpleImageMediaMarkupBuilder;
import io.wcm.handler.mediasource.dam.DamMediaSource;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Default implementation of configuration options of {@link MediaHandlerConfig} interface.
 * Subclasses may decide to override only some of the methods.
 */
public abstract class AbstractMediaHandlerConfig implements MediaHandlerConfig {

  private static final Set<String> DOWNLOAD_MEDIA_FORMATS = ImmutableSet.of(
      "download"
      );

  private static final List<Class<? extends MediaSource>> MEDIA_SOURCES =
      ImmutableList.<Class<? extends MediaSource>>of(
          DamMediaSource.class
          );

  private static final List<Class<? extends MediaMarkupBuilder>> MEDIA_MARKUP_BUILDERS =
      ImmutableList.<Class<? extends MediaMarkupBuilder>>of(
          SimpleImageMediaMarkupBuilder.class,
          EditPlaceholderMediaMarkupBuilder.class
          );

  @Override
  public List<Class<? extends MediaSource>> getMediaSources() {
    return MEDIA_SOURCES;
  }

  @Override
  public List<Class<? extends MediaMarkupBuilder>> getMediaMarkupBuilders() {
    return MEDIA_MARKUP_BUILDERS;
  }

  @Override
  public List<Class<? extends MediaMetadataProcessor>> getMediaMetadataPreProcessors() {
    // no processors
    return ImmutableList.of();
  }

  @Override
  public List<Class<? extends MediaMetadataProcessor>> getMediaMetadataPostProcessors() {
    // no processors
    return ImmutableList.of();
  }

  @Override
  public Set<String> getDownloadMediaFormats() {
    return DOWNLOAD_MEDIA_FORMATS;
  }

  @Override
  public String getMediaFormatsPath() {
    // no media formats
    return null;
  }

  @Override
  public double getDefaultImageQuality(String mimeType) {
    if (StringUtils.isNotEmpty(mimeType)) {
      String format = StringUtils.substringAfter(mimeType.toLowerCase(), "image/");
      if (StringUtils.equals(format, "jpg") || StringUtils.equals(format, "jpeg")) {
        return DEFAULT_JPEG_QUALITY;
      }
      else if (StringUtils.equals(format, "gif")) {
        return 256d; // 256 colors
      }
    }
    // return quality "1" for all other mime types
    return 1d;
  }

}
