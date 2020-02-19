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
package io.wcm.handler.media;

import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.commons.jcr.JcrConstants.NT_UNSTRUCTURED;
import static io.wcm.handler.media.MediaComponentPropertyResolver.PN_IMAGES_SIZES_SIZES;
import static io.wcm.handler.media.MediaComponentPropertyResolver.PN_IMAGES_SIZES_WIDTHS;
import static io.wcm.handler.media.MediaComponentPropertyResolver.PN_PICTURE_SOURCES_MEDIA;
import static io.wcm.handler.media.MediaComponentPropertyResolver.PN_PICTURE_SOURCES_MEDIAFORMAT;
import static io.wcm.handler.media.MediaComponentPropertyResolver.PN_PICTURE_SOURCES_SIZES;
import static io.wcm.handler.media.MediaComponentPropertyResolver.PN_PICTURE_SOURCES_WIDTHS;
import static io.wcm.handler.media.MediaComponentPropertyResolver.RESPONSIVE_TYPE_IMAGE_SIZES;
import static io.wcm.handler.media.MediaComponentPropertyResolver.RESPONSIVE_TYPE_PICTURE_SOURCES;
import static io.wcm.handler.media.impl.WidthUtils.parseWidths;
import static io.wcm.handler.media.MediaNameConstants.NN_COMPONENT_MEDIA_RESPONSIVEIMAGE_SIZES;
import static io.wcm.handler.media.MediaNameConstants.NN_COMPONENT_MEDIA_RESPONSIVEPICTURE_SOURCES;
import static io.wcm.handler.media.MediaNameConstants.NN_COMPONENT_MEDIA_RESPONSIVE_IMAGE_SIZES;
import static io.wcm.handler.media.MediaNameConstants.NN_COMPONENT_MEDIA_RESPONSIVE_PICTURE_SOURCES;
import static io.wcm.handler.media.MediaNameConstants.PN_COMPONENT_MEDIA_AUTOCROP;
import static io.wcm.handler.media.MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS;
import static io.wcm.handler.media.MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS_MANDATORY;
import static io.wcm.handler.media.MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS_MANDATORY_NAMES;
import static io.wcm.handler.media.MediaNameConstants.PN_COMPONENT_MEDIA_RESPONSIVE_TYPE;
import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.media.MediaArgs.ImageSizes;
import io.wcm.handler.media.MediaArgs.MediaFormatOption;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaArgs.WidthOption;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.component.ComponentPropertyResolverFactory;

@ExtendWith(AemContextExtension.class)
class MediaComponentPropertyResolverTest {

  private static final String RESOURCE_TYPE = "/apps/app1/components/comp1";

  private final AemContext context = AppAemContext.newAemContext();

  private ComponentPropertyResolverFactory componentPropertyResolverFactory;

  @BeforeEach
  void setUp() {
    componentPropertyResolverFactory = context.getService(ComponentPropertyResolverFactory.class);
  }

