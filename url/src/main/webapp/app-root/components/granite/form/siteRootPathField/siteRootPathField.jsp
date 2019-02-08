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
<%@page import="io.wcm.wcm.commons.util.Path"%>
<%@page import="io.wcm.handler.url.spi.UrlHandlerConfig"%>
<%@page import="com.adobe.granite.ui.components.Config"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="org.apache.sling.api.resource.ValueMap"%>
<%@page import="org.apache.sling.api.request.RequestDispatcherOptions"%>
<%@page import="org.apache.sling.api.wrappers.ValueMapDecorator"%>
<%@page import="com.google.common.collect.ImmutableMap"%>
<%@page import="io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource"%>
<%@page import="io.wcm.wcm.ui.granite.util.GraniteUi"%>
<%@include file="../../global/global.jsp" %><%

/*
 * Special version of pathfield widget which root is set to the site root of the URL handler config.
 */

String rootPath = "/content";

Resource contentResource = GraniteUi.getContentResourceOrParent(request);
if (contentResource != null) {
  UrlHandlerConfig urlHandlerConfig = contentResource.adaptTo(UrlHandlerConfig.class);
  if (urlHandlerConfig != null) {
    int siteRootLevel = urlHandlerConfig.getSiteRootLevel(contentResource);
    if (siteRootLevel >= 0) {
      rootPath = Path.getAbsoluteParent(contentResource.getPath(), siteRootLevel, resourceResolver);
    }
  }
}

ValueMap overwriteProperties = new ValueMapDecorator(ImmutableMap.<String,Object>of("rootPath", rootPath));

// simulate resource for dialog field def with new rootPath instead of configured one
Resource resourceWrapper = GraniteUiSyntheticResource.wrapMerge(resource, overwriteProperties);

RequestDispatcherOptions options = new RequestDispatcherOptions();
options.setForceResourceType("/libs/granite/ui/components/coral/foundation/form/pathfield");
RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(resourceWrapper, options);
dispatcher.include(slingRequest, slingResponse);

%>
