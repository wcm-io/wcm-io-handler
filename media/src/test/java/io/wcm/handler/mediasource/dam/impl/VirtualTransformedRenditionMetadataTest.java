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
package io.wcm.handler.mediasource.dam.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.image.Layer;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.mediasource.dam.AbstractDamTest;

@SuppressWarnings("null")
class VirtualTransformedRenditionMetadataTest extends AbstractDamTest {

  private Rendition rendition;
  private RenditionMetadata originalRendition;

  @BeforeEach
  void setUp() throws Exception {
    String path = MEDIAITEM_PATH_STANDARD + "/jcr:content/renditions/cq5dam.thumbnail.215.102.jpg";
    context.resourceResolver().delete(context.resourceResolver().getResource(path));
    context.load().binaryFile("/sample_image_215x102.jpg", path);

    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    Asset asset = media.getAsset().adaptTo(Asset.class);
    rendition = asset.getRendition("cq5dam.thumbnail.215.102.jpg");
    originalRendition = new RenditionMetadata(asset.getRendition("original"));
  }

  @Test
  void testGetLayer_cropping() throws Exception {
    VirtualTransformedRenditionMetadata underTest = new VirtualTransformedRenditionMetadata(rendition, 30, 25,
        null, new CropDimension(5, 5, 30, 25), null);

    Layer layer = underTest.getLayer();
    assertEquals(30, layer.getWidth());
    assertEquals(25, layer.getHeight());
  }

  @Test
  void testGetLayer_cropping_resize() throws Exception {
    VirtualTransformedRenditionMetadata underTest = new VirtualTransformedRenditionMetadata(rendition, 30, 25,
        null, new CropDimension(5, 5, 60, 50), null);

    Layer layer = underTest.getLayer();
    assertEquals(30, layer.getWidth());
    assertEquals(25, layer.getHeight());
  }

  @Test
  void testGetLayer_rotation() throws Exception {
    VirtualTransformedRenditionMetadata underTest = new VirtualTransformedRenditionMetadata(rendition, 102, 215,
        null, null, 90);

    Layer layer = underTest.getLayer();
    assertEquals(102, layer.getWidth());
    assertEquals(215, layer.getHeight());
  }

  @Test
  void testGetLayer_cropping_rotation() throws Exception {
    VirtualTransformedRenditionMetadata underTest = new VirtualTransformedRenditionMetadata(rendition, 25, 30,
        null, new CropDimension(5, 5, 25, 30), 180);

    Layer layer = underTest.getLayer();
    assertEquals(25, layer.getWidth());
    assertEquals(30, layer.getHeight());
  }

  @Test
  void testGetInputStream() throws Exception {
    VirtualTransformedRenditionMetadata underTest = new VirtualTransformedRenditionMetadata(rendition, 108, 51,
        null, new CropDimension(5, 5, 30, 25), null);

    InputStream is = underTest.getInputStream();
    assertNull(is);
  }

  @Test
  void testEquals() {
    VirtualTransformedRenditionMetadata m1 = new VirtualTransformedRenditionMetadata(rendition, 108, 51,
        null, new CropDimension(5, 5, 30, 25), null);
    VirtualTransformedRenditionMetadata m2 = new VirtualTransformedRenditionMetadata(rendition, 108, 51,
        null, new CropDimension(5, 5, 30, 25), null);
    VirtualTransformedRenditionMetadata m3 = new VirtualTransformedRenditionMetadata(rendition, 108, 51,
        null, new CropDimension(10, 10, 30, 25), null);
    VirtualTransformedRenditionMetadata m4 = new VirtualTransformedRenditionMetadata(rendition, 108, 51,
        null, new CropDimension(5, 5, 30, 25), 180);

    assertTrue(m1.equals(m2));
    assertFalse(m1.equals(m3));
    assertFalse(m1.equals(m4));
  }

  @Test
  void testCompareTo() {
    VirtualTransformedRenditionMetadata virtualRendition = new VirtualTransformedRenditionMetadata(rendition, 108, 51,
        null, new CropDimension(5, 5, 30, 25), null);
    RenditionMetadata biggerRendition = new RenditionMetadata(rendition);
    assertEquals(-2, virtualRendition.compareTo(originalRendition));
    assertEquals(-2, virtualRendition.compareTo(biggerRendition));
    assertEquals(0, virtualRendition.compareTo(virtualRendition));
    assertEquals(2, originalRendition.compareTo(virtualRendition));
    assertEquals(-1, originalRendition.compareTo(biggerRendition));
    assertEquals(0, originalRendition.compareTo(originalRendition));
    assertEquals(2, biggerRendition.compareTo(virtualRendition));
    assertEquals(1, biggerRendition.compareTo(originalRendition));
    assertEquals(0, biggerRendition.compareTo(biggerRendition));
  }

}
