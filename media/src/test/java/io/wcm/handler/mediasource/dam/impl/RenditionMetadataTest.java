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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.wcm.handler.media.Media;
import io.wcm.handler.mediasource.dam.AbstractDamTest;

import java.io.InputStream;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Test;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.image.Layer;

/**
 * Tests the {@link RenditionMetadata}, especially the compareTo method
 */
public class RenditionMetadataTest extends AbstractDamTest {

  private RenditionMetadata originalRendition;
  private RenditionMetadata originalRenditionCopy;
  private RenditionMetadata smallestRendition;
  private RenditionMetadata biggestRendition;

  @Before
  public void setUp() throws Exception {
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    Asset asset = media.getAsset().adaptTo(Asset.class);

    originalRendition = new RenditionMetadata(asset.getRendition("original"));
    assertNotNull(originalRendition);

    originalRenditionCopy = new RenditionMetadata(asset.getRendition("cq5dam.web.215.102.jpg"));
    assertNotNull(originalRenditionCopy);

    smallestRendition = new RenditionMetadata(asset.getRendition("cq5dam.web.450.213.jpg"));
    assertNotNull(smallestRendition);

    biggestRendition = new RenditionMetadata(asset.getRendition("cq5dam.web.960.455.jpg"));
    assertNotNull(biggestRendition);
  }

  /**
   * Two original renditions
   */
  @Test
  public void testCompareOriginalRenditionToItself() {
    assertTrue("original rendition is not eqaul to itself", originalRendition.compareTo(originalRendition) == 0);
  }

  /**
   * Small and big rendition
   */
  @Test
  public void testCompareSmallToBigRendition() {
    assertTrue("smaller rendition is not smaller", smallestRendition.compareTo(biggestRendition) == -1);
  }

  /**
   * Big and small rendition
   */
  @Test
  public void testCompareBigToSmallRendition() {
    assertTrue("bigger rendition is not bigger", biggestRendition.compareTo(smallestRendition) == 1);
  }

  /**
   * Two equal renditions
   */
  @Test
  public void testCompareTwoEqualRenditions() {
    assertTrue("two equal renditions are not equal", biggestRendition.compareTo(biggestRendition) == 0);
  }

  /**
   * Original rendition and a rendition, with the same dimension as original rendition
   */
  @Test
  public void testCompareOriginalRenditionToEqualRendition() {
    assertTrue("original rendition is not preferred over the equal rendition", originalRendition.compareTo(originalRenditionCopy) == -1);
    assertTrue("original rendition is not preferred over the equal rendition", originalRenditionCopy.compareTo(originalRendition) == 1);
  }

  @Test
  public void testMatchesExact() {
    assertTrue(smallestRendition.matches(450, 213));
    assertFalse(smallestRendition.matches(500, 500));
    assertFalse(smallestRendition.matches(450, 500));
    assertFalse(smallestRendition.matches(500, 213));
    assertTrue(smallestRendition.matches(450, 0));
    assertTrue(smallestRendition.matches(0, 213));
    assertTrue(smallestRendition.matches(0, 0));
  }

  @Test
  public void testMatchesSpec() {
    assertTrue(smallestRendition.matches(450, 213, 450, 213, 0d));
    assertTrue(smallestRendition.matches(200, 100, 500, 300, 0d));
    assertTrue(smallestRendition.matches(0, 213, 0, 213, 0d));
    assertTrue(smallestRendition.matches(450, 0, 450, 0, 0d));
    assertTrue(smallestRendition.matches(0, 0, 450, 213, 0d));
    assertTrue(smallestRendition.matches(450, 213, 0, 0, 0d));

    assertFalse(smallestRendition.matches(500, 0, 0, 0, 0d));
    assertFalse(smallestRendition.matches(0, 500, 0, 0, 0d));
    assertFalse(smallestRendition.matches(0, 0, 100, 0, 0d));
    assertFalse(smallestRendition.matches(0, 0, 0, 100, 0d));

    assertTrue(smallestRendition.matches(0, 0, 0, 0, 2.11d));
    assertFalse(smallestRendition.matches(0, 0, 0, 0, 2.2d));
  }

  @Test
  public void testGetLayer() throws PersistenceException {
    loadImageBinary_originalRenditionCopy();
    Layer layer = originalRenditionCopy.getLayer();
    assertEquals(215, layer.getWidth());
    assertEquals(102, layer.getHeight());
  }

  @Test
  public void testGetInputStream() throws Exception {
    loadImageBinary_originalRenditionCopy();
    InputStream is = originalRenditionCopy.getInputStream();
    assertNotNull(is);
    is.close();
  }

  @Test
  public void testAdaptTo() throws Exception {
    loadImageBinary_originalRenditionCopy();
    String path = MEDIAITEM_PATH_STANDARD + "/jcr:content/renditions/cq5dam.web.215.102.jpg";

    Rendition damRendition = originalRenditionCopy.adaptTo(Rendition.class);
    assertEquals(path, damRendition.getPath());

    Resource resource = originalRenditionCopy.adaptTo(Resource.class);
    assertEquals(path, resource.getPath());

    Layer layer = originalRenditionCopy.adaptTo(Layer.class);
    assertEquals(215, layer.getWidth());
    assertEquals(102, layer.getHeight());

    InputStream is = originalRenditionCopy.adaptTo(InputStream.class);
    assertNotNull(is);
    is.close();
  }

  @Test
  public void testEquals() {
    assertTrue(smallestRendition.equals(smallestRendition));
    assertFalse(smallestRendition.equals(biggestRendition));
  }

  private void loadImageBinary_originalRenditionCopy() throws PersistenceException {
    String path = MEDIAITEM_PATH_STANDARD + "/jcr:content/renditions/cq5dam.web.215.102.jpg";
    context.resourceResolver().delete(context.resourceResolver().getResource(path));
    context.load().binaryFile("/sample_image_215x102.jpg", path);
  }

}
