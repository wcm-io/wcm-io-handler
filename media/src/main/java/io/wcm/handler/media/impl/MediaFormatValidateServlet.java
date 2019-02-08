/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;

import com.day.cq.i18n.I18n;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.request.RequestParam;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Validates if the given asset references matches with the given
 * set of media formats. This is used for the File Upload Granite UI component.
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
    extensions = FileExtension.JSON,
    selectors = MediaFormatValidateServlet.SELECTOR,
    resourceTypes = "sling/servlet/default",
    methods = HttpConstants.METHOD_GET)
public final class MediaFormatValidateServlet extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  static final String SELECTOR = "wcm-io-handler-media-mediaformat-validate";

  static final String RP_MEDIA_FORMATS = "mediaFormats";
  static final String RP_MEDIA_FORMATS_MANDATORY = "mediaFormatsMandatory";
  static final String RP_MEDIA_CROPAUTO = "mediaCropAuto";
  static final String RP_MEDIA_REF = "mediaRef";

  /**
   * Prefix for i18n keys to generated messages for media invalid reasons.
   */
  public static final String MEDIA_INVALID_REASON_I18N_PREFIX = "io.wcm.handler.media.invalidReason.";

  @Override
  protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {

    // read and validated request parameters
    String[] mediaFormats = StringUtils.split(RequestParam.get(request, RP_MEDIA_FORMATS), ",");
    boolean mediaFormatsMandatory = RequestParam.getBoolean(request, RP_MEDIA_FORMATS_MANDATORY);
    boolean mediaCropAuto = RequestParam.getBoolean(request, RP_MEDIA_CROPAUTO);
    String mediaRef = RequestParam.get(request, RP_MEDIA_REF);

    if (mediaFormats == null || mediaFormats.length == 0
        || StringUtils.isEmpty(mediaRef)) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // try to resolve media
    MediaHandler mediaHandler = AdaptTo.notNull(request, MediaHandler.class);
    Media media = mediaHandler.get(mediaRef)
        .mediaFormatNames(mediaFormats)
        .mediaFormatsMandatory(mediaFormatsMandatory)
        .autoCrop(mediaCropAuto)
        .build();

    // response
    try {
      JSONObject result = new JSONObject();
      result.put("valid", media.isValid());
      if (!media.isValid()) {
        result.put("reason", getMediaInvalidReasonText(media.getMediaInvalidReason(), request));
      }
      response.setContentType(ContentType.JSON);
      response.getWriter().write(result.toString());
    }
    catch (JSONException ex) {
      throw new ServletException(ex);
    }
  }

  private String getMediaInvalidReasonText(MediaInvalidReason mediaInvalidReason, SlingHttpServletRequest request) {
    I18n i18n = getI18n(request);
    try {
      return i18n.get(MEDIA_INVALID_REASON_I18N_PREFIX + mediaInvalidReason.name());
    }
    catch (MissingResourceException ex) {
      return mediaInvalidReason.name();
    }
  }

  private I18n getI18n(SlingHttpServletRequest request) {
    PageManager pageManager = AdaptTo.notNull(request.getResourceResolver(), PageManager.class);
    Page currentPage = pageManager.getContainingPage(request.getResource());
    if (currentPage != null) {
      Locale locale = currentPage.getLanguage(false);
      ResourceBundle resourceBundle = request.getResourceBundle(locale);
      return new I18n(resourceBundle);
    }
    return new I18n(request);
  }

}
