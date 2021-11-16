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
<%@page import="org.apache.sling.api.resource.ResourceUtil"%>
<%@page import="org.apache.sling.api.resource.ValueMap"%>
<%@page import="org.apache.sling.api.request.RequestDispatcherOptions"%>
<%@page import="org.apache.sling.api.wrappers.ValueMapDecorator"%>
<%@page import="com.adobe.granite.ui.components.Config"%>
<%@page import="com.adobe.granite.ui.components.ExpressionHelper"%>
<%@page import="io.wcm.handler.media.MediaNameConstants"%>
<%@page import="io.wcm.handler.media.MediaComponentPropertyResolver"%>
<%@page import="io.wcm.handler.media.spi.MediaHandlerConfig"%>
<%@page import="io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource"%>
<%@page import="io.wcm.wcm.ui.granite.util.GraniteUi"%>
<%@include file="../../global/global.jsp" %>
<%@include file="../../global/damRootPathDetection.jsp" %>
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
   * Prefix for all property names in the this component.
   * Can be used to store the properties in another resource by setting e.g. to "./mySubNode/".
   * Property value is ignored for properties name, fileNameParameter or fileReferenceParameter if those are set explicitly.
   */
  - namePrefix (String) = "./"

  /**
   * The browse and selection filter for file selection. E.g. [".png",".jpg"] or ["image/\*"].
   */
  - mimeTypes (String) multiple = ["image/gif","image/jpeg","image/png","image/tiff","image/svg+xml"]

  /**
   * Indicates whether upload from local file system is allowed.
   */
  - allowUpload (BooleanEL) = 'false'

  /**
   * When the field description is not set, it is set automatically with an information about the
   * supported media formats.
   */
  - fieldDescription (String) = {media format information}

  /**
   * The path of the root of the pathfield.
   */
  - rootPath (StringEL) = {root path from media handler config}

  /**
   * The root path that is used as fallback when no root path could be detected dynamically,
   * e.g. because outside any site or within experience fragments.
   */
  - fallbackRootPath (StringEL) = "/content/dam"

  /**
   * Appendix path added to the (usually auto-detected) root path.
   */
  - appendPath (StringEL) = {path appendix}

  /**
   * List of media formats required by this component.
   * If not set the property value is looked up from component properties or policy.
   */
  - mediaFormats (String[]/StringEL)

  /**
   * List of mandatory media formats required by this component.
   * If not set the property value is looked up from component properties or policy.
   */
  - mediaFormatsMandatory (String[]/StringEL)

  /**
   * Enables "auto-cropping" mode.
   * If not set the property value is looked up from component properties or policy.
   */
  - mediaCropAuto (BooleanEL) = 'false'

  /**
   * Property is mandatory.
   */
  - required (Boolean) = 'false'


###--%><%

Config cfg = cmp.getConfig();
ExpressionHelper ex = cmp.getExpressionHelper();

// get default values for media ref properties as configured for media handler
String namePrefix = cfg.get("namePrefix", "./");
String propNameDefault = namePrefix + "file";
String propFileNameDefault = namePrefix + "fileName";
String propFileReferenceDefault = namePrefix + "fileReference";
Resource contentResource = GraniteUi.getContentResourceOrParent(request);
boolean hasTransformation = false;
if (contentResource != null) {
  MediaHandlerConfig mediaHandlerConfig = contentResource.adaptTo(MediaHandlerConfig.class);
  propNameDefault = namePrefix + mediaHandlerConfig.getMediaInlineNodeName();
  propFileNameDefault = namePrefix + mediaHandlerConfig.getMediaInlineNodeName() + "Name";
  propFileReferenceDefault = namePrefix + mediaHandlerConfig.getMediaRefProperty();

  // check if any transformations are defined
  ValueMap contentProps = contentResource.getValueMap();
  hasTransformation = (contentProps.get(mediaHandlerConfig.getMediaCropProperty(), String.class) != null)
      || (contentProps.get(mediaHandlerConfig.getMediaRotationProperty(), String.class) != null)
      || (contentProps.get(mediaHandlerConfig.getMediaMapProperty(), String.class) != null);
}

