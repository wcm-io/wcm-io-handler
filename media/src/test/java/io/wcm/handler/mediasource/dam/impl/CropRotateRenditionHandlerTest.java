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

import org.junit.Before;
import org.junit.Test;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.mediasource.dam.AbstractDamTest;

/**
 * Tests for {@link CropRotateRenditionHandler}
 */
@SuppressWarnings("null")
public class CropRotateRenditionHandlerTest extends AbstractDamTest {

  private Asset asset;
  private RenditionMetadata originalRendition;
  private CropDimension cropDimension;
  private CropDimension cropDimensionRotated;

  @Before
  public void setUp() throws Exception {
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    asset = media.getAsset().adaptTo(Asset.class);

    originalRendition = new RenditionMetadata(asset.getRendition("original"));
    assertNotNull(originalRendition);

    cropDimension = new CropDimension(0, 0, 960, 315);
    cropDimensionRotated = new CropDimension(0, 0, 315, 960);
  }

  /**
   * Tests if the candidates contain 7 renditions (without asset thumbnails)
   */
  @Test
  public void testCroppingNumberOfCandidates() {
    CropRotateRenditionHandler underTest = new CropRotateRenditionHandler(asset, cropDimension, null);
    assertEquals("candidates without thumbnails", 7, underTest.getAvailableRenditions(new MediaArgs()).size());
  }

  /**
   * Tests if the candidates contain 8 renditions (with asset thumbnails)
   */
  @Test
  public void testCroppingNumberOfCandidatesWithThumbnails() {
    CropRotateRenditionHandler underTest = new CropRotateRenditionHandler(asset, cropDimension, null);
    assertEquals("candidates with thumbnails", 8, underTest.getAvailableRenditions(new MediaArgs().includeAssetThumbnails(true)).size());
  }

  @Test
  public void testCroppingFirstCandidateIsVirtualRendition() {
    CropRotateRenditionHandler underTest = new CropRotateRenditionHandler(asset, cropDimension, null);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/test/standard.jpg/jcr:content/renditions/cq5dam.web.1280.1024.jpg.image_file.960.315.0,0,960,315.file/cq5dam.web.1280.1024.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testCroppingCandidatesContainOriginalRendition() {
    CropRotateRenditionHandler underTest = new CropRotateRenditionHandler(asset, cropDimension, null);
    assertTrue("candidates must contain original rendition", underTest.getAvailableRenditions(new MediaArgs()).contains(originalRendition));
  }

  @Test
  public void testRotation90WithoutCropping() {
    CropRotateRenditionHandler underTest = new CropRotateRenditionHandler(asset, null, 90);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/test/standard.jpg/jcr:content/renditions/cq5dam.web.1280.1024.jpg.image_file.1024.1280.-.90.file/cq5dam.web.1280.1024.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testRotation180WithoutCropping() {
    CropRotateRenditionHandler underTest = new CropRotateRenditionHandler(asset, null, 180);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/test/standard.jpg/jcr:content/renditions/cq5dam.web.1280.1024.jpg.image_file.1280.1024.-.180.file/cq5dam.web.1280.1024.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testRotation270WithoutCropping() {
    CropRotateRenditionHandler underTest = new CropRotateRenditionHandler(asset, null, 270);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/test/standard.jpg/jcr:content/renditions/cq5dam.web.1280.1024.jpg.image_file.1024.1280.-.270.file/cq5dam.web.1280.1024.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testInvalidRotationWithoutCropping() {
    CropRotateRenditionHandler underTest = new CropRotateRenditionHandler(asset, null, 45);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/test/standard.jpg/jcr:content/renditions/original./standard.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testRotation90WithCropping() {
    CropRotateRenditionHandler underTest = new CropRotateRenditionHandler(asset, cropDimensionRotated, 90);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/test/standard.jpg/jcr:content/renditions/cq5dam.web.960.455.jpg.image_file.315.960.0,0,315,960.90.file/cq5dam.web.960.455.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testRotation180WithCropping() {
    CropRotateRenditionHandler underTest = new CropRotateRenditionHandler(asset, cropDimension, 180);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals(
        "/content/dam/test/standard.jpg/jcr:content/renditions/cq5dam.web.960.455.jpg.image_file.960.315.0,0,960,315.180.file/cq5dam.web.960.455.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testRotation270WithCropping() {
    CropRotateRenditionHandler underTest = new CropRotateRenditionHandler(asset, cropDimensionRotated, 270);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/test/standard.jpg/jcr:content/renditions/cq5dam.web.960.455.jpg.image_file.315.960.0,0,315,960.270.file/cq5dam.web.960.455.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testInvalidRotationWithCropping() {
    CropRotateRenditionHandler underTest = new CropRotateRenditionHandler(asset, cropDimension, 45);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/test/standard.jpg/jcr:content/renditions/cq5dam.web.1280.1024.jpg.image_file.960.315.0,0,960,315.file/cq5dam.web.1280.1024.jpg",
        firstRendition.getMediaPath(false));
  }

}
