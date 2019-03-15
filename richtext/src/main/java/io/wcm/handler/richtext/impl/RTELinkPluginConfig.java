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
package io.wcm.handler.richtext.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import io.wcm.handler.link.spi.LinkHandlerConfig;
import io.wcm.handler.link.spi.LinkType;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Servlet providing RTE link plugin configuration in context of the referenced content page.
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
    resourceTypes = NameConstants.NT_PAGE,
    selectors = RTELinkPluginConfig.SELECTOR,
    extensions = FileExtension.JSON,
    methods = HttpConstants.METHOD_GET)
public class RTELinkPluginConfig extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  static final String SELECTOR = "wcmio-handler-richtext-rte-plugins-links-config";

  @Override
  protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {

    Resource resource = request.getResource();
    PageManager pageManager = AdaptTo.notNull(request.getResourceResolver(), PageManager.class);
    Page page = pageManager.getContainingPage(resource);

    LinkHandlerConfig linkHandlerConfig = AdaptTo.notNull(resource, LinkHandlerConfig.class);
    List<LinkType> linkTypes = linkHandlerConfig.getLinkTypes().stream()
        .map(linkTypeClass -> AdaptTo.notNull(resource, linkTypeClass))
        .collect(Collectors.toList());

    try {
      JSONObject result = new JSONObject();

      JSONObject linkTypesConfigs = new JSONObject();
      for (LinkType linkType : linkTypes) {
        JSONObject linkTypeConfig = new JSONObject();
        linkTypeConfig.put("value", linkType.getId());
        linkTypeConfig.put("text", linkType.getLabel());
        linkTypesConfigs.put(linkType.getId(), linkTypeConfig);
      }
      result.put("linkTypes", linkTypesConfigs);

      JSONObject rootPaths = new JSONObject();
      for (LinkType linkType : linkTypes) {
        String rootPath = linkHandlerConfig.getLinkRootPath(page, linkType.getId());
        if (rootPath != null) {
          rootPaths.put(linkType.getId(), rootPath);
        }
      }

      result.put("linkTypes", linkTypesConfigs);
      result.put("rootPaths", rootPaths);
      response.setContentType(ContentType.JSON);
      response.getWriter().write(result.toString());
    }
    catch (JSONException ex) {
      throw new ServletException(ex);
    }
  }

}
