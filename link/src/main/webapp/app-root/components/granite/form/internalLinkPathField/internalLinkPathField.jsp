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
<%@page import="com.adobe.granite.ui.components.Config"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="org.apache.sling.api.resource.ValueMap"%>
<%@page import="org.apache.sling.api.request.RequestDispatcherOptions"%>
<%@page import="org.apache.sling.api.wrappers.ValueMapDecorator"%>
<%@page import="io.wcm.handler.link.LinkNameConstants"%>
<%@page import="io.wcm.handler.link.spi.LinkHandlerConfig"%>
<%@page import="io.wcm.handler.link.type.InternalLinkType"%>
<%@page import="io.wcm.sling.commons.resource.ImmutableValueMap"%>
<%@page import="io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource"%>
<%@include file="../../global/global.jsp" %>
<%@include file="../../global/linkRootPathDetection.jsp" %><%--###

wcm.io URL Handler Site Root PathField
======================================

A field that allows the user to enter path.

It extends `/libs/granite/ui/components/coral/foundation/form/pathfield` component.

It supports the same properties as it's super component. The following properties
are overwritten or added.

.. gnd:gnd::

  /**
   * Default property name as defined for this link type.
   */
  - name (String) = {default property name for link type}

  /**
   * The path of the root of the pathfield. If not set, it's value is set automatically
   * to the root path for internal link type as returned by the current context's Link Handler configuration.
   */
  - rootPath (StringEL) = {link type-dependent root path}


###--%><%

// detect root path
Config cfg = cmp.getConfig();
String name = cfg.get("name", LinkNameConstants.PN_LINK_CONTENT_REF);
String rootPath = cfg.get("rootPath", String.class);
if (rootPath == null) {
  rootPath = getRootPath(slingRequest, InternalLinkType.ID, LinkHandlerConfig.DEFAULT_ROOT_PATH_CONTENT);
}

ValueMap overwriteProperties = new ValueMapDecorator(ImmutableValueMap.of(
    "name", name,
    "rootPath", rootPath));

// simulate resource for dialog field def with new rootPath instead of configured one
Resource resourceWrapper = GraniteUiSyntheticResource.wrapMerge(resource, overwriteProperties);

RequestDispatcherOptions options = new RequestDispatcherOptions();
options.setForceResourceType("granite/ui/components/coral/foundation/form/pathfield");
RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(resourceWrapper, options);
dispatcher.include(slingRequest, slingResponse);

%>