Map<String,Object> fileUploadProps = new HashMap<>();
fileUploadProps.put("name", cfg.get("name", propNameDefault));
fileUploadProps.put("fileNameParameter", cfg.get("fileNameParameter", propFileNameDefault));
fileUploadProps.put("fileReferenceParameter", cfg.get("fileReferenceParameter", propFileReferenceDefault));

// default values for allowUpload and mimeTypes
fileUploadProps.put("allowUpload", ex.getBoolean(cfg.get("allowUpload", String.class)));
fileUploadProps.put("mimeTypes", cfg.get("mimeTypes", new String[] {
    "image/gif","image/jpeg","image/png","image/tiff","image/svg+xml" }));

// media format properties for validation of associated media reference
String[] mediaFormats = null;
String[] mediaFormatsMandatory = null;
boolean mediaCropAuto = false;
if (contentResource != null) {
  MediaComponentPropertyResolver componentPropertyResolver = new MediaComponentPropertyResolver(contentResource);
  mediaFormats = getStringArrayWithExpressionSupport("mediaFormats",
      MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS, cfg, ex, componentPropertyResolver.getMediaFormatNames());
  mediaFormatsMandatory = getStringArrayWithExpressionSupport("mediaFormatsMandatory",
      MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS_MANDATORY, cfg, ex, componentPropertyResolver.getMandatoryMediaFormatNames());
  mediaCropAuto = getBooleanWithExpressionSupport("mediaCropAuto",
      MediaNameConstants.PN_COMPONENT_MEDIA_AUTOCROP, cfg, ex, componentPropertyResolver.isAutoCrop());

  // add info about media formats in field description
  String mediaFormatsFieldDescription = buildMediaFormatsFieldDescription(mediaFormats, contentResource, i18n);
  if (mediaFormatsFieldDescription != null) {
   String fieldDescription = cfg.get("fieldDescription", mediaFormatsFieldDescription);
   if (StringUtils.isBlank(fieldDescription)) {
     fieldDescription = null;
   }
   fileUploadProps.put("fieldDescription", fieldDescription);
  }
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
pathFieldProps.put("namePrefix", namePrefix);
pathFieldProps.put("granite:class", "cq-FileUpload cq-droptarget wcm-io-handler-media-fileupload-pathfield " + cfg.get("granite:class", ""));
pathFieldProps.put("required", cfg.get("required", false));

// detect root path
pathFieldProps.putAll(getDamRootPathProperties(cmp, slingRequest, "/content/dam"));

Resource pathField = GraniteUiSyntheticResource.child(fileUpload, "pathfield" ,
    "wcm-io/wcm/ui/granite/components/form/pathfield", new ValueMapDecorator(pathFieldProps));
Map<String,Object> dataProps = new HashMap<>();
ValueMap dataFromConfig = ResourceUtil.getValueMap(cfg.getChild("granite:data"));
for (Map.Entry<String,Object> entry : dataFromConfig.entrySet()) {
  dataProps.put(entry.getKey(), entry.getValue());
}
if (mediaFormats != null && mediaFormats.length > 0) {
  dataProps.put("wcmio-mediaformats", StringUtils.join(mediaFormats, ","));
  if (mediaFormatsMandatory != null && mediaFormatsMandatory.length > 0) {
    dataProps.put("wcmio-mediaformats-mandatory", StringUtils.join(mediaFormatsMandatory, ","));
  }
  dataProps.put("wcmio-media-cropauto", mediaCropAuto);
}
if (hasTransformation) {
  dataProps.put("wcmio-media-hastransformation", hasTransformation);
}
if (!dataProps.isEmpty()) {
  GraniteUiSyntheticResource.child(pathField, "granite:data", null, new ValueMapDecorator(dataProps));
}

dispatcher = slingRequest.getRequestDispatcher(pathField);
dispatcher.include(slingRequest, slingResponse);

%>
