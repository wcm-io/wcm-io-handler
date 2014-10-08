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
package io.wcm.handler.media.spi.helpers;

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.spi.MediaFormatProvider;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Abstract implementation of {@link MediaFormatProvider} providing list of media formats either from given
 * media format set, or from reading all public static fields from a given class definition.
 */
public abstract class AbstractMediaFormatProvider implements MediaFormatProvider {

  private final Set<MediaFormat> mediaFormats;

  /**
   * @param mediaFormats Set of media formats for parameter provider
   */
  protected AbstractMediaFormatProvider(Set<MediaFormat> mediaFormats) {
    this.mediaFormats = mediaFormats;
  }

  /**
   * @param type Type containing media format definitions as public static fields.
   */
  protected AbstractMediaFormatProvider(Class<?> type) {
    this(getMediaFormatsFromPublicFields(type));
  }

  @Override
  public final Set<MediaFormat> getMediaFormats() {
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
