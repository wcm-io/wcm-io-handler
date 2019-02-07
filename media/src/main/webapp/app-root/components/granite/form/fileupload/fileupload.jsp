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
<%@page import="com.adobe.granite.ui.components.Config"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="org.apache.sling.api.request.RequestDispatcherOptions"%>
<%@page import="org.apache.sling.api.wrappers.ValueMapDecorator"%>
<%@page import="io.wcm.handler.media.spi.MediaHandlerConfig"%>
<%@page import="io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource"%>
<%@page import="io.wcm.wcm.ui.granite.util.GraniteUi"%>
<%@include file="../../global/global.jsp" %><%

Config cfg = cmp.getConfig();

Map<String,Object> fileUploadProps = new HashMap<>();
fileUploadProps.put("name", "./file");
fileUploadProps.put("fileNameParameter", "./fileName");
fileUploadProps.put("fileReferenceParameter", "./fileReference");

// set media ref properties as configured for media handler
Resource contentResource = GraniteUi.getContentResourceOrParent(request);
if (contentResource != null) {
  MediaHandlerConfig mediaHandlerConfig = contentResource.adaptTo(MediaHandlerConfig.class);
  fileUploadProps.put("name", "./" + mediaHandlerConfig.getMediaInlineNodeName());
  fileUploadProps.put("fileNameParameter", "./" + mediaHandlerConfig.getMediaInlineNodeName() + "Name");
  fileUploadProps.put("fileReferenceParameter", "./" + mediaHandlerConfig.getMediaRefProperty());
}

// simulate resource for dialog field def with updated properties
Resource fileUpload = GraniteUiSyntheticResource.wrapMerge(resource, new ValueMapDecorator(fileUploadProps));

RequestDispatcherOptions options = new RequestDispatcherOptions();
options.setForceResourceType("/libs/cq/gui/components/authoring/dialog/fileupload");
RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(fileUpload, options);
dispatcher.include(slingRequest, slingResponse);

// add pathfield widget
Map<String,Object> pathFieldProps = new HashMap<>();
pathFieldProps.put("name", fileUploadProps.get("fileReferenceParameter"));
pathFieldProps.put("rootPath", cfg.get("rootPath", "/content/dam"));
pathFieldProps.put("granite:class", "wcmio-handler-media-fileupload-pathfield");
Resource pathField = GraniteUiSyntheticResource.child(fileUpload, "pathfield" ,
    "granite/ui/components/coral/foundation/form/pathfield", new ValueMapDecorator(pathFieldProps));

dispatcher = slingRequest.getRequestDispatcher(pathField);
dispatcher.include(slingRequest, slingResponse);
%>
