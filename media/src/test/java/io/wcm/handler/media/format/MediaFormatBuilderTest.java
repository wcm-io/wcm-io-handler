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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

@SuppressWarnings("null")
class MediaFormatBuilderTest {

  @Test
  void testBuilder_variant1() {
    MediaFormat mf = MediaFormatBuilder.create("name1")
        .label("label1")
        .description("description1")
        .width(800)
        .height(600)
        .ratio(1.333d)
        .fileSizeMax(10000L)
        .extensions("gif", "png")
        .renditionGroup("group1")
        .download(true)
        .internal(true)
        .ranking(500)
        .build();

    assertEquals("name1", mf.getName());
    assertEquals("label1", mf.getLabel());
    assertEquals("description1", mf.getDescription());
    assertEquals(800, mf.getWidth());
    assertEquals(600, mf.getHeight());
    assertEquals(1.333d, mf.getRatio(), 0.0001d);
    assertEquals(10000L, mf.getFileSizeMax());
    assertArrayEquals(new String[] {
        "gif", "png"
    }, mf.getExtensions());
    assertEquals("group1", mf.getRenditionGroup());
    assertTrue(mf.isDownload());
    assertTrue(mf.isInternal());
    assertEquals(500, mf.getRanking());
  }

  @Test
  void testBuilder_variant2() {
    MediaFormat mf = MediaFormatBuilder.create("name2")
        .width(400, 800)
        .height(300, 600)
        .ratio(100, 50)
        .build();

    assertEquals("name2", mf.getName());
    assertEquals("name2", mf.getLabel());
    assertNull(mf.getDescription());
    assertEquals(0, mf.getWidth());
    assertEquals(400, mf.getMinWidth());
    assertEquals(800, mf.getMaxWidth());
    assertEquals(0, mf.getHeight());
    assertEquals(300, mf.getMinHeight());
    assertEquals(600, mf.getMaxHeight());
    assertEquals(2d, mf.getRatio(), 0.0001d);
    assertEquals(100, mf.getRatioWidthAsDouble(), 0.0001d);
    assertEquals(50, mf.getRatioHeightAsDouble(), 0.0001d);
    assertEquals(0L, mf.getFileSizeMax());
    assertArrayEquals(new String[0], mf.getExtensions());
    assertNull(mf.getRenditionGroup());
    assertFalse(mf.isDownload());
    assertFalse(mf.isInternal());
    assertEquals(0, mf.getRanking());
  }

  @Test
  void testBuilder_variant3() {
    MediaFormat mf = MediaFormatBuilder.create("name2")
        .minWidth(400)
        .maxWidth(800)
        .minHeight(300)
        .maxHeight(600)
        .build();

    assertEquals(0, mf.getWidth());
    assertEquals(400, mf.getMinWidth());
    assertEquals(800, mf.getMaxWidth());
    assertEquals(0, mf.getHeight());
    assertEquals(300, mf.getMinHeight());
    assertEquals(600, mf.getMaxHeight());
  }

  @Test
  void testBuilder_variant4() {
    MediaFormat mf = MediaFormatBuilder.create("name3")
        .fixedDimension(1000, 500)
        .build();

    assertEquals("name3", mf.getName());
    assertEquals(1000, mf.getWidth());
    assertEquals(500, mf.getHeight());
  }

  @Test
  void testBuilder_MinWidthHeight() {
    MediaFormat mf = MediaFormatBuilder.create("name4_minWidthHeight")
        .minWidthHeight(500)
        .ratio(2, 1)
        .build();

    assertEquals("name4_minWidthHeight", mf.getName());
    assertEquals(500, mf.getMinWidthHeight());
    assertEquals("name4_minWidthHeight (min. 500px width/height; 2:1)", mf.getCombinedTitle());
  }

  @Test
  void testNullName() {
    assertThrows(IllegalArgumentException.class, () -> {
      MediaFormatBuilder.create(null).build();
    });
  }

  @Test
  void testIllegalName() {
    assertThrows(IllegalArgumentException.class, () -> {
      MediaFormatBuilder.create("name with spaces").build();
    });
  }

  @Test
  void testMinWidthHeight_withOtherWidthHeightRestrictions() {
    assertThrows(IllegalArgumentException.class, () -> {
      MediaFormatBuilder.create("name")
          .minWidth(500)
          .minHeight(500)
          .minWidthHeight(500)
          .build();
    });
  }

  @Test
  void testProperties() {
    Map<String, Object> props = ImmutableMap.<String, Object>of("prop1", "value1");

    MediaFormat mf = MediaFormatBuilder.create("name1")
        .property("prop3", "value3")
        .properties(props)
        .property("prop2", "value2")
        .build();

    assertEquals(3, mf.getProperties().size());
    assertEquals("value1", mf.getProperties().get("prop1", String.class));
    assertEquals("value2", mf.getProperties().get("prop2", String.class));
    assertEquals("value3", mf.getProperties().get("prop3", String.class));
  }

  /**
   * test unmodifiable extensions
   */
  @Test
  void testExtensions() {
    final String unmodifiableExtension = "png";
    String[] extensionsSource = {
        unmodifiableExtension
    };

    MediaFormat mf = MediaFormatBuilder.create("name")
        .extensions(extensionsSource)
        .build();

    // source modification should have no effect
    extensionsSource[0] = "ThisModificationShouldHaveNoEffect";
    assertEquals(unmodifiableExtension, mf.getExtensions()[0]);

    // now modify the returned extensions, should have no effect
    mf.getExtensions()[0] = "ThisModificationShouldHaveNoEffect";
    assertEquals(unmodifiableExtension, mf.getExtensions()[0]);
  }

}
