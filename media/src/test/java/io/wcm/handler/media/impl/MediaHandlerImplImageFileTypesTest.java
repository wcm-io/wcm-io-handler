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
package io.wcm.handler.media.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;
import com.day.image.Layer;

import ch.randelshofer.io.ByteArrayImageInputStream;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.media.testcontext.DummyMediaFormats;
import io.wcm.handler.mediasource.dam.impl.metadata.AssetSynchonizationService;
import io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataListenerService;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

/**
 * This is an "end-to-end" test handling image files with different content types
 * from classpath, handles them with and without cropping using media handler
 * and renders the result using the ImageFileServlet.
 */
@ExtendWith(AemContextExtension.class)
class MediaHandlerImplImageFileTypesTest {

  private final AemContext context = AppAemContext.newAemContext();

  private ImageFileServlet imageFileServlet;
  private MediaHandler mediaHandler;

  @BeforeEach
  void setUp() {
    // register RenditionMetadataListenerService to generate rendition metadata
    context.registerInjectActivateService(new AssetSynchonizationService());
    context.registerInjectActivateService(new RenditionMetadataListenerService(),
        "threadPoolSize", 0,
        "allowedRunMode", new String[0]);

    imageFileServlet = context.registerInjectActivateService(new ImageFileServlet());
    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
  }

  @Test
  void testJPEGOriginal() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia(asset, 100, 50,
        "/content/dam/sample.jpg/_jcr_content/renditions/original./sample.jpg");
  }

  @Test
  void testJPEGCrop() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildCropAssertMedia(asset, 50, 50,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.50.50.25,0,75,50.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testGIFOriginal() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    buildAssertMedia(asset, 100, 50,
        "/content/dam/sample.gif/_jcr_content/renditions/original./sample.gif");
  }

  @Test
  void testGIFCrop() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    buildCropAssertMedia(asset, 50, 50,
        "/content/dam/sample.gif/_jcr_content/renditions/original.image_file.50.50.25,0,75,50.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testPNGOriginal() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    buildAssertMedia(asset, 100, 50,
        "/content/dam/sample.png/_jcr_content/renditions/original./sample.png");
  }

  @Test
  void testPNGCrop() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    buildCropAssertMedia(asset, 50, 50,
        "/content/dam/sample.png/_jcr_content/renditions/original.image_file.50.50.25,0,75,50.file/sample.png",
        ContentType.PNG);
  }

  @Test
  void testTIFFOriginal() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildAssertMedia(asset, 100, 50,
        "/content/dam/sample.tif/_jcr_content/renditions/original./sample.tif");
  }

  @Test
  void testTIFFCrop() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildCropAssertMedia(asset, 50, 50,
        "/content/dam/sample.tif/_jcr_content/renditions/original.image_file.50.50.25,0,75,50.file/sample.png",
        ContentType.PNG);
  }

  private Asset createSampleAsset(String classpathResource, String contentType) {
    String fileName = FilenameUtils.getName(classpathResource);
    Asset asset = context.create().asset("/content/dam/" + fileName, classpathResource, contentType);
    context.create().assetRendition(asset, "cq5dam.web.sample.jpg", classpathResource, contentType);
    return asset;
  }

  private void buildAssertMedia(Asset asset, int width, int height, String mediaUrl) {
    Media media = mediaHandler.get(asset.getPath())
        .build();
    assertMedia(asset, media, width, height, mediaUrl, null);
  }

  private void buildCropAssertMedia(Asset asset, int width, int height, String mediaUrl, String contentType) {
    Media media = mediaHandler.get(asset.getPath())
        .mediaFormat(DummyMediaFormats.RATIO_SQUARE)
        .autoCrop(true)
        .build();
    assertMedia(asset, media, width, height, mediaUrl, contentType);
  }

  private void assertMedia(Asset asset, Media media, int width, int height, String mediaUrl, String contentType) {
    assertTrue(media.isValid(), "media valid");
    assertEquals(mediaUrl, media.getUrl(), "mediaUrl");

    Layer layer = AdaptTo.notNull(media.getRendition(), Layer.class);
    assertEquals(width, layer.getWidth(), "rendition layer width");
    assertEquals(height, layer.getHeight(), "rendition layer height");

    if (StringUtils.contains(mediaUrl, ".image_file.")) {
      // extract selector string from media url
      String selectors = StringUtils.substringBefore(StringUtils.substringAfter(mediaUrl, "/original."), ".file/");

      // render media response
      context.requestPathInfo().setSelectorString(selectors);
      context.requestPathInfo().setSuffix(FilenameUtils.getName(mediaUrl));
      context.currentResource(AdaptTo.notNull(asset.getOriginal(), Resource.class));
      try {
        imageFileServlet.service(context.request(), context.response());
      }
      catch (ServletException | IOException ex) {
        throw new RuntimeException(ex);
      }

      assertEquals(HttpServletResponse.SC_OK, context.response().getStatus(), "response status");
      assertEquals(contentType, context.response().getContentType(), "response content type");

      try (InputStream is = new ByteArrayImageInputStream(context.response().getOutput())) {
        Layer responseLayer = new Layer(is);
        assertEquals(width, responseLayer.getWidth(), "response data layer width");
        assertEquals(height, responseLayer.getHeight(), "response data layer height");
      }
      catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

}
