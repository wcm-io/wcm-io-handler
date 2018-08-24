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

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.spi.MediaFormatProvider;
import io.wcm.sling.commons.caservice.ContextAwareServiceResolver;
import io.wcm.sling.commons.caservice.ContextAwareServiceResolver.ResolveAllResult;

/**
 * Default implementation of {@link MediaFormatProviderManager}.
 */
@Component(service = MediaFormatProviderManager.class, immediate = true)
public final class MediaFormatProviderManagerImpl implements MediaFormatProviderManager {

  @Reference
  private ContextAwareServiceResolver serviceResolver;

  // cache resolving of media formats per combined cache key of context-aware services
  private final Cache<String, SortedSet<MediaFormat>> cache = CacheBuilder.newBuilder()
      .expireAfterWrite(1, TimeUnit.HOURS)
      .build();

  @Override
  public SortedSet<MediaFormat> getMediaFormats(Resource contextResource) {
    ResolveAllResult<MediaFormatProvider> result = serviceResolver.resolveAll(MediaFormatProvider.class, contextResource);
    String key = result.getCombinedKey();
    try {
      return cache.get(key, () -> result.getServices()
          .flatMap(provider -> provider.getMediaFormats().stream())
          .collect(Collectors.toCollection(() -> new TreeSet<MediaFormat>())));
    }
    catch (ExecutionException ex) {
      throw new RuntimeException("Error accessing media format provider result cache.", ex);
    }
  }

}
