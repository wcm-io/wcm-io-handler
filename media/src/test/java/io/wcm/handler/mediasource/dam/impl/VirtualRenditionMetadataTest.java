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

import io.wcm.handler.media.Media;
import io.wcm.handler.mediasource.dam.AbstractDamTest;

@SuppressWarnings("null")
class VirtualRenditionMetadataTest extends AbstractDamTest {

  private VirtualRenditionMetadata underTest;
  private Rendition rendition;

  @BeforeEach
  void setUp() throws Exception {
    String path = MEDIAITEM_PATH_STANDARD + "/jcr:content/renditions/cq5dam.thumbnail.215.102.jpg";
    context.resourceResolver().delete(context.resourceResolver().getResource(path));
    context.load().binaryFile("/sample_image_215x102.jpg", path);

    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    Asset asset = media.getAsset().adaptTo(Asset.class);
    rendition = asset.getRendition("cq5dam.thumbnail.215.102.jpg");
    underTest = new VirtualRenditionMetadata(rendition, 108, 51, null);
  }

  @Test
  void testGetLayer() throws Exception {
    Layer layer = underTest.getLayer();
    assertEquals(108, layer.getWidth());
    assertEquals(51, layer.getHeight());
  }

  @Test
  void testGetInputStream() throws Exception {
    InputStream is = underTest.getInputStream();
    assertNull(is);
  }

  @Test
  void testEquals() {
    VirtualRenditionMetadata m1 = new VirtualRenditionMetadata(rendition, 108, 51, null);
    VirtualRenditionMetadata m2 = new VirtualRenditionMetadata(rendition, 108, 51, null);
    VirtualRenditionMetadata m3 = new VirtualRenditionMetadata(rendition, 10, 20, null);

    assertTrue(m1.equals(m2));
    assertFalse(m1.equals(m3));
  }


}
