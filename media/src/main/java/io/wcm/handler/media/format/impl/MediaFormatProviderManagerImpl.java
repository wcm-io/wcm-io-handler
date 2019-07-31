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

import java.util.Collection;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatProviderManager;
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

  @Reference(cardinality = ReferenceCardinality.MULTIPLE,
      policy = ReferencePolicy.STATIC,
      policyOption = ReferencePolicyOption.GREEDY)
  private Collection<ServiceReference<MediaFormatProvider>> mediaFormatProviderServiceReferences;

  private BundleContext bundleContext;

  // cache resolving of media formats per combined cache key of context-aware services
  private final Cache<String, SortedSet<MediaFormat>> cache = CacheBuilder.newBuilder()
      .expireAfterWrite(1, TimeUnit.HOURS)
      .build();

  @Activate
  private void activate(BundleContext bc) {
    this.bundleContext = bc;
  }

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

  @Override
  public SortedMap<String, SortedSet<MediaFormat>> getAllMediaFormats() {
    SortedMap<String, SortedSet<MediaFormat>> result = new TreeMap<>();

    for (ServiceReference<MediaFormatProvider> serviceReference : mediaFormatProviderServiceReferences) {
      Bundle bundle = serviceReference.getBundle();
      String bundleName = StringUtils.defaultString(bundle.getHeaders().get(Constants.BUNDLE_NAME), bundle.getSymbolicName());
      SortedSet<MediaFormat> mediaFormats = result.getOrDefault(bundleName, new TreeSet<>());
      result.putIfAbsent(bundleName, mediaFormats);
      MediaFormatProvider mediaFormatProvider = bundleContext.getService(serviceReference);
      try {
        mediaFormats.addAll(mediaFormatProvider.getMediaFormats());
      }
      finally {
        bundleContext.ungetService(serviceReference);
      }
    }

    return result;
  }

}
