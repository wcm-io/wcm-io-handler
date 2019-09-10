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
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="org.apache.sling.api.resource.ValueMap"%>
<%@page import="org.apache.sling.api.request.RequestDispatcherOptions"%>
<%@page import="io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource"%>
<%@include file="../../global/global.jsp" %>
<%@include file="../siteRootPathField/pathDetection.jsp" %><%--###

wcm.io URL Handler Site Root PathBrowser
========================================

A field that allows the user to enter path.

It extends `/libs/granite/ui/components/coral/foundation/form/pathbrowser` component.

It supports the same properties as it's super component. The following properties
are overwritten or added.

.. gnd:gnd::

  /**
   * The root path of the path browser. If not set, it's value is set automatically
   * to the "Site Root" of the current site as configured via the URL Handler configuration.
   */
  - rootPath (StringEL) = {site root}

  /**
   * The root path that is used as fallback when no root path could be detected dynamically,
   * e.g. because outside any site or within experience fragments.
   */
  - fallbackRootPath (StringEL) = "/content"

  /**
   * Appendix path added to the (usually auto-detected) root path.
   */
  - appendPath (StringEL) = {path appendix}

###--%><%

// detect root path
ValueMap overwriteProperties = getRootPathProperties(cmp, slingRequest);

// simulate resource for dialog field def with new rootPath instead of configured one
Resource resourceWrapper = GraniteUiSyntheticResource.wrapMerge(resource, overwriteProperties);

RequestDispatcherOptions options = new RequestDispatcherOptions();
options.setForceResourceType("granite/ui/components/coral/foundation/form/pathbrowser");
RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(resourceWrapper, options);
dispatcher.include(slingRequest, slingResponse);

%>
