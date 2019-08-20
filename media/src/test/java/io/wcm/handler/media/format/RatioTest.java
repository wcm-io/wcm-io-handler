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
package io.wcm.handler.media.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.testcontext.DummyMediaFormats;

class RatioTest {

  @Test
  void testMatchesDouble() {
    assertTrue(Ratio.matches(16d / 9d, 16d / 9d));
    assertTrue(Ratio.matches(1.99d, 2.01d));
    assertFalse(Ratio.matches(16d / 9d, 16d / 10d));
  }

  @Test
  void testMatchesMediaFormat() {
    assertTrue(Ratio.matches(DummyMediaFormats.RATIO, DummyMediaFormats.RATIO));
    assertFalse(Ratio.matches(DummyMediaFormats.RATIO, DummyMediaFormats.RATIO2));
    assertFalse(Ratio.matches(DummyMediaFormats.RATIO, DummyMediaFormats.DOWNLOAD));
    assertFalse(Ratio.matches(DummyMediaFormats.DOWNLOAD, DummyMediaFormats.DOWNLOAD));
  }

  @Test
  void testGet() {
    assertEquals(2, Ratio.get(100, 50), 0.001d);
    assertEquals(0, Ratio.get(0, 50), 0.001d);
    assertEquals(0, Ratio.get(100, 0), 0.001d);
  }

  @Test
  void testGet_Dimension() {
    assertEquals(2, Ratio.get(new Dimension(100, 50)), 0.001d);
    assertEquals(0, Ratio.get(new Dimension(0, 50)), 0.001d);
    assertEquals(0, Ratio.get(new Dimension(100, 0)), 0.001d);
  }

}
