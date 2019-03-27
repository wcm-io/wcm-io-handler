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

import static io.wcm.handler.media.impl.MediaFormatValidateServlet.MEDIA_INVALID_REASON_I18N_PREFIX;
import static io.wcm.handler.media.impl.MediaFormatValidateServlet.RP_MEDIA_FORMATS;
import static io.wcm.handler.media.impl.MediaFormatValidateServlet.RP_MEDIA_REF;
import static io.wcm.handler.media.testcontext.AppAemContext.ROOTPATH_CONTENT;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.commons.json.JSONObject;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.day.cq.dam.api.Asset;
import com.google.common.collect.ImmutableMap;

import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.wcm.commons.contenttype.ContentType;

public class MediaFormatValidateServletTest {

  @Rule
  public AemContext context = AppAemContext.newAemContext();

  private MediaFormatValidateServlet underTest;

  @BeforeEach
  public void setUp() {
    underTest = context.registerInjectActivateService(new MediaFormatValidateServlet());
    context.currentPage(context.create().page(ROOTPATH_CONTENT));
  }

  @Test
  public void testValid() throws Exception {
    Asset asset = context.create().asset("/content/dam/sample.jpg",
        (int)EDITORIAL_1COL.getWidth(),
        (int)EDITORIAL_1COL.getHeight(),
        ContentType.JPEG);

    context.request().setParameterMap(ImmutableMap.of(
        RP_MEDIA_FORMATS, EDITORIAL_1COL.getName(),
        RP_MEDIA_REF, asset.getPath()));
    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    JSONObject result = new JSONObject(context.response().getOutputAsString());
    assertEquals(true, result.get("valid"));
    assertNull(result.opt("reason"));
  }

  @Test
  public void testInvalid() throws Exception {
    Asset asset = context.create().asset("/content/dam/sample.jpg",
        10,
        10,
        ContentType.JPEG);

    context.request().setParameterMap(ImmutableMap.of(
        RP_MEDIA_FORMATS, EDITORIAL_1COL.getName(),
        RP_MEDIA_REF, asset.getPath()));
    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    JSONObject result = new JSONObject(context.response().getOutputAsString());
    assertEquals(false, result.get("valid"));
    assertEquals(MEDIA_INVALID_REASON_I18N_PREFIX + MediaInvalidReason.NO_MATCHING_RENDITION.name(),
        result.get("reason"));
  }

}
