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

import static io.wcm.handler.mediasource.inline.testcontext.AppAemContext.ROOTPATH_CONTENT;
import static io.wcm.handler.mediasource.inline.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static io.wcm.handler.mediasource.inline.testcontext.DummyMediaFormats.EDITORIAL_2COL;
import static io.wcm.handler.mediasource.inline.testcontext.DummyMediaFormats.EDITORIAL_STANDARD;
import static io.wcm.handler.mediasource.inline.testcontext.DummyMediaFormats.HOLZAUTO_BANNER;
import static io.wcm.handler.mediasource.inline.testcontext.DummyMediaFormats.HOLZAUTO_CUTOUT_13PLUS;
import static io.wcm.handler.mediasource.inline.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS;
import static io.wcm.handler.mediasource.inline.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1;
import static io.wcm.handler.mediasource.inline.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1_ONLYHEIGHT;
import static io.wcm.handler.mediasource.inline.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH;
import static io.wcm.handler.mediasource.inline.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO1;
import static io.wcm.handler.mediasource.inline.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO2;
import static io.wcm.handler.mediasource.inline.testcontext.DummyMediaFormats.SHOWROOM_FLYOUT_FEATURE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaItem;
import io.wcm.handler.media.MediaMetadata;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.args.MediaArgs;
import io.wcm.handler.media.args.MediaArgsType;
import io.wcm.handler.media.impl.ImageFileServlet;
import io.wcm.handler.media.impl.MediaFileServlet;
import io.wcm.handler.mediasource.inline.testcontext.AppAemContext;
import io.wcm.handler.url.UrlModes;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.commons.contenttype.ContentType;

import java.io.ByteArrayInputStream;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;

/**
 * Test {@link InlineMediaSource}
 */
