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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.image.Layer;

public class ImageFileServletTest {

  @Rule
  public AemContext context = AppAemContext.newAemContext();

  private ImageFileServlet underTest;

  @Before
  public void setUp() {
    underTest = new ImageFileServlet();
    context.currentResource(context.load().binaryFile("/sample_image_215x102.jpg", "/content/sample_image.jpg"));
  }

  @Test
  public void testGet_NoSelector() throws Exception {
    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_NOT_FOUND, context.response().getStatus());
  }

  @Test
  public void testGet() throws Exception {
    context.requestPathInfo().setSelectorString("image_file.215.102");

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals(ContentType.JPEG, context.response().getContentType());
    assertResponseLayerSize(215, 102);
  }

  @Test
  public void testGet_Cropping() throws Exception {
    context.requestPathInfo().setSelectorString("image_file.215.102.10,10,20,25");

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals(ContentType.JPEG, context.response().getContentType());
    assertResponseLayerSize(10, 15);
  }

  @Test
  public void testGet_Cropping_InvalidSyntax() throws Exception {
    context.requestPathInfo().setSelectorString("image_file.215.102.10,10");

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals(ContentType.JPEG, context.response().getContentType());
    assertResponseLayerSize(215, 102);
  }

  @Test
  public void testGet_SizeTooLarge() throws Exception {
    context.requestPathInfo().setSelectorString("image_file.2150.1020");

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals(ContentType.JPEG, context.response().getContentType());
    assertResponseLayerSize(215, 102);
  }

  @Test
  public void testGet_RenderToPng() throws Exception {
    context.requestPathInfo().setSelectorString("image_file.215.102");
    context.requestPathInfo().setSuffix("image.png");

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals(ContentType.PNG, context.response().getContentType());
    assertResponseLayerSize(215, 102);
  }

  @Test
  public void testGetImageFileNameJpeg() {
    assertEquals("myimage.jpg", ImageFileServlet.getImageFileName("myimage.jpg"));
    assertEquals("myimage.jpg", ImageFileServlet.getImageFileName("myimage.jpeg"));
  }

  @Test
  public void testGetImageFileNamePng() {
    assertEquals("myImage.png", ImageFileServlet.getImageFileName("myImage.Png"));
  }

  @Test
  public void testGetImageFileNameOther() {
    assertEquals("myimage.jpg", ImageFileServlet.getImageFileName("myimage.gif"));
  }

  private void assertResponseLayerSize(long width, long height) throws IOException {
    InputStream is = new ByteArrayInputStream(context.response().getOutput());
    Layer layer = new Layer(is);
    is.close();
    assertEquals(width, layer.getWidth());
    assertEquals(height, layer.getHeight());
  }

}
