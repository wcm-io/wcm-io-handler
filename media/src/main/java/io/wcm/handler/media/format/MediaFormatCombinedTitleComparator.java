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
package io.wcm.handler.media.format;

import java.io.Serializable;
import java.util.Comparator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Sorts media formats by combined title.
 */
@ProviderType
public final class MediaFormatCombinedTitleComparator implements Comparator<MediaFormat>, Serializable {

  private static final long serialVersionUID = 1L;

  @Override
  public int compare(MediaFormat obj1, MediaFormat obj2) {
    return obj1.toString().compareTo(obj2.toString());
  }

}
