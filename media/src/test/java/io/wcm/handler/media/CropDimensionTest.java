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

public class CropDimensionTest {

  @Test
  public void testSimple() {
    CropDimension dimension = new CropDimension(15, 5, 20, 10);

    assertEquals(15, dimension.getLeft());
    assertEquals(5, dimension.getTop());
    assertEquals(20, dimension.getWidth());
    assertEquals(10, dimension.getHeight());
    assertEquals(35, dimension.getRight());
    assertEquals(15, dimension.getBottom());
    assertEquals("CropDimension[left=15,top=5,width=20,height=10]", dimension.toString());
    assertEquals("15,5,35,15", dimension.getCropString());
    assertEquals(15, dimension.getRectangle().getX(), 0.0001);
    assertEquals(5, dimension.getRectangle().getY(), 0.0001);
    assertEquals(20, dimension.getRectangle().getWidth(), 0.0001);
    assertEquals(10, dimension.getRectangle().getHeight(), 0.0001);
  }

  @Test
  public void testEquals() {
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

  @Test(expected = IllegalArgumentException.class)
  public void testFromCropStringNull() {
    CropDimension.fromCropString(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromCropStringEmpty() {
    CropDimension.fromCropString("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromCropStringInvalid1() {
    CropDimension.fromCropString("wurst");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromCropStringInvalid2() {
    CropDimension.fromCropString("w,u,r,s,t");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromCropStringInvalid3() {
    CropDimension.fromCropString("w,u,r,s");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromCropStringInvalid4() {
    CropDimension.fromCropString("0,0,0,0");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromCropStringInvalid5() {
    CropDimension.fromCropString("0,-1,10,10");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromCropStringInvalid6() {
    CropDimension.fromCropString("-1,0,10,10");
  }

  @Test
  public void testFromCropStringValid1() {
    CropDimension dimension = CropDimension.fromCropString("15,5,35,15");

    assertEquals(15, dimension.getLeft());
    assertEquals(5, dimension.getTop());
    assertEquals(20, dimension.getWidth());
    assertEquals(10, dimension.getHeight());
    assertEquals(35, dimension.getRight());
    assertEquals(15, dimension.getBottom());
    assertEquals("CropDimension[left=15,top=5,width=20,height=10]", dimension.toString());
    assertEquals("15,5,35,15", dimension.getCropString());
    assertEquals(15, dimension.getRectangle().getX(), 0.0001);
    assertEquals(5, dimension.getRectangle().getY(), 0.0001);
    assertEquals(20, dimension.getRectangle().getWidth(), 0.0001);
    assertEquals(10, dimension.getRectangle().getHeight(), 0.0001);
  }

  @Test
  public void testFromCropStringValid2() {
    CropDimension dimension = CropDimension.fromCropString("15,5,35,15/5,5");

    assertEquals(15, dimension.getLeft());
    assertEquals(5, dimension.getTop());
    assertEquals(20, dimension.getWidth());
    assertEquals(10, dimension.getHeight());
    assertEquals(35, dimension.getRight());
    assertEquals(15, dimension.getBottom());
    assertEquals("CropDimension[left=15,top=5,width=20,height=10]", dimension.toString());
    assertEquals("15,5,35,15", dimension.getCropString());
    assertEquals(15, dimension.getRectangle().getX(), 0.0001);
    assertEquals(5, dimension.getRectangle().getY(), 0.0001);
    assertEquals(20, dimension.getRectangle().getWidth(), 0.0001);
    assertEquals(10, dimension.getRectangle().getHeight(), 0.0001);
  }

}
