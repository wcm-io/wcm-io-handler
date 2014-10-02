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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test {@link MediaFormatHandler}
 */
@RunWith(MockitoJUnitRunner.class)
public class MediaFormatHandlerTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  private MediaFormatHandler underTest;

  @Before
  public void setUp() {
    context.load().json("/mediaformat-sample.json", AppAemContext.MEDIAFORMATS_PATH);
    underTest = context.request().adaptTo(MediaFormatHandler.class);
  }

  @Test
  public void testGetMediaFormats() {
    Set<MediaFormat> set = underTest.getMediaFormats();
    assertEquals("count", 54, set.size());

    MediaFormat mf = underTest.getMediaFormat("allProperties");
    assertEquals("testTitle", mf.getTitle());
    assertEquals("testDescription", mf.getDescription());
    assertEquals(500, mf.getWidth());
    assertEquals(250, mf.getWidthMin());
    assertEquals(1000, mf.getWidthMax());
    assertEquals(400, mf.getHeight());
    assertEquals(200, mf.getHeightMin());
    assertEquals(800, mf.getHeightMax());
    assertEquals(1.5d, mf.getRatio(), 0.001d);
    assertEquals(300, mf.getRatioWidth());
    assertEquals(200, mf.getRatioHeight());
    assertEquals(50000, mf.getFileSizeMax());
    assertArrayEquals(new String[] {
        "ext1", "ext2"
    }, mf.getExtension());
    assertEquals("/apps/test/renditiongroup/testGroup", mf.getRenditionGroup());
    assertTrue(mf.isInternal());
    assertEquals(500, mf.getRanking());

  }

  @Test
  public void testGetSameBiggerMediaFormats_Editorial() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(
        underTest.getMediaFormat(AppAemContext.MEDIAFORMATS_PATH + "/editorial_2col"), true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals("count", 4, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals("format1", AppAemContext.MEDIAFORMATS_PATH + "/editorial_standard", format1.getPath());

    MediaFormat format2 = iterator.next();
    assertEquals("format2", AppAemContext.MEDIAFORMATS_PATH + "/special_4col", format2.getPath());

    MediaFormat format3 = iterator.next();
    assertEquals("format3", AppAemContext.MEDIAFORMATS_PATH + "/editorial_3col", format3.getPath());

    MediaFormat format4 = iterator.next();
    assertEquals("format4", AppAemContext.MEDIAFORMATS_PATH + "/editorial_2col", format4.getPath());

  }

  @Test
  public void testGetSameBiggerMediaFormats_Wallpaper_1024_768() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(
        underTest.getMediaFormat(AppAemContext.MEDIAFORMATS_PATH + "/wallpaper_1024_768"), true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals("count", 2, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals("format1", AppAemContext.MEDIAFORMATS_PATH + "/wallpaper", format1.getPath());

    MediaFormat format2 = iterator.next();
    assertEquals("format2", AppAemContext.MEDIAFORMATS_PATH + "/wallpaper_1024_768", format2.getPath());

  }

  @Test
  public void testGetSameBiggerMediaFormats_Wallpaper_1440_900() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(
        underTest.getMediaFormat(AppAemContext.MEDIAFORMATS_PATH + "/wallpaper_1440_900"), true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals("count", 3, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals("format1", AppAemContext.MEDIAFORMATS_PATH + "/wallpaper", format1.getPath());

    MediaFormat format2 = iterator.next();
    assertEquals("format2", AppAemContext.MEDIAFORMATS_PATH + "/wallpaper_1680_1050", format2.getPath());

    MediaFormat format3 = iterator.next();
    assertEquals("format3", AppAemContext.MEDIAFORMATS_PATH + "/wallpaper_1440_900", format3.getPath());

  }

  @Test
  public void testGetSameBiggerMediaFormats_NoRenditionGroup() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(
        underTest.getMediaFormat(AppAemContext.MEDIAFORMATS_PATH + "/editorial_stage_large"), true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals("count", 1, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals("format1", AppAemContext.MEDIAFORMATS_PATH + "/editorial_stage_large", format1.getPath());

  }

  @Test
  public void testGetSameBiggerMediaFormats_NonFixed() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(
        underTest.getMediaFormat(AppAemContext.MEDIAFORMATS_PATH + "/nonfixed_big"), true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals("count", 5, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals("format1", AppAemContext.MEDIAFORMATS_PATH + "/nonfixed_raw", format1.getPath());

    MediaFormat format2 = iterator.next();
    assertEquals("format2", AppAemContext.MEDIAFORMATS_PATH + "/nonfixed_fullsize_overlay", format2.getPath());

    MediaFormat format3 = iterator.next();
    assertEquals("format3", AppAemContext.MEDIAFORMATS_PATH + "/nonfixed_fullsize", format3.getPath());

    MediaFormat format4 = iterator.next();
    assertEquals("format4", AppAemContext.MEDIAFORMATS_PATH + "/nonfixed_tab_fullsize", format4.getPath());

    MediaFormat format5 = iterator.next();
    assertEquals("format5", AppAemContext.MEDIAFORMATS_PATH + "/nonfixed_big", format5.getPath());

  }

  @Test
  public void testGetSameBiggerMediaFormats_FixedWidth() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(
        underTest.getMediaFormat(AppAemContext.MEDIAFORMATS_PATH + "/fixedwidth_288"), true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals("count", 3, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals("format1", AppAemContext.MEDIAFORMATS_PATH + "/fixedwidth_288", format1.getPath());

    MediaFormat format2 = iterator.next();
    assertEquals("format2", AppAemContext.MEDIAFORMATS_PATH + "/fixedwidth_maxwidth", format2.getPath());

    MediaFormat format3 = iterator.next();
    assertEquals("format3", AppAemContext.MEDIAFORMATS_PATH + "/fixedwidth_unconstrained", format3.getPath());

  }

  @Test
  public void testGetSameBiggerMediaFormats_FixedHeight() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(
        underTest.getMediaFormat(AppAemContext.MEDIAFORMATS_PATH + "/fixedheight_288"), true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals("count", 3, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals("format1", AppAemContext.MEDIAFORMATS_PATH + "/fixedheight_288", format1.getPath());

    MediaFormat format2 = iterator.next();
    assertEquals("format2", AppAemContext.MEDIAFORMATS_PATH + "/fixedheight_maxheight", format2.getPath());

    MediaFormat format3 = iterator.next();
    assertEquals("format3", AppAemContext.MEDIAFORMATS_PATH + "/fixedheight_unconstrained", format3.getPath());

  }

  @Test
  public void testGetSameSmallerMediaFormats_Editorial() {

    Set<MediaFormat> matchingFormats = underTest.getSameSmallerMediaFormats(
        underTest.getMediaFormat(AppAemContext.MEDIAFORMATS_PATH + "/editorial_2col"), true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals("count", 2, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals("format1", AppAemContext.MEDIAFORMATS_PATH + "/editorial_2col", format1.getPath());

    MediaFormat format2 = iterator.next();
    assertEquals("format2", AppAemContext.MEDIAFORMATS_PATH + "/editorial_1col", format2.getPath());

  }

  @Test
  public void testGetSameSmallerMediaFormats_Wallpaper_1024_768() {

    Set<MediaFormat> matchingFormats = underTest.getSameSmallerMediaFormats(
        underTest.getMediaFormat(AppAemContext.MEDIAFORMATS_PATH + "/wallpaper_1024_768"), true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals("count", 2, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals("format1", AppAemContext.MEDIAFORMATS_PATH + "/wallpaper", format1.getPath());

    MediaFormat format2 = iterator.next();
    assertEquals("format2", AppAemContext.MEDIAFORMATS_PATH + "/wallpaper_1024_768", format2.getPath());

  }

  @Test
  public void testGetSameSmallerMediaFormats_Wallpaper_1680_1050() {

    Set<MediaFormat> matchingFormats = underTest.getSameSmallerMediaFormats(
        underTest.getMediaFormat(AppAemContext.MEDIAFORMATS_PATH + "/wallpaper_1680_1050"), true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals("count", 3, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals("format1", AppAemContext.MEDIAFORMATS_PATH + "/wallpaper", format1.getPath());

    MediaFormat format2 = iterator.next();
    assertEquals("format2", AppAemContext.MEDIAFORMATS_PATH + "/wallpaper_1680_1050", format2.getPath());

    MediaFormat format3 = iterator.next();
    assertEquals("format3", AppAemContext.MEDIAFORMATS_PATH + "/wallpaper_1440_900", format3.getPath());

  }

  @Test
  public void testGetSameSmallerMediaFormats_NoRenditionGroup() {

    Set<MediaFormat> matchingFormats = underTest.getSameSmallerMediaFormats(
        underTest.getMediaFormat(AppAemContext.MEDIAFORMATS_PATH + "/editorial_stage_large"), true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals("count", 1, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals("format1", AppAemContext.MEDIAFORMATS_PATH + "/editorial_stage_large", format1.getPath());

  }

  @Test
  public void testGetSameSmallerMediaFormats_NonFixed() {

    Set<MediaFormat> matchingFormats = underTest.getSameSmallerMediaFormats(
        underTest.getMediaFormat(AppAemContext.MEDIAFORMATS_PATH + "/nonfixed_big"), true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals("count", 4, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals("format1", AppAemContext.MEDIAFORMATS_PATH + "/nonfixed_raw", format1.getPath());

    MediaFormat format2 = iterator.next();
    assertEquals("format2", AppAemContext.MEDIAFORMATS_PATH + "/nonfixed_big", format2.getPath());

    MediaFormat format3 = iterator.next();
    assertEquals("format3", AppAemContext.MEDIAFORMATS_PATH + "/nonfixed_small", format3.getPath());

    MediaFormat format4 = iterator.next();
    assertEquals("format4", AppAemContext.MEDIAFORMATS_PATH + "/nonfixed_tab_small", format4.getPath());

  }

  @Test
  public void testGetSameSmallerMediaFormats_FixedWidth() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(
        underTest.getMediaFormat(AppAemContext.MEDIAFORMATS_PATH + "/fixedwidth_maxwidth"), true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals("count", 4, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals("format1", AppAemContext.MEDIAFORMATS_PATH + "/fixedwidth_188", format1.getPath());

    MediaFormat format2 = iterator.next();
    assertEquals("format2", AppAemContext.MEDIAFORMATS_PATH + "/fixedwidth_288", format2.getPath());

    MediaFormat format3 = iterator.next();
    assertEquals("format3", AppAemContext.MEDIAFORMATS_PATH + "/fixedwidth_maxwidth", format3.getPath());

    MediaFormat format4 = iterator.next();
    assertEquals("format4", AppAemContext.MEDIAFORMATS_PATH + "/fixedwidth_unconstrained", format4.getPath());

  }

  @Test
  public void testGetSameSmallerMediaFormats_FixedHeight() {

    Set<MediaFormat> matchingFormats = underTest.getSameBiggerMediaFormats(
        underTest.getMediaFormat(AppAemContext.MEDIAFORMATS_PATH + "/fixedheight_maxheight"), true);
    Iterator<MediaFormat> iterator = matchingFormats.iterator();

    assertEquals("count", 4, matchingFormats.size());

    MediaFormat format1 = iterator.next();
    assertEquals("format1", AppAemContext.MEDIAFORMATS_PATH + "/fixedheight_188", format1.getPath());

    MediaFormat format2 = iterator.next();
    assertEquals("format2", AppAemContext.MEDIAFORMATS_PATH + "/fixedheight_288", format2.getPath());

    MediaFormat format3 = iterator.next();
    assertEquals("format3", AppAemContext.MEDIAFORMATS_PATH + "/fixedheight_maxheight", format3.getPath());

    MediaFormat format4 = iterator.next();
    assertEquals("format4", AppAemContext.MEDIAFORMATS_PATH + "/fixedheight_unconstrained", format4.getPath());

  }

  @Test
  public void testDetectMediaFormat() {

    assertEquals("invalid", null, underTest.detectMediaFormat("txt", 100, 570, 270));

    // test extension match
    assertEquals("download", "/apps/test/mediaformat/download",
        underTest.detectMediaFormat("zip", 0, 0, 0).getPath());

    // test direct match
    assertEquals("editorial_2col", "/apps/test/mediaformat/editorial_2col",
        underTest.detectMediaFormat("jpg", 100, 450, 213).getPath());

    // test ranking match
    assertEquals("showroom_campaign", "/apps/test/mediaformat/showroom_campaign",
        underTest.detectMediaFormat("jpg", 100, 960, 455).getPath());

  }

  @Test
  public void testDetectMediaFormats() {

    assertTrue("invalid", underTest.detectMediaFormats("txt", 100, 570, 270).isEmpty());

    // test extension match
    assertEquals("swf", 11, underTest.detectMediaFormats("swf", 0, 0, 0).size());

    // test direct match
    assertEquals("editorial_2col", 6, underTest.detectMediaFormats("jpg", 100, 450, 213).size());

    // test ranking match
    SortedSet<MediaFormat> mediaFormats = underTest.detectMediaFormats("jpg", 100, 960, 455);
    assertEquals("showroom", 8, mediaFormats.size());
    assertEquals("showroom_campaign", "/apps/test/mediaformat/showroom_campaign",
        mediaFormats.first().getPath());

    // test ratio match
    MediaFormat ratioFormat = underTest.getMediaFormat("/apps/test/mediaformat/ratio");
    // ratio mismatch
    mediaFormats = underTest.detectMediaFormats("png", 100, 50, 50);
    assertFalse("nonfixed_raw ratio mismatch", mediaFormats.contains(ratioFormat));
    // ratio match
    mediaFormats = underTest.detectMediaFormats("png", 100, 160, 100);
    assertTrue("nonfixed_raw ratio match", mediaFormats.contains(ratioFormat));

  }

}
