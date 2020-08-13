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
package io.wcm.handler.mediasource.inline;

import static io.wcm.handler.media.MediaNameConstants.NN_MEDIA_INLINE;
import static io.wcm.handler.media.MediaNameConstants.PN_MEDIA_ALTTEXT;
import static io.wcm.handler.media.MediaNameConstants.PN_MEDIA_CROP;
import static io.wcm.handler.media.MediaNameConstants.PN_MEDIA_ROTATION;
import static io.wcm.handler.media.MediaNameConstants.PROP_BREAKPOINT;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_2COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_3COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_STANDARD;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.PRODUCT_BANNER;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.PRODUCT_CUTOUT_13PLUS;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO2;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1_ONLYHEIGHT;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO1;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO2;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_FLYOUT_FEATURE;
import static io.wcm.handler.media.testcontext.MediaSourceInlineAppAemContext.ROOTPATH_CONTENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.day.image.Layer;
import com.google.common.collect.ImmutableList;

import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.imagemap.impl.ImageMapParserImplTest;
import io.wcm.handler.media.impl.ImageFileServlet;
import io.wcm.handler.media.impl.MediaFileServlet;
import io.wcm.handler.media.spi.ImageMapLinkResolver;
import io.wcm.handler.media.testcontext.DummyImageMapLinkResolver;
import io.wcm.handler.media.testcontext.MediaSourceInlineAppAemContext;
import io.wcm.handler.url.UrlModes;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

/**
 * Test {@link InlineMediaSource}
 */
