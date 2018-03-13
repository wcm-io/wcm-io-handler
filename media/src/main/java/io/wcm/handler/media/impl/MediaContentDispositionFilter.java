/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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
package io.wcm.handler.media.impl;

import static io.wcm.handler.media.impl.AbstractMediaFileServlet.HEADER_CONTENT_DISPOSITION;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_GET;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;

/**
 * Servlet filter that sets the Content-Disposition header according to it's configuration.
 *
 * The filter is called AFTER the Sling Security Content-Disposition Filter and explicitly overrides the Content-Disposition Header to "inline" for all
 * non-blacklisted mimetypes.
 *
 * The filter-pattern ensures, that we don't intercept any requests that are usually processed by the "Sling ContentDispositionFilter""
 */
@Component(service = Filter.class, property = {
    "sling.filter.scope=request",
    "sling.filter.pattern=/content/dam/.*/(jcr:content|_jcr_content)/renditions/.*",
    "service.ranking=-25001"
})
@Designate(ocd = MediaContentDispositionFilter.Config.class)
public final class MediaContentDispositionFilter implements Filter {

  @ObjectClassDefinition(name = "wcm.io Content Disposition Filter")
  @interface Config {

    @AttributeDefinition(name = "Enabled",
      description = "Enables this filter",
      type = AttributeType.BOOLEAN)
    boolean enabled() default true;

    @AttributeDefinition(name = "Blacklisted Mime Types",
      description = "Mime types that should be NOT be handled by this filter which sets content-disposition:inline")
    String[] blacklistedMimeTypes() default {"text/html", "application/octet-stream"};
  }

  private Config config;

  @Activate
  private void activate(Config config) {
    this.config = config;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // do nothing
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    if (config.enabled()) {
      SlingHttpServletRequest slingRequest = (SlingHttpServletRequest)request;
      SlingHttpServletResponse slingResponse = (SlingHttpServletResponse)response;
      // only check if this is a GET request and there are configured whitelist mime types
      if (accepts(slingRequest) && config.blacklistedMimeTypes().length > 0) {
        setContentDisposition(slingRequest, slingResponse);
      }
    }
    filterChain.doFilter(request, response);
  }

  private void setContentDisposition(SlingHttpServletRequest request, SlingHttpServletResponse response) {
    Resource resource = request.getResource();
    Asset asset = DamUtil.resolveToAsset(resource);

    if (asset != null) {
      String mimeType = asset.getMimeType();
      if (StringUtils.isNotBlank(mimeType)
        && !Arrays.asList(config.blacklistedMimeTypes()).contains(mimeType.toLowerCase())) {
        response.setHeader(HEADER_CONTENT_DISPOSITION, "inline");
      }
    }
  }

  /**
   *
   * This filter only processes GET requests
   *
   * @param request
   * @return true if the filter accepts the given request
   */
  private boolean accepts(SlingHttpServletRequest request) {
    return request.getMethod().equalsIgnoreCase(METHOD_GET);
  }

  @Override
  public void destroy() {
    // do nothing
  }

}
