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
<%@page import="io.wcm.handler.commons.component.ComponentPropertyResolver"%>
<%@page import="io.wcm.handler.media.MediaNameConstants"%>
<%@page import="io.wcm.handler.media.spi.MediaHandlerConfig"%>
<%@page import="io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource"%>
<%@page import="io.wcm.wcm.ui.granite.util.GraniteUi"%>
<%@include file="../../global/global.jsp" %><%--###

FileUpload
==========

A field component for uploading or selecting files from an authoring dialog context.

It extends `/libs/cq/gui/components/authoring/dialog/fileupload` component.

It has the following content structure:

.. gnd:gnd::

  [author:DialogFileUpload] > granite:FormField

  /**
   * The name that identifies the file upload location. E.g. ./file or ./image/file
   */
  - name (String) = {default value configured in media handler}

  /**
   * Indicates if the field is in a disabled state.
   */
  - disabled (Boolean)

  /**
   * Indicates if it is mandatory to complete the field.
   */
  - required (Boolean)

  /**
   * The name of the validator to be applied. E.g. ``foundation.jcr.name``.
   * See :doc:`validation </jcr_root/libs/granite/ui/components/coral/foundation/clientlibs/foundation/js/validation/index>` in Granite UI.
   */
  - validation (String) multiple

  /**
   * The file size limit.
   */
  - sizeLimit (Long)

  /**
   * The browse and selection filter for file selection. E.g. [".png",".jpg"] or ["image/\*"].
   */
  - mimeTypes (String) multiple = ["image","image/gif","image/jpeg","image/png"]

  /**
   * The icon.
   */
  - icon (String)

  /**
   * The location for storing the name of the file. E.g. ./fileName or ./image/fileName
   */
  - fileNameParameter (String) = {default value configured in media handler}

  /**
   * The location for storing a DAM file reference. E.g. ./fileReference or ./image/fileReference
   */
  - fileReferenceParameter (String) = {default value configured in media handler}

  /**
   * Indicates whether upload from local file system is allowed.
   */
  - allowUpload (Boolean) = 'false'

  /**
   * The URI Template used for editing the DAM file referenced.
   */
  - viewInAdminURI (String) = '/assetdetails.html{+item}'

  /**
   * Show pathfield widget additionally to the file upload widget.
   */
  - showPathfield (Boolean) = 'true'

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

//simulate resource for dialog field def with updated properties
Resource fileUpload = GraniteUiSyntheticResource.wrapMerge(resource, new ValueMapDecorator(fileUploadProps));

// media format properties for validation of associated media reference
ComponentPropertyResolver componentPropertyResolver = new ComponentPropertyResolver(contentResource);
String[] mediaFormats = cfg.get("mediaFormats",
 componentPropertyResolver.get(MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS, String[].class));
boolean mediaFormatsMandatory = cfg.get("mediaFormatsMandatory",
 componentPropertyResolver.get(MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS_MANDATORY, false));
if (mediaFormats != null) {
  Map<String,Object> dataProps = new HashMap<>();
  dataProps.put("wcmio-mediaformats", StringUtils.join(mediaFormats, ","));
  dataProps.put("wcmio-mediaformats-mandatory", mediaFormatsMandatory);
  GraniteUiSyntheticResource.child(fileUpload, "granite:data", null, new ValueMapDecorator(dataProps));
}

// render original fileupload widget
RequestDispatcherOptions options = new RequestDispatcherOptions();
options.setForceResourceType("/libs/cq/gui/components/authoring/dialog/fileupload");
RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(fileUpload, options);
dispatcher.include(slingRequest, slingResponse);

// add pathfield widget
if (cfg.get("showPathfield", true)) {
  Map<String,Object> pathFieldProps = new HashMap<>();
  pathFieldProps.put("name", fileUploadProps.get("fileReferenceParameter"));
  pathFieldProps.put("rootPath", cfg.get("rootPath", "/content/dam"));
  pathFieldProps.put("granite:class", "wcmio-handler-media-fileupload-pathfield");
  Resource pathField = GraniteUiSyntheticResource.child(fileUpload, "pathfield" ,
      "granite/ui/components/coral/foundation/form/pathfield", new ValueMapDecorator(pathFieldProps));
  
  dispatcher = slingRequest.getRequestDispatcher(pathField);
  dispatcher.include(slingRequest, slingResponse);
}
%>
