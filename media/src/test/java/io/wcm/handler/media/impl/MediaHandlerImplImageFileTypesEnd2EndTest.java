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

import static io.wcm.handler.media.MediaNameConstants.NN_MEDIA_INLINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.day.cq.dam.scene7.api.constants.Scene7Constants;
import com.day.image.Layer;

import ch.randelshofer.io.ByteArrayImageInputStream;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.Rendition;
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
class MediaHandlerImplImageFileTypesEnd2EndTest {

  final AemContext context = AppAemContext.newAemContext();

  ImageFileServlet imageFileServlet;
  MediaHandler mediaHandler;
  boolean dynamicMediaDisabled;

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
  void testAsset_JPEG_Original() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia(asset, 100, 50,
        "/content/dam/sample.jpg/_jcr_content/renditions/original./sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testAsset_JPEG_Original_ContentDisposition() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia_ContentDisposition(asset, 100, 50,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.media_file.download_attachment.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testAsset_JPEG_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia_Rescale(asset, 80, 40,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.80.40.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testAsset_JPEG_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.50.50.25,0,75,50.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testFileUpload_JPEG_Original() {
    Resource resource = createSampleFileUpload("/filetype/sample.jpg");
    buildAssertMedia(resource, 100, 50,
        "/content/upload/mediaInline./sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testFileUpload_JPEG_Original_ContentDisposition() {
    Resource resource = createSampleFileUpload("/filetype/sample.jpg");
    buildAssertMedia_ContentDisposition(resource, 100, 50,
        "/content/upload/mediaInline.media_file.download_attachment.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testFileUpload_JPEG_Rescale() {
    Resource resource = createSampleFileUpload("/filetype/sample.jpg");
    buildAssertMedia_Rescale(resource, 80, 40,
        "/content/upload/mediaInline.image_file.80.40.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testFileUpload_JPEG_AutoCrop() {
    Resource resource = createSampleFileUpload("/filetype/sample.jpg");
    buildAssertMedia_AutoCrop(resource, 50, 50,
        "/content/upload/mediaInline.image_file.50.50.25,0,75,50.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testAsset_GIF_Original() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    buildAssertMedia(asset, 100, 50,
        "/content/dam/sample.gif/_jcr_content/renditions/original./sample.gif",
        ContentType.GIF);
  }

  @Test
  void testAsset_GIF_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    buildAssertMedia_Rescale(asset, 80, 40,
        "/content/dam/sample.gif/_jcr_content/renditions/original.image_file.80.40.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testAsset_GIF_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "/content/dam/sample.gif/_jcr_content/renditions/original.image_file.50.50.25,0,75,50.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testFileUpload_GIF_Original() {
    Resource resource = createSampleFileUpload("/filetype/sample.gif");
    buildAssertMedia(resource, 100, 50,
        "/content/upload/mediaInline./sample.gif",
        ContentType.GIF);
  }

  @Test
  void testFileUpload_GIF_Rescale() {
    Resource resource = createSampleFileUpload("/filetype/sample.gif");
    buildAssertMedia_Rescale(resource, 80, 40,
        "/content/upload/mediaInline.image_file.80.40.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testFileUpload_GIF_AutoCrop() {
    Resource resource = createSampleFileUpload("/filetype/sample.gif");
    buildAssertMedia_AutoCrop(resource, 50, 50,
        "/content/upload/mediaInline.image_file.50.50.25,0,75,50.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testAsset_PNG_Original() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    buildAssertMedia(asset, 100, 50,
        "/content/dam/sample.png/_jcr_content/renditions/original./sample.png",
        ContentType.PNG);
  }

  @Test
  void testAsset_PNG_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    buildAssertMedia_Rescale(asset, 80, 40,
        "/content/dam/sample.png/_jcr_content/renditions/original.image_file.80.40.file/sample.png",
        ContentType.PNG);
  }

  @Test
  void testAsset_PNG_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "/content/dam/sample.png/_jcr_content/renditions/original.image_file.50.50.25,0,75,50.file/sample.png",
        ContentType.PNG);
  }

  @Test
  void testFileUpload_PNG_Original() {
    Resource resource = createSampleFileUpload("/filetype/sample.png");
    buildAssertMedia(resource, 100, 50,
        "/content/upload/mediaInline./sample.png",
        ContentType.PNG);
  }

  @Test
  void testFileUpload_PNG_Rescale() {
    Resource resource = createSampleFileUpload("/filetype/sample.png");
    buildAssertMedia_Rescale(resource, 80, 40,
        "/content/upload/mediaInline.image_file.80.40.file/sample.png",
        ContentType.PNG);
  }

  @Test
  void testFileUpload_PNG_AutoCrop() {
    Resource resource = createSampleFileUpload("/filetype/sample.png");
    buildAssertMedia_AutoCrop(resource, 50, 50,
        "/content/upload/mediaInline.image_file.50.50.25,0,75,50.file/sample.png",
        ContentType.PNG);
  }

  @Test
  void testAsset_TIFF_Original() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildAssertMedia(asset, 100, 50,
        "/content/dam/sample.tif/_jcr_content/renditions/original.image_file.100.50.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testAsset_TIFF_Original_ContentDisposition() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildAssertMedia_ContentDisposition(asset, 100, 50,
        "/content/dam/sample.tif/_jcr_content/renditions/original.media_file.download_attachment.file/sample.tif",
        ContentType.TIFF);
  }

  @Test
  void testAsset_TIFF_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildAssertMedia_Rescale(asset, 80, 40,
        "/content/dam/sample.tif/_jcr_content/renditions/original.image_file.80.40.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testAsset_TIFF_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "/content/dam/sample.tif/_jcr_content/renditions/original.image_file.50.50.25,0,75,50.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testFileUpload_TIFF_Original() {
    Resource resource = createSampleFileUpload("/filetype/sample.tif");
    buildAssertMedia(resource, 100, 50,
        "/content/upload/mediaInline.image_file.100.50.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testFileUpload_TIFF_Original_ContentDisposition() {
    Resource resource = createSampleFileUpload("/filetype/sample.tif");
    buildAssertMedia_ContentDisposition(resource, 100, 50,
        "/content/upload/mediaInline.media_file.download_attachment.file/sample.tif",
        ContentType.TIFF);
  }

  @Test
  void testFileUpload_TIFF_Rescale() {
    Resource resource = createSampleFileUpload("/filetype/sample.tif");
    buildAssertMedia_Rescale(resource, 80, 40,
        "/content/upload/mediaInline.image_file.80.40.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testFileUpload_TIFF_AutoCrop() {
    Resource resource = createSampleFileUpload("/filetype/sample.tif");
    buildAssertMedia_AutoCrop(resource, 50, 50,
        "/content/upload/mediaInline.image_file.50.50.25,0,75,50.file/sample.jpg",
        ContentType.JPEG);
  }

  @Test
  void testAsset_SVG_Original() {
    Asset asset = createSampleAsset("/filetype/sample.svg", ContentType.SVG);
    buildAssertMedia(asset, 100, 50,
        "/content/dam/sample.svg/_jcr_content/renditions/original./sample.svg",
        ContentType.SVG);
  }

  @Test
  void testAsset_SVG_Original_ContentDisposition() {
    Asset asset = createSampleAsset("/filetype/sample.svg", ContentType.SVG);
    buildAssertMedia_ContentDisposition(asset, 100, 50,
        "/content/dam/sample.svg/_jcr_content/renditions/original.media_file.download_attachment.file/sample.svg",
        ContentType.SVG);
  }

  @Test
  void testAsset_SVG_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.svg", ContentType.SVG);
    buildAssertMedia_Rescale(asset, 80, 40,
        "/content/dam/sample.svg/_jcr_content/renditions/original./sample.svg",
        ContentType.SVG);
  }

  @Test
  void testAsset_SVG_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.svg", ContentType.SVG);
    buildAssertInvalidMedia_AutoCrop(asset);
  }

  @Test
  void testFileUpload_SVG_Original() {
    Resource resource = createSampleFileUpload("/filetype/sample.svg");
    buildAssertMedia(resource, 100, 50,
        "/content/upload/mediaInline./sample.svg",
        ContentType.SVG);
  }

  @Test
  void testFileUpload_SVG_Original_ContentDisposition() {
    Resource resource = createSampleFileUpload("/filetype/sample.svg");
    buildAssertMedia_ContentDisposition(resource, 100, 50,
        "/content/upload/mediaInline.media_file.download_attachment.file/sample.svg",
        ContentType.SVG);
  }

  @Test
  void testFileUpload_SVG_Rescale() {
    Resource resource = createSampleFileUpload("/filetype/sample.svg");
    buildAssertMedia_Rescale(resource, 80, 40,
        "/content/upload/mediaInline./sample.svg",
        ContentType.SVG);
  }

  @Test
  void testFileUpload_SVG_AutoCrop() {
    Resource resource = createSampleFileUpload("/filetype/sample.svg");
    buildAssertInvalidMedia_AutoCrop(resource);
  }

  Asset createSampleAsset(String classpathResource, String contentType) {
    String fileName = FilenameUtils.getName(classpathResource);
    String fileExtension = FilenameUtils.getExtension(classpathResource);
    Asset asset = context.create().asset("/content/dam/" + fileName, classpathResource, contentType,
        Scene7Constants.PN_S7_FILE, "DummyFolder/" + fileName);
    context.create().assetRendition(asset, "cq5dam.web.sample." + fileExtension, classpathResource, contentType);
    return asset;
  }

  void buildAssertMedia(Asset asset, int width, int height, String mediaUrl,
      String contentType) {
    Media media = mediaHandler.get(asset.getPath())
        .dynamicMediaDisabled(dynamicMediaDisabled)
        .build();
    assertMedia(AdaptTo.notNull(asset.getOriginal(), Resource.class), media, width, height, mediaUrl, contentType);
  }

  void buildAssertMedia_ContentDisposition(Asset asset, int width, int height, String mediaUrl,
      String contentType) {
    Media media = mediaHandler.get(asset.getPath())
        .dynamicMediaDisabled(dynamicMediaDisabled)
        .contentDispositionAttachment(true)
        .build();
    assertMedia(AdaptTo.notNull(asset.getOriginal(), Resource.class), media, width, height, mediaUrl, contentType);
  }

  void buildAssertMedia_Rescale(Asset asset, int width, int height, String mediaUrl, String contentType) {
    Media media = mediaHandler.get(asset.getPath())
        .dynamicMediaDisabled(dynamicMediaDisabled)
        .fixedDimension(width, height)
        .build();
    assertMedia(AdaptTo.notNull(asset.getOriginal(), Resource.class), media, width, height, mediaUrl, contentType);
  }

  void buildAssertMedia_AutoCrop(Asset asset, int width, int height, String mediaUrl, String contentType) {
    Media media = mediaHandler.get(asset.getPath())
        .dynamicMediaDisabled(dynamicMediaDisabled)
        .mediaFormat(DummyMediaFormats.RATIO_SQUARE)
        .autoCrop(true)
        .build();
    assertMedia(AdaptTo.notNull(asset.getOriginal(), Resource.class), media, width, height, mediaUrl, contentType);
  }

  void buildAssertInvalidMedia_AutoCrop(Asset asset) {
    Media media = mediaHandler.get(asset.getPath())
        .dynamicMediaDisabled(dynamicMediaDisabled)
        .mediaFormat(DummyMediaFormats.RATIO_SQUARE)
        .autoCrop(true)
        .build();
    assertFalse(media.isValid(), "media valid");
  }

  void assertMedia(Resource resource, Media media, int width, int height, String mediaUrl, String contentType) {
    assertTrue(media.isValid(), "media valid");
    assertEquals(mediaUrl, media.getUrl(), "mediaUrl");

    Layer layer = AdaptTo.notNull(media.getRendition(), Layer.class);
    assertEquals(width, layer.getWidth(), "rendition layer width");
    assertEquals(height, layer.getHeight(), "rendition layer height");

    if (!StringUtils.contains(mediaUrl, ".download_attachment.") && !StringUtils.contains(mediaUrl, "/is/image/")) {
      Rendition rendition = media.getRendition();
      assertEquals(FilenameUtils.getName(mediaUrl), rendition.getFileName());
      assertEquals(FilenameUtils.getExtension(mediaUrl), rendition.getFileExtension());
    }

    if (StringUtils.contains(mediaUrl, ".image_file.")) {
      // extract selector string from media url
      String selectors = "image_file." + StringUtils.substringBefore(StringUtils.substringAfter(mediaUrl, ".image_file."), ".file/");

      // render media response
      context.requestPathInfo().setSelectorString(selectors);
      context.requestPathInfo().setSuffix(FilenameUtils.getName(mediaUrl));
      context.currentResource(resource);
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

  Resource createSampleFileUpload(String classpathResource) {
    String fileName = FilenameUtils.getName(classpathResource);
    Resource resource = context.create().resource("/content/upload",
        NN_MEDIA_INLINE + "Name", fileName);
    context.load().binaryFile(classpathResource, resource.getPath() + "/" + NN_MEDIA_INLINE);
    return resource;
  }

  void buildAssertMedia_Rescale(Resource resource, int width, int height, String mediaUrl, String contentType) {
    Media media = mediaHandler.get(resource)
        .dynamicMediaDisabled(dynamicMediaDisabled)
        .fixedDimension(width, height)
        .build();
    assertMedia(resource.getChild(NN_MEDIA_INLINE), media, width, height, mediaUrl, contentType);
  }

  void buildAssertMedia_AutoCrop(Resource resource, int width, int height, String mediaUrl, String contentType) {
    Media media = mediaHandler.get(resource)
        .dynamicMediaDisabled(dynamicMediaDisabled)
        .mediaFormat(DummyMediaFormats.RATIO_SQUARE)
        .autoCrop(true)
        .build();
    assertMedia(resource.getChild(NN_MEDIA_INLINE), media, width, height, mediaUrl, contentType);
  }

  void buildAssertInvalidMedia_AutoCrop(Resource resource) {
    Media media = mediaHandler.get(resource)
        .dynamicMediaDisabled(dynamicMediaDisabled)
        .mediaFormat(DummyMediaFormats.RATIO_SQUARE)
        .autoCrop(true)
        .build();
    assertFalse(media.isValid(), "media valid");
  }

  void buildAssertMedia(Resource resource, int width, int height, String mediaUrl, String contentType) {
    Media media = mediaHandler.get(resource)
        .dynamicMediaDisabled(dynamicMediaDisabled)
        .build();
    assertMedia(resource.getChild(NN_MEDIA_INLINE), media, width, height, mediaUrl, contentType);
  }

  void buildAssertMedia_ContentDisposition(Resource resource, int width, int height, String mediaUrl, String contentType) {
    Media media = mediaHandler.get(resource)
        .dynamicMediaDisabled(dynamicMediaDisabled)
        .contentDispositionAttachment(true)
        .build();
    assertMedia(resource.getChild(NN_MEDIA_INLINE), media, width, height, mediaUrl, contentType);
  }

}
