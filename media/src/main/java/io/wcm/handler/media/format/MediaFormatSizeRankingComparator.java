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
 * Sorts media formats by size (biggest first), ranking and name.
 */
@ProviderType
public final class MediaFormatSizeRankingComparator implements Comparator<MediaFormat>, Serializable {

  private static final long serialVersionUID = 1L;

  @Override
  public int compare(MediaFormat obj1, MediaFormat obj2) {
    long totalSize1 = calcTotalSize(obj1);
    long totalSize2 = calcTotalSize(obj2);

    if (totalSize1 == totalSize2) {
      long ranking1 = obj1.getRanking();
      long ranking2 = obj2.getRanking();

      if (ranking1 == ranking2) {
        return obj1.toString().compareTo(obj2.toString());
      }
      else {
        return Long.compare(ranking1, ranking2);
      }
    }
    else {
      return Long.compare(totalSize2, totalSize1);
    }
  }

  private long calcTotalSize(MediaFormat mf) {
    if (mf.getMinWidthHeight() > 0) {
      return mf.getMinWidthHeight() * mf.getMinWidthHeight();
    }
    else {
      return mf.getEffectiveMaxWidth() * mf.getEffectiveMaxHeight();
    }
  }

}
