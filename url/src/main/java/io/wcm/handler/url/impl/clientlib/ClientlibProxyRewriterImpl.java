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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rewrites resource links to client libraries that are in "allowProxy" mode to /etc.clientlibs.
 */
@Component(service = ClientlibProxyRewriter.class, immediate = true)
public class ClientlibProxyRewriterImpl implements ClientlibProxyRewriter {

  private static final Pattern STATIC_RESOURCE_PATH_PATTERN = Pattern.compile("^(/(apps|libs)/.*)/resources/.*$");

  private static final Logger log = LoggerFactory.getLogger(ClientlibProxyRewriterImpl.class);

  @Reference
  private ResourceResolverFactory resourceResolverFactory;

  private volatile ClientlibPathCache clientlibPathCache;

  @Deactivate
  @SuppressWarnings("PMD.SignatureDeclareThrowsException")
  private void deactivate() throws Exception {
    if (clientlibPathCache != null) {
      this.clientlibPathCache.close();
    }
    this.clientlibPathCache = null;
  }

  private ClientlibPathCache getClientlibPathCache() {
    if (this.clientlibPathCache == null) {
      // lazy initialization
      synchronized (this) {
        if (this.clientlibPathCache == null) {
          this.clientlibPathCache = new ClientlibPathCache(resourceResolverFactory);
        }
      }
    }
    return this.clientlibPathCache;
  }

  @Override
  public @NotNull String rewriteStaticResourcePath(@NotNull String path) {
    Matcher matcher = STATIC_RESOURCE_PATH_PATTERN.matcher(path);
    if (matcher.matches()) {
      String clientlibPath = matcher.group(1);
      boolean clientlibProxyMode = getClientlibPathCache().isClientlibWithAllowProxy(clientlibPath);
      if (clientlibProxyMode) {
        return rewriteClientlibProxyPath(path);
      }
    }
    return path;
  }

  private String rewriteClientlibProxyPath(String path) {
    // replace /apps or /libs with /etc.clientlibs
    String rewrittenPath = "/etc.clientlibs" + path.substring(5);
    log.debug("Rewrite {} to {}", path, rewrittenPath);
    return rewrittenPath;
  }

}
