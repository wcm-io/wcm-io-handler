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
import io.wcm.handler.url.suffix.SuffixBuilder;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.commons.contenttype.FileExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.image.Layer;

public class DummyImageServletTest {

  @Rule
  public AemContext context = AppAemContext.newAemContext();

  private DummyImageServlet underTest;

  @Before
  public void setUp() {
    underTest = new DummyImageServlet();
    context.requestPathInfo().setExtension(FileExtension.PNG);
  }

  @Test
  public void testGet_NoSuffix() throws Exception {
    underTest.service(context.request(), context.response());
    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertResponseLayerDimension(1, 1);
  }

  @Test
  public void testGet_WidthHeight() throws Exception {
    context.requestPathInfo().setSuffix(new SuffixBuilder()
    .put(DummyImageServlet.SUFFIX_WIDTH, 100)
    .put(DummyImageServlet.SUFFIX_HEIGHT, 50)
    .build());

    underTest.service(context.request(), context.response());
    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertResponseLayerDimension(100, 50);
  }

  @Test
  public void testGet_WidthHeightName() throws Exception {
    context.requestPathInfo().setSuffix(new SuffixBuilder()
    .put(DummyImageServlet.SUFFIX_WIDTH, 100)
    .put(DummyImageServlet.SUFFIX_HEIGHT, 50)
    .put(DummyImageServlet.SUFFIX_MEDIA_FORMAT_NAME, "myName")
    .build());

    underTest.service(context.request(), context.response());
    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertResponseLayerDimension(100, 50);
  }

  private void assertResponseLayerDimension(int width, int height) throws IOException {
    try (InputStream is = new ByteArrayInputStream(context.response().getOutput())) {
      Layer layer = new Layer(is);
      assertEquals(width, layer.getWidth());
      assertEquals(height, layer.getHeight());
    }
  }

}
