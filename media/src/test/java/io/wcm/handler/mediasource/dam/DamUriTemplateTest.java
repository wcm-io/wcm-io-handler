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

import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.DynamicMediaSupportServiceImpl.ASSETS_SCENE7_FEATURE_FLAG_PID;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.io.FilenameUtils;
import org.apache.sling.featureflags.impl.ConfiguredFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.scene7.api.constants.Scene7Constants;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Test DAM URI template
 */
@ExtendWith(AemContextExtension.class)
class DamUriTemplateTest {

  final AemContext context = AppAemContext.newAemContext();

  private MediaHandler mediaHandler;
  private Asset asset;

  @BeforeEach
  void setUp() {
    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
    asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
  }

  @Test
  void testGetUriTemplate_CropCenter() {
    Media media = mediaHandler.get(asset.getPath()).build();

    UriTemplate uriTemplate = media.getAsset().getUriTemplate(UriTemplateType.CROP_CENTER);
    assertEquals("/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.{height}.file/sample.jpg", uriTemplate.getUriTemplate());
    assertEquals(100, uriTemplate.getMaxWidth());
    assertEquals(50, uriTemplate.getMaxHeight());
    assertEquals(UriTemplateType.CROP_CENTER, uriTemplate.getType());
  }

  @Test
  void testGetUriTemplate_CropCenter_EnforceOutputFileExtension() {
    Media media = mediaHandler.get(asset.getPath())
        .enforceOutputFileExtension(FileExtension.PNG)
        .build();

    UriTemplate uriTemplate = media.getAsset().getUriTemplate(UriTemplateType.CROP_CENTER);
    assertEquals("/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.{height}.file/sample.png", uriTemplate.getUriTemplate());
    assertEquals(100, uriTemplate.getMaxWidth());
    assertEquals(50, uriTemplate.getMaxHeight());
    assertEquals(UriTemplateType.CROP_CENTER, uriTemplate.getType());
  }

  @Test
  void testGetUriTemplate_ScaleWidth() {
    Media media = mediaHandler.get(asset.getPath()).build();

    UriTemplate uriTemplate = media.getAsset().getUriTemplate(UriTemplateType.SCALE_WIDTH);
    assertEquals("/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.0.file/sample.jpg", uriTemplate.getUriTemplate());
    assertEquals(100, uriTemplate.getMaxWidth());
    assertEquals(50, uriTemplate.getMaxHeight());
    assertEquals(UriTemplateType.SCALE_WIDTH, uriTemplate.getType());
  }

  @Test
  void testGetUriTemplate_ScaleHeight() {
    Media media = mediaHandler.get(asset.getPath()).build();

    UriTemplate uriTemplate = media.getAsset().getUriTemplate(UriTemplateType.SCALE_HEIGHT);
    assertEquals("/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.0.{height}.file/sample.jpg", uriTemplate.getUriTemplate());
    assertEquals(100, uriTemplate.getMaxWidth());
    assertEquals(50, uriTemplate.getMaxHeight());
    assertEquals(UriTemplateType.SCALE_HEIGHT, uriTemplate.getType());
  }

  @Test
  void testGetUriTemplate_CropCenter_DynamicMedia() {
    // activate dynamic media
    context.registerInjectActivateService(new ConfiguredFeature(),
        "name", ASSETS_SCENE7_FEATURE_FLAG_PID,
        "enabled", true);

    Media media = mediaHandler.get(asset.getPath()).build();

    UriTemplate uriTemplate = media.getAsset().getUriTemplate(UriTemplateType.CROP_CENTER);
    assertEquals("https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?wid={width}&hei={height}&fit=crop", uriTemplate.getUriTemplate());
    assertEquals(100, uriTemplate.getMaxWidth());
    assertEquals(50, uriTemplate.getMaxHeight());
    assertEquals(UriTemplateType.CROP_CENTER, uriTemplate.getType());
  }

  @Test
  void testGetUriTemplate_ScaleWidth_DynamicMedia() {
    // activate dynamic media
    context.registerInjectActivateService(new ConfiguredFeature(),
        "name", ASSETS_SCENE7_FEATURE_FLAG_PID,
        "enabled", true);

    Media media = mediaHandler.get(asset.getPath()).build();

    UriTemplate uriTemplate = media.getAsset().getUriTemplate(UriTemplateType.SCALE_WIDTH);
    assertEquals("https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?wid={width}", uriTemplate.getUriTemplate());
    assertEquals(100, uriTemplate.getMaxWidth());
    assertEquals(50, uriTemplate.getMaxHeight());
    assertEquals(UriTemplateType.SCALE_WIDTH, uriTemplate.getType());
  }

  @Test
  void testGetUriTemplate_ScaleHeight_DynamicMedia() {
    // activate dynamic media
    context.registerInjectActivateService(new ConfiguredFeature(),
        "name", ASSETS_SCENE7_FEATURE_FLAG_PID,
        "enabled", true);

    Media media = mediaHandler.get(asset.getPath()).build();

    UriTemplate uriTemplate = media.getAsset().getUriTemplate(UriTemplateType.SCALE_HEIGHT);
    assertEquals("https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?hei={height}", uriTemplate.getUriTemplate());
    assertEquals(100, uriTemplate.getMaxWidth());
    assertEquals(50, uriTemplate.getMaxHeight());
    assertEquals(UriTemplateType.SCALE_HEIGHT, uriTemplate.getType());
  }

  Asset createSampleAsset(String classpathResource, String contentType) {
    String fileName = FilenameUtils.getName(classpathResource);
    return context.create().asset("/content/dam/" + fileName, classpathResource, contentType,
        Scene7Constants.PN_S7_FILE, "DummyFolder/" + fileName);
  }

}
