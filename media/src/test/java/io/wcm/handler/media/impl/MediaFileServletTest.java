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

import static org.junit.Assert.assertEquals;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.commons.contenttype.ContentType;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MediaFileServletTest {

  private static final long EXPECTED_CONTENT_LENGTH = 15471;

  @Rule
  public AemContext context = AppAemContext.newAemContext();

  private MediaFileServlet underTest;

  @Before
  public void setUp() {
    underTest = new MediaFileServlet();
    context.currentResource(context.load().binaryFile("/sample_image_215x102.jpg", "/content/sample_image.jpg"));
  }

  @Test
  public void testGet() throws Exception {
    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals(ContentType.JPEG, context.response().getContentType());
    assertEquals(EXPECTED_CONTENT_LENGTH, context.response().getOutput().length);
    assertEquals(EXPECTED_CONTENT_LENGTH, context.response().getContentLength());
  }

  @Test
  public void testGet_Download() throws Exception {
    context.requestPathInfo().setSelectorString(AbstractMediaFileServlet.SELECTOR_DOWNLOAD);

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals(ContentType.DOWNLOAD, context.response().getContentType());
    assertEquals(EXPECTED_CONTENT_LENGTH, context.response().getOutput().length);
    assertEquals(EXPECTED_CONTENT_LENGTH, context.response().getContentLength());
    assertEquals("attachment;", context.response().getHeader(AbstractMediaFileServlet.HEADER_CONTENT_DISPOSITION));
  }

  @Test
  public void testGet_Download_Suffix() throws Exception {
    context.requestPathInfo().setSelectorString(AbstractMediaFileServlet.SELECTOR_DOWNLOAD);
    context.requestPathInfo().setSuffix("sample_image.jpg");

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals(ContentType.DOWNLOAD, context.response().getContentType());
    assertEquals(EXPECTED_CONTENT_LENGTH, context.response().getOutput().length);
    assertEquals(EXPECTED_CONTENT_LENGTH, context.response().getContentLength());
    assertEquals("attachment;filename=\"sample_image.jpg\"", context.response().getHeader(AbstractMediaFileServlet.HEADER_CONTENT_DISPOSITION));
  }

  @Test
  public void testGet_NoResource() throws Exception {
    context.currentResource((Resource)null);

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_NOT_FOUND, context.response().getStatus());
  }

  @Test
  public void testGet_NoBinaryDataResource() throws Exception {
    context.currentResource(context.create().resource("/content/nobinarydata"));

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_NOT_FOUND, context.response().getStatus());
  }

}
