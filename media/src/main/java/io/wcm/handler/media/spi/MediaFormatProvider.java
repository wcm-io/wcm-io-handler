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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.osgi.annotation.versioning.ConsumerType;

import com.google.common.collect.ImmutableSet;

import io.wcm.handler.commons.spisupport.SpiMatcher;
import io.wcm.handler.media.format.MediaFormat;

/**
 * {@link MediaFormatProvider} OSGi services provide media formats for the media handler.
 * Via the {@link SpiMatcher} methods it can be controlled if this configuration applies to all or only certain
 * resources.
 */
@ConsumerType
public abstract class MediaFormatProvider implements SpiMatcher {

  private final Set<MediaFormat> mediaFormats;

  /**
   * @param mediaFormats Set of media formats for parameter provider
   */
  protected MediaFormatProvider(Set<MediaFormat> mediaFormats) {
    this.mediaFormats = mediaFormats;
  }

  /**
   * @param type Type containing media format definitions as public static fields.
   */
  protected MediaFormatProvider(Class<?> type) {
    this(getMediaFormatsFromPublicFields(type));
  }

  /**
   * @return Media formats that the application defines
   */
  public Set<MediaFormat> getMediaFormats() {
    return mediaFormats;
  }

  /**
   * Get all media formats defined as public static fields in the given type.
   * @param type Type
   * @return Set of media formats
   */
  private static Set<MediaFormat> getMediaFormatsFromPublicFields(Class<?> type) {
    Set<MediaFormat> params = new HashSet<>();
    try {
      Field[] fields = type.getFields();
      for (Field field : fields) {
        if (field.getType().isAssignableFrom(MediaFormat.class)) {
          params.add((MediaFormat)field.get(null));
        }
      }
    }
    catch (IllegalArgumentException | IllegalAccessException ex) {
      throw new RuntimeException("Unable to access fields of " + type.getName(), ex);
    }
    return ImmutableSet.copyOf(params);
  }

}
