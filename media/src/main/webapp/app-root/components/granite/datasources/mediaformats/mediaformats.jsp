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
<%@page import="com.adobe.granite.ui.components.ds.DataSource"%>
<%@page import="com.adobe.granite.ui.components.ds.SimpleDataSource"%>
<%@page import="com.adobe.granite.ui.components.ds.ValueMapResource"%>
<%@page import="com.day.cq.commons.jcr.JcrConstants"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.SortedMap"%>
<%@page import="java.util.SortedSet"%>
<%@page import="java.util.TreeSet"%>
<%@page import="io.wcm.handler.media.format.MediaFormat"%>
<%@page import="io.wcm.handler.media.format.MediaFormatHandler"%>
<%@page import="io.wcm.handler.media.format.MediaFormatProviderManager"%>
<%@page import="io.wcm.wcm.ui.granite.util.GraniteUi"%>
<%@page import="org.apache.commons.collections.Transformer"%>
<%@page import="org.apache.commons.collections.iterators.TransformIterator"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="org.apache.sling.api.resource.ResourceResolver"%>
<%@page import="org.apache.sling.api.resource.ResourceMetadata"%>
<%@page import="org.apache.sling.api.resource.ValueMap"%>
<%@page import="org.apache.sling.api.wrappers.ValueMapDecorator"%>
<%@include file="../../global/global.jsp" %>
<%
/**
  A datasource returning all media formats for the current context context path.

  @datasource
  @name Media Formats
  @location /apps/wcm-io/handler/media/components/granite/datasources/mediaformats
 */

// try to fetch media formats matching for current content resource
Resource contentResource = GraniteUi.getContentResourceOrParent(request);
SortedSet<MediaFormat> mediaFormats = null;
if (contentResource != null) {
  MediaFormatHandler mediaFormatHandler = contentResource.adaptTo(MediaFormatHandler.class);
  mediaFormats = mediaFormatHandler.getMediaFormats();
  if (mediaFormats.size() == 0) {
    mediaFormats = null;
  }
}

// if none found display all media formats of all bundles deployed on this instance
if (mediaFormats == null) {
  mediaFormats = new TreeSet<>();
  MediaFormatProviderManager mediaFormatProviderManager = sling.getService(MediaFormatProviderManager.class);
  SortedMap<String, SortedSet<MediaFormat>> groupedMediaFormats = mediaFormatProviderManager.getAllMediaFormats();
  for (Map.Entry<String, SortedSet<MediaFormat>> entry : groupedMediaFormats.entrySet()) {
    String bundleName = entry.getKey();
    for (MediaFormat mf : entry.getValue()) {
      if (!mediaFormats.contains(mf)) {
        mediaFormats.add(mf);
      }
    }
  }
}

final ResourceResolver resolver = resourceResolver;
DataSource ds = new SimpleDataSource(new TransformIterator(mediaFormats.iterator(), new Transformer() {
  public Object transform(Object obj) {
    MediaFormat mediaFormat = (MediaFormat)obj;
    ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());
    vm.put("value", mediaFormat.getName());
    vm.put("text", mediaFormat.toString());
    return new ValueMapResource(resolver, new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, vm);
  }
}));

request.setAttribute(DataSource.class.getName(), ds);
%>