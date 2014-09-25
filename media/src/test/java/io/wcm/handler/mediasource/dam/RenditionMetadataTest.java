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
package io.wcm.handler.mediasource.dam;

import static org.junit.Assert.assertNotNull;
import io.wcm.handler.media.MediaMetadata;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.day.cq.dam.api.Asset;

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
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(MEDIAITEM_PATH_STANDARD);
    Asset asset = ((DamMediaItem)mediaMetadata.getMediaItem()).getDamAsset();

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
    Assert.assertTrue("original rendition is not eqaul to itself", originalRendition.compareTo(originalRendition) == 0);
  }

  /**
   * Small and big rendition
   */
  @Test
  public void testCompareSmallToBigRendition() {
    Assert.assertTrue("smaller rendition is not smaller", smallestRendition.compareTo(biggestRendition) == -1);
  }

  /**
   * Big and small rendition
   */
  @Test
  public void testCompareBigToSmallRendition() {
    Assert.assertTrue("bigger rendition is not bigger", biggestRendition.compareTo(smallestRendition) == 1);
  }

  /**
   * Two equal renditions
   */
  @Test
  public void testCompareTwoEqualRenditions() {
    Assert.assertTrue("two equal renditions are not equal", biggestRendition.compareTo(biggestRendition) == 0);
  }

  /**
   * Original rendition and a rendition, with the same dimension as original rendition
   */
  @Test
  public void testCompareOriginalRenditionToEqualRendition() {
    Assert.assertTrue("original rendition is not preferred over the equal rendition", originalRendition.compareTo(originalRenditionCopy) == -1);
    Assert.assertTrue("original rendition is not preferred over the equal rendition", originalRenditionCopy.compareTo(originalRendition) == 1);
  }
}
