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
package io.wcm.handler.commons.caservice.impl;

import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

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

import io.wcm.handler.commons.caservice.ContextAwareService;
import io.wcm.handler.commons.caservice.ContextAwareServiceResolver;

/**
 * {@link ContextAwareServiceResolver} implementation.
 */
@Component(service = ContextAwareServiceResolver.class, immediate = true)
public class ContextAwareServiceResolverImpl implements ContextAwareServiceResolver {

  private BundleContext bundleContext;

  // cache of service trackers for each SPI interface
  private final LoadingCache<String, ContextAwareServiceTracker> cache = CacheBuilder.newBuilder()
      .removalListener(new RemovalListener<String, ContextAwareServiceTracker>() {
        @Override
        public void onRemoval(RemovalNotification<String, ContextAwareServiceTracker> notification) {
          notification.getValue().dispose();
        }
      })
      .build(new CacheLoader<String, ContextAwareServiceTracker>() {
        @Override
        public ContextAwareServiceTracker load(String className) throws Exception {
          return new ContextAwareServiceTracker(className, bundleContext);
        }
      });

  @Activate
  private void activate(BundleContext context) {
    this.bundleContext = context;
  }

  @Deactivate
  private void deactivate(BundleContext context) {
    cache.invalidateAll();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends ContextAwareService> T resolve(Class<T> serviceClass, Adaptable adaptable) {
    Resource resource = getResource(adaptable);
    ContextAwareServiceTracker serviceTracker = getServiceTracker(serviceClass);
    return (T)serviceTracker.resolve(resource).findFirst().orElse(null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends ContextAwareService> Stream<T> resolveAll(Class<T> serviceClass, Adaptable adaptable) {
    Resource resource = getResource(adaptable);
    ContextAwareServiceTracker serviceTracker = getServiceTracker(serviceClass);
    return (Stream<T>)serviceTracker.resolve(resource);
  }

  private Resource getResource(Adaptable adaptable) {
    if (adaptable instanceof Resource) {
      return (Resource)adaptable;
    }
    else if (adaptable instanceof SlingHttpServletRequest) {
      return ((SlingHttpServletRequest)adaptable).getResource();
    }
    return null;
  }

  private ContextAwareServiceTracker getServiceTracker(Class<?> serviceClass) {
    try {
      return cache.get(serviceClass.getName());
    }
    catch (ExecutionException ex) {
      throw new RuntimeException("Error getting service tracker for " + serviceClass.getName() + " from cache.", ex);
    }
  }

}
