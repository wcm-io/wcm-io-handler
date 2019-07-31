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
<%@page import="java.util.HashMap"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="org.apache.sling.api.request.RequestDispatcherOptions"%>
<%@page import="org.apache.sling.api.wrappers.ValueMapDecorator"%>
<%@page import="com.day.cq.commons.jcr.JcrConstants"%>
<%@page import="com.adobe.granite.ui.components.Config"%>
<%@page import="io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource"%>
<%@page import="io.wcm.wcm.ui.granite.util.GraniteUi"%>
<%@page import="io.wcm.sling.commons.resource.ImmutableValueMap"%>
<%@include file="../../global/global.jsp" %><%--###

Media Format Multiple Select
============================

Allows to pick one or multiple media formats.

It extends `/libs/granite/ui/components/coral/foundation/form/select` component.

It supports the same properties as it's super component. The following properties
are overwritten or added.

.. gnd:gnd::

  /**
   * Media format select is always multiple
   */
  - multiple (Boolean) = 'true'


###--%><%

Config cfg = cmp.getConfig();

Map<String,Object> selectProps = new HashMap<>();
selectProps.put("multiple", true);

Resource select = GraniteUiSyntheticResource.wrapMerge(resource, new ValueMapDecorator(selectProps));
GraniteUiSyntheticResource.child(select, Config.DATASOURCE, JcrConstants.NT_UNSTRUCTURED,
    ImmutableValueMap.of("sling:resourceType", "wcm-io/handler/media/components/granite/datasources/mediaformats"));

//render original component
RequestDispatcherOptions options = new RequestDispatcherOptions();
options.setForceResourceType("granite/ui/components/coral/foundation/form/select");
RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(select, options);
dispatcher.include(slingRequest, slingResponse);

%>