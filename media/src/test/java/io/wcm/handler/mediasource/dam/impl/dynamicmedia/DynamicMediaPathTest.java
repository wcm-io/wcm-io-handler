/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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
package io.wcm.handler.mediasource.dam.impl.dynamicmedia;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.ImageProfileImpl.CROP_TYPE_SMART;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.ImageProfileImpl.PN_BANNER;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.ImageProfileImpl.PN_CROP_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.scene7.api.constants.Scene7Constants;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.mediasource.dam.impl.DamContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
class DynamicMediaPathTest {

  private final AemContext context = AppAemContext.newAemContext();

  private DamContext damContext;

  @BeforeEach
  void setUp() {
    Resource profile1 = context.create().resource("/conf/global/settings/dam/adminui-extension/imageprofile/profile1",
        PN_CROP_TYPE, CROP_TYPE_SMART,
        PN_BANNER, "Crop-1,90,60|Crop-2,30,20");

    Resource assetFolder = context.create().resource("/content/dam/folder1");
    context.create().resource(assetFolder, JCR_CONTENT, DamConstants.IMAGE_PROFILE, profile1.getPath());

    Asset asset = context.create().asset(assetFolder.getPath() + "/test.jpg", 50, 30, ContentType.JPEG,
        Scene7Constants.PN_S7_FILE, "DummyFolder/test.jpg");
    DynamicMediaSupportService dynamicMediaSupportService = context.getService(DynamicMediaSupportService.class);
    damContext = new DamContext(asset, dynamicMediaSupportService, context.request());
  }

  @Test
  void testWidthHeight() {
    String result = DynamicMediaPath.build(damContext, 30, 20);
    assertEquals("DummyFolder/test.jpg?wid=30&hei=20&fit=stretch", result);
  }

  @Test
  void testCrop() {
    String result = DynamicMediaPath.build(damContext, 30, 20, new CropDimension(5, 2, 10, 8), null);
    assertEquals("DummyFolder/test.jpg?crop=5,2,10,8&wid=30&hei=20&fit=stretch", result);
  }

  @Test
  void testAutoCrop_SmartCrop() {
    String result = DynamicMediaPath.build(damContext, 30, 20, new CropDimension(5, 2, 10, 8, true), null);
    assertEquals("DummyFolder/test.jpg%3ACrop-2", result);
  }

  @Test
  void testWidthHeight_MaxWidth() {
    String result = DynamicMediaPath.build(damContext, 5000, 2500);
    assertEquals("DummyFolder/test.jpg?wid=4000&hei=2000&fit=stretch", result);
  }

  @Test
  void testWidthHeight_MaxHeight() {
    String result = DynamicMediaPath.build(damContext, 2500, 5000);
    assertEquals("DummyFolder/test.jpg?wid=2000&hei=4000&fit=stretch", result);
  }

  @Test
  void testWidthHeight_MaxWidthHeight() {
    String result = DynamicMediaPath.build(damContext, 6000, 8000);
    assertEquals("DummyFolder/test.jpg?wid=3000&hei=4000&fit=stretch", result);
  }

  @Test
  void testRotate() {
    String result = DynamicMediaPath.build(damContext, 30, 20, null, 180);
    assertEquals("DummyFolder/test.jpg?rotate=180&wid=30&hei=20&fit=stretch", result);
  }

  @Test
  void testCropRotate() {
    String result = DynamicMediaPath.build(damContext, 30, 20, new CropDimension(5, 2, 10, 8), 90);
    assertEquals("DummyFolder/test.jpg?crop=5,2,10,8&rotate=90&wid=30&hei=20&fit=stretch", result);
  }

  @Test
  void testAutoCropRotate_NoSmartCrop() {
    String result = DynamicMediaPath.build(damContext, 30, 20, new CropDimension(5, 2, 10, 8, true), 90);
    assertEquals("DummyFolder/test.jpg?crop=5,2,10,8&rotate=90&wid=30&hei=20&fit=stretch", result);
  }

}
