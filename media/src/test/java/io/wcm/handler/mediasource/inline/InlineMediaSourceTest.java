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

import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_2COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_3COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_STANDARD;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.HOLZAUTO_BANNER;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.HOLZAUTO_CUTOUT_13PLUS;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1_ONLYHEIGHT;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO1;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO2;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_FLYOUT_FEATURE;
import static io.wcm.handler.media.testcontext.MediaSourceInlineAppAemContext.ROOTPATH_CONTENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.impl.ImageFileServlet;
import io.wcm.handler.media.impl.MediaFileServlet;
import io.wcm.handler.media.testcontext.MediaSourceInlineAppAemContext;
import io.wcm.handler.url.UrlModes;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.commons.contenttype.ContentType;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;

/**
 * Test {@link InlineMediaSource}
 */
public class InlineMediaSourceTest {

  @Rule
  public final AemContext context = MediaSourceInlineAppAemContext.newAemContext();

  private static final byte[] DUMMY_BINARY = new byte[] {
    0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10
  };

  private static final String PAR_INLINEIMAGE_PATH = ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineSampleImage";

  private Resource ntFileResource;
  private Resource ntResourceResource;
  private Resource mediaInlineResource;
  private Resource mediaInlineWithFileResource;
  private Resource mediaInlineSampleImageResource;
  private Resource emptyResource;

  @Before
  public void setUp() throws Exception {

    Page page = context.currentPage();
    Resource contentNode = page.getContentResource();

    // prepare inline media object: nt:file with nt:resource data
    Resource fileNode = context.load().binaryFile(new ByteArrayInputStream(DUMMY_BINARY), contentNode, "filedata.bin");

    // prepare inline media object: nt:resource data without file name
    Resource resourceNode = context.load().binaryResource(new ByteArrayInputStream(DUMMY_BINARY), contentNode.getPath() + "/resource/data");

    // prepare inline media object: node with mediaInline subnode (only nt:resource) and with filename
    Resource unstructuredNodeMediaInline = context.resourceResolver().create(contentNode, "resourceMediaInline",
        ImmutableValueMap.builder()
        .put(MediaNameConstants.NN_MEDIA_INLINE + "Name", "mediainlinedata.bin")
        .put(MediaNameConstants.PN_MEDIA_ALTTEXT, "Inline Media Alt. Text")
        .build());
    context.load().binaryResource(new ByteArrayInputStream(DUMMY_BINARY), unstructuredNodeMediaInline, MediaNameConstants.NN_MEDIA_INLINE);

    // prepare inline media object: node with mediaInline subnode (nt:file and nt:resource) and with filename
    Resource unstructuredNodeMediaInlineWithFile = context.resourceResolver().create(contentNode, "resourceMediaInlineWithFile",
        ImmutableValueMap.builder()
        .put(MediaNameConstants.NN_MEDIA_INLINE + "Name", "mediainlinedata2.bin")
        .put(MediaNameConstants.PN_MEDIA_ALTTEXT, "Inline Media Alt. Text 2")
        .build());
    context.load().binaryFile(new ByteArrayInputStream(DUMMY_BINARY), unstructuredNodeMediaInlineWithFile, MediaNameConstants.NN_MEDIA_INLINE);

    // prepare inline media object with real image binary data to test scaling
    Resource unstructuredNodeMediaInlineSampleImage = context.resourceResolver().create(contentNode, "resourceMediaInlineSampleImage",
        ImmutableValueMap.builder()
        .put(MediaNameConstants.NN_MEDIA_INLINE + "Name", "sample_image_215x102.jpg")
        .build());
    context.load().binaryResource("/sample_image_215x102.jpg",
        unstructuredNodeMediaInlineSampleImage.getPath() + "/" + MediaNameConstants.NN_MEDIA_INLINE, ContentType.JPEG);

    // prepare invalid resource
    Resource emptyNode = context.resourceResolver().create(contentNode, "emptyNode", ValueMap.EMPTY);

    context.resourceResolver().commit();

    ntFileResource = context.resourceResolver().getResource(fileNode.getPath());
    ntResourceResource = context.resourceResolver().getResource(resourceNode.getPath());
    mediaInlineResource = context.resourceResolver().getResource(unstructuredNodeMediaInline.getPath());
    mediaInlineWithFileResource = context.resourceResolver().getResource(unstructuredNodeMediaInlineWithFile.getPath());
    mediaInlineSampleImageResource = context.resourceResolver().getResource(unstructuredNodeMediaInlineSampleImage.getPath());
    emptyResource = context.resourceResolver().getResource(emptyNode.getPath());

  }

