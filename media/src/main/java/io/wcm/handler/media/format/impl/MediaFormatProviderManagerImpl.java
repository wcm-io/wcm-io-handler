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
package io.wcm.handler.media.format.impl;

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.spi.MediaFormatProvider;
import io.wcm.sling.commons.osgi.RankedServices;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

/**
 * Default implementation of {@link MediaFormatProviderManager}.
 */
@Component(immediate = true, metatype = false)
@Service(MediaFormatProviderManager.class)
public final class MediaFormatProviderManagerImpl implements MediaFormatProviderManager {

  private volatile Map<String, SortedSet<MediaFormat>> mediaFormats = ImmutableMap.of();

  /**
   * Parameter providers implemented by installed applications.
   */
  @Reference(name = "mediaFormatProvider", referenceInterface = MediaFormatProvider.class,
      cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
  private final RankedServices<MediaFormatProvider> mediaFormatProviders = new RankedServices<>(new MediaFormatProviderChangeListener());

  @Override
  public SortedSet<MediaFormat> getMediaFormats(String applicationId) {
    SortedSet<MediaFormat> mediaFormatSet = mediaFormats.get(applicationId);
    if (mediaFormatSet == null) {
      mediaFormatSet = ImmutableSortedSet.<MediaFormat>of();
    }
    return mediaFormatSet;
  }

  void bindMediaFormatProvider(MediaFormatProvider service, Map<String, Object> props) {
    mediaFormatProviders.bind(service, props);
  }

  void unbindMediaFormatProvider(MediaFormatProvider service, Map<String, Object> props) {
    mediaFormatProviders.unbind(service, props);
  }


  /**
   * Synchronizes the field mediaFormats whenever a media format provider service is added or removed.
   */
  private class MediaFormatProviderChangeListener implements RankedServices.ChangeListener {

    @Override
    public void changed() {
      Map<String, SortedSet<MediaFormat>> mediaFormatMap = new HashMap<>();

      for (MediaFormatProvider provider : MediaFormatProviderManagerImpl.this.mediaFormatProviders) {
        for (MediaFormat mediaFormat : provider.getMediaFormats()) {
          SortedSet<MediaFormat> mediaFormatSet = mediaFormatMap.get(mediaFormat.getApplicationId());
          if (mediaFormatSet == null) {
            mediaFormatSet = new TreeSet<>();
            mediaFormatMap.put(mediaFormat.getApplicationId(), mediaFormatSet);
          }
          mediaFormatSet.add(mediaFormat);
        }
      }

      Set<String> applicationIds = ImmutableSet.copyOf(mediaFormatMap.keySet());
      for (String applicationId : applicationIds) {
        mediaFormatMap.put(applicationId, ImmutableSortedSet.copyOf(mediaFormatMap.get(applicationId)));
      }

      MediaFormatProviderManagerImpl.this.mediaFormats = ImmutableMap.copyOf(mediaFormatMap);
    }

  }

}
