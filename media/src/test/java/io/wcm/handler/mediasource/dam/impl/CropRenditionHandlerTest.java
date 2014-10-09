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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.mediasource.dam.AbstractDamTest;

import org.junit.Before;
import org.junit.Test;

import com.day.cq.dam.api.Asset;

/**
 * Tests for {@link CropRenditionHandler}
 */
public class CropRenditionHandlerTest extends AbstractDamTest {

  private CropRenditionHandler underTest;
  private RenditionMetadata originalRendition;

  @Before
  public void setUp() throws Exception {
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    Asset asset = ((DamAsset)media.getAsset()).getDamAsset();

    underTest = new CropRenditionHandler(asset, new CropDimension(0, 0, 960, 315));

    originalRendition = new RenditionMetadata(asset.getRendition("original"));
    assertNotNull(originalRendition);
  }

  /**
   * Tests if the candidates contain 2 renditions
   */
  @Test
  public void testNumberOfCandidates() {
    assertEquals("there must be two candidates", 2, underTest.getAvailableRenditions().size());
  }

  /**
   * Tests if the candidates contain 2 renditions
   */
  @Test
  public void testCandidatesContainOriginalRendition() {
    assertTrue("candidates must contain original rendition", underTest.getAvailableRenditions().contains(originalRendition));
  }

}
