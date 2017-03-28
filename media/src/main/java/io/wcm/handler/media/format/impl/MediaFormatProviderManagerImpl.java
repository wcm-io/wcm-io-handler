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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.spi.MediaFormatProvider;
import io.wcm.sling.commons.osgi.RankedServices;

/**
 * Default implementation of {@link MediaFormatProviderManager}.
 */
@Component(service = MediaFormatProviderManager.class, immediate = true,
    reference = {
        @Reference(name = "mediaFormatProvider", service = MediaFormatProvider.class,
            cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
            bind = "bindMediaFormatProvider", unbind = "unbindMediaFormatProvider")

    })
public final class MediaFormatProviderManagerImpl implements MediaFormatProviderManager {

  private volatile Map<String, SortedSet<MediaFormat>> mediaFormats = ImmutableMap.of();

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
