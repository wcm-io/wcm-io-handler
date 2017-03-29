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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.osgi.Order;
import org.apache.sling.commons.osgi.RankedServices;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import io.wcm.handler.commons.spisupport.SpiMatcher;

class SpiServiceTracker implements ServiceTrackerCustomizer<SpiMatcher, SpiMatcher> {

  private final BundleContext bundleContext;
  private final ServiceTracker<SpiMatcher, SpiMatcher> serviceTracker;
  private volatile RankedServices<SpiMatcher> rankedServices;

  SpiServiceTracker(String className, BundleContext bundleContext) {
    this.bundleContext = bundleContext;
    this.rankedServices = new RankedServices<SpiMatcher>(Order.DESCENDING);
    this.serviceTracker = new ServiceTracker<SpiMatcher, SpiMatcher>(bundleContext, className, this);
    this.serviceTracker.open();
  }

  public void dispose() {
    serviceTracker.close();
    rankedServices = null;
  }

  @Override
  public SpiMatcher addingService(ServiceReference<SpiMatcher> reference) {
    SpiMatcher service = bundleContext.getService(reference);
    Map<String, Object> props = getProperties(reference);
    if (rankedServices != null) {
      rankedServices.bind(service, props);
    }
    return service;
  }

  @Override
  public void modifiedService(ServiceReference<SpiMatcher> reference, SpiMatcher service) {
    // nothing to do
  }

  @Override
  public void removedService(ServiceReference<SpiMatcher> reference, SpiMatcher service) {
    Map<String, Object> props = getProperties(reference);
    if (rankedServices != null) {
      rankedServices.unbind(service, props);
    }
    bundleContext.ungetService(reference);
  }

  private Map<String, Object> getProperties(ServiceReference<SpiMatcher> reference) {
    Map<String, Object> props = new HashMap<>();
    for (String key : reference.getPropertyKeys()) {
      props.put(key, reference.getProperty(key));
    }
    return props;
  }

  public Stream<SpiMatcher> resolve(Resource resource) {
    if (rankedServices == null) {
      return Stream.empty();
    }
    return rankedServices.getList().stream()
        .filter(service -> (resource != null || service.supportsNullResource()) && service.matches(resource));
  }

}
