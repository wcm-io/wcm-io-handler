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

import static io.wcm.handler.media.format.MediaFormatBuilder.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class MediaFormatTest {

  private static final String APP_ID = "/apps/sample";

  @Test
  public void testRatioNone() {
    MediaFormat mf = create("mf1", APP_ID).build();
    assertEquals(0d, mf.getRatio(), 0.001d);
    assertNull(mf.getRatioDisplayString());
  }

  @Test
  public void testRatio() {
    MediaFormat mf = create("mf1", APP_ID).ratio(1.25d).build();
    assertEquals(1.25d, mf.getRatio(), 0.001d);
    assertEquals("R1.25", mf.getRatioDisplayString());
  }

  @Test
  public void testRatioWidthHeight() {
    MediaFormat mf = create("mf1", APP_ID).ratio(16, 9).build();
    assertEquals(1.777d, mf.getRatio(), 0.001d);
    assertEquals("16:9", mf.getRatioDisplayString());
  }

  @Test
  public void testRatioFixedDimension() {
    MediaFormat mf = create("mf1", APP_ID).fixedDimension(100, 75).build();
    assertEquals(1.333d, mf.getRatio(), 0.001d);
    assertNull(mf.getRatioDisplayString());
  }

  @Test
  public void testIsImage() {
    MediaFormat mf1 = create("mf1", APP_ID).extensions("gif").build();
    MediaFormat mf2 = create("mf2", APP_ID).extensions("zip", "gif").build();
    MediaFormat mf3 = create("mf3", APP_ID).extensions("zip").build();

    assertTrue(mf1.isImage());
    assertTrue(mf2.isImage());
    assertFalse(mf3.isImage());
  }

  @Test
  public void testFixedDimension() {
    MediaFormat mf1 = create("mf1", APP_ID).fixedDimension(100, 50).build();
    assertTrue(mf1.isFixedWidth());
    assertTrue(mf1.isFixedHeight());
    assertTrue(mf1.isFixedDimension());

    MediaFormat mf2 = create("mf2", APP_ID).width(100).build();
    assertTrue(mf2.isFixedWidth());
    assertFalse(mf2.isFixedHeight());
    assertFalse(mf2.isFixedDimension());

    MediaFormat mf3 = create("mf3", APP_ID).height(50).build();
    assertFalse(mf3.isFixedWidth());
    assertTrue(mf3.isFixedHeight());
    assertFalse(mf3.isFixedDimension());

    MediaFormat mf4 = create("mf4", APP_ID).width(50, 100).height(100, 150).build();
    assertFalse(mf4.isFixedWidth());
    assertFalse(mf4.isFixedHeight());
    assertFalse(mf4.isFixedDimension());
  }

  @Test
  public void testGetEffectiveMinWidth() {
    MediaFormat mf1 = create("mf1", APP_ID).width(75).build();
    assertEquals(75, mf1.getEffectiveMinWidth());

    MediaFormat mf2 = create("mf2", APP_ID).width(50, 100).build();
    assertEquals(50, mf2.getEffectiveMinWidth());
  }

  @Test
  public void testGetEffectiveMaxWidth() {
    MediaFormat mf1 = create("mf1", APP_ID).width(75).build();
    assertEquals(75, mf1.getEffectiveMaxWidth());

    MediaFormat mf2 = create("mf2", APP_ID).width(50, 100).build();
    assertEquals(100, mf2.getEffectiveMaxWidth());
  }

  @Test
  public void testGetEffectiveMinHeight() {
    MediaFormat mf1 = create("mf1", APP_ID).height(75).build();
    assertEquals(75, mf1.getEffectiveMinHeight());

    MediaFormat mf2 = create("mf2", APP_ID).height(50, 100).build();
    assertEquals(50, mf2.getEffectiveMinHeight());
  }

  @Test
  public void testGetEffectiveMaxHeight() {
    MediaFormat mf1 = create("mf1", APP_ID).height(75).build();
    assertEquals(75, mf1.getEffectiveMaxHeight());

    MediaFormat mf2 = create("mf2", APP_ID).height(50, 100).build();
    assertEquals(100, mf2.getEffectiveMaxHeight());
  }

  @Test
  public void testGetMinDimension() {
    MediaFormat mf1 = create("mf1", APP_ID).build();
    assertNull(mf1.getMinDimension());

    MediaFormat mf2 = create("mf2", APP_ID).fixedDimension(100, 50).build();
    assertEquals(100, mf2.getMinDimension().getWidth());
    assertEquals(50, mf2.getMinDimension().getHeight());

    MediaFormat mf3 = create("mf3", APP_ID).width(50, 100).height(75, 200).build();
    assertEquals(50, mf3.getMinDimension().getWidth());
    assertEquals(75, mf3.getMinDimension().getHeight());

    MediaFormat mf4 = create("mf4", APP_ID).width(100).ratio(1.333d).build();
    assertEquals(100, mf4.getMinDimension().getWidth());
    assertEquals(75, mf4.getMinDimension().getHeight());

    MediaFormat mf5 = create("mf5", APP_ID).height(75).ratio(1.333d).build();
    assertEquals(100, mf5.getMinDimension().getWidth());
    assertEquals(75, mf5.getMinDimension().getHeight());
  }

  @Test
  public void testGetCombinedTitle() {
    MediaFormat mf1 = create("mf1", APP_ID).build();
    assertEquals("mf1", mf1.getCombinedTitle());

    MediaFormat mf2 = create("mf2", APP_ID).label("MF2").fixedDimension(100, 50).build();
    assertEquals("MF2 (100x50px)", mf2.getCombinedTitle());
    assertEquals("MF2 (100x50px)", mf2.toString());

    MediaFormat mf3 = create("mf3", APP_ID).label("MF3").width(50, 100).height(75, 200).build();
    assertEquals("MF3 (50..100x75..200px)", mf3.getCombinedTitle());

    MediaFormat mf4 = create("mf4", APP_ID).fixedDimension(100, 50).extensions("gif", "jpg").build();
    assertEquals("mf4 (100x50px; gif,jpg)", mf4.getCombinedTitle());

    MediaFormat mf5 = create("mf5", APP_ID).ratio(16, 9).extensions("gif", "jpg").build();
    assertEquals("mf5 (16:9; gif,jpg)", mf5.getCombinedTitle());

    MediaFormat mf6 = create("mf6", APP_ID).extensions("e1", "e2", "e3", "e4", "e5", "e6", "e7").build();
    assertEquals("mf6 (e1,e2,e3,e4,e5,e6...)", mf6.getCombinedTitle());

  }

  @Test
  public void testSort() {
    SortedSet<MediaFormat> set = new TreeSet<>();
    set.add(create("mf1", APP_ID).build());
    set.add(create("mf3", APP_ID).build());
    set.add(create("mf2", APP_ID).build());

    List<MediaFormat> result = ImmutableList.copyOf(set);
    assertEquals("mf1", result.get(0).getName());
    assertEquals("mf2", result.get(1).getName());
    assertEquals("mf3", result.get(2).getName());
  }

}
