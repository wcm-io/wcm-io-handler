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
package io.wcm.handler.media.impl;

import org.apache.sling.featureflags.impl.ConfiguredFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.entitlement.api.EntitlementConstants;

import io.wcm.handler.mediasource.dam.impl.dynamicmedia.DynamicMediaPath;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

/**
 * Executes the same "end-to-end" as {@link MediaHandlerImplImageFileTypesEnd2EndTest}, but
 * with rendering via dynamic media. As for some cases that are not suited for dynamic media
 * standard media handling delivery is used, this method overrides only the test cases where
 * scene7 is actually used.
 */
@ExtendWith(AemContextExtension.class)
class MediaHandlerImplImageFileTypesEnd2EndDynamicMediaTest extends MediaHandlerImplImageFileTypesEnd2EndTest {

  @Override
  @BeforeEach
  void setUp() {
    // activate dynamic media
    context.registerInjectActivateService(new ConfiguredFeature(),
        "name", EntitlementConstants.ASSETS_SCENE7_FEATURE_FLAG_PID,
        "enabled", true);
    super.setUp();
  }

  @Override
  @Test
  void testAsset_JPEG_Original() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia(asset, 100, 50,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?wid=100&hei=50&fit=stretch",
        ContentType.JPEG);
  }

  @Test
  void testAsset_JPEG_Original_DynamicMediaDisabled() {
    // disabling dynamic media produced media handler URL
    dynamicMediaDisabled = true;
    super.testAsset_JPEG_Original();
  }

  @Override
  @Test
  void testAsset_JPEG_Original_ContentDisposition() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia_ContentDisposition(asset, 100, 50,
        "https://dummy.scene7.com/is/content/DummyFolder/sample.jpg" + DynamicMediaPath.DOWNLOAD_SUFFIX,
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_JPEG_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia_Rescale(asset, 80, 40,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?wid=80&hei=40&fit=stretch",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_JPEG_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?crop=25,0,50,50&wid=50&hei=50&fit=stretch",
        ContentType.JPEG);
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
    buildAssertMedia(asset, 100, 50,
        "https://dummy.scene7.com/is/content/DummyFolder/sample.gif",
        ContentType.GIF);
  }

  @Override
  @Test
  void testAsset_GIF_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    buildAssertMedia_Rescale(asset, 80, 40,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.gif?wid=80&hei=40&fit=stretch",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_GIF_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.gif?crop=25,0,50,50&wid=50&hei=50&fit=stretch",
        ContentType.JPEG);
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
    buildAssertMedia(asset, 100, 50,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.png?wid=100&hei=50&fit=stretch",
        ContentType.PNG);
  }

  @Override
  @Test
  void testAsset_PNG_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    buildAssertMedia_Rescale(asset, 80, 40,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.png?wid=80&hei=40&fit=stretch",
        ContentType.PNG);
  }

  @Override
  @Test
  void testAsset_PNG_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.png?crop=25,0,50,50&wid=50&hei=50&fit=stretch",
        ContentType.PNG);
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
    buildAssertMedia(asset, 100, 50,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.tif?wid=100&hei=50&fit=stretch",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_TIFF_Original_ContentDisposition() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildAssertMedia_ContentDisposition(asset, 100, 50,
        "https://dummy.scene7.com/is/content/DummyFolder/sample.tif" + DynamicMediaPath.DOWNLOAD_SUFFIX,
        ContentType.TIFF);
  }

  @Override
  @Test
  void testAsset_TIFF_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildAssertMedia_Rescale(asset, 80, 40,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.tif?wid=80&hei=40&fit=stretch",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_TIFF_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.tif?crop=25,0,50,50&wid=50&hei=50&fit=stretch",
        ContentType.JPEG);
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
    buildAssertMedia(asset, 100, 50,
        "https://dummy.scene7.com/is/content/DummyFolder/sample.svg",
        ContentType.SVG);
  }

  @Override
  @Test
  void testAsset_SVG_Original_ContentDisposition() {
    Asset asset = createSampleAsset("/filetype/sample.svg", ContentType.SVG);
    buildAssertMedia_ContentDisposition(asset, 100, 50,
        "https://dummy.scene7.com/is/content/DummyFolder/sample.svg" + DynamicMediaPath.DOWNLOAD_SUFFIX,
        ContentType.SVG);
  }

  @Override
  @Test
  void testAsset_SVG_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.svg", ContentType.SVG);
    buildAssertMedia_Rescale(asset, 80, 40,
        "https://dummy.scene7.com/is/content/DummyFolder/sample.svg",
        ContentType.SVG);
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

}
