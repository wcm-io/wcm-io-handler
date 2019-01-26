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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.image.Layer;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.mediasource.dam.AbstractDamTest;

@SuppressWarnings("null")
public class VirtualCropRotateRenditionMetadataTest extends AbstractDamTest {

  private Rendition rendition;
  private RenditionMetadata originalRendition;

  @Before
  public void setUp() throws Exception {
    String path = MEDIAITEM_PATH_STANDARD + "/jcr:content/renditions/cq5dam.thumbnail.215.102.jpg";
    context.resourceResolver().delete(context.resourceResolver().getResource(path));
    context.load().binaryFile("/sample_image_215x102.jpg", path);

    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    Asset asset = media.getAsset().adaptTo(Asset.class);
    rendition = asset.getRendition("cq5dam.thumbnail.215.102.jpg");
    originalRendition = new RenditionMetadata(asset.getRendition("original"));
  }

  @Test
  public void testGetLayer_cropping() throws Exception {
    VirtualCropRotateRenditionMetadata underTest = new VirtualCropRotateRenditionMetadata(rendition, 30, 25,
        new CropDimension(5, 5, 30, 25), null);

    Layer layer = underTest.getLayer();
    assertEquals(30, layer.getWidth());
    assertEquals(25, layer.getHeight());
  }

  @Test
  public void testGetLayer_cropping_resize() throws Exception {
    VirtualCropRotateRenditionMetadata underTest = new VirtualCropRotateRenditionMetadata(rendition, 30, 25,
        new CropDimension(5, 5, 60, 50), null);

    Layer layer = underTest.getLayer();
    assertEquals(30, layer.getWidth());
    assertEquals(25, layer.getHeight());
  }

  @Test
  public void testGetLayer_rotation() throws Exception {
    VirtualCropRotateRenditionMetadata underTest = new VirtualCropRotateRenditionMetadata(rendition, 102, 215,
        null, 90);

    Layer layer = underTest.getLayer();
    assertEquals(102, layer.getWidth());
    assertEquals(215, layer.getHeight());
  }

  @Test
  public void testGetLayer_cropping_rotation() throws Exception {
    VirtualCropRotateRenditionMetadata underTest = new VirtualCropRotateRenditionMetadata(rendition, 25, 30,
        new CropDimension(5, 5, 25, 30), 180);

    Layer layer = underTest.getLayer();
    assertEquals(25, layer.getWidth());
    assertEquals(30, layer.getHeight());
  }

  @Test
  public void testGetInputStream() throws Exception {
    VirtualCropRotateRenditionMetadata underTest = new VirtualCropRotateRenditionMetadata(rendition, 108, 51,
        new CropDimension(5, 5, 30, 25), null);

    InputStream is = underTest.getInputStream();
    assertNull(is);
  }

  @Test
  public void testEquals() {
    VirtualCropRotateRenditionMetadata m1 = new VirtualCropRotateRenditionMetadata(rendition, 108, 51,
        new CropDimension(5, 5, 30, 25), null);
    VirtualCropRotateRenditionMetadata m2 = new VirtualCropRotateRenditionMetadata(rendition, 108, 51,
        new CropDimension(5, 5, 30, 25), null);
    VirtualCropRotateRenditionMetadata m3 = new VirtualCropRotateRenditionMetadata(rendition, 108, 51,
        new CropDimension(10, 10, 30, 25), null);
    VirtualCropRotateRenditionMetadata m4 = new VirtualCropRotateRenditionMetadata(rendition, 108, 51,
        new CropDimension(5, 5, 30, 25), 180);

    assertTrue(m1.equals(m2));
    assertFalse(m1.equals(m3));
    assertFalse(m1.equals(m4));
  }

  @Test
  public void testCompareTo() {
    VirtualCropRotateRenditionMetadata virtualRendition = new VirtualCropRotateRenditionMetadata(rendition, 108, 51,
        new CropDimension(5, 5, 30, 25), null);
    RenditionMetadata biggerRendition = new RenditionMetadata(rendition);
    assertEquals(-1, virtualRendition.compareTo(originalRendition));
    assertEquals(-1, virtualRendition.compareTo(biggerRendition));
    assertEquals(1, originalRendition.compareTo(virtualRendition));
    assertEquals(1, biggerRendition.compareTo(virtualRendition));
  }

}
