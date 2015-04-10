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
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.testcontext.DummyMediaFormats;

import org.junit.Test;


public class ResponsiveMediaFormatsBuilderTest {

  @Test
  public void testBuild() {
    MediaFormat[] mediaFormats = new ResponsiveMediaFormatsBuilder(DummyMediaFormats.RATIO)
    .breakpoint("B1", 160, 100)
    .breakpoint("B2", 320, 200)
    .build();

    assertEquals(ResponsiveMediaFormatsBuilder.buildCombinedName(DummyMediaFormats.RATIO, "B1", 160, 100), mediaFormats[0].getName());
    assertEquals(DummyMediaFormats.RATIO.getApplicationId(), mediaFormats[0].getApplicationId());
    assertEquals(DummyMediaFormats.RATIO.getLabel(), mediaFormats[0].getLabel());
    assertArrayEquals(DummyMediaFormats.RATIO.getExtensions(), mediaFormats[0].getExtensions());
    assertEquals(DummyMediaFormats.RATIO.getRatio(), mediaFormats[0].getRatio(), 0.001d);
    assertEquals(160, mediaFormats[0].getWidth());
    assertEquals(100, mediaFormats[0].getHeight());
    assertEquals("B1", mediaFormats[0].getProperties().get(MediaNameConstants.PROP_BREAKPOINT));

    assertEquals(ResponsiveMediaFormatsBuilder.buildCombinedName(DummyMediaFormats.RATIO, "B2", 320, 200), mediaFormats[1].getName());
    assertEquals(DummyMediaFormats.RATIO.getApplicationId(), mediaFormats[1].getApplicationId());
    assertEquals(DummyMediaFormats.RATIO.getLabel(), mediaFormats[1].getLabel());
    assertArrayEquals(DummyMediaFormats.RATIO.getExtensions(), mediaFormats[1].getExtensions());
    assertEquals(DummyMediaFormats.RATIO.getRatio(), mediaFormats[1].getRatio(), 0.001d);
    assertEquals(320, mediaFormats[1].getWidth());
    assertEquals(200, mediaFormats[1].getHeight());
    assertEquals("B2", mediaFormats[1].getProperties().get(MediaNameConstants.PROP_BREAKPOINT));
  }

}
