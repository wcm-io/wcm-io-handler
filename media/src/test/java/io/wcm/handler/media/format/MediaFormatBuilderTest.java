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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class MediaFormatBuilderTest {

  private static final String APP_ID = "/apps/sample";

  @Test
  public void testBuilder_variant1() {
    MediaFormat mf = MediaFormatBuilder.create("name1", APP_ID)
        .label("label1")
        .description("description1")
        .width(800)
        .height(600)
        .ratio(1.333d)
        .fileSizeMax(10000L)
        .extensions("gif", "png")
        .renditionGroup("group1")
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
    assertTrue(mf.isInternal());
    assertEquals(500, mf.getRanking());
  }

  @Test
  public void testBuilder_variant2() {
    MediaFormat mf = MediaFormatBuilder.create()
        .name("name2")
        .applicationId(APP_ID)
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
    assertEquals(100, mf.getRatioWidth());
    assertEquals(50, mf.getRatioHeight());
    assertEquals(0L, mf.getFileSizeMax());
    assertArrayEquals(new String[0], mf.getExtensions());
    assertNull("group1", mf.getRenditionGroup());
    assertFalse(mf.isInternal());
    assertEquals(0, mf.getRanking());
  }

  @Test
  public void testBuilder_variant3() {
    MediaFormat mf = MediaFormatBuilder.create()
        .name("name2")
        .applicationId(APP_ID)
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
  public void testBuilder_variant4() {
    MediaFormat mf = MediaFormatBuilder.create()
        .name("name3")
        .applicationId(APP_ID)
        .fixedDimension(1000, 500)
        .build();

    assertEquals("name3", mf.getName());
    assertEquals(1000, mf.getWidth());
    assertEquals(500, mf.getHeight());
  }

  /**
   * test if {@link io.wcm.handler.media.format.MediaFormatBuilder#create(io.wcm.handler.media.format.MediaFormat)}
   * copies all data
   * @throws IllegalAccessException on reflection errors
   * @throws IllegalAccessException on reflection errors
   * @throws InvocationTargetException on reflection errors
   */
  @Test
  public void testBuilder_variant5() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    MediaFormat mfOriginal = getFullFeaturedMediaFormat();
    MediaFormat mfNew = MediaFormatBuilder.create(mfOriginal).build();

    for (Method method : MediaFormat.class.getMethods()) {
      if (StringUtils.startsWith(method.getName(), "get")) {
        if (method.getReturnType() == String[].class) {
          assertArrayEquals("assert method " + method.getName(), (String[])method.invoke(mfOriginal, new Object[0]),
              (String[])method.invoke(mfNew, new Object[0]));
        }
        else {
          assertEquals("assert method " + method.getName(), method.invoke(mfOriginal, new Object[0]), method.invoke(mfNew, new Object[0]));
        }
      }
    }
  }

  /**
   * test unmodifiable extensions
   */
  @Test
  public void testExtensions() {
    final String unmodifiableExtension = "png";
    String[] extensionsSource = {
        unmodifiableExtension
    };

    MediaFormat mf = MediaFormatBuilder.create("name", APP_ID)
        .extensions(extensionsSource)
        .build();

    // source modification should have no effect
    extensionsSource[0] = "ThisModificationShouldHaveNoEffect";
    assertEquals(unmodifiableExtension, mf.getExtensions()[0]);

    // now modify the returned extensions, should have no effect
    mf.getExtensions()[0] = "ThisModificationShouldHaveNoEffect";
    assertEquals(unmodifiableExtension, mf.getExtensions()[0]);
  }

  /**
   * test the {@link #getFullFeaturedMediaFormat()} method if really everything is set
   * @throws IllegalAccessException on reflection errors
   * @throws IllegalArgumentException on reflection errors
   * @throws InvocationTargetException on reflection errors
   */
  @Test
  public void fullFeaturedMediaFormatTest() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    MediaFormat mf = getFullFeaturedMediaFormat();

    for (Method method : MediaFormat.class.getMethods()) {
      if (StringUtils.startsWith(method.getName(), "get")) {
        Assert.assertNotNull("assert method " + method.getName(), method.invoke(mf, new Object[0]));
      }
    }
  }

  /**
   * helper method to get a media format with all data set.
   * @return media format
   */
  protected MediaFormat getFullFeaturedMediaFormat() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("key1", "value1");
    return MediaFormatBuilder.create()
        .name("name")
        .applicationId(APP_ID)
        .label("label")
        .description("description")
        .width(1)
        .minWidth(2)
        .maxWidth(3)
        .height(4)
        .minHeight(5)
        .maxHeight(6)
        .ratio(7)
        .ratio(8, 9)
        .fileSizeMax(10)
        .extensions(new String[] {
            "png"
        })
        .renditionGroup("renditionGroup")
        .internal(true)
        .ranking(11)
        .properties(properties)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingNameApplicationId() {
    MediaFormatBuilder.create().build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingName() {
    MediaFormatBuilder.create().applicationId(APP_ID).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingApplicationId() {
    MediaFormatBuilder.create().name("mf1").build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullName() {
    MediaFormatBuilder.create().name(null).applicationId(APP_ID).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullApplicationId() {
    MediaFormatBuilder.create().name("mf1").applicationId(null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalName() {
    MediaFormatBuilder.create("name with spaces", APP_ID).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalApplicationId() {
    MediaFormatBuilder.create("mf1", "illegal app id").build();
  }

  @Test
  public void testProperties() {
    Map<String, Object> props = ImmutableMap.<String, Object>of("prop1", "value1");

    MediaFormat mf = MediaFormatBuilder.create("name1", APP_ID)
        .property("prop3", "value3")
        .properties(props)
        .property("prop2", "value2")
        .build();

    assertEquals(3, mf.getProperties().size());
    assertEquals("value1", mf.getProperties().get("prop1", String.class));
    assertEquals("value2", mf.getProperties().get("prop2", String.class));
    assertEquals("value3", mf.getProperties().get("prop3", String.class));
  }

}
