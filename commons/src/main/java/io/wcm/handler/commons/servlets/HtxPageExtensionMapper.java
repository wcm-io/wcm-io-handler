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
package io.wcm.handler.commons.servlets;

import io.wcm.wcm.commons.contenttype.FileExtension;

import java.io.IOException;
import java.util.Dictionary;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Virtually maps an *.htx request to a cq:Page resource to a *.html request internally (because components
 * and JSPs are normally only registered to *.html extension). Mapping can be enabled or disabled.
 */
@SlingServlet(paths = "/apps/foundation/components/primary/cq/Page/Page." + FileExtension.HTML_UNCACHED + ".servlet",
methods = HttpConstants.METHOD_GET,
extensions = FileExtension.HTML_UNCACHED,
label = "wcm.io htx Page Extension Mapper",
description = "Mapps all *.htx requests on Pages to *.html view.",
metatype = true)
public class HtxPageExtensionMapper extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(HtxPageExtensionMapper.class);

  @Property(boolValue = HtxPageExtensionMapper.DEFAULT_ENABLED,
      label = "Enabled",
      description = "Enable mapping.")
  static final String PROPERTY_ENABLED = "enabled";
  static final boolean DEFAULT_ENABLED = true;

  private boolean enabled;

  // ---------- SCR Integration ----------------------------------------------

  @SuppressWarnings("unchecked")
  protected void activate(ComponentContext pContext) {
    // read config
    final Dictionary<String, Object> props = pContext.getProperties();
    this.enabled = PropertiesUtil.toBoolean(props.get(PROPERTY_ENABLED), DEFAULT_ENABLED);
  }

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

    // if not enabled: sent http 404
    if (!enabled) {
      response.sendError(404);
      return;
    }

    // rebuild requested url, but use "html" as extension instead of "htx"
    // to allow re-using all component definitions that are registered to "html"
    RequestPathInfo info = request.getRequestPathInfo();
    StringBuilder url = new StringBuilder();
    url.append(info.getResourcePath());
    if (info.getSelectorString() != null) {
      url.append(".").append(info.getSelectorString());
    }
    url.append("." + FileExtension.HTML); // use html extension instead of .htx extension
    if (info.getSuffix() != null) {
      url.append("/").append(info.getSuffix());
    }

    if (log.isDebugEnabled()) {
      log.debug("Dispatch request {} to {}", request.getRequestURI(), url);
    }

    // execute request with *.html extension
    RequestDispatcher requestDispatcher = request.getRequestDispatcher(url.toString());
    if (requestDispatcher != null) {
      requestDispatcher.include(request, response);
      return;
    }
    else {
      log.error("Unable to dispatch proxy request for {} referrer={}", request.getRequestURI(), request.getHeader("Referrer"));
      throw new ServletException("No Content");
    }

  }

}
