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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

@SuppressWarnings("null")
class CropDimensionTest {

  @Test
  void testSimple() {
    CropDimension dimension = new CropDimension(15, 5, 20, 10);

    assertEquals(15, dimension.getLeft());
    assertEquals(5, dimension.getTop());
    assertEquals(20, dimension.getWidth());
    assertEquals(10, dimension.getHeight());
    assertEquals(35, dimension.getRight());
    assertEquals(15, dimension.getBottom());
    assertEquals("[left=15,top=5,width=20,height=10]", dimension.toString());
    assertEquals("15,5,35,15", dimension.getCropString());
    assertEquals(15, dimension.getRectangle().getX(), 0.0001);
    assertEquals(5, dimension.getRectangle().getY(), 0.0001);
    assertEquals(20, dimension.getRectangle().getWidth(), 0.0001);
    assertEquals(10, dimension.getRectangle().getHeight(), 0.0001);
    assertFalse(dimension.isAutoCrop());
  }

  @Test
  void testEquals() {
    CropDimension dimension1 = new CropDimension(15, 5, 20, 10);
    CropDimension dimension2 = new CropDimension(15, 5, 20, 10);
    CropDimension dimension3 = new CropDimension(15, 5, 21, 10);
    CropDimension dimension4 = new CropDimension(15, 5, 20, 11);
    CropDimension dimension5 = new CropDimension(16, 5, 20, 10);
    CropDimension dimension6 = new CropDimension(15, 6, 20, 10);

    assertTrue(dimension1.equals(dimension2));
    assertFalse(dimension1.equals(dimension3));
    assertFalse(dimension1.equals(dimension4));
    assertFalse(dimension1.equals(dimension5));
    assertFalse(dimension1.equals(dimension6));

    assertTrue(dimension2.equals(dimension1));
    assertFalse(dimension2.equals(dimension3));
    assertFalse(dimension2.equals(dimension4));
    assertFalse(dimension2.equals(dimension5));
    assertFalse(dimension2.equals(dimension6));

    assertFalse(dimension3.equals(dimension1));
    assertFalse(dimension3.equals(dimension2));
    assertFalse(dimension3.equals(dimension4));
    assertFalse(dimension3.equals(dimension5));
    assertFalse(dimension3.equals(dimension6));

    assertFalse(dimension4.equals(dimension1));
    assertFalse(dimension4.equals(dimension2));
    assertFalse(dimension4.equals(dimension3));
    assertFalse(dimension4.equals(dimension5));
    assertFalse(dimension4.equals(dimension6));

    assertFalse(dimension5.equals(dimension1));
    assertFalse(dimension5.equals(dimension2));
    assertFalse(dimension5.equals(dimension3));
    assertFalse(dimension5.equals(dimension4));
    assertFalse(dimension5.equals(dimension6));

    assertFalse(dimension6.equals(dimension1));
    assertFalse(dimension6.equals(dimension2));
    assertFalse(dimension6.equals(dimension3));
    assertFalse(dimension6.equals(dimension4));
    assertFalse(dimension6.equals(dimension5));
  }

  @Test
  void testFromCropStringNull() {
    assertThrows(IllegalArgumentException.class, () -> {
      CropDimension.fromCropString(null);
    });
  }

  @Test
  void testFromCropStringEmpty() {
    assertThrows(IllegalArgumentException.class, () -> {
      CropDimension.fromCropString("");
    });
  }

  @Test
  void testFromCropStringInvalid1() {
    assertThrows(IllegalArgumentException.class, () -> {
      CropDimension.fromCropString("wurst");
    });
  }

  @Test
  void testFromCropStringInvalid2() {
    assertThrows(IllegalArgumentException.class, () -> {
      CropDimension.fromCropString("w,u,r,s,t");
    });
  }

  @Test
  void testFromCropStringInvalid3() {
    assertThrows(IllegalArgumentException.class, () -> {
      CropDimension.fromCropString("w,u,r,s");
    });
  }

  @Test
  void testFromCropStringInvalid4() {
    assertThrows(IllegalArgumentException.class, () -> {
      CropDimension.fromCropString("0,0,0,0");
    });
  }

  @Test
  void testFromCropStringInvalid5() {
    assertThrows(IllegalArgumentException.class, () -> {
      CropDimension.fromCropString("0,-1,10,10");
    });
  }

  @Test
  void testFromCropStringInvalid6() {
    assertThrows(IllegalArgumentException.class, () -> {
      CropDimension.fromCropString("-1,0,10,10");
    });
  }

  @Test
  void testFromCropStringValid1() {
    CropDimension dimension = CropDimension.fromCropString("15,5,35,15");

    assertEquals(15, dimension.getLeft());
    assertEquals(5, dimension.getTop());
    assertEquals(20, dimension.getWidth());
    assertEquals(10, dimension.getHeight());
    assertEquals(35, dimension.getRight());
    assertEquals(15, dimension.getBottom());
    assertEquals("[left=15,top=5,width=20,height=10]", dimension.toString());
    assertEquals("15,5,35,15", dimension.getCropString());
    assertEquals("15,5,20,10", dimension.getCropStringWidthHeight());
    assertEquals(15, dimension.getRectangle().getX(), 0.0001);
    assertEquals(5, dimension.getRectangle().getY(), 0.0001);
    assertEquals(20, dimension.getRectangle().getWidth(), 0.0001);
    assertEquals(10, dimension.getRectangle().getHeight(), 0.0001);
  }

  @Test
  void testFromCropStringValid2() {
    CropDimension dimension = CropDimension.fromCropString("15,5,35,15/5,5");

    assertEquals(15, dimension.getLeft());
    assertEquals(5, dimension.getTop());
    assertEquals(20, dimension.getWidth());
    assertEquals(10, dimension.getHeight());
    assertEquals(35, dimension.getRight());
    assertEquals(15, dimension.getBottom());
    assertEquals("[left=15,top=5,width=20,height=10]", dimension.toString());
    assertEquals("15,5,35,15", dimension.getCropString());
    assertEquals("15,5,20,10", dimension.getCropStringWidthHeight());
    assertEquals(15, dimension.getRectangle().getX(), 0.0001);
    assertEquals(5, dimension.getRectangle().getY(), 0.0001);
    assertEquals(20, dimension.getRectangle().getWidth(), 0.0001);
    assertEquals(10, dimension.getRectangle().getHeight(), 0.0001);
  }

  @Test
  void testSimpleMarkedAutoCrop() {
    CropDimension dimension = new CropDimension(15, 5, 20, 10, true);
    assertTrue(dimension.isAutoCrop());
  }

}
