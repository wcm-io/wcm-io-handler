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

import static io.wcm.handler.media.MediaNameConstants.PN_MEDIA_REF;
import static io.wcm.handler.media.testcontext.AppAemContext.ROOTPATH_CONTENT;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_2COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CAMPAIGN;
import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jdom2.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class ResourceMediaTest {

  private final AemContext context = AppAemContext.newAemContext();

  private Asset asset;

  @BeforeEach
  void setUp() {
    asset = context.create().asset("/content/dam/asset1.jpg",
        (int)EDITORIAL_2COL.getWidth(), (int)EDITORIAL_2COL.getHeight(), ContentType.JPEG);

    Resource resource = context.create().resource(ROOTPATH_CONTENT + "/jcr:content/media",
        PROPERTY_RESOURCE_TYPE, "/dummy/resourcetype",
        PN_MEDIA_REF, asset.getPath());

    context.currentResource(resource);
  }

  @Test
  void testWithValidMediaFormat() {
    context.request().setAttribute("mediaFormat", EDITORIAL_1COL.getName());

    ResourceMedia underTest = context.request().adaptTo(ResourceMedia.class);
    assertTrue(underTest.isValid());
    assertEquals("/content/dam/asset1.jpg/_jcr_content/renditions/original.image_file.215.102.file/asset1.jpg",
        underTest.getMetadata().getUrl());
  }

  @Test
  void testWithInvalidMediaFormat() {
    context.request().setAttribute("mediaFormat", SHOWROOM_CAMPAIGN.getName());

    ResourceMedia underTest = context.request().adaptTo(ResourceMedia.class);
    assertFalse(underTest.isValid());
  }

  @Test
  void testWithoutMediaFormat() {
    ResourceMedia underTest = context.request().adaptTo(ResourceMedia.class);
    assertTrue(underTest.isValid());
    assertEquals("/content/dam/asset1.jpg/_jcr_content/renditions/original./asset1.jpg",
        underTest.getMetadata().getUrl());
  }

  @Test
  void testWithCss() {
    context.request().setAttribute("cssClass", "mycss");

    ResourceMedia underTest = context.request().adaptTo(ResourceMedia.class);
    assertTrue(underTest.isValid());
    assertEquals("mycss", underTest.getMetadata().getElement().getCssClass());
  }

  @Test
  void testWithRefCropProperty() {
    context.request().setAttribute("mediaFormat", RATIO.getName());
    context.request().setAttribute("refProperty", "myRefProp");
    context.request().setAttribute("cropProperty", "myCropProp");

    Asset asset2 = context.create().asset("/content/dam/asset2.jpg",
        160, 100, ContentType.JPEG);
    Resource resource2 = context.create().resource(ROOTPATH_CONTENT + "/jcr:content/media2",
        PROPERTY_RESOURCE_TYPE, "/dummy/resourcetype",
        "myRefProp", asset2.getPath(),
        "myCropProp", "20,12,140,88");
    context.currentResource(resource2);

    ResourceMedia underTest = context.request().adaptTo(ResourceMedia.class);
    assertTrue(underTest.isValid());
    assertEquals("/content/dam/asset2.jpg/_jcr_content/renditions/original.image_file.120.76.20,12,140,88.file/asset2.jpg",
        underTest.getMetadata().getUrl());

    context.request().removeAttribute("cropProperty");
    ResourceMedia underTest2 = context.request().adaptTo(ResourceMedia.class);
    assertTrue(underTest2.isValid());
    assertEquals("/content/dam/asset2.jpg/_jcr_content/renditions/original./asset2.jpg",
        underTest2.getMetadata().getUrl());
  }

  @Test
  void testWithRotationProperty() {
    context.request().setAttribute("mediaFormat", EDITORIAL_2COL.getName());
    context.request().setAttribute("rotationProperty", "myRotationProp");

    Resource resource2 = context.create().resource(ROOTPATH_CONTENT + "/jcr:content/media2",
        PROPERTY_RESOURCE_TYPE, "/dummy/resourcetype",
        PN_MEDIA_REF, asset.getPath(),
        "myRotationProp", "180");
    context.currentResource(resource2);

    ResourceMedia underTest = context.request().adaptTo(ResourceMedia.class);
    assertTrue(underTest.isValid());
    assertEquals("/content/dam/asset1.jpg/_jcr_content/renditions/original.image_file.450.213.-.180.file/asset1.jpg",
        underTest.getMetadata().getUrl());
  }

  @Test
  void testWithImageSizeAndWidthOptions_ValidCase_AllMandatoryWidthsAreAvailable() {
    final String sizes = "sizes string";

    context.request().setAttribute("mediaFormat", EDITORIAL_2COL.getName());
    context.request().setAttribute("imageWidths", "50,100,150,200,3000?");
    context.request().setAttribute("imageSizes", sizes);

    ResourceMedia underTest = context.request().adaptTo(ResourceMedia.class);
    assertTrue(underTest.isValid());

    HtmlElement<?> img = underTest.getMetadata().getElement();
    assertTrue(img instanceof io.wcm.handler.commons.dom.Image);
    assertEquals("/content/dam/asset1.jpg/_jcr_content/renditions/original.image_file.50.24.file/asset1.jpg 50w, "
        + "/content/dam/asset1.jpg/_jcr_content/renditions/original.image_file.100.47.file/asset1.jpg 100w, "
        + "/content/dam/asset1.jpg/_jcr_content/renditions/original.image_file.150.71.file/asset1.jpg 150w, "
        + "/content/dam/asset1.jpg/_jcr_content/renditions/original.image_file.200.95.file/asset1.jpg 200w",
        img.getAttributeValue("srcset"));
    assertEquals(sizes, img.getAttributeValue("sizes"));
    assertEquals("/content/dam/asset1.jpg/_jcr_content/renditions/original./asset1.jpg", img.getAttributeValue("src"));
  }

  @Test
  void testWithImageSizeAndWidthOptions_InvalidCase_MandatoryWithIsUnavailable() {
    final String sizes = "sizes string";

    context.request().setAttribute("mediaFormat", EDITORIAL_2COL.getName());
    context.request().setAttribute("imageWidths", "50,100,150,200,3000"); // 3000px is larger than the mediaformat width and makes the media invalid
    context.request().setAttribute("imageSizes", sizes);

    ResourceMedia underTest = context.request().adaptTo(ResourceMedia.class);
    assertFalse(underTest.isValid());
  }

  @Test
  void testWithPictureSourceProperties() {
    context.request().setAttribute("pictureSourceMediaFormat", new String[] {
        EDITORIAL_2COL.getName(), EDITORIAL_1COL.getName()
    });
    context.request().setAttribute("pictureSourceMedia", new String[] {
        "media1", ""
    });
    context.request().setAttribute("pictureSourceWidths", new String[] {
        "150,100", "90,40"
    });

    ResourceMedia underTest = context.request().adaptTo(ResourceMedia.class);
    assertTrue(underTest.isValid());

    // validate picture and sources
    HtmlElement<?> picture = underTest.getMetadata().getElement();
    assertTrue(picture instanceof io.wcm.handler.commons.dom.Picture);

    List<Element> sources = picture.getChildren("source");
    assertEquals(2, sources.size());

    HtmlElement<?> source1 = (HtmlElement<?>)sources.get(0);
    assertEquals("media1", source1.getAttributeValue("media"));
    assertEquals(
        "/content/dam/asset1.jpg/_jcr_content/renditions/original.image_file.150.71.file/asset1.jpg 150w, "
            + "/content/dam/asset1.jpg/_jcr_content/renditions/original.image_file.100.47.file/asset1.jpg 100w",
        source1.getAttributeValue("srcset"));

    HtmlElement<?> source2 = (HtmlElement<?>)sources.get(1);
    assertNull(source2.getAttributeValue("media"));
    assertEquals(
        "/content/dam/asset1.jpg/_jcr_content/renditions/original.image_file.90.43.file/asset1.jpg 90w, "
            + "/content/dam/asset1.jpg/_jcr_content/renditions/original.image_file.40.19.file/asset1.jpg 40w",
        source2.getAttributeValue("srcset"));

    // validate img
    HtmlElement<?> img = (HtmlElement<?>)picture.getChild("img");
    assertTrue(img instanceof io.wcm.handler.commons.dom.Image);
  }

  @Test
  void testWithCustomProperties() {
    context.request().setAttribute("property:prop1", true);
    context.request().setAttribute("property:prop2", "value2");
    context.request().setAttribute("property:prop3", new String[]{"array-item1", "array-item2"});
    context.request().setAttribute("property:prop4", null);
    context.request().setAttribute("property:prop5", new HashMap<>());
    context.request().setAttribute("property:", "invalid-prop--no-name");
    context.request().setAttribute("nonRelevantAttribute", "some-value");

    ResourceMedia underTest = context.request().adaptTo(ResourceMedia.class);

    assertNotNull(underTest);
    assertTrue(underTest.isValid());

    final ValueMap properties = underTest.getMetadata().getMediaRequest().getMediaArgs().getProperties();
    assertEquals(4, properties.size());
    assertEquals(true, properties.get("prop1"));
    assertEquals("value2", properties.get("prop2"));
    assertTrue(properties.get("prop3") instanceof String[]);
    assertEquals(2, properties.get("prop3", new String[0]).length);
    assertEquals("array-item2", properties.get("prop3", new String[0])[1]);
    assertEquals("null-value--is-ignored", properties.get("prop4", "null-value--is-ignored"));
    assertTrue(properties.get("prop5") instanceof Map);
  }

}
