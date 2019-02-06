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

import static io.wcm.handler.media.impl.ipeconfig.IPEConfigResourceProvider.buildPath;
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
    assertFalse(new PathParser("/wcmio:mediaHandler/ipeConfig/my/path").isValid());
    assertFalse(new PathParser("/wcmio:mediaHandler/ipeConfig/my/path/wcmio:mediaFormat").isValid());
    assertFalse(new PathParser("/wcmio:mediaHandler/ipeConfig/my/path/wcmio:mediaFormat/mf1/mf2").isValid());
  }

  @Test
  public void testValidPath() {
    PathParser underTest = new PathParser(buildPath("/my/path", ImmutableSet.of("mf1", "mf2")));
    assertTrue(underTest.isValid());
    assertEquals("/my/path", underTest.getComponentContentPath());
    assertEquals(ImmutableSet.of("mf1", "mf2"), underTest.getMediaFormatNames());
    assertNull(underTest.getRelativeConfigPath());

    assertFalse(underTest.isPluginsCropNode());
    assertFalse(underTest.isAspectRatiosNode());
    assertFalse(underTest.isAspectRatioItem());
    assertNull(underTest.getAspectRatioItemName());
  }

  @Test
  public void testPluginsCropNode() {
    PathParser underTest = new PathParser(buildPath("/my/path", ImmutableSet.of("mf1")) + "/plugins/crop");
    assertTrue(underTest.isValid());
    assertEquals("/my/path", underTest.getComponentContentPath());
    assertEquals(ImmutableSet.of("mf1"), underTest.getMediaFormatNames());
    assertEquals("/plugins/crop", underTest.getRelativeConfigPath());

    assertTrue(underTest.isPluginsCropNode());
    assertFalse(underTest.isAspectRatiosNode());
    assertFalse(underTest.isAspectRatioItem());
    assertNull(underTest.getAspectRatioItemName());
  }

  @Test
  public void testAspectRatiosNode() {
    PathParser underTest = new PathParser(buildPath("/my/path", ImmutableSet.of("mf1")) + "/plugins/crop/aspectRatios");
    assertTrue(underTest.isValid());
    assertEquals("/my/path", underTest.getComponentContentPath());
    assertEquals(ImmutableSet.of("mf1"), underTest.getMediaFormatNames());
    assertEquals("/plugins/crop/aspectRatios", underTest.getRelativeConfigPath());

    assertFalse(underTest.isPluginsCropNode());
    assertTrue(underTest.isAspectRatiosNode());
    assertFalse(underTest.isAspectRatioItem());
    assertNull(underTest.getAspectRatioItemName());
  }

  @Test
  public void testAspectRatioItem() {
    PathParser underTest = new PathParser(buildPath("/my/path", ImmutableSet.of("mf1")) + "/plugins/crop/aspectRatios/mf2");
    assertTrue(underTest.isValid());
    assertEquals("/my/path", underTest.getComponentContentPath());
    assertEquals(ImmutableSet.of("mf1"), underTest.getMediaFormatNames());
    assertEquals("/plugins/crop/aspectRatios/mf2", underTest.getRelativeConfigPath());

    assertFalse(underTest.isPluginsCropNode());
    assertFalse(underTest.isAspectRatiosNode());
    assertTrue(underTest.isAspectRatioItem());
    assertEquals("mf2", underTest.getAspectRatioItemName());
  }

}
