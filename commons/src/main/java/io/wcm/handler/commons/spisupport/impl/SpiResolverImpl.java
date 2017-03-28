/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.handler.commons.spisupport.impl;

import java.util.concurrent.ExecutionException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import io.wcm.handler.commons.spisupport.SpiMatcher;
import io.wcm.handler.commons.spisupport.SpiResolver;

/**
 * SPI resolver implementation.
 */
@Component(service = SpiResolver.class, immediate = true)
public class SpiResolverImpl implements SpiResolver {

  private BundleContext bundleContext;

  // cache of service trackers for each SPI interface
  private final LoadingCache<String, SpiServiceTracker> cache = CacheBuilder.newBuilder()
      .removalListener(new RemovalListener<String, SpiServiceTracker>() {
        @Override
        public void onRemoval(RemovalNotification<String, SpiServiceTracker> notification) {
          notification.getValue().dispose();
        }
      })
      .build(new CacheLoader<String, SpiServiceTracker>() {
        @Override
        public SpiServiceTracker load(String className) throws Exception {
          return new SpiServiceTracker(className, bundleContext);
        }
      });

  @SuppressWarnings("unchecked")
  @Override
  public <T extends SpiMatcher> T resolve(Class<T> spiInterface, Adaptable adaptable) {
    Resource resource = null;
    if (adaptable instanceof Resource) {
      resource = (Resource)adaptable;
    }
    else if (adaptable instanceof SlingHttpServletRequest) {
      resource = ((SlingHttpServletRequest)adaptable).getResource();
    }
    try {
      SpiServiceTracker serviceTracker = cache.get(spiInterface.getName());
      return (T)serviceTracker.resolve(resource);
    }
    catch (ExecutionException ex) {
      throw new RuntimeException("Error getting service tracker for " + spiInterface.getName() + " from cache.", ex);
    }
  }

  @Activate
  private void activate(BundleContext context) {
    this.bundleContext = context;
  }

  @Deactivate
  private void deactivate(BundleContext context) {
    cache.invalidateAll();
  }

}
