<%--
  #%L
  wcm.io
  %%
  Copyright (C) 2019 wcm.io
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  --%>
<%@page import="java.util.Map"%>
<%@page import="com.day.cq.wcm.api.Page"%>
<%@page import="io.wcm.handler.media.spi.MediaHandlerConfig"%>
<%@page import="io.wcm.wcm.ui.granite.util.GraniteUi"%>
<%@page import="io.wcm.wcm.ui.granite.util.RootPathDetector"%>
<%@page import="io.wcm.wcm.ui.granite.util.RootPathResolver"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="org.apache.sling.api.SlingHttpServletRequest"%><%!

static Map<String,Object> getDamRootPathProperties(ComponentHelper cmp, SlingHttpServletRequest request,
    String fallbackRootPath) {
  RootPathResolver rootPathResolver = new RootPathResolver(cmp, request);
  rootPathResolver.setFallbackRootPath(fallbackRootPath);
  
  rootPathResolver.setRootPathDetector(new RootPathDetector() {
    public String detectRootPath(ComponentHelper cmp, SlingHttpServletRequest request) {
      String rootPath = null;
      Page contentPage = GraniteUi.getContentPage(request);
      if (contentPage != null) {
        MediaHandlerConfig mediaHandlerConfig = contentPage.getContentResource().adaptTo(MediaHandlerConfig.class);
        rootPath = mediaHandlerConfig.getDamRootPath(contentPage);
      }
      return rootPath;
    }
  });
  
  return rootPathResolver.getOverrideProperties();
}

%>
