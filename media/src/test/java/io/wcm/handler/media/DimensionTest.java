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
package io.wcm.handler.media;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DimensionTest {

  @Test
  public void testSimple() {
    Dimension dimension = new Dimension(20, 10);

    assertEquals(20, dimension.getWidth());
    assertEquals(10, dimension.getHeight());
    assertEquals("Dimension[width=20,height=10]", dimension.toString());
  }

  @Test
  public void testEquals() {
    Dimension dimension1 = new Dimension(20, 10);
    Dimension dimension2 = new Dimension(20, 10);
    Dimension dimension3 = new Dimension(21, 10);
    Dimension dimension4 = new Dimension(20, 11);

    assertTrue(dimension1.equals(dimension2));
    assertFalse(dimension1.equals(dimension3));
    assertFalse(dimension1.equals(dimension4));

    assertTrue(dimension2.equals(dimension1));
    assertFalse(dimension2.equals(dimension3));
    assertFalse(dimension2.equals(dimension4));

    assertFalse(dimension3.equals(dimension1));
    assertFalse(dimension3.equals(dimension2));
    assertFalse(dimension3.equals(dimension4));

    assertFalse(dimension4.equals(dimension1));
    assertFalse(dimension4.equals(dimension2));
    assertFalse(dimension4.equals(dimension3));
  }

}
