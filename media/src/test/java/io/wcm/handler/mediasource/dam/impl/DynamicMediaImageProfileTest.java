/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static io.wcm.handler.mediasource.dam.impl.DynamicMediaImageProfile.CROP_TYPE_SMART;
import static io.wcm.handler.mediasource.dam.impl.DynamicMediaImageProfile.PN_BANNER;
import static io.wcm.handler.mediasource.dam.impl.DynamicMediaImageProfile.PN_CROP_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.scene7.api.constants.Scene7Constants;
import com.google.common.collect.ImmutableList;

import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.mediasource.dam.impl.DynamicMediaImageProfile.NamedDimension;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class DynamicMediaImageProfileTest {

  private final AemContext context = AppAemContext.newAemContext();

  @Test
  void testValidProfileWithCroppingPresets() {
    Resource profile1 = context.create().resource("/conf/global/settings/dam/adminui-extension/imageprofile/profile1",
        PN_CROP_TYPE, CROP_TYPE_SMART,
        PN_BANNER, "Crop-1,100,60|Crop-2,50,30");

    Resource assetFolder1 = context.create().resource("/content/dam/folder1");
    context.create().resource(assetFolder1, JCR_CONTENT, DamConstants.IMAGE_PROFILE, profile1.getPath());

    Asset asset1 = context.create().asset(assetFolder1.getPath() + "/asset1.jpg", 50, 30, ContentType.JPEG,
        Scene7Constants.PN_S7_FILE, "DummyFolder/asset1.jpg");

    DynamicMediaImageProfile underTest = DynamicMediaImageProfile.get(asset1);
    assertNotNull(underTest);
    assertEquals(ImmutableList.of(new NamedDimension("Crop-1", 100, 60), new NamedDimension("Crop-2", 50, 30)),
        underTest.getSmartCropDefinitions());
  }

  @Test
  void testValidProfileWithCroppingPresetsDefinedInParentFolder() {
    Resource profile1 = context.create().resource("/conf/global/settings/dam/adminui-extension/imageprofile/profile1",
        PN_CROP_TYPE, CROP_TYPE_SMART,
        PN_BANNER, "Crop-1,100,60|Crop-2,50,30");

    Resource assetFolder1 = context.create().resource("/content/dam/folder1");
    context.create().resource(assetFolder1, JCR_CONTENT, DamConstants.IMAGE_PROFILE, profile1.getPath());

    Resource assetFolder11 = context.create().resource("/content/dam/folder1/folder11");
    context.create().resource(assetFolder11, JCR_CONTENT);

    Asset asset1 = context.create().asset(assetFolder11.getPath() + "/asset1.jpg", 50, 30, ContentType.JPEG,
        Scene7Constants.PN_S7_FILE, "DummyFolder/asset1.jpg");

    DynamicMediaImageProfile underTest = DynamicMediaImageProfile.get(asset1);
    assertNotNull(underTest);
    assertEquals(ImmutableList.of(new NamedDimension("Crop-1", 100, 60), new NamedDimension("Crop-2", 50, 30)),
        underTest.getSmartCropDefinitions());
  }

  @Test
  void testValidProfileWithInvalidCroppingPresets() {
    Resource profile1 = context.create().resource("/conf/global/settings/dam/adminui-extension/imageprofile/profile1",
        PN_CROP_TYPE, CROP_TYPE_SMART,
        PN_BANNER, "Crop-0|Crop-1,100|Crop-2,50,30,1234|Crop-3,jodel,kaiser|Crop-4,10,20|");

    Resource assetFolder1 = context.create().resource("/content/dam/folder1");
    context.create().resource(assetFolder1, JCR_CONTENT, DamConstants.IMAGE_PROFILE, profile1.getPath());

    Asset asset1 = context.create().asset(assetFolder1.getPath() + "/asset1.jpg", 50, 30, ContentType.JPEG,
        Scene7Constants.PN_S7_FILE, "DummyFolder/asset1.jpg");

    DynamicMediaImageProfile underTest = DynamicMediaImageProfile.get(asset1);
    assertNotNull(underTest);
    assertEquals(ImmutableList.of(new NamedDimension("Crop-4", 10, 20)), underTest.getSmartCropDefinitions());
  }

  @Test
  void testInvalidProfile() {
    Resource assetFolder1 = context.create().resource("/content/dam/folder1");
    context.create().resource(assetFolder1, JCR_CONTENT,
        DamConstants.IMAGE_PROFILE, "/conf/global/settings/dam/adminui-extension/imageprofile/profile1");

    Asset asset1 = context.create().asset(assetFolder1.getPath() + "/asset1.jpg", 50, 30, ContentType.JPEG,
        Scene7Constants.PN_S7_FILE, "DummyFolder/asset1.jpg");

    DynamicMediaImageProfile underTest = DynamicMediaImageProfile.get(asset1);
    assertNull(underTest);
  }

  @Test
  void testMissingProfile() {
    Resource assetFolder1 = context.create().resource("/content/dam/folder1");
    context.create().resource(assetFolder1, JCR_CONTENT);

    Asset asset1 = context.create().asset(assetFolder1.getPath() + "/asset1.jpg", 50, 30, ContentType.JPEG,
        Scene7Constants.PN_S7_FILE, "DummyFolder/asset1.jpg");

    DynamicMediaImageProfile underTest = DynamicMediaImageProfile.get(asset1);
    assertNull(underTest);
  }

}
