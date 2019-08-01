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
<%@page import="com.adobe.granite.ui.components.ExpressionHelper"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="io.wcm.handler.media.format.MediaFormat"%>
<%@page import="io.wcm.handler.media.format.MediaFormatHandler"%>
<%@page import="io.wcm.wcm.commons.component.ComponentPropertyResolver"%>
<%!

static String buildMediaFormatsFieldDescription(String[] mediaFormats, Resource resource) {
  if (mediaFormats == null || mediaFormats.length == 0) {
    return null;
  }

  List<String> mediaFormatDescriptions = new ArrayList<>();
  MediaFormatHandler mediaFormatHandler = resource.adaptTo(MediaFormatHandler.class);
  for (String mediaFormatName : mediaFormats) {
    MediaFormat mediaFormat = mediaFormatHandler.getMediaFormat(mediaFormatName);
    if (mediaFormat != null) {
      mediaFormatDescriptions.add(mediaFormat.toString());
    }
  }
  if (mediaFormatDescriptions.isEmpty()) {
    return null;
  }

  String fieldDescription;
  if (mediaFormatDescriptions.size() == 1) {
    fieldDescription = "Media format: ";
  }
  else {
    fieldDescription = "Media formats: ";
  }
  fieldDescription += StringUtils.join(mediaFormatDescriptions, ", ");
  return fieldDescription;
}

static String[] getStringArrayWithExpressionSupport(String propertyName, String componentPropertyName,
    Config cfg, ExpressionHelper ex, ComponentPropertyResolver componentPropertyResolver) {
  String[] result = null;

  Object value = cfg.get(propertyName, (Object)null);
  if (value instanceof String) {
    // try to resolve as expression
    value = ex.get((String)value, Object.class);
    if ((value instanceof String) && StringUtils.isNotEmpty((String)value)) {
      result = new String[] { (String)value };
    }
    else if (value instanceof String[]) {
      result = (String[])value;
    }
  }
  else {
    // try to get directly from config
    result = cfg.get(propertyName, String[].class);
  }

  // fallback to component properties
  if (result == null) {
    result = componentPropertyResolver.get(componentPropertyName, String[].class);
  }

  return result;
}

static boolean getBooleanWithExpressionSupport(String propertyName, String componentPropertyName,
    Config cfg, ExpressionHelper ex, ComponentPropertyResolver componentPropertyResolver) {
  String[] mediaFormats = null;

  Boolean result = null;
  Object value = cfg.get(propertyName, (Object)null);
  if ((value instanceof String) && StringUtils.isNotEmpty((String)value)) {
    // try to resolve as expression
    result = ex.get((String)value, Boolean.class);
  }  

  // try to get directly from config, fallback to component properties
  if (result == null) {
    result = cfg.get(propertyName, componentPropertyResolver.get(componentPropertyName, false));
  }

  return result;
}

%>