@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class InlineMediaSourceTest {

  final AemContext context = MediaSourceInlineAppAemContext.newAemContext();

  private static final byte[] DUMMY_BINARY = new byte[] {
    0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10
  };

  private static final String PAR_INLINEIMAGE_PATH = ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineSampleImage";

  private Resource ntFileResource;
  private Resource ntResourceResource;
  private Resource mediaInlineResource;
  private Resource mediaInlineWithFileResource;
  private Resource mediaInlineSampleImageResource;
  private Resource mediaInlineSampleImageResource_16_10;
  private Resource emptyResource;

  protected Adaptable adaptable() {
    return context.request();
  }

  @BeforeEach
  void setUp() throws Exception {

    Page page = context.currentPage();
    Resource contentNode = page.getContentResource();

    // prepare inline media object: nt:file with nt:resource data
    Resource fileNode = context.load().binaryFile(new ByteArrayInputStream(DUMMY_BINARY), contentNode, "filedata.bin");

    // prepare inline media object: nt:resource data without file name
    Resource resourceNode = context.load().binaryResource(new ByteArrayInputStream(DUMMY_BINARY), contentNode.getPath() + "/resource/data");

    // prepare inline media object: node with mediaInline subnode (only nt:resource) and with filename
    Resource unstructuredNodeMediaInline = context.resourceResolver().create(contentNode, "resourceMediaInline",
        ImmutableValueMap.builder()
            .put(NN_MEDIA_INLINE + "Name", "mediainlinedata.bin")
            .put(PN_MEDIA_ALTTEXT, "Inline Media Alt. Text")
        .build());
    context.load().binaryResource(new ByteArrayInputStream(DUMMY_BINARY), unstructuredNodeMediaInline, NN_MEDIA_INLINE);

    // prepare inline media object: node with mediaInline subnode (nt:file and nt:resource) and with filename
    Resource unstructuredNodeMediaInlineWithFile = context.resourceResolver().create(contentNode, "resourceMediaInlineWithFile",
        ImmutableValueMap.builder()
            .put(NN_MEDIA_INLINE + "Name", "mediainlinedata2.bin")
            .put(PN_MEDIA_ALTTEXT, "Inline Media Alt. Text 2")
        .build());
    context.load().binaryFile(new ByteArrayInputStream(DUMMY_BINARY), unstructuredNodeMediaInlineWithFile, NN_MEDIA_INLINE);

    // prepare inline media object with real image binary data to test scaling
    Resource unstructuredNodeMediaInlineSampleImage = context.resourceResolver().create(contentNode, "resourceMediaInlineSampleImage",
        ImmutableValueMap.builder()
            .put(NN_MEDIA_INLINE + "Name", "sample_image_215x102.jpg")
        .build());
    context.load().binaryResource("/sample_image_215x102.jpg",
        unstructuredNodeMediaInlineSampleImage.getPath() + "/" + NN_MEDIA_INLINE, ContentType.JPEG);

    // prepare inline media object with real image binary data to test scaling in 16:10 format
    Resource unstructuredNodeMediaInlineSampleImage_16_10 = context.resourceResolver().create(contentNode, "resourceMediaInlineSampleImage16_10",
        ImmutableValueMap.builder()
            .put(NN_MEDIA_INLINE + "Name", "sample_image_400x250.jpg")
        .build());
    context.load().binaryResource("/sample_image_400x250.jpg",
        unstructuredNodeMediaInlineSampleImage_16_10.getPath() + "/" + NN_MEDIA_INLINE, ContentType.JPEG);

    // prepare invalid resource
    Resource emptyNode = context.resourceResolver().create(contentNode, "emptyNode", ValueMap.EMPTY);

    context.resourceResolver().commit();

    ntFileResource = context.resourceResolver().getResource(fileNode.getPath());
    ntResourceResource = context.resourceResolver().getResource(resourceNode.getPath());
    mediaInlineResource = context.resourceResolver().getResource(unstructuredNodeMediaInline.getPath());
    mediaInlineWithFileResource = context.resourceResolver().getResource(unstructuredNodeMediaInlineWithFile.getPath());
    mediaInlineSampleImageResource = context.resourceResolver().getResource(unstructuredNodeMediaInlineSampleImage.getPath());
    mediaInlineSampleImageResource_16_10 = context.resourceResolver().getResource(unstructuredNodeMediaInlineSampleImage_16_10.getPath());
    emptyResource = context.resourceResolver().getResource(emptyNode.getPath());

  }

  @Test
  void testInvalidResource() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);

    Media media = mediaHandler.get(emptyResource).build();

    assertFalse(media.isValid(), "media invalid");
    assertEquals(MediaInvalidReason.MEDIA_REFERENCE_MISSING, media.getMediaInvalidReason(), "invalid reason");
  }

  @Test
  void testNtFile() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);

    Media media = mediaHandler.get(ntFileResource).build();

    assertTrue(media.isValid(), "media valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    assertEquals(ROOTPATH_CONTENT + "/_jcr_content/filedata.bin", media.getUrl(), "media url");

    Asset asset = media.getAsset();
    assertNotNull(asset, "mediaitem");
    assertEquals("filedata.bin", asset.getTitle(), "mediaitem.title");
    assertNull(asset.getAltText(), "mediaitem.altText");
    assertEquals(ROOTPATH_CONTENT + "/jcr:content/filedata.bin/jcr:content", asset.getPath(), "mediaitem.path");

    Rendition rendition = media.getRendition();
    assertNotNull(rendition, "rendition");
    assertEquals(ROOTPATH_CONTENT + "/_jcr_content/filedata.bin", rendition.getUrl(), "rendition.mediaurl");
    assertEquals(ROOTPATH_CONTENT + "/jcr:content/filedata.bin/jcr:content", rendition.getPath(), "rendition.path");
    assertEquals("filedata.bin", rendition.getFileName(), "rendition.filename");
    assertEquals("bin", rendition.getFileExtension(), "rendition.fileextension");
    assertEquals(DUMMY_BINARY.length, rendition.getFileSize(), "rendition.filesize");
    assertEquals(0, rendition.getWidth(), "rendition.width");
    assertEquals(0, rendition.getHeight(), "rendition.height");
    assertEquals(ContentType.OCTET_STREAM, rendition.getMimeType(), "rendition.mimetype");
  }

  @Test
  void testNtResource() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);

    Media media = mediaHandler.get(ntResourceResource).build();

    assertTrue(media.isValid(), "media valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    assertEquals(ROOTPATH_CONTENT + "/_jcr_content/resource/data./file.bin", media.getUrl(), "media url");

    Asset asset = media.getAsset();
    assertNotNull(asset, "mediaitem");
    assertEquals("file.bin", asset.getTitle(), "mediaitem.title");
    assertNull(asset.getAltText(), "mediaitem.altText");
    assertEquals(ROOTPATH_CONTENT + "/jcr:content/resource/data", asset.getPath(), "mediaitem.path");

    Rendition rendition = media.getRendition();
    assertNotNull(rendition, "rendition");
    assertEquals(ROOTPATH_CONTENT + "/_jcr_content/resource/data./file.bin", rendition.getUrl(), "rendition.mediaurl");
    assertEquals(ROOTPATH_CONTENT + "/jcr:content/resource/data", rendition.getPath(), "rendition.path");
    assertEquals("file.bin", rendition.getFileName(), "rendition.filename");
    assertEquals("bin", rendition.getFileExtension(), "rendition.fileextension");
    assertEquals(DUMMY_BINARY.length, rendition.getFileSize(), "rendition.filesize");
    assertEquals(0, rendition.getWidth(), "rendition.width");
    assertEquals(0, rendition.getHeight(), "rendition.height");
    assertEquals(ContentType.OCTET_STREAM, rendition.getMimeType(), "rendition.mimetype");
  }

  @Test
  void testMediaInline() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);

    Media media = mediaHandler.get(mediaInlineResource).build();

    assertTrue(media.isValid(), "media valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    assertEquals(ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInline/mediaInline./mediainlinedata.bin",
        media.getUrl(), "media url");

    Asset asset = media.getAsset();
    assertNotNull(asset, "mediaitem");
    assertEquals("mediainlinedata.bin", asset.getTitle(), "mediaitem.title");
    assertEquals("Inline Media Alt. Text", asset.getAltText(), "mediaitem.altText");
    assertEquals(ROOTPATH_CONTENT + "/jcr:content/resourceMediaInline/mediaInline", asset.getPath(), "mediaitem.path");

    Rendition rendition = media.getRendition();
    assertNotNull(rendition, "rendition");
    assertEquals(ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInline/mediaInline./mediainlinedata.bin",
        rendition.getUrl(), "rendition.mediaurl");
    assertEquals(ROOTPATH_CONTENT + "/jcr:content/resourceMediaInline/mediaInline", rendition.getPath(), "rendition.path");
    assertEquals("mediainlinedata.bin", rendition.getFileName(), "rendition.filename");
    assertEquals("bin", rendition.getFileExtension(), "rendition.fileextension");
    assertEquals(DUMMY_BINARY.length, rendition.getFileSize(), "rendition.filesize");
    assertEquals(0, rendition.getWidth(), "rendition.width");
    assertEquals(0, rendition.getHeight(), "rendition.height");
  }

  @Test
  void testMediaInlineWithFile() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);

    Media media = mediaHandler.get(mediaInlineWithFileResource).build();

    assertTrue(media.isValid(), "media valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    assertEquals(ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineWithFile/mediaInline./mediainlinedata2.bin",
        media.getUrl(), "media url");

    Asset asset = media.getAsset();
    assertNotNull(asset, "mediaitem");
    assertEquals("mediainlinedata2.bin", asset.getTitle(), "mediaitem.title");
    assertEquals("Inline Media Alt. Text 2", asset.getAltText(), "mediaitem.altText");
    assertEquals(ROOTPATH_CONTENT + "/jcr:content/resourceMediaInlineWithFile/mediaInline/jcr:content", asset.getPath(), "mediaitem.path");

    Rendition rendition = media.getRendition();
    assertNotNull(rendition, "rendition");
    assertEquals(ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineWithFile/mediaInline./mediainlinedata2.bin",
        rendition.getUrl(), "rendition.mediaurl");
    assertEquals(ROOTPATH_CONTENT + "/jcr:content/resourceMediaInlineWithFile/mediaInline/jcr:content", rendition.getPath(), "rendition.path");
    assertEquals("mediainlinedata2.bin", rendition.getFileName(), "rendition.filename");
    assertEquals("bin", rendition.getFileExtension(), "rendition.fileextension");
    assertEquals(DUMMY_BINARY.length, rendition.getFileSize(), "rendition.filesize");
    assertEquals(0, rendition.getWidth(), "rendition.width");
    assertEquals(0, rendition.getHeight(), "rendition.height");
  }

  @SuppressWarnings("deprecation")
  @Test
  void testMediaInlineSampleImage() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);

    Media media = mediaHandler.get(mediaInlineSampleImageResource).build();

    assertTrue(media.isValid(), "media valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    assertEquals(ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineSampleImage/mediaInline./sample_image_215x102.jpg",
        media.getUrl(), "media url");

    Asset asset = media.getAsset();
    assertNotNull(asset, "mediaitem");
    assertEquals("sample_image_215x102.jpg", asset.getTitle(), "mediaitem.title");
    assertNull(asset.getAltText(), "mediaitem.altText");
    assertEquals(ROOTPATH_CONTENT + "/jcr:content/resourceMediaInlineSampleImage/mediaInline", asset.getPath(), "mediaitem.path");

    Rendition rendition = media.getRendition();
    assertNotNull(rendition, "rendition");
    assertEquals(ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineSampleImage/mediaInline./sample_image_215x102.jpg",
        rendition.getUrl(), "rendition.mediaurl");
    assertEquals(ROOTPATH_CONTENT + "/jcr:content/resourceMediaInlineSampleImage/mediaInline", rendition.getPath(), "rendition.path");
    assertEquals("sample_image_215x102.jpg", rendition.getFileName(), "rendition.filename");
    assertEquals("jpg", rendition.getFileExtension(), "rendition.fileextension");
    assertEquals(15471, rendition.getFileSize(), "rendition.filesize");
    assertEquals(215, rendition.getWidth(), "rendition.width");
    assertEquals(102, rendition.getHeight(), "rendition.height");
    assertEquals(215d / 102d, rendition.getRatio(), 0.0001, "rendition.ratio");

    assertNotNull(media.getAsset().getImageRendition(new MediaArgs()));
    assertNull(media.getAsset().getFlashRendition(new MediaArgs()));
    assertNull(media.getAsset().getDownloadRendition(new MediaArgs()));

    Layer layer = AdaptTo.notNull(rendition, Layer.class);
    assertEquals(215, layer.getWidth());
    assertEquals(102, layer.getHeight());
  }

  @Test
  void testWithAltText() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    Media media;

    MediaArgs mediaArgs = new MediaArgs().altText("Der Jodelkaiser");

    media = mediaHandler.get(ntFileResource, mediaArgs).build();
    assertEquals("Der Jodelkaiser", media.getAsset().getAltText());

    media = mediaHandler.get(ntResourceResource, mediaArgs).build();
    assertEquals("Der Jodelkaiser", media.getAsset().getAltText());

    media = mediaHandler.get(mediaInlineResource, mediaArgs).build();
    assertEquals("Der Jodelkaiser", media.getAsset().getAltText());

    media = mediaHandler.get(mediaInlineWithFileResource, mediaArgs).build();
    assertEquals("Der Jodelkaiser", media.getAsset().getAltText());

    media = mediaHandler.get(mediaInlineSampleImageResource, mediaArgs).build();
    assertEquals("Der Jodelkaiser", media.getAsset().getAltText());

  }

  @Test
  void testWithUrlMode() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);

    MediaArgs mediaArgs = new MediaArgs().urlMode(UrlModes.FULL_URL);

    assertEquals("http://www.dummysite.org" + ROOTPATH_CONTENT + "/_jcr_content/filedata.bin",
        mediaHandler.get(ntFileResource, mediaArgs).buildUrl());
    assertEquals("http://www.dummysite.org" + ROOTPATH_CONTENT + "/_jcr_content/resource/data./file.bin",
        mediaHandler.get(ntResourceResource, mediaArgs).buildUrl());
    assertEquals("http://www.dummysite.org" + ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInline/mediaInline./mediainlinedata.bin",
        mediaHandler.get(mediaInlineResource, mediaArgs).buildUrl());
    assertEquals("http://www.dummysite.org" + ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineWithFile/mediaInline./mediainlinedata2.bin",
        mediaHandler.get(mediaInlineWithFileResource, mediaArgs).buildUrl());
    assertEquals("http://www.dummysite.org" + ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineSampleImage/mediaInline./sample_image_215x102.jpg",
        mediaHandler.get(mediaInlineSampleImageResource, mediaArgs).buildUrl());
    assertNull(mediaHandler.get(emptyResource, mediaArgs).buildUrl());

  }

  @Test
  void testWithFixedDimensions() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    Rendition rendition;

    // test invalid resource
    rendition = mediaHandler.get(emptyResource, new MediaArgs().fixedDimension(10, 10)).build().getRendition();
    assertNull(rendition, "rendition invalid");

    // test non-image resource - dimensions have no effect
    rendition = mediaHandler.get(mediaInlineResource, new MediaArgs().fixedDimension(10, 10)).build().getRendition();
    assertEquals(0, rendition.getWidth(), "width");
    assertEquals(0, rendition.getHeight(), "height");
    assertEquals(ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInline/mediaInline./mediainlinedata.bin", rendition.getUrl(), "url");

    // test image resource with invalid aspect ratio
    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(10, 10)).refProperty("mediaInline").build().getRendition();
    assertNull(rendition, "rendition invalid");

    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(20, 10)).refProperty("mediaInline").build().getRendition();
    assertNull(rendition, "rendition invalid");

    // test image resource with dimensions that are too big
    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(430, 204)).refProperty("mediaInline").build().getRendition();
    assertNull(rendition, "rendition invalid");

    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(430, 0)).refProperty("mediaInline").build().getRendition();
    assertNull(rendition, "rendition invalid");

    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(0, 204)).refProperty("mediaInline").build().getRendition();
    assertNull(rendition, "rendition invalid");

    // test image resource with dimensions exact fit
    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(215, 102)).refProperty("mediaInline").build().getRendition();
    assertEquals(215, rendition.getWidth(), "width");
    assertEquals(102, rendition.getHeight(), "height");
    assertEquals(PAR_INLINEIMAGE_PATH + "/mediaInline./sample_image_215x102.jpg", rendition.getUrl(), "url");

    // test image resource with dimensions smaller
    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(108, 51)).refProperty("mediaInline").build().getRendition();
    assertEquals(108, rendition.getWidth(), "width");
    assertEquals(51, rendition.getHeight(), "height");
    assertEquals(PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".108.51.file/sample_image_215x102.jpg", rendition.getUrl(), "url");

    Layer layer = AdaptTo.notNull(rendition, Layer.class);
    assertEquals(108, layer.getWidth());
    assertEquals(51, layer.getHeight());

    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(42, 20)).refProperty("mediaInline").build().getRendition();
    assertEquals(42, rendition.getWidth(), "width");
    assertEquals(20, rendition.getHeight(), "height");
    assertEquals(PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".42.20.file/sample_image_215x102.jpg", rendition.getUrl(), "url");

    // test image resource with dimensions only width
    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(42, 0)).refProperty("mediaInline").build().getRendition();
    assertEquals(42, rendition.getWidth(), "width");
    assertEquals(20, rendition.getHeight(), "height");
    assertEquals(PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".42.20.file/sample_image_215x102.jpg", rendition.getUrl(), "url");

    // test image resource with dimensions only height
    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(0, 20)).refProperty("mediaInline").build().getRendition();
    assertEquals(42, rendition.getWidth(), "width");
    assertEquals(20, rendition.getHeight(), "height");
    assertEquals(PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".42.20.file/sample_image_215x102.jpg", rendition.getUrl(), "url");

    // test with force download
    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(0, 20).contentDispositionAttachment(true))
        .refProperty("mediaInline").build().getRendition();
    assertEquals(42, rendition.getWidth(), "width");
    assertEquals(20, rendition.getHeight(), "height");
    assertEquals(PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".42.20.download_attachment.file/sample_image_215x102.jpg",
        rendition.getUrl(), "url");

  }

  @Test
  void testWithMediaFormats() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    Media media;
    Rendition rendition;

    // test invalid resource
    media = mediaHandler.get(emptyResource, new MediaArgs(SHOWROOM_CONTROLS)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertFalse(media.isValid(), "media invalid");
    assertEquals(MediaInvalidReason.MEDIA_REFERENCE_MISSING, media.getMediaInvalidReason(), "invalid reason");
    assertNull(rendition, "rendition invalid");

    // test image resource with media formats with invalid aspect ratio
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(PRODUCT_BANNER)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertFalse(media.isValid(), "media invalid");
    assertEquals(MediaInvalidReason.NO_MATCHING_RENDITION, media.getMediaInvalidReason(), "invalid reason");
    assertNull(rendition, "rendition invalid");

    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(PRODUCT_CUTOUT_13PLUS)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertFalse(media.isValid(), "media invalid");
    assertEquals(MediaInvalidReason.NO_MATCHING_RENDITION, media.getMediaInvalidReason(), "invalid reason");
    assertNull(rendition, "rendition invalid");

    // test image resource with media formats that are too big
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(EDITORIAL_STANDARD)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertFalse(media.isValid(), "media invalid");
    assertEquals(MediaInvalidReason.NO_MATCHING_RENDITION, media.getMediaInvalidReason(), "invalid reason");
    assertNull(rendition, "rendition invalid");

    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(EDITORIAL_2COL)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertFalse(media.isValid(), "media invalid");
    assertEquals(MediaInvalidReason.NO_MATCHING_RENDITION, media.getMediaInvalidReason(), "invalid reason");
    assertNull(rendition, "rendition invalid");

    // test image resource with media format exact fit
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(EDITORIAL_1COL)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertTrue(media.isValid(), "media valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    assertEquals(215, rendition.getWidth(), "width");
    assertEquals(102, rendition.getHeight(), "height");
    assertEquals(PAR_INLINEIMAGE_PATH + "/mediaInline./sample_image_215x102.jpg", rendition.getUrl(), "url");
    assertEquals(EDITORIAL_1COL, rendition.getMediaFormat());

    // test image resource with dimensions smaller
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(SHOWROOM_FLYOUT_FEATURE)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertTrue(media.isValid(), "media valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    assertEquals(205, rendition.getWidth(), "width");
    assertEquals(97, rendition.getHeight(), "height");
    assertEquals(PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".205.97.file/sample_image_215x102.jpg", rendition.getUrl(), "url");
    assertEquals(SHOWROOM_FLYOUT_FEATURE, rendition.getMediaFormat());

    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(SHOWROOM_CONTROLS_SCALE1)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertTrue(media.isValid(), "media valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    assertEquals(64, rendition.getWidth(), "width");
    assertEquals(30, rendition.getHeight(), "height");
    assertEquals(PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".64.30.file/sample_image_215x102.jpg", rendition.getUrl(), "url");
    assertEquals(SHOWROOM_CONTROLS_SCALE1, rendition.getMediaFormat());

    Layer layer = AdaptTo.notNull(rendition, Layer.class);
    assertEquals(64, layer.getWidth());
    assertEquals(30, layer.getHeight());

    // test image resource with dimensions only width
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH)).refProperty("mediaInline")
        .build();
    rendition = media.getRendition();
    assertTrue(media.isValid(), "media valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    assertEquals(64, rendition.getWidth(), "width");
    assertEquals(30, rendition.getHeight(), "height");
    assertEquals(PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".64.30.file/sample_image_215x102.jpg", rendition.getUrl(), "url");
    assertEquals(SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH, rendition.getMediaFormat());

    // test image resource with dimensions only height
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(SHOWROOM_CONTROLS_SCALE1_ONLYHEIGHT)).refProperty("mediaInline")
        .build();
    rendition = media.getRendition();
    assertTrue(media.isValid(), "media valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    assertEquals(63, rendition.getWidth(), "width");
    assertEquals(30, rendition.getHeight(), "height");
    assertEquals(PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".63.30.file/sample_image_215x102.jpg", rendition.getUrl(), "url");
    assertEquals(SHOWROOM_CONTROLS_SCALE1_ONLYHEIGHT, rendition.getMediaFormat());

    // test image resource with dimensions only width, fitting ratio
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO1))
        .refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertTrue(media.isValid(), "media valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    assertEquals(64, rendition.getWidth(), "width");
    assertEquals(30, rendition.getHeight(), "height");
    assertEquals(PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".64.30.file/sample_image_215x102.jpg", rendition.getUrl(), "url");
    assertEquals(SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO1, rendition.getMediaFormat());

    // test image resource with dimensions only width, invalid ratio
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO2))
        .refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertFalse(media.isValid(), "media invalid");
    assertEquals(MediaInvalidReason.NO_MATCHING_RENDITION, media.getMediaInvalidReason(), "invalid reason");
    assertNull(rendition, "rendition invalid");

  }

  @Test
  void testDownloadMediaElement() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    MediaArgs mediaArgs = new MediaArgs().contentDispositionAttachment(true);
    Media media = mediaHandler.get(mediaInlineSampleImageResource, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    assertNotNull(media.getAsset(), "asset?");
    assertNotNull(media.getRendition(), "rendition?");
    assertEquals(ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineSampleImage/mediaInline."
            + MediaFileServlet.SELECTOR + "." + MediaFileServlet.SELECTOR_DOWNLOAD + ".file/sample_image_215x102.jpg",
        media.getRendition().getUrl(), "rendition.mediaUrl");
  }

  @Test
  void testWithCroppping() {
    // set cropping parameters
    ModifiableValueMap props = mediaInlineSampleImageResource.adaptTo(ModifiableValueMap.class);
    props.put(PN_MEDIA_CROP, "5,10,69,40");

    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    MediaArgs mediaArgs = new MediaArgs(SHOWROOM_CONTROLS_SCALE1);
    Media media = mediaHandler.get(mediaInlineSampleImageResource, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(1, media.getRenditions().size(), "renditions");
    assertEquals(
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline.image_file.64.30.5,10,69,40.file/sample_image_215x102.jpg",
        media.getUrl(), "rendition.mediaUrl");

    Rendition rendition = media.getRendition();
    assertEquals(64, rendition.getWidth());
    assertEquals(30, rendition.getHeight());
    assertEquals(SHOWROOM_CONTROLS_SCALE1, media.getRendition().getMediaFormat());

    Layer layer = AdaptTo.notNull(rendition, Layer.class);
    assertEquals(64, layer.getWidth());
    assertEquals(30, layer.getHeight());
  }

  @Test
  void testWithAutoCroppping() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    MediaArgs mediaArgs = new MediaArgs(SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO2)
        .autoCrop(true);
    Media media = mediaHandler.get(mediaInlineSampleImageResource, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(1, media.getRenditions().size(), "renditions");
    assertEquals(
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline.image_file.64.64.57,0,159,102.file/sample_image_215x102.jpg",
        media.getUrl(), "rendition.mediaUrl");

    Rendition rendition = media.getRendition();
    assertEquals(64, rendition.getWidth());
    assertEquals(64, rendition.getHeight());
    assertEquals(SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO2, media.getRendition().getMediaFormat());

    Layer layer = AdaptTo.notNull(rendition, Layer.class);
    assertEquals(64, layer.getWidth());
    assertEquals(64, layer.getHeight());
  }

  @Test
  void testWithRotation() {
    // set rotation parameters
    ModifiableValueMap props = mediaInlineSampleImageResource.adaptTo(ModifiableValueMap.class);
    props.put(PN_MEDIA_ROTATION, "90");

    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    MediaArgs mediaArgs = new MediaArgs();
    Media media = mediaHandler.get(mediaInlineSampleImageResource, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(1, media.getRenditions().size(), "renditions");
    assertEquals(
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline.image_file.215.102.-.90.file/sample_image_215x102.jpg",
        media.getUrl(), "rendition.mediaUrl");

    Rendition rendition = media.getRendition();
    assertEquals(102, rendition.getWidth());
    assertEquals(215, rendition.getHeight());
    assertNull(media.getRendition().getMediaFormat());

    Layer layer = AdaptTo.notNull(rendition, Layer.class);
    assertEquals(102, layer.getWidth());
    assertEquals(215, layer.getHeight());
  }

  @Test
  void testWithCroppingAndRotation() {
    // set cropping and rotation parameters
    ModifiableValueMap props = mediaInlineSampleImageResource.adaptTo(ModifiableValueMap.class);
    props.put(PN_MEDIA_CROP, "5,10,69,40");
    props.put(PN_MEDIA_ROTATION, "270");

    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    MediaArgs mediaArgs = new MediaArgs(SHOWROOM_CONTROLS_SCALE1);
    Media media = mediaHandler.get(mediaInlineSampleImageResource, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(1, media.getRenditions().size(), "renditions");
    assertEquals(
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline.image_file.64.30.5,10,69,40.270.file/sample_image_215x102.jpg",
        media.getUrl(), "rendition.mediaUrl");

    Rendition rendition = media.getRendition();
    assertEquals(30, rendition.getWidth());
    assertEquals(64, rendition.getHeight());
    assertEquals(SHOWROOM_CONTROLS_SCALE1, media.getRendition().getMediaFormat());

    Layer layer = AdaptTo.notNull(rendition, Layer.class);
    assertEquals(30, layer.getWidth());
    assertEquals(64, layer.getHeight());
  }

  @Test
  void testMultipleMandatoryMediaFormatsWithCropping_AlsoMatchOriginal() {
    // set cropping parameters
    ModifiableValueMap props = mediaInlineSampleImageResource_16_10.adaptTo(ModifiableValueMap.class);
    props.put(PN_MEDIA_CROP, "0,0,320,152");

    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(SHOWROOM_CONTROLS, RATIO);
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    Media media = mediaHandler.get(mediaInlineSampleImageResource_16_10).args(mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(2, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals(
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage16_10/mediaInline.image_file.84.40.0,0,320,152.file/sample_image_400x250.jpg",
        renditions.get(0).getUrl(), "rendition.mediaUrl.1");
    assertEquals(SHOWROOM_CONTROLS, renditions.get(0).getMediaFormat());

    assertEquals("/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage16_10/mediaInline./sample_image_400x250.jpg",
        renditions.get(1).getUrl(), "rendition.mediaUrl.2");
    assertEquals(RATIO, renditions.get(1).getMediaFormat());
  }

  @Test
  void testMultipleMandatoryMediaFormatsWithCropping_AlsoMatchOriginal_AutoCrop() {
    // set cropping parameters
    ModifiableValueMap props = mediaInlineSampleImageResource_16_10.adaptTo(ModifiableValueMap.class);
    props.put(PN_MEDIA_CROP, "0,0,320,152");

    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(SHOWROOM_CONTROLS, RATIO, RATIO2, EDITORIAL_1COL)
        .autoCrop(true);
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    Media media = mediaHandler.get(mediaInlineSampleImageResource_16_10).args(mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(4, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals(
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage16_10/mediaInline.image_file.84.40.0,0,320,152.file/sample_image_400x250.jpg",
        renditions.get(0).getUrl(), "rendition.mediaUrl.1");
    assertEquals(SHOWROOM_CONTROLS, renditions.get(0).getMediaFormat());

    assertEquals("/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage16_10/mediaInline./sample_image_400x250.jpg",
        renditions.get(1).getUrl(), "rendition.mediaUrl.2");
    assertEquals(RATIO, renditions.get(1).getMediaFormat());

    assertEquals(
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage16_10/mediaInline.image_file.333.250.34,0,367,250.file/sample_image_400x250.jpg",
        renditions.get(2).getUrl(), "rendition.mediaUrl.1");
    assertEquals(RATIO2, renditions.get(2).getMediaFormat());

    assertEquals(
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage16_10/mediaInline.image_file.215.102.0,0,320,152.file/sample_image_400x250.jpg",
        renditions.get(3).getUrl(), "rendition.mediaUrl.1");
    assertEquals(EDITORIAL_1COL, renditions.get(3).getMediaFormat());
  }

  @Test
  void testWithCropppingInvalid() {
    // set invalid cropping parameters - should be ignored and media resolved without cropping
    ModifiableValueMap props = mediaInlineSampleImageResource.adaptTo(ModifiableValueMap.class);
    props.put(PN_MEDIA_CROP, "10,10,20,20");

    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    MediaArgs mediaArgs = new MediaArgs(SHOWROOM_CONTROLS_SCALE1);
    Media media = mediaHandler.get(mediaInlineSampleImageResource, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertEquals(
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline.image_file.64.30.file/sample_image_215x102.jpg",
        media.getUrl(), "rendition.mediaUrl");
    assertEquals(SHOWROOM_CONTROLS_SCALE1, media.getRendition().getMediaFormat());
  }

  @Test
  void testMultipleMediaMediaFormats() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    MediaArgs mediaArgs = new MediaArgs(EDITORIAL_1COL, EDITORIAL_2COL, EDITORIAL_3COL);
    Media media = mediaHandler.get(mediaInlineSampleImageResource, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(1, media.getRenditions().size(), "renditions");
    assertEquals("/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline./sample_image_215x102.jpg",
        media.getUrl(), "rendition.mediaUrl");
    assertEquals(EDITORIAL_1COL, media.getRendition().getMediaFormat());
  }

  @Test
  void testMultipleMandatoryMediaFormats() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(EDITORIAL_1COL, SHOWROOM_FLYOUT_FEATURE, SHOWROOM_CONTROLS_SCALE1);
    Media media = mediaHandler.get(mediaInlineSampleImageResource, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(3, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals("/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline./sample_image_215x102.jpg",
        renditions.get(0).getUrl(), "rendition.mediaUrl.1");
    assertEquals(EDITORIAL_1COL, renditions.get(0).getMediaFormat());

    assertEquals("/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline.image_file.205.97.file/sample_image_215x102.jpg",
        renditions.get(1).getUrl(), "rendition.mediaUrl.2");
    assertEquals(SHOWROOM_FLYOUT_FEATURE, renditions.get(1).getMediaFormat());

    assertEquals("/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline.image_file.64.30.file/sample_image_215x102.jpg",
        renditions.get(2).getUrl(), "rendition.mediaUrl.3");
    assertEquals(SHOWROOM_CONTROLS_SCALE1, renditions.get(2).getMediaFormat());
  }

  @Test
  void testMultipleMandatoryMediaFormatsNotAllMatch() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(EDITORIAL_2COL, SHOWROOM_FLYOUT_FEATURE, SHOWROOM_CONTROLS_SCALE1);
    Media media = mediaHandler.get(mediaInlineSampleImageResource, mediaArgs).build();
    assertFalse(media.isValid(), "valid?");
    assertEquals(MediaInvalidReason.NOT_ENOUGH_MATCHING_RENDITIONS, media.getMediaInvalidReason());
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(2, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals("/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline.image_file.205.97.file/sample_image_215x102.jpg",
        renditions.get(0).getUrl(), "rendition.mediaUrl.1");
    assertEquals(SHOWROOM_FLYOUT_FEATURE, renditions.get(0).getMediaFormat());

    assertEquals("/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline.image_file.64.30.file/sample_image_215x102.jpg",
        renditions.get(1).getUrl(), "rendition.mediaUrl.2");
    assertEquals(SHOWROOM_CONTROLS_SCALE1, renditions.get(1).getMediaFormat());
  }

  @Test
  @SuppressWarnings("deprecation")
  void testMultipleMandatoryMediaFormats_OnThyFlyMediaFormats() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(new io.wcm.handler.media.format.ResponsiveMediaFormatsBuilder(RATIO)
        .breakpoint("B1", 160, 100)
        .breakpoint("B2", 320, 200)
        .build());

    Media media = mediaHandler.get(mediaInlineSampleImageResource_16_10, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(2, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    Rendition rendition0 = renditions.get(0);
    assertEquals(
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage16_10/mediaInline.image_file.160.100.file/sample_image_400x250.jpg",
        rendition0.getUrl(), "rendition.mediaUrl.1");
    assertEquals(160, rendition0.getWidth());
    assertEquals(100, rendition0.getHeight());

    MediaFormat mediaFormat0 = renditions.get(0).getMediaFormat();
    assertEquals(RATIO.getLabel(), mediaFormat0.getLabel());
    assertEquals(RATIO.getRatio(), mediaFormat0.getRatio(), 0.001d);
    assertEquals(160, mediaFormat0.getWidth());
    assertEquals(100, mediaFormat0.getHeight());
    assertEquals("B1", mediaFormat0.getProperties().get(PROP_BREAKPOINT));

    Rendition rendition1 = renditions.get(1);
    assertEquals(
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage16_10/mediaInline.image_file.320.200.file/sample_image_400x250.jpg",
        rendition1.getUrl(), "rendition.mediaUrl.2");
    assertEquals(320, rendition1.getWidth());
    assertEquals(200, rendition1.getHeight());

    MediaFormat mediaFormat1 = renditions.get(1).getMediaFormat();
    assertEquals(RATIO.getLabel(), mediaFormat1.getLabel());
    assertEquals(RATIO.getRatio(), mediaFormat1.getRatio(), 0.001d);
    assertEquals(320, mediaFormat1.getWidth());
    assertEquals(200, mediaFormat1.getHeight());
    assertEquals("B2", mediaFormat1.getProperties().get(PROP_BREAKPOINT));
  }

  @Test
  void testMultipleMandatoryMediaFormats_OnThyFlyMediaFormats_PictureSources() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    Media media = mediaHandler.get(mediaInlineSampleImageResource_16_10)
        .mediaFormat(RATIO)
        .pictureSource(new PictureSource(RATIO).media("media1").widths(160))
        .pictureSource(new PictureSource(RATIO).media("media2").widths(320))
        .build();

    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(3, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    Rendition rendition0 = renditions.get(0);
    assertEquals("/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage16_10/mediaInline./sample_image_400x250.jpg",
        rendition0.getUrl(), "rendition.mediaUrl.1");
    assertEquals(400, rendition0.getWidth());
    assertEquals(250, rendition0.getHeight());
    assertEquals(160d / 100d, rendition0.getRatio(), 0.0001);

    MediaFormat mediaFormat0 = rendition0.getMediaFormat();
    assertEquals(RATIO.getName(), mediaFormat0.getName());

    Rendition rendition1 = renditions.get(1);
    assertEquals(
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage16_10/mediaInline.image_file.160.100.file/sample_image_400x250.jpg",
        rendition1.getUrl(), "rendition.mediaUrl.2");
    assertEquals(160, rendition1.getWidth());
    assertEquals(100, rendition1.getHeight());
    assertEquals(160d / 100d, rendition1.getRatio(), 0.0001);

    MediaFormat mediaFormat1 = rendition1.getMediaFormat();
    assertEquals(RATIO.getLabel(), mediaFormat1.getLabel());
    assertEquals(RATIO.getRatio(), mediaFormat1.getRatio(), 0.001d);
    assertEquals(160, mediaFormat1.getWidth());

    Rendition rendition2 = renditions.get(2);
    assertEquals(
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage16_10/mediaInline.image_file.320.200.file/sample_image_400x250.jpg",
        rendition2.getUrl(), "rendition.mediaUrl.3");
    assertEquals(320, rendition2.getWidth());
    assertEquals(200, rendition2.getHeight());

    MediaFormat mediaFormat2 = rendition2.getMediaFormat();
    assertEquals(RATIO.getLabel(), mediaFormat2.getLabel());
    assertEquals(RATIO.getRatio(), mediaFormat2.getRatio(), 0.001d);
    assertEquals(320, mediaFormat2.getWidth());
  }

  @Test
  void testImageMap() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    context.registerService(ImageMapLinkResolver.class, new DummyImageMapLinkResolver(context));

    // put map string in resource
    ModifiableValueMap props = mediaInlineResource.adaptTo(ModifiableValueMap.class);
    props.put(MediaNameConstants.PN_MEDIA_MAP, ImageMapParserImplTest.MAP_STRING);

    Media media = mediaHandler.get(mediaInlineResource).build();
    assertTrue(media.isValid(), "media valid");

    // assert map
    assertEquals(ImageMapParserImplTest.EXPECTED_AREAS_RESOLVED, media.getMap());
  }

  @Test
  void testImageMap_CustomProperty() {
    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);
    context.registerService(ImageMapLinkResolver.class, new DummyImageMapLinkResolver(context));

    // put map string in resource
    ModifiableValueMap props = mediaInlineResource.adaptTo(ModifiableValueMap.class);
    props.put("customMapProperty", ImageMapParserImplTest.MAP_STRING);

    Media media = mediaHandler.get(mediaInlineResource)
        .mapProperty("customMapProperty")
        .build();
    assertTrue(media.isValid(), "valid");

    // assert map
    assertEquals(ImageMapParserImplTest.EXPECTED_AREAS_RESOLVED, media.getMap());
  }

}
