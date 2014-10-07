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
package io.wcm.handler.commons.dom;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ImageTest {

  @Test
  public void testImage() {
    Image img1 = new Image("src1");
    assertEquals("src1", img1.getSrc());

    Image img2 = new Image("src2", "alt2");
    assertEquals("src2", img2.getSrc());
    assertEquals("alt2", img2.getAlt());

    Image img3 = new Image("src3", 20, 30);
    assertEquals("src3", img3.getSrc());
    assertEquals(20, img3.getWidth());
    assertEquals(30, img3.getHeight());

    Image img4 = new Image("src4", "alt4", 20, 30);
    assertEquals("src4", img4.getSrc());
    assertEquals("alt4", img4.getAlt());
    assertEquals(20, img4.getWidth());
    assertEquals(30, img4.getHeight());
  }

  @Test
  public void testSimpleAttributes() throws Exception {
    Image img = new Image();
    assertEquals("img", img.getName());

    img.setSrc("src1");
    assertEquals("src1", img.getSrc());

    img.setAlt("alt1");
    assertEquals("alt1", img.getAlt());

    img.setWidth(20);
    assertEquals(20, img.getWidth());

    img.setHeight(30);
    assertEquals(30, img.getHeight());

    img.setBorder(2);
    assertEquals(2, img.getBorder());

    img.setHSpace(3);
    assertEquals(3, img.getHSpace());

    img.setVSpace(4);
    assertEquals(4, img.getVSpace());
  }

}
