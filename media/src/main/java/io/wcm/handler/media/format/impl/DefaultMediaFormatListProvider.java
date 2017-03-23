/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.handler.media.format.impl;

import java.io.IOException;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import com.google.common.collect.ImmutableSet;

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatHandler;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Exports the list of media format available for the addressed media path in JSON format to the response.
 */
@Component(service = Servlet.class, immediate = true, property = {
    "sling.servlet.extensions=" + FileExtension.JSON,
    "sling.servlet.selectors=wcmio_handler_media_mediaformat_list",
    "sling.servlet.resourceTypes=sling/servlet/default",
    "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public final class DefaultMediaFormatListProvider extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    try {

      // get list of media formats for current medialib path
      Set<MediaFormat> mediaFormats = getMediaFormats(request);

      response.setContentType(ContentType.JSON);

      JSONArray mediaFormatList = new JSONArray();

      if (mediaFormats != null) {
        for (MediaFormat mediaFormat : mediaFormats) {
          if (!mediaFormat.isInternal()) {
            JSONObject mediaFormatItem = new JSONObject();
            mediaFormatItem.put("name", mediaFormat.getName());
            mediaFormatItem.put("text", mediaFormat.toString());
            mediaFormatItem.put("width", mediaFormat.getWidth());
            mediaFormatItem.put("height", mediaFormat.getHeight());
            mediaFormatItem.put("widthMin", mediaFormat.getMinWidth());
            mediaFormatItem.put("heightMin", mediaFormat.getMinHeight());
            mediaFormatItem.put("isImage", mediaFormat.isImage());
            mediaFormatItem.put("ratioWidth", mediaFormat.getRatioWidth());
            mediaFormatItem.put("ratioHeight", mediaFormat.getRatioHeight());
            mediaFormatList.put(mediaFormatItem);
          }
        }
      }

      response.getWriter().write(mediaFormatList.toString());
    }
    catch (JSONException ex) {
      throw new ServletException(ex);
    }
  }

  protected Set<MediaFormat> getMediaFormats(SlingHttpServletRequest request) {
    MediaFormatHandler mediaFormatHandler = request.adaptTo(MediaFormatHandler.class);
    if (mediaFormatHandler != null) {
      return mediaFormatHandler.getMediaFormats();
    }
    else {
      return ImmutableSet.of();
    }
  }

}