  @Test
  void testIsAutoCrop_Default() throws Exception {
    Resource resource = context.create().resource("/content/r1");
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertFalse(underTest.isAutoCrop());
    }
  }

  @Test
  void testIsAutoCrop_Component() throws Exception {
    context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_AUTOCROP, true);
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertTrue(underTest.isAutoCrop());
    }
  }

  @Test
  void testIsAutoCrop_Component_Subresource() throws Exception {
    context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_AUTOCROP, true);
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    Resource subresource1 = context.create().resource(resource, "subresource1",
        JCR_PRIMARYTYPE, NT_UNSTRUCTURED);
    Resource subresource2 = context.create().resource(subresource1, "subresource2");
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(subresource2, componentPropertyResolverFactory)) {
      assertTrue(underTest.isAutoCrop());
    }
  }

  @Test
  void testIsAutoCrop_Component_Policy() throws Exception {
    context.contentPolicyMapping(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_AUTOCROP, false);

    context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_AUTOCROP, true);
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertFalse(underTest.isAutoCrop());
    }
  }

  @Test
  void testGetMediaFormatOptions_Single() throws Exception {
    context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_FORMATS, "home_stage");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertArrayEquals(new MediaFormatOption[] {
          new MediaFormatOption("home_stage", false)
      }, underTest.getMediaFormatOptions());
    }
  }

  @Test
  void testGetMediaFormatOptions_Multi() throws Exception {
    context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_FORMATS, new String[] { "home_stage", "home_teaser" });
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertArrayEquals(new MediaFormatOption[] {
          new MediaFormatOption("home_stage", false),
          new MediaFormatOption("home_teaser", false)
      }, underTest.getMediaFormatOptions());

      assertArrayEquals(new String[] {
          "home_stage",
          "home_teaser"
      }, underTest.getMediaFormatNames());
      assertNull(underTest.getMandatoryMediaFormatNames());
    }
  }

  @Test
  void testGetMediaFormatOptions_Multi_MandatoryLegacy() throws Exception {
    context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_FORMATS, new String[] { "home_stage", "home_teaser" },
        PN_COMPONENT_MEDIA_FORMATS_MANDATORY, true);
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertArrayEquals(new MediaFormatOption[] {
          new MediaFormatOption("home_stage", true),
          new MediaFormatOption("home_teaser", true)
      }, underTest.getMediaFormatOptions());

      assertArrayEquals(new String[] {
          "home_stage",
          "home_teaser"
      }, underTest.getMediaFormatNames());
      assertArrayEquals(new String[] {
          "home_stage",
          "home_teaser"
      }, underTest.getMandatoryMediaFormatNames());
    }
  }

  @Test
  void testGetMediaFormatOptions_Multi_Mandatory() throws Exception {
    context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_FORMATS, new String[] { "home_stage", "home_teaser" },
        PN_COMPONENT_MEDIA_FORMATS_MANDATORY, new Boolean[] { true, false });
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertArrayEquals(new MediaFormatOption[] {
          new MediaFormatOption("home_stage", true),
          new MediaFormatOption("home_teaser", false)
      }, underTest.getMediaFormatOptions());

      assertArrayEquals(new String[] {
          "home_stage",
          "home_teaser"
      }, underTest.getMediaFormatNames());
      assertArrayEquals(new String[] {
          "home_stage"
      }, underTest.getMandatoryMediaFormatNames());
    }
  }

  @Test
  void testGetMediaFormatOptions_Multi_Mandatory_Names() throws Exception {
    context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_FORMATS, new String[] { "home_stage", "home_teaser" },
        PN_COMPONENT_MEDIA_FORMATS_MANDATORY_NAMES, new String[] { "home_stage", "product_banner" });
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertArrayEquals(new MediaFormatOption[] {
          new MediaFormatOption("home_stage", true),
          new MediaFormatOption("home_teaser", false),
          new MediaFormatOption("product_banner", true)
      }, underTest.getMediaFormatOptions());

      assertArrayEquals(new String[] {
          "home_stage",
          "home_teaser",
          "product_banner"
      }, underTest.getMediaFormatNames());
      assertArrayEquals(new String[] {
          "home_stage",
          "product_banner"
      }, underTest.getMandatoryMediaFormatNames());
    }
  }

  @Test
  void testParseWidths() {
    assertNull(parseWidths(null));
    assertNull(parseWidths(""));
    assertNull(parseWidths("jodel,kaiser"));

    assertArrayEquals(new WidthOption[] {
        new WidthOption(100, true)
    }, parseWidths("100"));

    assertArrayEquals(new WidthOption[] {
        new WidthOption(500, false),
        new WidthOption(400, true),
        new WidthOption(300, true),
        new WidthOption(200, false),
        new WidthOption(100, true),
    }, parseWidths(" 100  , 200? , 500?,400,300  "));
  }

  @Test
  void testGetImageSizes_NotExisting() throws Exception {
    context.create().resource(RESOURCE_TYPE);
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertNull(underTest.getImageSizes());
    }
  }

  @Test
  void testGetImageSizes_Empty() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE);
    context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVEIMAGE_SIZES);
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertNull(underTest.getImageSizes());
    }
  }

  @Test
  void testGetImageSizes_Valid() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE);
    context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVEIMAGE_SIZES,
        PN_IMAGES_SIZES_SIZES, "sizes1",
        PN_IMAGES_SIZES_WIDTHS, "200,400,600?");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertEquals(new ImageSizes("sizes1", new WidthOption[] {
          new WidthOption(600, false),
          new WidthOption(400, true),
          new WidthOption(200, true),
      }), underTest.getImageSizes());
    }
  }

  @Test
  void testGetImageSizes_Invalid() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE);
    context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVEIMAGE_SIZES,
        PN_IMAGES_SIZES_SIZES, "sizes1",
        PN_IMAGES_SIZES_WIDTHS, "wurst?");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertNull(underTest.getImageSizes());
    }
  }

  @Test
  void testGetImageSizes_Valid_Active() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_RESPONSIVE_TYPE, RESPONSIVE_TYPE_IMAGE_SIZES);
    context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVEIMAGE_SIZES,
        PN_IMAGES_SIZES_SIZES, "sizes1",
        PN_IMAGES_SIZES_WIDTHS, "200,400");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertEquals(new ImageSizes("sizes1", 400, 200), underTest.getImageSizes());
    }
  }

  @Test
  void testGetImageSizes_Valid_NotActive() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_RESPONSIVE_TYPE, RESPONSIVE_TYPE_PICTURE_SOURCES);
    context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVEIMAGE_SIZES,
        PN_IMAGES_SIZES_SIZES, "sizes1",
        PN_IMAGES_SIZES_WIDTHS, "200,400");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertNull(underTest.getImageSizes());
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  void testGetImageSizes_Empty_Deprecated() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE);
    context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVE_IMAGE_SIZES);
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertNull(underTest.getImageSizes());
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  void testGetImageSizes_Valid_Deprecated() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE);
    context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVE_IMAGE_SIZES,
        PN_IMAGES_SIZES_SIZES, "sizes1",
        PN_IMAGES_SIZES_WIDTHS, "200,400,600?");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertEquals(new ImageSizes("sizes1", new WidthOption[] {
          new WidthOption(600, false),
          new WidthOption(400, true),
          new WidthOption(200, true),
      }), underTest.getImageSizes());
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  void testGetImageSizes_Invalid_Deprecated() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE);
    context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVE_IMAGE_SIZES,
        PN_IMAGES_SIZES_SIZES, "sizes1",
        PN_IMAGES_SIZES_WIDTHS, "wurst?");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertNull(underTest.getImageSizes());
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  void testGetImageSizes_Valid_Active_Deprecated() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_RESPONSIVE_TYPE, RESPONSIVE_TYPE_IMAGE_SIZES);
    context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVE_IMAGE_SIZES,
        PN_IMAGES_SIZES_SIZES, "sizes1",
        PN_IMAGES_SIZES_WIDTHS, "200,400");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertEquals(new ImageSizes("sizes1", 400, 200), underTest.getImageSizes());
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  void testGetImageSizes_Valid_NotActive_Deprecated() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_RESPONSIVE_TYPE, RESPONSIVE_TYPE_PICTURE_SOURCES);
    context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVE_IMAGE_SIZES,
        PN_IMAGES_SIZES_SIZES, "sizes1",
        PN_IMAGES_SIZES_WIDTHS, "200,400");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertNull(underTest.getImageSizes());
    }
  }


  @Test
  void testGetPictureSources_NotExisting() throws Exception {
    context.create().resource(RESOURCE_TYPE);
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertNull(underTest.getPictureSources());
    }
  }

  @Test
  void testGetPictureSources_Empty() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE);
    context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVEPICTURE_SOURCES);
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertNull(underTest.getImageSizes());
    }
  }

  @Test
  void testGetPictureSources_Valid() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE);
    Resource sources = context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVEPICTURE_SOURCES);
    context.create().resource(sources, "source1",
        PN_PICTURE_SOURCES_MEDIAFORMAT, "home_stage",
        PN_PICTURE_SOURCES_MEDIA, "media1",
        PN_PICTURE_SOURCES_SIZES, "sizes1",
        PN_PICTURE_SOURCES_WIDTHS, "200,400?");
    context.create().resource(sources, "source2",
        PN_PICTURE_SOURCES_MEDIAFORMAT, "home_teaser",
        PN_PICTURE_SOURCES_WIDTHS, "200,300");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertArrayEquals(new PictureSource[] {
          new PictureSource("home_stage")
              .media("media1")
              .sizes("sizes1")
              .widthOptions(new WidthOption[] {
                  new WidthOption(400, false),
                  new WidthOption(200, true)
              }),
          new PictureSource("home_teaser")
              .widths(300, 200)
      }, underTest.getPictureSources());
    }
  }

  @Test
  void testGetPictureSources_Invalid() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE);
    Resource sources = context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVEPICTURE_SOURCES);
    context.create().resource(sources, "source1",
        PN_PICTURE_SOURCES_MEDIAFORMAT, "home_stage",
        PN_PICTURE_SOURCES_MEDIA, "media1",
        PN_PICTURE_SOURCES_WIDTHS, "jodel,kaiser");
    context.create().resource(sources, "source2",
        PN_PICTURE_SOURCES_WIDTHS, "200,300");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertNull(underTest.getPictureSources());
    }
  }

  @Test
  void testGetPictureSources_Valid_Active() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_RESPONSIVE_TYPE, RESPONSIVE_TYPE_PICTURE_SOURCES);
    Resource sources = context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVEPICTURE_SOURCES);
    context.create().resource(sources, "source1",
        PN_PICTURE_SOURCES_MEDIAFORMAT, "home_stage",
        PN_PICTURE_SOURCES_WIDTHS, "200,400");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertArrayEquals(new PictureSource[] {
          new PictureSource("home_stage").widths(400, 200)
      }, underTest.getPictureSources());
    }
  }

  @Test
  void testGetPictureSources_Valid_Inactive() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_RESPONSIVE_TYPE, RESPONSIVE_TYPE_IMAGE_SIZES);
    Resource sources = context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVEPICTURE_SOURCES);
    context.create().resource(sources, "source1",
        PN_PICTURE_SOURCES_MEDIAFORMAT, "home_stage",
        PN_PICTURE_SOURCES_WIDTHS, "200,400");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertNull(underTest.getPictureSources());
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  void testGetPictureSources_Empty_Deprecated() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE);
    context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVE_PICTURE_SOURCES);
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertNull(underTest.getImageSizes());
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  void testGetPictureSources_Valid_Deprecated() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE);
    Resource sources = context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVE_PICTURE_SOURCES);
    context.create().resource(sources, "source1",
        PN_PICTURE_SOURCES_MEDIAFORMAT, "home_stage",
        PN_PICTURE_SOURCES_MEDIA, "media1",
        PN_PICTURE_SOURCES_SIZES, "sizes1",
        PN_PICTURE_SOURCES_WIDTHS, "200,400?");
    context.create().resource(sources, "source2",
        PN_PICTURE_SOURCES_MEDIAFORMAT, "home_teaser",
        PN_PICTURE_SOURCES_WIDTHS, "200,300");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertArrayEquals(new PictureSource[] {
          new PictureSource("home_stage")
              .media("media1")
              .sizes("sizes1")
              .widthOptions(new WidthOption[] {
                  new WidthOption(400, false),
                  new WidthOption(200, true)
              }),
          new PictureSource("home_teaser")
              .widths(300, 200)
      }, underTest.getPictureSources());
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  void testGetPictureSources_Invalid_Deprecated() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE);
    Resource sources = context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVE_PICTURE_SOURCES);
    context.create().resource(sources, "source1",
        PN_PICTURE_SOURCES_MEDIAFORMAT, "home_stage",
        PN_PICTURE_SOURCES_MEDIA, "media1",
        PN_PICTURE_SOURCES_WIDTHS, "jodel,kaiser");
    context.create().resource(sources, "source2",
        PN_PICTURE_SOURCES_WIDTHS, "200,300");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertNull(underTest.getPictureSources());
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  void testGetPictureSources_Valid_Active_Deprecated() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_RESPONSIVE_TYPE, RESPONSIVE_TYPE_PICTURE_SOURCES);
    Resource sources = context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVE_PICTURE_SOURCES);
    context.create().resource(sources, "source1",
        PN_PICTURE_SOURCES_MEDIAFORMAT, "home_stage",
        PN_PICTURE_SOURCES_WIDTHS, "200,400");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertArrayEquals(new PictureSource[] {
          new PictureSource("home_stage").widths(400, 200)
      }, underTest.getPictureSources());
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  void testGetPictureSources_Valid_Inactive_Deprecated() throws Exception {
    Resource component = context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_MEDIA_RESPONSIVE_TYPE, RESPONSIVE_TYPE_IMAGE_SIZES);
    Resource sources = context.create().resource(component, NN_COMPONENT_MEDIA_RESPONSIVE_PICTURE_SOURCES);
    context.create().resource(sources, "source1",
        PN_PICTURE_SOURCES_MEDIAFORMAT, "home_stage",
        PN_PICTURE_SOURCES_WIDTHS, "200,400");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    context.resourceResolver().commit();

    try (MediaComponentPropertyResolver underTest = new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertNull(underTest.getPictureSources());
    }
  }

}
