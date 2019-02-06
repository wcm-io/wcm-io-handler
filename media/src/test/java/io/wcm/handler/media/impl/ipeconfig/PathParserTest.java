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
package io.wcm.handler.media.impl.ipeconfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;


public class PathParserTest {

  @Test
  public void testInvalidPath() {
    assertFalse(new PathParser("/invalid/path").isValid());
    assertFalse(new PathParser("/wcmio:mediaHandler/ipeConfig").isValid());
    assertFalse(new PathParser("/wcmio:mediaHandler/ipeConfig/mf1").isValid());
    assertFalse(new PathParser("/wcmio:mediaHandler/ipeConfig/mf1/wcmio:content").isValid());
  }

  @Test
  public void testValidPath() {
    PathParser underTest = new PathParser("/wcmio:mediaHandler/ipeConfig/mf1/mf2/wcmio:content/my/path");
    assertTrue(underTest.isValid());
    assertEquals(ImmutableSet.of("mf1", "mf2"), underTest.getMediaFormatNames());
    assertEquals("/my/path", underTest.getOverlayPath());

    assertFalse(underTest.isPluginsCropNode());
    assertFalse(underTest.isAspectRatiosNode());
    assertFalse(underTest.isAspectRatioItem());
    assertNull(underTest.getAspectRatioItemName());
  }

  @Test
  public void testPluginsCropNode() {
    PathParser underTest = new PathParser("/wcmio:mediaHandler/ipeConfig/mf1/wcmio:content/my/path/plugins/crop");
    assertTrue(underTest.isValid());
    assertEquals(ImmutableSet.of("mf1"), underTest.getMediaFormatNames());
    assertEquals("/my/path/plugins/crop", underTest.getOverlayPath());

    assertTrue(underTest.isPluginsCropNode());
    assertFalse(underTest.isAspectRatiosNode());
    assertFalse(underTest.isAspectRatioItem());
    assertNull(underTest.getAspectRatioItemName());
  }

  @Test
  public void testAspectRatiosNode() {
    PathParser underTest = new PathParser("/wcmio:mediaHandler/ipeConfig/mf1/wcmio:content/my/path/plugins/crop/aspectRatios");
    assertTrue(underTest.isValid());
    assertEquals(ImmutableSet.of("mf1"), underTest.getMediaFormatNames());
    assertEquals("/my/path/plugins/crop/aspectRatios", underTest.getOverlayPath());

    assertFalse(underTest.isPluginsCropNode());
    assertTrue(underTest.isAspectRatiosNode());
    assertFalse(underTest.isAspectRatioItem());
    assertNull(underTest.getAspectRatioItemName());
  }

  @Test
  public void testAspectRatioItem() {
    PathParser underTest = new PathParser("/wcmio:mediaHandler/ipeConfig/mf1/wcmio:content/my/path/plugins/crop/aspectRatios/mf2");
    assertTrue(underTest.isValid());
    assertEquals(ImmutableSet.of("mf1"), underTest.getMediaFormatNames());
    assertEquals("/my/path/plugins/crop/aspectRatios/mf2", underTest.getOverlayPath());

    assertFalse(underTest.isPluginsCropNode());
    assertFalse(underTest.isAspectRatiosNode());
    assertTrue(underTest.isAspectRatioItem());
    assertEquals("mf2", underTest.getAspectRatioItemName());
  }

}
