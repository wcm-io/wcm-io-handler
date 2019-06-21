/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.handler.url.impl.clientlib;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.jackrabbit.api.observation.JackrabbitEventFilter;
import org.apache.jackrabbit.api.observation.JackrabbitObservationManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Checks given path for client library folder and allowProxy flag status.
 * Caches the results in memory.
 * Clears cache if any observation events occured on client libraries.
 */
class ClientlibPathCache implements EventListener, AutoCloseable {

  private final ResourceResolverFactory resourceResolverFactory;
  private ResourceResolver listenerServiceResourceResolver;

  private static final String NT_CLIENTLIBRARY = "cq:ClientLibraryFolder";
  private static final String PN_ALLOWPROXY = "allowProxy";

  /**
   * Service user for accessing clientlib resources below /apps and /libs
   */
  private static final String CLIENTLIBS_SERVICE = "clientlibs-service";

  private static final String SERVICE_USER_MAPPING_WARNING = "Missing service user mapping for "
      + "'io.wcm.handler.url:" + CLIENTLIBS_SERVICE + "' - see https://wcm.io/handler/url/configuration.html";

  private final LoadingCache<String, ClientlibPathCacheEntry> cache = CacheBuilder.newBuilder()
      .maximumSize(10000)
      .build(new CacheLoader<String, ClientlibPathCacheEntry>() {
        @Override
        public ClientlibPathCacheEntry load(String path) throws Exception {
          try (ResourceResolver resourceResolver = getServiceResourceResolver()) {
            Resource resource = resourceResolver.getResource(path);
            if (resource != null) {
              Node node = resource.adaptTo(Node.class);
              if (node != null && node.isNodeType(NT_CLIENTLIBRARY)) {
                boolean isAllowProxy = resource.getValueMap().get(PN_ALLOWPROXY, false);
                ClientlibPathCacheEntry entry = new ClientlibPathCacheEntry(path, true, isAllowProxy);
                log.debug("Detected client library: {}", entry);
                return entry;
              }
            }
          }
          catch (LoginException ex) {
            log.warn(SERVICE_USER_MAPPING_WARNING);
          }
          return new ClientlibPathCacheEntry(path, false, false);
        }
      });

  private static final Logger log = LoggerFactory.getLogger(ClientlibPathCache.class);

  ClientlibPathCache(ResourceResolverFactory resourceResolverFactory) {
    this.resourceResolverFactory = resourceResolverFactory;
    try {
      this.listenerServiceResourceResolver = getServiceResourceResolver();
      enableObservationForClientLibraries();
    }
    catch (LoginException ex) {
      log.warn(SERVICE_USER_MAPPING_WARNING);
    }
  }

  private ResourceResolver getServiceResourceResolver() throws LoginException {
    return resourceResolverFactory.getServiceResourceResolver(
        Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, (Object)CLIENTLIBS_SERVICE));
  }

  /**
   * Enable observation in the JCR repository for any events on client library folders.
   */
  private void enableObservationForClientLibraries() {
    Session session = this.listenerServiceResourceResolver.adaptTo(Session.class);
    if (session != null) {
      try {
        JackrabbitObservationManager observationManager = (JackrabbitObservationManager)session.getWorkspace().getObservationManager();
        JackrabbitEventFilter eventFilter = new JackrabbitEventFilter();
        eventFilter.setAdditionalPaths("/apps", "/libs");
        eventFilter.setNodeTypes(new String[] { NT_CLIENTLIBRARY });
        observationManager.addEventListener(this, eventFilter);
      }
      catch (RepositoryException ex) {
        log.warn("Unable to register obervation for client libraries.");
      }
    }
  }

  /**
   * If any event on any client library occurs clear the client library cache.
   */
  @Override
  public void onEvent(EventIterator events) {
    log.debug("Clear client library path cache.");
    cache.invalidateAll();
  }

  /**
   * Checks if the given path is a client library, and if this has enabled the "allowProxy" mode.
   * @param path Path of a (potential) client library
   * @return true if it is a client library, and if it has set the "allowProxy" flag.
   */
  public boolean isClientlibWithAllowProxy(String path) {
    try {
      ClientlibPathCacheEntry entry = cache.get(path);
      return entry.isClientLibrary() && entry.isAllowProxy();
    }
    catch (ExecutionException ex) {
      log.warn("Error accessing cache.", ex);
      return false;
    }
  }

  @Override
  public void close() throws Exception {
    if (this.listenerServiceResourceResolver != null) {
      this.listenerServiceResourceResolver.close();
    }
  }

}
