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
<%@page import="com.adobe.granite.ui.components.ExpressionHelper"%>
<%@page import="io.wcm.handler.media.MediaNameConstants"%>
<%@page import="io.wcm.handler.media.MediaComponentPropertyResolver"%>
<%@page import="io.wcm.handler.media.spi.MediaHandlerConfig"%>
<%@page import="io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource"%>
<%@page import="io.wcm.wcm.ui.granite.util.GraniteUi"%>
<%@include file="../../global/global.jsp" %>
<%@include file="../../global/damRootPathDetection.jsp" %>
<%@include file="../fileupload/mediaFormatSupport.jsp" %><%--###

wcm.io Media Handler PathField
==============================

A field that allows the user to enter path.

It extends `/apps/wcm-io/wcm/ui/granite/components/form/pathfield` component.

It supports the same properties as it's super component. The following properties
are overwritten or added.

.. gnd:gnd::

  /**
   * The name that identifies the file upload location. E.g. ./file or ./image/file
   */
  - name (String) = {default value configured in media handler for media reference}

  /**
   * The path of the root of the pathfield.
   */
  - rootPath (StringEL) = '/content/dam', depending on media handler configuration

  /**
   * When the field description is not set, it is set automatically with an information about the
   * supported media formats.
   */
  - fieldDescription (String) = {media format information}

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


###--%><%

Config cfg = cmp.getConfig();
ExpressionHelper ex = cmp.getExpressionHelper();

// get default values for media ref properties as configured for media handler
String propNameDefault = "./fileReference";
String damRootPath = getDamRootPath(slingRequest, "/content/dam");
Resource contentResource = GraniteUi.getContentResourceOrParent(request);
if (contentResource != null) {
  MediaHandlerConfig mediaHandlerConfig = contentResource.adaptTo(MediaHandlerConfig.class);
  propNameDefault = "./" + mediaHandlerConfig.getMediaRefProperty();
}

Map<String,Object> pathFieldProps = new HashMap<>();
pathFieldProps.put("name", cfg.get("name", propNameDefault));
pathFieldProps.put("rootPath", ex.getString(cfg.get("rootPath", damRootPath)));
pathFieldProps.put("granite:class", "cq-FileUpload cq-droptarget wcm-io-handler-media-pathfield");

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
  String mediaFormatsFieldDescription = buildMediaFormatsFieldDescription(mediaFormats, contentResource);
  if (mediaFormatsFieldDescription != null) {
    String fieldDescription = cfg.get("fieldDescription", mediaFormatsFieldDescription);
    if (StringUtils.isBlank(fieldDescription)) {
      fieldDescription = null;
    }
    pathFieldProps.put("fieldDescription", fieldDescription);
  }
}

// simulate resource for dialog field def with updated properties
Resource pathField = GraniteUiSyntheticResource.wrapMerge(resource, new ValueMapDecorator(pathFieldProps));
if (mediaFormats != null && mediaFormats.length > 0) {
  Map<String,Object> dataProps = new HashMap<>();
  dataProps.put("wcmio-mediaformats", StringUtils.join(mediaFormats, ","));
  if (mediaFormatsMandatory != null && mediaFormatsMandatory.length > 0) {
    dataProps.put("wcmio-mediaformats-mandatory", StringUtils.join(mediaFormatsMandatory, ","));
  }
  dataProps.put("wcmio-media-cropauto", mediaCropAuto);
  GraniteUiSyntheticResource.child(pathField, "granite:data", null, new ValueMapDecorator(dataProps));
}

// render original component
RequestDispatcherOptions options = new RequestDispatcherOptions();
options.setForceResourceType("wcm-io/wcm/ui/granite/components/form/pathfield");
RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(pathField, options);
dispatcher.include(slingRequest, slingResponse);

%>
