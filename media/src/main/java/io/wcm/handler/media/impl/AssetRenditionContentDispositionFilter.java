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

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_MIMETYPE;
import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.commons.jcr.JcrConstants.NT_FILE;
import static io.wcm.handler.media.impl.AbstractMediaFileServlet.HEADER_CONTENT_DISPOSITION;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_GET;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Servlet filter that applies the logic of the AEM "Dam Safe Binary Filter" also to direct
 * references to renditions at <code>{asset-path}/jcr_content/renditions/*</code>.
 * It re-used the configuration of the "Dam Safe Binary Filter"
 * (com.day.cq.dam.core.impl.servlet.DamContentDispositionFilter),
 * so both filters have the same result.
 * <p>
 * Unlike for Asset paths where the "Dam Safe Binary Filter" applies for rendition paths the
 * "Sling Content Disposition Filter" is applied first which adds a "attachment" content disposition header for all
 * paths that are not whitelisted. This filter resets this to an "inline" content disposition header for all mime types
 * that are not blacklisted.
 * </p>
 */
@Component(service = Filter.class,
    name = "com.day.cq.dam.core.impl.servlet.DamContentDispositionFilter", // reuse config from 'Dam Safe Binary Filter'
    property = {
        "sling.filter.scope=request",
        "sling.filter.pattern=/content/dam/.*/(jcr:content|_jcr_content)/renditions/.*",
        "service.ranking=-25001"
})
public final class AssetRenditionContentDispositionFilter implements Filter {

  static final String BLACK_LIST_MIME_TYPE_CONFIG = "cq.mime.type.blacklist";
  static final String ALLOW_EMPTY_MIME = "cq.dam.empty.mime";

  private Set<String> mimetypeBlacklist;
  private boolean allowEmptyMime;

  @Activate
  private void activate(Map<String, Object> config) {
    String[] mimetypeBlacklistArray = PropertiesUtil.toStringArray(config.get(BLACK_LIST_MIME_TYPE_CONFIG));
    if (mimetypeBlacklistArray != null) {
      mimetypeBlacklist = Arrays.stream(mimetypeBlacklistArray)
          .map(StringUtils::lowerCase)
          .collect(Collectors.toSet());
    }
    else {
      mimetypeBlacklist = Collections.emptySet();
    }
    allowEmptyMime = PropertiesUtil.toBoolean(config.get(ALLOW_EMPTY_MIME), false);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // do nothing
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    SlingHttpServletRequest slingRequest = (SlingHttpServletRequest)request;
    SlingHttpServletResponse slingResponse = (SlingHttpServletResponse)response;
    if (accepts(slingRequest)) {
      setContentDisposition(slingRequest, slingResponse);
    }
    filterChain.doFilter(request, response);
  }

  @SuppressWarnings("null")
  private void setContentDisposition(SlingHttpServletRequest request, SlingHttpServletResponse response) {
    Resource resource = request.getResource();

    // get mimetype from nt:file resource
    String mimeType = resource.getValueMap().get(JCR_CONTENT + "/" + JCR_MIMETYPE, String.class);

    // if mimetype is not blacklisted, or empty (and this is allowed) send "inline" content disposition header
    if ((StringUtils.isNotBlank(mimeType) && !mimetypeBlacklist.contains(mimeType.toLowerCase()))
        || (StringUtils.isBlank(mimeType) && allowEmptyMime)) {
      response.setHeader(HEADER_CONTENT_DISPOSITION, "inline");
    }
  }

  /**
   * This filter only processes GET requests that targets a nt:file resource.
   * @param request
   * @return true if the filter accepts the given request
   */
  @SuppressWarnings("null")
  private boolean accepts(SlingHttpServletRequest request) {
    return request.getMethod().equalsIgnoreCase(METHOD_GET)
        && request.getResource() != null
        && StringUtils.equals(request.getResource().getValueMap().get(JCR_PRIMARYTYPE, String.class), NT_FILE);
  }

  @Override
  public void destroy() {
    // do nothing
  }

}
