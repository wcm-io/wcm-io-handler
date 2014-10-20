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
package io.wcm.handler.media.impl;

import io.wcm.handler.commons.jcr.JcrBinary;
import io.wcm.sling.commons.request.RequestPath;
import io.wcm.wcm.commons.caching.CacheHeader;
import io.wcm.wcm.commons.contenttype.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

/**
 * Stream binary data stored in a nt:file or nt:resource node.
 * Optional support for Content-Disposition header ("download_attachment").
 */
abstract class AbstractMediaFileServlet extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Selector for forcing a "save-as" dialog in the browser
   */
  public static final String SELECTOR_DOWNLOAD = "download_attachment";

  /**
   * Content disposition header
   */
  public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

    // get binary data resource
    Resource resource = getBinaryDataResource(request);
    if (resource == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // check if the resource was modified since last request
    if (isNotModified(resource, request, response)) {
      return;
    }

    // get binary data and send to client
    byte[] binaryData = getBinaryData(resource, request);
    if (binaryData == null || binaryData.length == 0) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    else {
      String contentType = getContentType(resource, request);
      sendBinaryData(binaryData, contentType, request, response);
    }

  }

  /**
   * Get resource containing the binary data to deliver.
   * @param request Request
   * @return Resource pointing to nt:file or nt:resource node
   */
  protected Resource getBinaryDataResource(SlingHttpServletRequest request) {
    return request.getResource();
  }

  /**
   * Checks if the resource was modified since last request
   * @param resource Resource pointing to nt:file or nt:resource node
   * @param request Request
   * @param response Response
   * @return true if the resource is not modified and should not be delivered anew
   * @throws IOException
   */
  protected boolean isNotModified(Resource resource, SlingHttpServletRequest request,
      SlingHttpServletResponse response) throws IOException {
    // check resource's modification date against the If-Modified-Since header and send 304 if resource wasn't modified
    // never send expires header on author or publish instance (performance optimization - if medialib items changes
    // users have to refresh browsers cache)
    return CacheHeader.isNotModified(resource, request, response, false);
  }

  /**
   * Get binary data from the referenced nt:file or nt:resourcer resource.
   * @param resource Resource
   * @return Binary data or null if not binary data found
   * @throws IOException
   */
  protected byte[] getBinaryData(Resource resource, SlingHttpServletRequest request) throws IOException {
    InputStream is = resource.adaptTo(InputStream.class);
    if (is == null) {
      return null;
    }
    try {
      return IOUtils.toByteArray(is);
    }
    finally {
      is.close();
    }
  }

  /**
   * Get content type from the reference nt:file or nt:resourcer resource.
   * @param resource Resource
   * @return Content type (never null)
   */
  protected String getContentType(Resource resource, SlingHttpServletRequest request) {
    String mimeType = JcrBinary.getMimeType(resource);
    if (StringUtils.isEmpty(mimeType)) {
      mimeType = ContentType.OCTET_STREAM;
    }
    return mimeType;
  }

  /**
   * Send binary data to output stream. Respect optional content disposition header handling.
   * @param binaryData Binary data array.
   * @param contentType Content type
   * @param request Request
   * @param response Response
   * @throws IOException
   */
  protected void sendBinaryData(byte[] binaryData, String contentType,
      SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

    // set content type and length
    response.setContentType(contentType);
    response.setContentLength(binaryData.length);

    // Handling of the "force download" selector
    if (RequestPath.hasSelector(request, SELECTOR_DOWNLOAD)) {
      // Overwrite MIME type with one suited for downloads
      response.setContentType(ContentType.DOWNLOAD);

      // Construct disposition header
      StringBuilder dispositionHeader = new StringBuilder("attachment;");
      String suffix = request.getRequestPathInfo().getSuffix();
      suffix = StringUtils.stripStart(suffix, "/");
      if (StringUtils.isNotEmpty(suffix)) {
        dispositionHeader.append("filename=\"").append(suffix).append('\"');
      }

      response.setHeader(HEADER_CONTENT_DISPOSITION, dispositionHeader.toString());
    }

    // write binary data
    OutputStream out = response.getOutputStream();
    out.write(binaryData);
    out.flush();

  }

}
