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
package io.wcm.handler.media.imagemap.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableList;

import io.wcm.handler.media.imagemap.ImageArea;
import io.wcm.handler.media.imagemap.ImageMapParser;
import io.wcm.handler.media.spi.ImageMapLinkResolver;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
public class ImageMapParserImplTest {

  private static final String EXTERNAL_REF = "http://myhost";
  private static final String VALID_CONTENT_REF = "/content/valid";
  private static final String INVALID_CONTENT_REF = "/content/invalid";

  public static final String MAP_STRING = "[circle(256,256,256)\"" + EXTERNAL_REF + "\"|\"\"|\"\"|(0.2000,0.3001,0.2000)]"
      + "[rect(256,171,1023,682)\"" + VALID_CONTENT_REF + "\"|\"\"|\"altText\"|(0.1992,0.2005,0.7992,0.7995)]"
      + "[poly(917,344,1280,852,532,852)\"" + EXTERNAL_REF + "\"|\"_blank\"|\"\"|(0.7164,0.4033,1.0000,0.9988,0.4156,0.9988)]"
      // this rect has an invalid content reference and thus should be removed during parsing
      + "[rect(256,171,1023,682)\"" + INVALID_CONTENT_REF + "\"|\"\"|\"altText\"|(0.1992,0.2005,0.7992,0.7995)]";

  public static final List<ImageArea> EXPECTED_AREAS_RESOLVED = ImmutableList.of(
      new ImageAreaImpl("circle", "256,256,256", "0.2000,0.3001,0.2000", EXTERNAL_REF, null, null),
      new ImageAreaImpl("rect", "256,171,1023,682", "0.1992,0.2005,0.7992,0.7995", VALID_CONTENT_REF + ".html", null, "altText"),
      new ImageAreaImpl("poly", "917,344,1280,852,532,852", "0.7164,0.4033,1.0000,0.9988,0.4156,0.9988", EXTERNAL_REF, "_blank", null));

  public static final List<ImageArea> EXPECTED_AREAS_UNRESOLVED = ImmutableList.of(
      new ImageAreaImpl("circle", "256,256,256", "0.2000,0.3001,0.2000", EXTERNAL_REF, null, null),
      new ImageAreaImpl("rect", "256,171,1023,682", "0.1992,0.2005,0.7992,0.7995", VALID_CONTENT_REF, null, "altText"),
      new ImageAreaImpl("poly", "917,344,1280,852,532,852", "0.7164,0.4033,1.0000,0.9988,0.4156,0.9988", EXTERNAL_REF, "_blank", null),
      new ImageAreaImpl("rect", "256,171,1023,682", "0.1992,0.2005,0.7992,0.7995", INVALID_CONTENT_REF, null, "altText"));

  private final AemContext context = AppAemContext.newAemContext();

  @Mock(lenient = true)
  private ImageMapLinkResolver linkResolver;

  @BeforeEach
  void setUp() {
    when(linkResolver.resolve(eq(EXTERNAL_REF), any(Resource.class))).thenReturn(EXTERNAL_REF);
    when(linkResolver.resolve(eq(VALID_CONTENT_REF), any(Resource.class))).thenReturn(VALID_CONTENT_REF + ".html");
    when(linkResolver.resolve(eq(INVALID_CONTENT_REF), any(Resource.class))).thenReturn(null);

    context.currentResource(context.create().resource("/content/test/resource1"));
  }

  @Test
  void testMap_WithLinkResolver() {
    context.registerService(ImageMapLinkResolver.class, linkResolver);
    ImageMapParser underTest = AdaptTo.notNull(context.request(), ImageMapParser.class);

    assertEquals(EXPECTED_AREAS_RESOLVED, underTest.parseMap(MAP_STRING));
  }

  @Test
  void testMap_WithoutLinkResolver() {
    ImageMapParser underTest = AdaptTo.notNull(context.request(), ImageMapParser.class);

    assertEquals(EXPECTED_AREAS_UNRESOLVED, underTest.parseMap(MAP_STRING));
  }

  @Test
  void testNull() {
    ImageMapParser underTest = AdaptTo.notNull(context.request(), ImageMapParser.class);

    assertEquals(0, underTest.parseMap(null).size());
  }

  @Test
  void testEmptyString() {
    ImageMapParser underTest = AdaptTo.notNull(context.request(), ImageMapParser.class);

    assertEquals(0, underTest.parseMap("").size());
  }

  @Test
  void testInvalidString() {
    ImageMapParser underTest = AdaptTo.notNull(context.request(), ImageMapParser.class);

    assertEquals(0, underTest.parseMap("[xyz][").size());
  }

}
