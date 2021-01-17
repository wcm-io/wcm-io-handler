/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
class InlineImageAuthorPreviewServletTest {

  private static final long EXPECTED_CONTENT_LENGTH = 15471;

  private final AemContext context = AppAemContext.newAemContext();

  private InlineImageAuthorPreviewServlet underTest;

  @BeforeEach
  void setUp() {
    underTest = new InlineImageAuthorPreviewServlet();
  }

  @Test
  void testGet() throws Exception {
    Resource componentResource = context.create().resource("/content/myresource",
        "sling:resourceType", "app1/components/myresource");
    context.load().binaryFile("/sample_image_215x102.jpg", componentResource.getPath() + "/" + MediaNameConstants.NN_MEDIA_INLINE);
    context.currentResource(componentResource);

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals(ContentType.JPEG, context.response().getContentType());
    assertEquals(EXPECTED_CONTENT_LENGTH, context.response().getOutput().length);
    assertEquals(EXPECTED_CONTENT_LENGTH, context.response().getContentLength());
  }

  @Test
  void testGet_NoBinaryDataResource() throws Exception {
    Resource componentResource = context.create().resource("/content/myresource",
        "sling:resourceType", "app1/components/myresource");
    context.currentResource(componentResource);

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_NOT_FOUND, context.response().getStatus());
  }

}
