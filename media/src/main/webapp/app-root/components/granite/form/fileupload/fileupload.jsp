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
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="org.apache.sling.api.request.RequestDispatcherOptions"%>
<%@page import="org.apache.sling.api.wrappers.ValueMapDecorator"%>
<%@page import="com.adobe.granite.ui.components.Config"%>
<%@page import="io.wcm.handler.media.MediaNameConstants"%>
<%@page import="io.wcm.handler.media.spi.MediaHandlerConfig"%>
<%@page import="io.wcm.wcm.commons.component.ComponentPropertyResolver"%>
<%@page import="io.wcm.wcm.commons.component.ComponentPropertyResolution"%>
<%@page import="io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource"%>
<%@page import="io.wcm.wcm.ui.granite.util.GraniteUi"%>
<%@include file="../../global/global.jsp" %>
<%@include file="mediaFormatSupport.jsp" %><%--###

wcm.io Media Handler FileUpload
===============================

A field component for uploading or selecting files from an authoring dialog context.

It extends `/libs/cq/gui/components/authoring/dialog/fileupload` component.

It supports the same properties as it's super component. The following properties
are overwritten or added.

.. gnd:gnd::

  /**
   * The name that identifies the file upload location. E.g. ./file or ./image/file
   */
  - name (String) = {default value configured in media handler}

  /**
   * The location for storing the name of the file. E.g. ./fileName or ./image/fileName
   */
  - fileNameParameter (String) = {default value configured in media handler}

  /**
   * The location for storing a DAM file reference. E.g. ./fileReference or ./image/fileReference
   */
  - fileReferenceParameter (String) = {default value configured in media handler}

  /**
   * The browse and selection filter for file selection. E.g. [".png",".jpg"] or ["image/\*"].
   */
  - mimeTypes (String) multiple = ["image","image/gif","image/jpeg","image/png"]

  /**
   * Indicates whether upload from local file system is allowed.
   */
  - allowUpload (Boolean) = 'false'

  /**
   * When the field description is not set, it is set automatically with an information about the
   * supported media formats.
   */
  - fieldDescription (String) = {media format information}

  /**
   * The path of the root of the pathfield.
   */
  - rootPath (StringEL) = '/content/dam'

  /**
   * List of media formats required by this component.
   * If not set the property value is looked up from component properties or policy.
   */
  - mediaFormats (String[])

  /**
   * Resolving of all media formats is mandatory.
   * If not set the property value is looked up from component properties or policy.
   */
  - mediaFormatsMandatory (Boolean) = 'false'

  /**
   * Enables "auto-cropping" mode.
   * If not set the property value is looked up from component properties or policy.
   */
  - mediaCropAuto (Boolean) = 'false'


###--%><%

Config cfg = cmp.getConfig();

// get default values for media ref properties as configured for media handler
String propNameDefault = "./file";
String propFileNameDefault = "./fileName";
String propFileReferenceDefault = "./fileReference";
Resource contentResource = GraniteUi.getContentResourceOrParent(request);
if (contentResource != null) {
  MediaHandlerConfig mediaHandlerConfig = contentResource.adaptTo(MediaHandlerConfig.class);
  propNameDefault = "./" + mediaHandlerConfig.getMediaInlineNodeName();
  propFileNameDefault = "./" + mediaHandlerConfig.getMediaInlineNodeName() + "Name";
  propFileReferenceDefault = "./" + mediaHandlerConfig.getMediaRefProperty();
}

Map<String,Object> fileUploadProps = new HashMap<>();
fileUploadProps.put("name", cfg.get("name", propNameDefault));
fileUploadProps.put("fileNameParameter", cfg.get("fileNameParameter", propFileNameDefault));
fileUploadProps.put("fileReferenceParameter", cfg.get("fileReferenceParameter", propFileReferenceDefault));

// default values for allowUpload and mimeTypes
fileUploadProps.put("allowUpload", cfg.get("allowUpload", false));
fileUploadProps.put("mimeTypes", cfg.get("mimeTypes", new String[] {
    "image", "image/gif", "image/jpeg", "image/png" }));

// media format properties for validation of associated media reference
ComponentPropertyResolver componentPropertyResolver = new ComponentPropertyResolver(contentResource)
    .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
String[] mediaFormats = cfg.get("mediaFormats",
    componentPropertyResolver.get(MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS, String[].class));
boolean mediaFormatsMandatory = cfg.get("mediaFormatsMandatory",
    componentPropertyResolver.get(MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS_MANDATORY, false));
boolean mediaCropAuto = cfg.get("mediaCropAuto",
    componentPropertyResolver.get(MediaNameConstants.PN_COMPONENT_MEDIA_AUTOCROP, false));

// add info about media formats in field description
String mediaFormatsFieldDescription = buildMediaFormatsFieldDescription(mediaFormats, contentResource);
if (mediaFormatsFieldDescription != null) {
  fileUploadProps.put("fieldDescription", cfg.get("fieldDescription", mediaFormatsFieldDescription));
}

// simulate resource for dialog field def with updated properties
Resource fileUpload = GraniteUiSyntheticResource.wrapMerge(resource, new ValueMapDecorator(fileUploadProps));

// render original fileupload widget
RequestDispatcherOptions options = new RequestDispatcherOptions();
options.setForceResourceType("cq/gui/components/authoring/dialog/fileupload");
RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(fileUpload, options);
dispatcher.include(slingRequest, slingResponse);

// add pathfield widget
Map<String,Object> pathFieldProps = new HashMap<>();
pathFieldProps.put("name", fileUploadProps.get("fileReferenceParameter"));
pathFieldProps.put("rootPath", cfg.get("rootPath", "/content/dam"));
pathFieldProps.put("granite:class", "cq-FileUpload cq-droptarget wcm-io-handler-media-fileupload-pathfield");
Resource pathField = GraniteUiSyntheticResource.child(fileUpload, "pathfield" ,
    "wcm-io/wcm/ui/granite/components/form/pathfield", new ValueMapDecorator(pathFieldProps));
if (mediaFormats != null && mediaFormats.length > 0) {
  Map<String,Object> dataProps = new HashMap<>();
  dataProps.put("wcmio-mediaformats", StringUtils.join(mediaFormats, ","));
  dataProps.put("wcmio-mediaformats-mandatory", mediaFormatsMandatory);
  dataProps.put("wcmio-media-cropauto", mediaCropAuto);
  GraniteUiSyntheticResource.child(pathField, "granite:data", null, new ValueMapDecorator(dataProps));
}

dispatcher = slingRequest.getRequestDispatcher(pathField);
dispatcher.include(slingRequest, slingResponse);

%>
