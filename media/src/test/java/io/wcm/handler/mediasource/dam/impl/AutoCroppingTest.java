/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import static com.day.cq.dam.api.DamConstants.PREFIX_ASSET_WEB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class AutoCroppingTest {

  private final AemContext context = AppAemContext.newAemContext();

  @Test
  void testCalculateAutoCropDimension_AdaptWidth() {
    CropDimension result = AutoCropping.calculateAutoCropDimension(180, 90, 16d / 9d);
    assertEquals(10, result.getLeft());
    assertEquals(0, result.getTop());
    assertEquals(160, result.getWidth());
    assertEquals(90, result.getHeight());
  }

  @Test
  void testCalculateAutoCropDimension_AdaptHeight() {
    CropDimension result = AutoCropping.calculateAutoCropDimension(160, 100, 16d / 9d);
    assertEquals(0, result.getLeft());
    assertEquals(5, result.getTop());
    assertEquals(160, result.getWidth());
    assertEquals(90, result.getHeight());
  }

  @Test
  @SuppressWarnings("null")
  void testGetWebRenditionForCropping() {
    Asset asset = context.create().asset("/content/dam/asset1.jpg", 160, 90, "image/jpeg");
    Rendition webRendition = context.create().assetRendition(asset, PREFIX_ASSET_WEB + ".80.45.jpg", 80, 45, "image/jpeg");

    RenditionMetadata result = AutoCropping.getWebRenditionForCropping(asset);
    assertEquals(webRendition.getPath(), result.getRendition().getPath());
  }

  @Test
  void testGetWebRenditionNotExisting() {
    Asset assetWithoutRenditions = context.create().asset("/content/dam/asset2.jpg", 160, 90, "image/jpeg");
    assertNull(AutoCropping.getWebRenditionForCropping(assetWithoutRenditions));
  }


}