  /**
   * Test invalid resource
   */
  @Test
  public void testInvalid() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(emptyResource).build();

    assertFalse("media invalid", media.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MEDIA_SOURCE, media.getMediaInvalidReason());
  }

  /**
   * Test nt:resource resource without filename
   */
  @Test
  public void testNtFile() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(ntFileResource).build();

    assertTrue("media valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    assertEquals("media url", ROOTPATH_CONTENT + "/_jcr_content/filedata.bin", media.getUrl());

    Asset asset = media.getAsset();
    assertNotNull("mediaitem", asset);
    assertEquals("mediaitem.title", "filedata.bin", asset.getTitle());
    assertNull("mediaitem.altText", asset.getAltText());
    assertEquals("mediaitem.path", ROOTPATH_CONTENT + "/jcr:content/filedata.bin/jcr:content", asset.getPath());

    Rendition rendition = media.getRendition();
    assertNotNull("rendition", rendition);
    assertEquals("rendition.mediaurl", ROOTPATH_CONTENT + "/_jcr_content/filedata.bin", rendition.getUrl());
    assertEquals("rendition.path", ROOTPATH_CONTENT + "/jcr:content/filedata.bin/jcr:content", rendition.getPath());
    assertEquals("rendition.filename", "filedata.bin", rendition.getFileName());
    assertEquals("rendition.fileextension", "bin", rendition.getFileExtension());
    assertEquals("rendition.filesize", DUMMY_BINARY.length, rendition.getFileSize());
    assertEquals("rendition.width", 0, rendition.getWidth());
    assertEquals("rendition.height", 0, rendition.getHeight());
  }

  /**
   * Test nt:resource resource
   */
  @Test
  public void testNtResource() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(ntResourceResource).build();

    assertTrue("media valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    assertEquals("media url", ROOTPATH_CONTENT + "/_jcr_content/resource/data./file.bin", media.getUrl());

    Asset asset = media.getAsset();
    assertNotNull("mediaitem", asset);
    assertEquals("mediaitem.title", "file.bin", asset.getTitle());
    assertNull("mediaitem.altText", asset.getAltText());
    assertEquals("mediaitem.path", ROOTPATH_CONTENT + "/jcr:content/resource/data", asset.getPath());

    Rendition rendition = media.getRendition();
    assertNotNull("rendition", rendition);
    assertEquals("rendition.mediaurl", ROOTPATH_CONTENT + "/_jcr_content/resource/data./file.bin", rendition.getUrl());
    assertEquals("rendition.path", ROOTPATH_CONTENT + "/jcr:content/resource/data", rendition.getPath());
    assertEquals("rendition.filename", "file.bin", rendition.getFileName());
    assertEquals("rendition.fileextension", "bin", rendition.getFileExtension());
    assertEquals("rendition.filesize", DUMMY_BINARY.length, rendition.getFileSize());
    assertEquals("rendition.width", 0, rendition.getWidth());
    assertEquals("rendition.height", 0, rendition.getHeight());
  }

  /**
   * Test resource mediaInline child node (nt:resource) with filename
   */
  @Test
  public void testMediaInline() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(mediaInlineResource).build();

    assertTrue("media valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    assertEquals("media url", ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInline/mediaInline./mediainlinedata.bin",
        media.getUrl());

    Asset asset = media.getAsset();
    assertNotNull("mediaitem", asset);
    assertEquals("mediaitem.title", "mediainlinedata.bin", asset.getTitle());
    assertEquals("mediaitem.altText", "Inline Media Alt. Text", asset.getAltText());
    assertEquals("mediaitem.path", ROOTPATH_CONTENT + "/jcr:content/resourceMediaInline/mediaInline", asset.getPath());

    Rendition rendition = media.getRendition();
    assertNotNull("rendition", rendition);
    assertEquals("rendition.mediaurl", ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInline/mediaInline./mediainlinedata.bin",
        rendition.getUrl());
    assertEquals("rendition.path", ROOTPATH_CONTENT + "/jcr:content/resourceMediaInline/mediaInline", rendition.getPath());
    assertEquals("rendition.filename", "mediainlinedata.bin", rendition.getFileName());
    assertEquals("rendition.fileextension", "bin", rendition.getFileExtension());
    assertEquals("rendition.filesize", DUMMY_BINARY.length, rendition.getFileSize());
    assertEquals("rendition.width", 0, rendition.getWidth());
    assertEquals("rendition.height", 0, rendition.getHeight());
  }

  /**
   * Test resource mediaInline child node (nt:file + nt:resource) with filename
   */
  @Test
  public void testMediaInlineWithFile() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(mediaInlineWithFileResource).build();

    assertTrue("media valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    assertEquals("media url", ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineWithFile/mediaInline./mediainlinedata2.bin",
        media.getUrl());

    Asset asset = media.getAsset();
    assertNotNull("mediaitem", asset);
    assertEquals("mediaitem.title", "mediainlinedata2.bin", asset.getTitle());
    assertEquals("mediaitem.altText", "Inline Media Alt. Text 2", asset.getAltText());
    assertEquals("mediaitem.path", ROOTPATH_CONTENT + "/jcr:content/resourceMediaInlineWithFile/mediaInline/jcr:content", asset.getPath());

    Rendition rendition = media.getRendition();
    assertNotNull("rendition", rendition);
    assertEquals("rendition.mediaurl", ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineWithFile/mediaInline./mediainlinedata2.bin",
        rendition.getUrl());
    assertEquals("rendition.path", ROOTPATH_CONTENT + "/jcr:content/resourceMediaInlineWithFile/mediaInline/jcr:content", rendition.getPath());
    assertEquals("rendition.filename", "mediainlinedata2.bin", rendition.getFileName());
    assertEquals("rendition.fileextension", "bin", rendition.getFileExtension());
    assertEquals("rendition.filesize", DUMMY_BINARY.length, rendition.getFileSize());
    assertEquals("rendition.width", 0, rendition.getWidth());
    assertEquals("rendition.height", 0, rendition.getHeight());
  }

  /**
   * Test resource mediaInline child node (nt:file + nt:resource) with filename
   */
  @Test
  public void testMediaInlineSampleImage() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(mediaInlineSampleImageResource).build();

    assertTrue("media valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    assertEquals("media url", ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineSampleImage/mediaInline./sample_image_215x102.jpg",
        media.getUrl());

    Asset asset = media.getAsset();
    assertNotNull("mediaitem", asset);
    assertEquals("mediaitem.title", "sample_image_215x102.jpg", asset.getTitle());
    assertNull("mediaitem.altText", asset.getAltText());
    assertEquals("mediaitem.path", ROOTPATH_CONTENT + "/jcr:content/resourceMediaInlineSampleImage/mediaInline", asset.getPath());

    Rendition rendition = media.getRendition();
    assertNotNull("rendition", rendition);
    assertEquals("rendition.mediaurl", ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineSampleImage/mediaInline./sample_image_215x102.jpg",
        rendition.getUrl());
    assertEquals("rendition.path", ROOTPATH_CONTENT + "/jcr:content/resourceMediaInlineSampleImage/mediaInline", rendition.getPath());
    assertEquals("rendition.filename", "sample_image_215x102.jpg", rendition.getFileName());
    assertEquals("rendition.fileextension", "jpg", rendition.getFileExtension());
    assertEquals("rendition.filesize", 25918, rendition.getFileSize());
    assertEquals("rendition.width", 215, rendition.getWidth());
    assertEquals("rendition.height", 102, rendition.getHeight());
  }

  /**
   * Test with alt. text
   */
  @Test
  public void testWithAltText() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
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

  /**
   * Test with url mode
   */
  @Test
  public void testWithUrlMode() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

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

  /**
   * Test with fixed dimensions
   */
  @Test
  public void testWithFixedDimensions() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
    Rendition rendition;

    // test invalid resource
    rendition = mediaHandler.get(emptyResource, new MediaArgs().fixedDimension(10, 10)).build().getRendition();
    assertNull("rendition invalid", rendition);

    // test non-image resource - dimensions have no effect
    rendition = mediaHandler.get(mediaInlineResource, new MediaArgs().fixedDimension(10, 10)).build().getRendition();
    assertEquals("width", 0, rendition.getWidth());
    assertEquals("height", 0, rendition.getHeight());
    assertEquals("url", ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInline/mediaInline./mediainlinedata.bin", rendition.getUrl());

    // test image resource with invalid aspect ratio
    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(10, 10)).refProperty("mediaInline").build().getRendition();
    assertNull("rendition invalid", rendition);

    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(20, 10)).refProperty("mediaInline").build().getRendition();
    assertNull("rendition invalid", rendition);

    // test image resource with dimensions that are too big
    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(430, 204)).refProperty("mediaInline").build().getRendition();
    assertNull("rendition invalid", rendition);

    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(430, 0)).refProperty("mediaInline").build().getRendition();
    assertNull("rendition invalid", rendition);

    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(0, 204)).refProperty("mediaInline").build().getRendition();
    assertNull("rendition invalid", rendition);

    // test image resource with dimensions exact fit
    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(215, 102)).refProperty("mediaInline").build().getRendition();
    assertEquals("width", 215, rendition.getWidth());
    assertEquals("height", 102, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline./sample_image_215x102.jpg", rendition.getUrl());

    // test image resource with dimensions smaller
    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(108, 51)).refProperty("mediaInline").build().getRendition();
    assertEquals("width", 108, rendition.getWidth());
    assertEquals("height", 51, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".108.51.file/sample_image_215x102.jpg", rendition.getUrl());

    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(42, 20)).refProperty("mediaInline").build().getRendition();
    assertEquals("width", 42, rendition.getWidth());
    assertEquals("height", 20, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".42.20.file/sample_image_215x102.jpg", rendition.getUrl());

    // test image resource with dimensions only width
    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(42, 0)).refProperty("mediaInline").build().getRendition();
    assertEquals("width", 42, rendition.getWidth());
    assertEquals("height", 20, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".42.20.file/sample_image_215x102.jpg", rendition.getUrl());

    // test image resource with dimensions only height
    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(0, 20)).refProperty("mediaInline").build().getRendition();
    assertEquals("width", 42, rendition.getWidth());
    assertEquals("height", 20, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".42.20.file/sample_image_215x102.jpg", rendition.getUrl());

    // test with force download
    rendition = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs().fixedDimension(0, 20).forceDownload(true))
        .refProperty("mediaInline").build().getRendition();
    assertEquals("width", 42, rendition.getWidth());
    assertEquals("height", 20, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".42.20.download_attachment.file/sample_image_215x102.jpg",
        rendition.getUrl());

  }

  /**
   * Test with media formats
   */
  @Test
  public void testWithMediaFormats() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
    Media media;
    Rendition rendition;

    // test invalid resource
    media = mediaHandler.get(emptyResource, new MediaArgs(SHOWROOM_CONTROLS)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertFalse("media invalid", media.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MEDIA_SOURCE, media.getMediaInvalidReason());
    assertNull("rendition invalid", rendition);

    // test image resource with media formats with invalid aspect ratio
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(HOLZAUTO_BANNER)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertFalse("media invalid", media.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MATCHING_RENDITION, media.getMediaInvalidReason());
    assertNull("rendition invalid", rendition);

    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(HOLZAUTO_CUTOUT_13PLUS)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertFalse("media invalid", media.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MATCHING_RENDITION, media.getMediaInvalidReason());
    assertNull("rendition invalid", rendition);

    // test image resource with media formats that are too big
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(EDITORIAL_STANDARD)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertFalse("media invalid", media.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MATCHING_RENDITION, media.getMediaInvalidReason());
    assertNull("rendition invalid", rendition);

    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(EDITORIAL_2COL)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertFalse("media invalid", media.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MATCHING_RENDITION, media.getMediaInvalidReason());
    assertNull("rendition invalid", rendition);

    // test image resource with media format exact fit
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(EDITORIAL_1COL)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertTrue("media valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    assertEquals("width", 215, rendition.getWidth());
    assertEquals("height", 102, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline./sample_image_215x102.jpg", rendition.getUrl());
    assertEquals(EDITORIAL_1COL, rendition.getMediaFormat());

    // test image resource with dimensions smaller
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(SHOWROOM_FLYOUT_FEATURE)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertTrue("media valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    assertEquals("width", 205, rendition.getWidth());
    assertEquals("height", 97, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".205.97.file/sample_image_215x102.jpg", rendition.getUrl());
    assertEquals(SHOWROOM_FLYOUT_FEATURE, rendition.getMediaFormat());

    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(SHOWROOM_CONTROLS_SCALE1)).refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertTrue("media valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    assertEquals("width", 64, rendition.getWidth());
    assertEquals("height", 30, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".64.30.file/sample_image_215x102.jpg", rendition.getUrl());
    assertEquals(SHOWROOM_CONTROLS_SCALE1, rendition.getMediaFormat());

    // test image resource with dimensions only width
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH)).refProperty("mediaInline")
        .build();
    rendition = media.getRendition();
    assertTrue("media valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    assertEquals("width", 64, rendition.getWidth());
    assertEquals("height", 30, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".64.30.file/sample_image_215x102.jpg", rendition.getUrl());
    assertEquals(SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH, rendition.getMediaFormat());

    // test image resource with dimensions only height
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(SHOWROOM_CONTROLS_SCALE1_ONLYHEIGHT)).refProperty("mediaInline")
        .build();
    rendition = media.getRendition();
    assertTrue("media valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    assertEquals("width", 63, rendition.getWidth());
    assertEquals("height", 30, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".63.30.file/sample_image_215x102.jpg", rendition.getUrl());
    assertEquals(SHOWROOM_CONTROLS_SCALE1_ONLYHEIGHT, rendition.getMediaFormat());

    // test image resource with dimensions only width, fitting ratio
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO1))
        .refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertTrue("media valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    assertEquals("width", 64, rendition.getWidth());
    assertEquals("height", 30, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".64.30.file/sample_image_215x102.jpg", rendition.getUrl());
    assertEquals(SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO1, rendition.getMediaFormat());

    // test image resource with dimensions only width, invalid ratio
    media = mediaHandler.get(mediaInlineSampleImageResource, new MediaArgs(SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO2))
        .refProperty("mediaInline").build();
    rendition = media.getRendition();
    assertFalse("media invalid", media.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MATCHING_RENDITION, media.getMediaInvalidReason());
    assertNull("rendition invalid", rendition);

  }

  @Test
  public void testDownloadMediaElement() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
    MediaArgs mediaArgs = new MediaArgs().forceDownload(true);
    Media media = mediaHandler.get(mediaInlineSampleImageResource, mediaArgs).build();
    assertTrue("valid?", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    assertNotNull("asset?", media.getAsset());
    assertNotNull("rendition?", media.getRendition());
    assertEquals("rendition.mediaUrl",
        ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineSampleImage/mediaInline."
            + MediaFileServlet.SELECTOR + "." + MediaFileServlet.SELECTOR_DOWNLOAD + ".file/sample_image_215x102.jpg",
            media.getRendition().getUrl());
  }

  @Test
  public void testMultipleMediaMediaFormats() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
    MediaArgs mediaArgs = new MediaArgs(EDITORIAL_1COL, EDITORIAL_2COL, EDITORIAL_3COL);
    Media media = mediaHandler.get(mediaInlineSampleImageResource, mediaArgs).build();
    assertTrue("valid?", media.isValid());
    assertNotNull("asset?", media.getAsset());
    assertEquals("renditions", 1, media.getRenditions().size());
    assertEquals("rendition.mediaUrl",
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline./sample_image_215x102.jpg",
        media.getUrl());
    assertEquals(EDITORIAL_1COL, media.getRendition().getMediaFormat());
  }

  @Test
  public void testMultipleMandatoryMediaFormats() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(EDITORIAL_1COL, SHOWROOM_FLYOUT_FEATURE, SHOWROOM_CONTROLS_SCALE1);
    Media media = mediaHandler.get(mediaInlineSampleImageResource, mediaArgs).build();
    assertTrue("valid?", media.isValid());
    assertNotNull("asset?", media.getAsset());
    assertEquals("renditions", 3, media.getRenditions().size());
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals("rendition.mediaUrl.1",
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline./sample_image_215x102.jpg",
        renditions.get(0).getUrl());
    assertEquals(EDITORIAL_1COL, renditions.get(0).getMediaFormat());

    assertEquals("rendition.mediaUrl.2",
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline.image_file.205.97.file/sample_image_215x102.jpg",
        renditions.get(1).getUrl());
    assertEquals(SHOWROOM_FLYOUT_FEATURE, renditions.get(1).getMediaFormat());

    assertEquals("rendition.mediaUrl.3",
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline.image_file.64.30.file/sample_image_215x102.jpg",
        renditions.get(2).getUrl());
    assertEquals(SHOWROOM_CONTROLS_SCALE1, renditions.get(2).getMediaFormat());
  }

  @Test
  public void testMultipleMandatoryMediaFormatsNotAllMatch() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(EDITORIAL_2COL, SHOWROOM_FLYOUT_FEATURE, SHOWROOM_CONTROLS_SCALE1);
    Media media = mediaHandler.get(mediaInlineSampleImageResource, mediaArgs).build();
    assertFalse("valid?", media.isValid());
    assertEquals(MediaInvalidReason.NOT_ENOUGH_MATCHING_RENDITIONS, media.getMediaInvalidReason());
    assertNotNull("asset?", media.getAsset());
    assertEquals("renditions", 2, media.getRenditions().size());
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals("rendition.mediaUrl.1",
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline.image_file.205.97.file/sample_image_215x102.jpg",
        renditions.get(0).getUrl());
    assertEquals(SHOWROOM_FLYOUT_FEATURE, renditions.get(0).getMediaFormat());

    assertEquals("rendition.mediaUrl.2",
        "/content/unittest/de_test/brand/de/_jcr_content/resourceMediaInlineSampleImage/mediaInline.image_file.64.30.file/sample_image_215x102.jpg",
        renditions.get(1).getUrl());
    assertEquals(SHOWROOM_CONTROLS_SCALE1, renditions.get(1).getMediaFormat());
  }

}
