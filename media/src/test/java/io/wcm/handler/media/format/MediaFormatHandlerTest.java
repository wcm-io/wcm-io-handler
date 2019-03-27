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
package io.wcm.handler.media.format;

import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_2COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_3COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_STAGE_LARGE;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_STANDARD;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.FIXEDHEIGHT_188;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.FIXEDHEIGHT_288;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.FIXEDHEIGHT_MAXHEIGHT;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.FIXEDHEIGHT_UNCONSTRAINED;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.FIXEDWIDTH_188;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.FIXEDWIDTH_288;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.FIXEDWIDTH_MAXWIDTH;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.FIXEDWIDTH_UNCONSTRAINED;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.NONFIXED_BIG;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.NONFIXED_FULLSIZE;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.NONFIXED_FULLSIZE_OVERLAY;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.NONFIXED_RAW;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.NONFIXED_SMALL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.NONFIXED_TAB_FULLSIZE;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.NONFIXED_TAB_SMALL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SPECIAL_4COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.WALLPAPER;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.WALLPAPER_1024_768;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.WALLPAPER_1440_900;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.WALLPAPER_1680_1050;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import org.apache.sling.api.adapter.Adaptable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * Test {@link MediaFormatHandler}
 */
@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class MediaFormatHandlerTest {

  final AemContext context = AppAemContext.newAemContext();

  protected Adaptable adaptable() {
    return context.request();
  }

  private MediaFormatHandler underTest;

  @BeforeEach
  void setUp() {
    underTest = adaptable().adaptTo(MediaFormatHandler.class);
  }

  @Test
  void testGetMediaFormat() {
    MediaFormat mediaFormat = underTest.getMediaFormat("editorial_2col");
    assertEquals(EDITORIAL_2COL, mediaFormat);

    mediaFormat = underTest.getMediaFormat("unknown_format");
    assertNull(mediaFormat);
  }

  @Test
  void testGetSameBiggerMediaFormats_Editorial() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(EDITORIAL_2COL, true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals(4, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals(EDITORIAL_STANDARD, format1);

    MediaFormat format2 = iterator.next();
    assertEquals(SPECIAL_4COL, format2);

    MediaFormat format3 = iterator.next();
    assertEquals(EDITORIAL_3COL, format3);

    MediaFormat format4 = iterator.next();
    assertEquals(EDITORIAL_2COL, format4);

  }

  @Test
  void testGetSameBiggerMediaFormats_Wallpaper_1024_768() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(WALLPAPER_1024_768, true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals(2, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals(WALLPAPER, format1);

    MediaFormat format2 = iterator.next();
    assertEquals(WALLPAPER_1024_768, format2);

  }

  @Test
  void testGetSameBiggerMediaFormats_Wallpaper_1440_900() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(WALLPAPER_1440_900, true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals(3, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals(WALLPAPER, format1);

    MediaFormat format2 = iterator.next();
    assertEquals(WALLPAPER_1680_1050, format2);

    MediaFormat format3 = iterator.next();
    assertEquals(WALLPAPER_1440_900, format3);

  }

  @Test
  void testGetSameBiggerMediaFormats_NoRenditionGroup() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(EDITORIAL_STAGE_LARGE, true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals(1, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals(EDITORIAL_STAGE_LARGE, format1);

  }

  @Test
  void testGetSameBiggerMediaFormats_NonFixed() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(NONFIXED_BIG, true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals(5, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals(NONFIXED_RAW, format1);

    MediaFormat format2 = iterator.next();
    assertEquals(NONFIXED_FULLSIZE_OVERLAY, format2);

    MediaFormat format3 = iterator.next();
    assertEquals(NONFIXED_FULLSIZE, format3);

    MediaFormat format4 = iterator.next();
    assertEquals(NONFIXED_TAB_FULLSIZE, format4);

    MediaFormat format5 = iterator.next();
    assertEquals(NONFIXED_BIG, format5);

  }

  @Test
  void testGetSameBiggerMediaFormats_FixedWidth() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(FIXEDWIDTH_288, true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals(3, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals(FIXEDWIDTH_288, format1);

    MediaFormat format2 = iterator.next();
    assertEquals(FIXEDWIDTH_MAXWIDTH, format2);

    MediaFormat format3 = iterator.next();
    assertEquals(FIXEDWIDTH_UNCONSTRAINED, format3);

  }

  @Test
  void testGetSameBiggerMediaFormats_FixedHeight() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(FIXEDHEIGHT_288, true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals(3, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals(FIXEDHEIGHT_288, format1);

    MediaFormat format2 = iterator.next();
    assertEquals(FIXEDHEIGHT_MAXHEIGHT, format2);

    MediaFormat format3 = iterator.next();
    assertEquals(FIXEDHEIGHT_UNCONSTRAINED, format3);

  }

  @Test
  void testGetSameSmallerMediaFormats_Editorial() {

    Set<MediaFormat> matchingFormats = underTest.getSameSmallerMediaFormats(EDITORIAL_2COL, true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals(2, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals(EDITORIAL_2COL, format1);

    MediaFormat format2 = iterator.next();
    assertEquals(EDITORIAL_1COL, format2);

  }

  @Test
  void testGetSameSmallerMediaFormats_Wallpaper_1024_768() {

    Set<MediaFormat> matchingFormats = underTest.getSameSmallerMediaFormats(WALLPAPER_1024_768, true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals(2, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals(WALLPAPER, format1);

    MediaFormat format2 = iterator.next();
    assertEquals(WALLPAPER_1024_768, format2);

  }

  @Test
  void testGetSameSmallerMediaFormats_Wallpaper_1680_1050() {

    Set<MediaFormat> matchingFormats = underTest.getSameSmallerMediaFormats(WALLPAPER_1680_1050, true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals(3, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals(WALLPAPER, format1);

    MediaFormat format2 = iterator.next();
    assertEquals(WALLPAPER_1680_1050, format2);

    MediaFormat format3 = iterator.next();
    assertEquals(WALLPAPER_1440_900, format3);

  }

  @Test
  void testGetSameSmallerMediaFormats_NoRenditionGroup() {

    Set<MediaFormat> matchingFormats = underTest.getSameSmallerMediaFormats(EDITORIAL_STAGE_LARGE, true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals(1, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals(EDITORIAL_STAGE_LARGE, format1);

  }

  @Test
  void testGetSameSmallerMediaFormats_NonFixed() {

    Set<MediaFormat> matchingFormats = underTest.getSameSmallerMediaFormats(NONFIXED_BIG, true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals(4, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals(NONFIXED_RAW, format1);

    MediaFormat format2 = iterator.next();
    assertEquals(NONFIXED_BIG, format2);

    MediaFormat format3 = iterator.next();
    assertEquals(NONFIXED_SMALL, format3);

    MediaFormat format4 = iterator.next();
    assertEquals(NONFIXED_TAB_SMALL, format4);

  }

  @Test
  void testGetSameSmallerMediaFormats_FixedWidth() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(FIXEDWIDTH_MAXWIDTH, true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals(4, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals(FIXEDWIDTH_188, format1);

    MediaFormat format2 = iterator.next();
    assertEquals(FIXEDWIDTH_288, format2);

    MediaFormat format3 = iterator.next();
    assertEquals(FIXEDWIDTH_MAXWIDTH, format3);

    MediaFormat format4 = iterator.next();
    assertEquals(FIXEDWIDTH_UNCONSTRAINED, format4);

  }

  @Test
  void testGetSameSmallerMediaFormats_FixedHeight() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(FIXEDHEIGHT_MAXHEIGHT, true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals(4, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals(FIXEDHEIGHT_188, format1);

    MediaFormat format2 = iterator.next();
    assertEquals(FIXEDHEIGHT_288, format2);

    MediaFormat format3 = iterator.next();
    assertEquals(FIXEDHEIGHT_MAXHEIGHT, format3);

    MediaFormat format4 = iterator.next();
    assertEquals(FIXEDHEIGHT_UNCONSTRAINED, format4);

  }

  @Test
  void testDetectMediaFormat() {

    assertNull(underTest.detectMediaFormat("txt", 100, 570, 270));

    // test extension match
    assertEquals("download", "download", underTest.detectMediaFormat("zip", 0, 0, 0).getName());

    // test direct match
    assertEquals("editorial_2col", "editorial_2col", underTest.detectMediaFormat("jpg", 100, 450, 213).getName());

    // test ranking match
    assertEquals("showroom_campaign", "showroom_campaign", underTest.detectMediaFormat("jpg", 100, 960, 455).getName());

  }

  @Test
  void testDetectMediaFormats() {

    assertTrue(underTest.detectMediaFormats("txt", 100, 570, 270).isEmpty(), "invalid");

    // test extension match
    assertEquals(11, underTest.detectMediaFormats("swf", 0, 0, 0).size(), "swf");

    // test direct match
    assertEquals(6, underTest.detectMediaFormats("jpg", 100, 450, 213).size(), "editorial_2col");

    // test ranking match
    SortedSet<MediaFormat> mediaFormats = underTest.detectMediaFormats("jpg", 100, 960, 455);
    assertEquals(8, mediaFormats.size(), "showroom");
    assertEquals("showroom_campaign", mediaFormats.first().getName(), "showroom_campaign");

    // test ratio match
    MediaFormat ratioFormat = RATIO;
    // ratio mismatch
    mediaFormats = underTest.detectMediaFormats("png", 100, 50, 50);
    assertFalse(mediaFormats.contains(ratioFormat), "nonfixed_raw ratio mismatch");
    // ratio match
    mediaFormats = underTest.detectMediaFormats("png", 100, 160, 100);
    assertTrue(mediaFormats.contains(ratioFormat), "nonfixed_raw ratio match");

  }

}
