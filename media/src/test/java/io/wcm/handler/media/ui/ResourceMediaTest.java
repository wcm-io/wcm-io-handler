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
package io.wcm.handler.media.ui;

import static io.wcm.handler.media.testcontext.AppAemContext.ROOTPATH_CONTENT;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_2COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CAMPAIGN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.commons.contenttype.ContentType;

@SuppressWarnings("null")
public class ResourceMediaTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  private Asset asset;

  @Before
  public void setUp() {
    asset = context.create().asset("/content/dam/asset1.jpg",
        (int)EDITORIAL_2COL.getWidth(), (int)EDITORIAL_2COL.getHeight(), ContentType.JPEG);

    Resource resource = context.create().resource(ROOTPATH_CONTENT + "/jcr:content/media",
        ImmutableValueMap.of(MediaNameConstants.PN_MEDIA_REF, asset.getPath()));

    context.currentResource(resource);
  }

  @Test
  public void testWithValidMediaFormat() {
    context.request().setAttribute("mediaFormat", EDITORIAL_1COL.getName());

    ResourceMedia underTest = context.request().adaptTo(ResourceMedia.class);
    assertTrue(underTest.isValid());
    assertEquals("/content/dam/asset1.jpg/_jcr_content/renditions/original.image_file.215.102.file/asset1.jpg",
        underTest.getMetadata().getUrl());
  }

  @Test
  public void testWithInvalidMediaFormat() {
    context.request().setAttribute("mediaFormat", SHOWROOM_CAMPAIGN.getName());

    ResourceMedia underTest = context.request().adaptTo(ResourceMedia.class);
    assertFalse(underTest.isValid());
  }

  @Test
  public void testWithoutMediaFormat() {
    ResourceMedia underTest = context.request().adaptTo(ResourceMedia.class);
    assertTrue(underTest.isValid());
    assertEquals("/content/dam/asset1.jpg/_jcr_content/renditions/original./asset1.jpg",
        underTest.getMetadata().getUrl());
  }

  @Test
  public void testWithCss() {
    context.request().setAttribute("cssClass", "mycss");

    ResourceMedia underTest = context.request().adaptTo(ResourceMedia.class);
    assertTrue(underTest.isValid());
    assertEquals("mycss", underTest.getMetadata().getElement().getCssClass());
  }

  @Test
  public void testWithRefCropProperty() {
    context.request().setAttribute("mediaFormat", EDITORIAL_2COL.getName());
    context.request().setAttribute("refProperty", "myRefProp");
    context.request().setAttribute("cropProperty", "myCropProp");

    Resource resource2 = context.create().resource(ROOTPATH_CONTENT + "/jcr:content/media2",
        ImmutableValueMap.of("myRefProp", asset.getPath()));
    context.currentResource(resource2);

    ResourceMedia underTest = context.request().adaptTo(ResourceMedia.class);
    assertTrue(underTest.isValid());
    assertEquals("/content/dam/asset1.jpg/_jcr_content/renditions/original./asset1.jpg",
        underTest.getMetadata().getUrl());
  }

}