public class InlineMediaSourceTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

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

    MediaMetadata mediaMetadata = mediaHandler.getMediaMetadata(emptyResource, new MediaArgs());

    assertFalse("media invalid", mediaMetadata.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MEDIA_SOURCE, mediaMetadata.getMediaInvalidReason());
  }

  /**
   * Test nt:resource resource without filename
   */
  @Test
  public void testNtFile() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    MediaMetadata mediaMetadata = mediaHandler.getMediaMetadata(ntFileResource, new MediaArgs());

    assertTrue("media valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    assertEquals("media url", ROOTPATH_CONTENT + "/_jcr_content/filedata.bin", mediaMetadata.getMediaUrl());

    MediaItem mediaItem = mediaMetadata.getMediaItem();
    assertNotNull("mediaitem", mediaItem);
    assertEquals("mediaitem.title", "filedata.bin", mediaItem.getTitle());
    assertNull("mediaitem.altText", mediaItem.getAltText());
    assertEquals("mediaitem.path", ROOTPATH_CONTENT + "/jcr:content/filedata.bin/jcr:content", mediaItem.getPath());

    Rendition rendition = mediaMetadata.getRendition();
    assertNotNull("rendition", rendition);
    assertEquals("rendition.mediaurl", ROOTPATH_CONTENT + "/_jcr_content/filedata.bin", rendition.getMediaUrl());
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

    MediaMetadata mediaMetadata = mediaHandler.getMediaMetadata(ntResourceResource, new MediaArgs());

    assertTrue("media valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    assertEquals("media url", ROOTPATH_CONTENT + "/_jcr_content/resource/data./file.bin", mediaMetadata.getMediaUrl());

    MediaItem mediaItem = mediaMetadata.getMediaItem();
    assertNotNull("mediaitem", mediaItem);
    assertEquals("mediaitem.title", "file.bin", mediaItem.getTitle());
    assertNull("mediaitem.altText", mediaItem.getAltText());
    assertEquals("mediaitem.path", ROOTPATH_CONTENT + "/jcr:content/resource/data", mediaItem.getPath());

    Rendition rendition = mediaMetadata.getRendition();
    assertNotNull("rendition", rendition);
    assertEquals("rendition.mediaurl", ROOTPATH_CONTENT + "/_jcr_content/resource/data./file.bin", rendition.getMediaUrl());
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

    MediaMetadata mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineResource, new MediaArgs());

    assertTrue("media valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    assertEquals("media url", ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInline/mediaInline./mediainlinedata.bin",
        mediaMetadata.getMediaUrl());

    MediaItem mediaItem = mediaMetadata.getMediaItem();
    assertNotNull("mediaitem", mediaItem);
    assertEquals("mediaitem.title", "mediainlinedata.bin", mediaItem.getTitle());
    assertEquals("mediaitem.altText", "Inline Media Alt. Text", mediaItem.getAltText());
    assertEquals("mediaitem.path", ROOTPATH_CONTENT + "/jcr:content/resourceMediaInline/mediaInline", mediaItem.getPath());

    Rendition rendition = mediaMetadata.getRendition();
    assertNotNull("rendition", rendition);
    assertEquals("rendition.mediaurl", ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInline/mediaInline./mediainlinedata.bin",
        rendition.getMediaUrl());
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

    MediaMetadata mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineWithFileResource, new MediaArgs());

    assertTrue("media valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    assertEquals("media url", ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineWithFile/mediaInline./mediainlinedata2.bin",
        mediaMetadata.getMediaUrl());

    MediaItem mediaItem = mediaMetadata.getMediaItem();
    assertNotNull("mediaitem", mediaItem);
    assertEquals("mediaitem.title", "mediainlinedata2.bin", mediaItem.getTitle());
    assertEquals("mediaitem.altText", "Inline Media Alt. Text 2", mediaItem.getAltText());
    assertEquals("mediaitem.path", ROOTPATH_CONTENT + "/jcr:content/resourceMediaInlineWithFile/mediaInline/jcr:content", mediaItem.getPath());

    Rendition rendition = mediaMetadata.getRendition();
    assertNotNull("rendition", rendition);
    assertEquals("rendition.mediaurl", ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineWithFile/mediaInline./mediainlinedata2.bin",
        rendition.getMediaUrl());
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

    MediaMetadata mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, new MediaArgs());

    assertTrue("media valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    assertEquals("media url", ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineSampleImage/mediaInline./sample_image_215x102.jpg",
        mediaMetadata.getMediaUrl());

    MediaItem mediaItem = mediaMetadata.getMediaItem();
    assertNotNull("mediaitem", mediaItem);
    assertEquals("mediaitem.title", "sample_image_215x102.jpg", mediaItem.getTitle());
    assertNull("mediaitem.altText", mediaItem.getAltText());
    assertEquals("mediaitem.path", ROOTPATH_CONTENT + "/jcr:content/resourceMediaInlineSampleImage/mediaInline", mediaItem.getPath());

    Rendition rendition = mediaMetadata.getRendition();
    assertNotNull("rendition", rendition);
    assertEquals("rendition.mediaurl", ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineSampleImage/mediaInline./sample_image_215x102.jpg",
        rendition.getMediaUrl());
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
    MediaMetadata mediaMetadata;

    MediaArgsType mediaArgs = new MediaArgs();
    mediaArgs.setAltText("Der Jodelkaiser");

    mediaMetadata = mediaHandler.getMediaMetadata(ntFileResource, mediaArgs);
    assertEquals("Der Jodelkaiser", mediaMetadata.getMediaItem().getAltText());

    mediaMetadata = mediaHandler.getMediaMetadata(ntResourceResource, mediaArgs);
    assertEquals("Der Jodelkaiser", mediaMetadata.getMediaItem().getAltText());

    mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineResource, mediaArgs);
    assertEquals("Der Jodelkaiser", mediaMetadata.getMediaItem().getAltText());

    mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineWithFileResource, mediaArgs);
    assertEquals("Der Jodelkaiser", mediaMetadata.getMediaItem().getAltText());

    mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, mediaArgs);
    assertEquals("Der Jodelkaiser", mediaMetadata.getMediaItem().getAltText());

  }

  /**
   * Test with url mode
   */
  @Test
  public void testWithUrlMode() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    MediaArgsType mediaArgs = MediaArgs.urlMode(UrlModes.FULL_URL);

    assertEquals("http://www.dummysite.org" + ROOTPATH_CONTENT + "/_jcr_content/filedata.bin",
        mediaHandler.getMediaUrl(ntFileResource, mediaArgs));
    assertEquals("http://www.dummysite.org" + ROOTPATH_CONTENT + "/_jcr_content/resource/data./file.bin",
        mediaHandler.getMediaUrl(ntResourceResource, mediaArgs));
    assertEquals("http://www.dummysite.org" + ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInline/mediaInline./mediainlinedata.bin",
        mediaHandler.getMediaUrl(mediaInlineResource, mediaArgs));
    assertEquals("http://www.dummysite.org" + ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineWithFile/mediaInline./mediainlinedata2.bin",
        mediaHandler.getMediaUrl(mediaInlineWithFileResource, mediaArgs));
    assertEquals("http://www.dummysite.org" + ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineSampleImage/mediaInline./sample_image_215x102.jpg",
        mediaHandler.getMediaUrl(mediaInlineSampleImageResource, mediaArgs));
    assertNull(mediaHandler.getMediaUrl(emptyResource, mediaArgs));

  }

  /**
   * Test with fixed dimensions
   */
  @Test
  public void testWithFixedDimensions() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
    Rendition rendition;

    // test invalid resource
    rendition = mediaHandler.getMediaMetadata(emptyResource, MediaArgs.fixedDimension(10, 10)).getRendition();
    assertNull("rendition invalid", rendition);

    // test non-image resource - dimensions have no effect
    rendition = mediaHandler.getMediaMetadata(mediaInlineResource, MediaArgs.fixedDimension(10, 10)).getRendition();
    assertEquals("width", 0, rendition.getWidth());
    assertEquals("height", 0, rendition.getHeight());
    assertEquals("url", ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInline/mediaInline./mediainlinedata.bin", rendition.getMediaUrl());

    // test image resource with invalid aspect ratio
    rendition = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.fixedDimension(10, 10)).getRendition();
    assertNull("rendition invalid", rendition);

    rendition = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.fixedDimension(20, 10)).getRendition();
    assertNull("rendition invalid", rendition);

    // test image resource with dimensions that are too big
    rendition = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.fixedDimension(430, 204)).getRendition();
    assertNull("rendition invalid", rendition);

    rendition = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.fixedDimension(430, 0)).getRendition();
    assertNull("rendition invalid", rendition);

    rendition = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.fixedDimension(0, 204)).getRendition();
    assertNull("rendition invalid", rendition);

    // test image resource with dimensions exact fit
    rendition = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.fixedDimension(215, 102)).getRendition();
    assertEquals("width", 215, rendition.getWidth());
    assertEquals("height", 102, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline./sample_image_215x102.jpg", rendition.getMediaUrl());

    // test image resource with dimensions smaller
    rendition = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.fixedDimension(108, 51)).getRendition();
    assertEquals("width", 108, rendition.getWidth());
    assertEquals("height", 51, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".108.51.file/sample_image_215x102.jpg", rendition.getMediaUrl());

    rendition = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.fixedDimension(42, 20)).getRendition();
    assertEquals("width", 42, rendition.getWidth());
    assertEquals("height", 20, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".42.20.file/sample_image_215x102.jpg", rendition.getMediaUrl());

    // test image resource with dimensions only width
    rendition = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.fixedDimension(42, 0)).getRendition();
    assertEquals("width", 42, rendition.getWidth());
    assertEquals("height", 20, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".42.20.file/sample_image_215x102.jpg", rendition.getMediaUrl());

    // test image resource with dimensions only height
    rendition = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.fixedDimension(0, 20)).getRendition();
    assertEquals("width", 42, rendition.getWidth());
    assertEquals("height", 20, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".42.20.file/sample_image_215x102.jpg", rendition.getMediaUrl());

    // test with force download
    rendition = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.fixedDimension(0, 20).setForceDownload(true))
        .getRendition();
    assertEquals("width", 42, rendition.getWidth());
    assertEquals("height", 20, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".42.20.download_attachment.file/sample_image_215x102.jpg",
        rendition.getMediaUrl());

  }

  /**
   * Test with media formats
   */
  @Test
  public void testWithMediaFormats() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
    MediaMetadata mediaMetadata;
    Rendition rendition;

    // test invalid resource
    mediaMetadata = mediaHandler.getMediaMetadata(emptyResource, "mediaInline", MediaArgs.mediaFormat(SHOWROOM_CONTROLS));
    rendition = mediaMetadata.getRendition();
    assertFalse("media invalid", mediaMetadata.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MEDIA_SOURCE, mediaMetadata.getMediaInvalidReason());
    assertNull("rendition invalid", rendition);

    // test image resource with media formats with invalid aspect ratio
    mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.mediaFormat(HOLZAUTO_BANNER));
    rendition = mediaMetadata.getRendition();
    assertFalse("media invalid", mediaMetadata.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MATCHING_RENDITION, mediaMetadata.getMediaInvalidReason());
    assertNull("rendition invalid", rendition);

    mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.mediaFormat(HOLZAUTO_CUTOUT_13PLUS));
    rendition = mediaMetadata.getRendition();
    assertFalse("media invalid", mediaMetadata.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MATCHING_RENDITION, mediaMetadata.getMediaInvalidReason());
    assertNull("rendition invalid", rendition);

    // test image resource with media formats that are too big
    mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.mediaFormat(EDITORIAL_STANDARD));
    rendition = mediaMetadata.getRendition();
    assertFalse("media invalid", mediaMetadata.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MATCHING_RENDITION, mediaMetadata.getMediaInvalidReason());
    assertNull("rendition invalid", rendition);

    mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.mediaFormat(EDITORIAL_2COL));
    rendition = mediaMetadata.getRendition();
    assertFalse("media invalid", mediaMetadata.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MATCHING_RENDITION, mediaMetadata.getMediaInvalidReason());
    assertNull("rendition invalid", rendition);

    // test image resource with media format exact fit
    mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.mediaFormat(EDITORIAL_1COL));
    rendition = mediaMetadata.getRendition();
    assertTrue("media valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    assertEquals("width", 215, rendition.getWidth());
    assertEquals("height", 102, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline./sample_image_215x102.jpg", rendition.getMediaUrl());

    // test image resource with dimensions smaller
    mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.mediaFormat(SHOWROOM_FLYOUT_FEATURE));
    rendition = mediaMetadata.getRendition();
    assertTrue("media valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    assertEquals("width", 205, rendition.getWidth());
    assertEquals("height", 97, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".205.97.file/sample_image_215x102.jpg", rendition.getMediaUrl());

    mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline", MediaArgs.mediaFormat(SHOWROOM_CONTROLS_SCALE1));
    rendition = mediaMetadata.getRendition();
    assertTrue("media valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    assertEquals("width", 64, rendition.getWidth());
    assertEquals("height", 30, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".64.30.file/sample_image_215x102.jpg", rendition.getMediaUrl());

    // test image resource with dimensions only width
    mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline",
        MediaArgs.mediaFormat(SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH));
    rendition = mediaMetadata.getRendition();
    assertTrue("media valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    assertEquals("width", 64, rendition.getWidth());
    assertEquals("height", 30, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".64.30.file/sample_image_215x102.jpg", rendition.getMediaUrl());

    // test image resource with dimensions only height
    mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline",
        MediaArgs.mediaFormat(SHOWROOM_CONTROLS_SCALE1_ONLYHEIGHT));
    rendition = mediaMetadata.getRendition();
    assertTrue("media valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    assertEquals("width", 63, rendition.getWidth());
    assertEquals("height", 30, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".63.30.file/sample_image_215x102.jpg", rendition.getMediaUrl());

    // test image resource with dimensions only width, fitting ratio
    mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline",
        MediaArgs.mediaFormat(SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO1));
    rendition = mediaMetadata.getRendition();
    assertTrue("media valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    assertEquals("width", 64, rendition.getWidth());
    assertEquals("height", 30, rendition.getHeight());
    assertEquals("url", PAR_INLINEIMAGE_PATH + "/mediaInline." + ImageFileServlet.SELECTOR + ".64.30.file/sample_image_215x102.jpg", rendition.getMediaUrl());

    // test image resource with dimensions only width, invalid ratio
    mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, "mediaInline",
        MediaArgs.mediaFormat(SHOWROOM_CONTROLS_SCALE1_ONLYWIDTH_RATIO2));
    rendition = mediaMetadata.getRendition();
    assertFalse("media invalid", mediaMetadata.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MATCHING_RENDITION, mediaMetadata.getMediaInvalidReason());
    assertNull("rendition invalid", rendition);

  }

  /**
   * Test method for {@link MediaHandler#getMediaMetadata(Resource, MediaArgsType)}
   */
  @Test
  public void testDownloadMediaElement() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
    MediaArgsType mediaArgs = new MediaArgs().setForceDownload(true);
    MediaMetadata mediaMetadata = mediaHandler.getMediaMetadata(mediaInlineSampleImageResource, mediaArgs);
    assertTrue("valid?", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    assertNotNull("mediaItem?", mediaMetadata.getMediaItem());
    assertNotNull("rendition?", mediaMetadata.getRendition());
    assertEquals("rendition.mediaUrl",
        ROOTPATH_CONTENT + "/_jcr_content/resourceMediaInlineSampleImage/mediaInline."
            + MediaFileServlet.SELECTOR + "." + MediaFileServlet.SELECTOR_DOWNLOAD + ".file/sample_image_215x102.jpg",
            mediaMetadata.getRendition().getMediaUrl());
  }

}
