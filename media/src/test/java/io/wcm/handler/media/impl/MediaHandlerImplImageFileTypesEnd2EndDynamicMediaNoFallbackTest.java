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
package io.wcm.handler.media.impl;

import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.DynamicMediaSupportServiceImpl.ASSETS_SCENE7_FEATURE_FLAG_PID;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.commons.io.FilenameUtils;
import org.apache.sling.featureflags.impl.ConfiguredFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Constants;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.Media;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.DynamicMediaSupportServiceImpl;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

/**
 * Executes the same "end-to-end" as {@link MediaHandlerImplImageFileTypesEnd2EndTest}, but
 * with rendering via dynamic media. As for some cases that are not suited for dynamic media
 * standard media handling delivery is used, this method overrides only the test cases where
 * scene7 is actually used.
 */
@ExtendWith(AemContextExtension.class)
class MediaHandlerImplImageFileTypesEnd2EndDynamicMediaNoFallbackTest extends MediaHandlerImplImageFileTypesEnd2EndTest {

  @Override
  @BeforeEach
  void setUp() {
    // activate dynamic media
    context.registerInjectActivateService(new ConfiguredFeature(),
        "name", ASSETS_SCENE7_FEATURE_FLAG_PID,
        "enabled", true);
    // disable AEM fallback
    context.registerInjectActivateService(new DynamicMediaSupportServiceImpl(),
        Constants.SERVICE_RANKING, 100,
        "disableAemFallback", true);
    super.setUp();
  }

  @Override
  @Test
  void testAsset_JPEG_Original() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testAsset_JPEG_Original_ContentDisposition() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testAsset_JPEG_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testAsset_JPEG_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testFileUpload_JPEG_Original() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_JPEG_Original();
  }

  @Override
  @Test
  void testFileUpload_JPEG_Original_ContentDisposition() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_JPEG_Original_ContentDisposition();
  }

  @Override
  @Test
  void testFileUpload_JPEG_Rescale() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_JPEG_Rescale();
  }

  @Override
  @Test
  void testFileUpload_JPEG_AutoCrop() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_JPEG_AutoCrop();
  }

  @Override
  @Test
  void testAsset_GIF_Original() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testAsset_GIF_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testAsset_GIF_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testFileUpload_GIF_Original() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_GIF_Original();
  }

  @Override
  @Test
  void testFileUpload_GIF_Rescale() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_GIF_Rescale();
  }

  @Override
  @Test
  void testFileUpload_GIF_AutoCrop() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_GIF_AutoCrop();
  }

  @Override
  @Test
  void testAsset_PNG_Original() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testAsset_PNG_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testAsset_PNG_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testFileUpload_PNG_Original() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_PNG_Original();
  }

  @Override
  @Test
  void testFileUpload_PNG_Rescale() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_PNG_Rescale();
  }

  @Override
  @Test
  void testFileUpload_PNG_AutoCrop() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_PNG_AutoCrop();
  }

  @Override
  @Test
  void testAsset_TIFF_Original() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testAsset_TIFF_Original_ContentDisposition() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testAsset_TIFF_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testAsset_TIFF_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testFileUpload_TIFF_Original() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_TIFF_Original();
  }

  @Override
  @Test
  void testFileUpload_TIFF_Original_ContentDisposition() {
    // fallback to media handler rendering: content disposition header not supported by scene7
    super.testFileUpload_TIFF_Original_ContentDisposition();
  }

  @Override
  @Test
  void testFileUpload_TIFF_Rescale() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_TIFF_Rescale();
  }

  @Override
  @Test
  void testFileUpload_TIFF_AutoCrop() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_TIFF_AutoCrop();
  }

  @Override
  @Test
  void testAsset_SVG_Original() {
    Asset asset = createSampleAsset("/filetype/sample.svg", ContentType.SVG);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testAsset_SVG_Original_ContentDisposition() {
    Asset asset = createSampleAsset("/filetype/sample.svg", ContentType.SVG);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testAsset_SVG_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.svg", ContentType.SVG);
    Media media = mediaHandler.get(asset.getPath()).build();
    assertFalse(media.isValid());
  }

  @Override
  @Test
  void testAsset_SVG_AutoCrop() {
    // fallback to media handler rendering: SVG is scaled by browser directly
    super.testAsset_SVG_AutoCrop();
  }

  @Override
  @Test
  void testFileUpload_SVG_Original() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_SVG_Original();
  }

  @Override
  @Test
  void testFileUpload_SVG_Original_ContentDisposition() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_SVG_Original_ContentDisposition();
  }

  @Override
  @Test
  void testFileUpload_SVG_Rescale() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_SVG_Rescale();
  }

  @Override
  @Test
  void testFileUpload_SVG_AutoCrop() {
    // fallback to media handler rendering: only DAM assets supported by scene7
    super.testFileUpload_SVG_AutoCrop();
  }

  @Override
  Asset createSampleAsset(String classpathResource, String contentType) {
    String fileName = FilenameUtils.getName(classpathResource);
    String fileExtension = FilenameUtils.getExtension(classpathResource);
    Asset asset = context.create().asset("/content/dam/" + fileName, classpathResource, contentType);
    context.create().assetRendition(asset, "cq5dam.web.sample." + fileExtension, classpathResource, contentType);
    return asset;
  }

}
