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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.MediaArgsType;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaItem;
import io.wcm.handler.media.MediaMarkupBuilder;
import io.wcm.handler.media.MediaMetadata;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.args.MediaArgs;
import io.wcm.handler.url.integrator.IntegratorHandler;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Test;

import com.day.cq.dam.api.DamConstants;
import com.day.cq.wcm.api.WCMMode;

/**
 * Test {@link DamMediaSource}
 */
public class DamMediaSourceTest extends AbstractDamTest {

  @Test
  public void testGetMediaItemInfoStringExisting() {
    // get MediaItemInfo for an existing item - should return the info object
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(MEDIAITEM_PATH_STANDARD);
    assertTrue("valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    MediaItem info = mediaMetadata.getMediaItem();
    assertNotNull("returned info?", info);
    assertEquals("path equals?", MEDIAITEM_PATH_STANDARD, info.getPath());
  }

  @Test
  public void testGetMediaItemInfoStringNonExistant() {
    // get MediaItemInfo for an item that does not exist - should return null
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(MEDIAITEM_PATH_NONEXISTANT);
    assertFalse("valid", mediaMetadata.isValid());
    assertEquals("invalid reason", MediaInvalidReason.MEDIA_REFERENCE_INVALID, mediaMetadata.getMediaInvalidReason());
    MediaItem info = mediaMetadata.getMediaItem();
    assertNull("returned null?", info);
  }

  @Test
  public void testGetMediaItemInfoStringEmpty() {
    // get MediaItemInfo for empty string path - should not crash but return null
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata("");
    assertFalse("valid", mediaMetadata.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MEDIA_SOURCE, mediaMetadata.getMediaInvalidReason());
    MediaItem info = mediaMetadata.getMediaItem();
    assertNull("returned null?", info);
  }

  @Test
  public void testGetMediaItemInfoStringNull() {
    // get MediaItemInfo for null path - should not crash but return null
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata((String)null);
    assertFalse("valid", mediaMetadata.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MEDIA_SOURCE, mediaMetadata.getMediaInvalidReason());
    MediaItem info = mediaMetadata.getMediaItem();
    assertNull("returned null?", info);
  }

  @Test
  public void testGetMediaItemInfoResource() {
    // get the info for the paragraph that contains a mediaRef to the 'standard' media item
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(parStandardMediaRef);
    assertTrue("valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    MediaItem info = mediaMetadata.getMediaItem();
    assertNotNull("returned info?", info);
    assertEquals("mediaRef correctly resolved?", MEDIAITEM_PATH_STANDARD, info.getPath());
    assertEquals("alt text from medialib?", "Editorial Standard 1", info.getAltText());
  }

  @Test
  public void testGetMediaItemInfoResourceCrop() {
    // get the info for the paragraph that contains a mediaRef to the 'standard' media item
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(parStandardMediaRefCrop);
    assertTrue("valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    MediaItem info = mediaMetadata.getMediaItem();
    assertNotNull("returned info?", info);
    assertEquals("mediaRef correctly resolved?", MEDIAITEM_PATH_STANDARD, info.getPath());
  }

  @Test
  public void testGetMediaItemInfoResourceAltText() {
    // get the info for the paragraph that contains a mediaRef to the 'standard' media item & editorial alt.Text
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(parStandardMediaRefAltText);
    assertTrue("valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    MediaItem info = mediaMetadata.getMediaItem();
    assertNotNull("returned info?", info);
    assertEquals("mediaRef correctly resolved?", MEDIAITEM_PATH_STANDARD, info.getPath());
    assertEquals("alt text from paragraph?", "Alt. Text from Paragraph", info.getAltText());
  }

  @Test
  public void testGetMediaItemInfoResourceInvalid() {
    // get the info for the paragraph that contains a invalid mediaRef - should return null
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(parInvalidMediaRef);
    assertFalse("valid", mediaMetadata.isValid());
    assertEquals("invalid reason", MediaInvalidReason.MEDIA_REFERENCE_INVALID, mediaMetadata.getMediaInvalidReason());
    MediaItem info = mediaMetadata.getMediaItem();
    assertNull("returned null?", info);
  }

  @Test
  public void testGetMediaItemInfoResourceEmpty() {
    // get the info for the paragraph that contains an empty mediaRef - should return null
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(parEmptyMediaRef);
    assertFalse("valid", mediaMetadata.isValid());
    assertEquals("invalid reason", MediaInvalidReason.MEDIA_REFERENCE_MISSING, mediaMetadata.getMediaInvalidReason());
    MediaItem info = mediaMetadata.getMediaItem();
    assertNull("returned null?", info);
  }

  @Test
  public void testGetMediaItemInfoResourceNull() {
    // get the info for the paragraph that contains a null mediaRef - should return null
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(parNullMediaRef);
    assertFalse("valid", mediaMetadata.isValid());
    assertEquals("invalid reason", MediaInvalidReason.MEDIA_REFERENCE_MISSING, mediaMetadata.getMediaInvalidReason());
    MediaItem info = mediaMetadata.getMediaItem();
    assertNull("returned null?", info);
  }

  @Test
  public void testGetMediaItemInfoResourceString() {
    // get the info for the paragraph that contains a mediaRef2 to the 'standard' media item
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(parStandardMediaRef2, "mediaRef2");
    assertTrue("valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());
    MediaItem info = mediaMetadata.getMediaItem();
    assertNotNull("returned info?", info);
    assertEquals("mediaRef correctly resolved?", MEDIAITEM_PATH_STANDARD, info.getPath());
    assertEquals("alt text from medialib?", "Editorial Standard 1", info.getAltText());
  }

  // TEST METHODS GENERATING HTML ELEMENTS *****************************************************************************

  @Test
  public void testGetMediaElementInvalid() {
    // create img element for a paragraph with invalid media ref - should not crash but return null
    HtmlElement img = mediaHandler().getMedia(parInvalidMediaRef, new MediaArgs());
    assertNull("returned null?", img);
  }

  @Test
  public void testGetMediaElementEmpty() {
    // create img element for a paragraph with empty media ref - should not crash but return null
    HtmlElement img = mediaHandler().getMedia(parEmptyMediaRef, new MediaArgs());
    assertNull("returned null?", img);
  }

  @Test
  public void testGetMediaElementNull() {
    // create img element for a paragraph with null media ref - should not crash but return null
    HtmlElement img = mediaHandler().getMedia(parNullMediaRef, new MediaArgs());
    assertNull("returned null?", img);
  }

  @Test
  public void testGetMediaElementNullResource() {
    // pass-in null for the paragraph resource - should not crash but return null
    HtmlElement img = mediaHandler().getMedia((Resource)null, new MediaArgs());
    assertNull("returned null?", img);
  }

  @Test
  public void testGetMediaElementImageAltTextFromMediaLib() {
    // create img element for the first rendition of the 'standard' media-item
    MediaArgsType args = new MediaArgs();
    HtmlElement img = mediaHandler().getMedia(parStandardMediaRef, args);
    assertNotNull("returned html element?", img);
    assertEquals("is img?", "img", img.getName());
    assertEquals("src set?", mediaHandler().getMediaUrl(MEDIAITEM_PATH_STANDARD, args), img.getAttributeValue("src"));
    assertEquals("width set?", 215, img.getAttributeValueAsInteger("width"));
    assertEquals("height set?", 102, img.getAttributeValueAsInteger("height"));
    assertEquals("alt text from medialib?", "Editorial Standard 1", img.getAttributeValue("alt"));
  }

  @Test
  public void testGetMediaElementImageAltTextFromParagraph() {
    // create img element for the paragraph that has a editorial alt-text defined in the paragraph
    HtmlElement img = mediaHandler().getMedia(parStandardMediaRefAltText, new MediaArgs());
    assertNotNull("returned html element?", img);
    assertEquals("alt text from paragraph?", "Alt. Text from Paragraph", img.getAttributeValue("alt"));
  }

  @Test
  public void testGetMediaElementImageAltTextOverride() {
    // define alt-text-override via MediaArgsType and check if it is appears in the img-tag
    MediaArgsType args = new MediaArgs();
    args.setAltText("Alt. Text Override!");
    HtmlElement img = mediaHandler().getMedia(parStandardMediaRef, args);
    assertNotNull("returned html element?", img);
    assertEquals("alt text from override?", "Alt. Text Override!", img.getAttributeValue("alt"));
  }

  @Test
  public void testGetMediaElementImageNoAltTextNoDimensions() {
    // create img-tag for the medialib-item that has no alt-text, and its rendition lacks dimension information
    HtmlElement img = mediaHandler().getMedia(parImgNoAltNoDimension, new MediaArgs());
    assertNotNull("returned html element?", img);
    assertEquals("src set?", mediaHandler().getMediaUrl(MEDIAITEM_PATH_IMAGE_NOALT_NODIMENSIONS, new MediaArgs()), img.getAttributeValue("src"));

    assertEquals("alt text", "Image with no altText and a rendition w/o fileSize & dimensions", img.getAttributeValue("alt"));

    assertEquals("width from mediaformat?", 0, img.getAttributeValueAsInteger("width"));
    assertEquals("height from mediaformat?", 0, img.getAttributeValueAsInteger("height"));
  }

  @Test
  public void testGetMediaElementImageSpecificMediaFormat() {
    // create img element for rendition with standard_2col media format
    MediaArgsType args = MediaArgs.mediaFormat("/apps/test/mediaformat/standard_2col");
    HtmlElement img = mediaHandler().getMedia(parStandardMediaRef, args);
    assertNotNull("returned html element?", img);
    assertEquals("is img?", "img", img.getName());
    // check that this is the requested mediaformat via width/height-attributes of the img-tag
    assertEquals("width set?", 450, img.getAttributeValueAsInteger("width"));
    assertEquals("height set?", 213, img.getAttributeValueAsInteger("height"));
  }

  @Test
  public void testGetMediaElementImageSpecificMediaFormat_ShortFormat() {
    // create img element for rendition with standard_2col media format
    MediaArgsType args = MediaArgs.mediaFormat("standard_2col");
    HtmlElement img = mediaHandler().getMedia(parStandardMediaRef, args);
    assertNotNull("returned html element?", img);
    assertEquals("is img?", "img", img.getName());
    // check that this is the requested mediaformat via width/height-attributes of the img-tag
    assertEquals("width set?", 450, img.getAttributeValueAsInteger("width"));
    assertEquals("height set?", 213, img.getAttributeValueAsInteger("height"));
  }

  @Test
  public void testGetMediaElementImageSpecificMediaFormatCrop() {
    // create img element for rendition with standard_2col media format
    MediaArgsType args = MediaArgs.mediaFormat("home_teaser_scale1");
    HtmlElement img = mediaHandler().getMedia(parStandardMediaRefCrop, args);
    assertNotNull("returned html element?", img);
    assertEquals("is img?", "img", img.getName());
    // check that this is the requested mediaformat via width/height-attributes of the img-tag
    assertEquals("width set?", 158, img.getAttributeValueAsInteger("width"));
    assertEquals("height set?", 80, img.getAttributeValueAsInteger("height"));
  }

  @Test
  public void testGetMediaElementImageSpecificMediaFormatCropInvalid() {
    // create img element for rendition with standard_2col media format
    MediaArgsType args = MediaArgs.mediaFormat("standard_2col");
    HtmlElement img = mediaHandler().getMedia(parStandardMediaRefCrop, args);
    assertNull("returned html element?", img);
  }

  @Test
  public void testGetMediaElementImageInvalidMediaFormat() {
    // create img element in a mediaFormat for which there is no rendition is available - returns any rendition
    MediaArgsType args = MediaArgs.mediaFormat("/apps/test/mediaformat/someotherformat");
    HtmlElement img = mediaHandler().getMedia(parStandardMediaRef, args);
    assertNotNull("returned null?", img);
  }

  @Test
  public void testGetMediaElementFlashWithoutFallback() {
    // create media-element from a flash mediaRef with no fallback image - should return a div
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(parFlashWithoutFallbackMediaRef, new MediaArgs());
    assertTrue("valid?", mediaMetadata.isValid());
    assertNotNull("mediaItem?", mediaMetadata.getMediaItem());
    assertNotNull("rendition?", mediaMetadata.getRendition());
    assertEquals("rendition.mediaUrl", "/content/dam/test/flashWithoutFallback.swf/_jcr_content/renditions/original./flashWithoutFallback.swf",
        mediaMetadata.getRendition().getMediaUrl());
    assertNull("fallbackRendition?", mediaMetadata.getFallbackRendition());
  }

  @Test
  public void testGetMediaElementEditModeDummyImage() {
    // simulate edit-mode
    WCMMode.EDIT.toRequest(context.request());

    // dummy image is added only if a specific media format is requested
    MediaArgsType args = MediaArgs.mediaFormat("standard_2col");
    HtmlElement img = mediaHandler().getMedia(parInvalidMediaRef, args);

    assertNotNull("returned element?", img);
    assertEquals("is img?", "img", img.getName());
    assertEquals("src set?", MediaMarkupBuilder.DUMMY_IMAGE, img.getAttributeValue("src"));
    assertEquals("width set?", 450, img.getAttributeValueAsInteger("width"));
    assertEquals("height set?", 213, img.getAttributeValueAsInteger("height"));
    assertTrue("has dummy css class?", img.getCssClass().contains(MediaNameConstants.CSS_DUMMYIMAGE));
  }

  @Test
  public void testGetMediaElementEditModeDummyImageThumbnail() {
    // simulate edit-mode
    WCMMode.EDIT.toRequest(context.request());

    // if fixed dimensions are specified, the image must have exactly the specified size
    MediaArgsType args = MediaArgs.mediaFormat("standard_2col");
    args.setFixedDimensions(100, 100);
    HtmlElement img = mediaHandler().getMedia(parNullMediaRef, args);

    assertNotNull("returned element?", img);
    assertEquals("width set?", 100, img.getAttributeValueAsInteger("width"));
    assertEquals("height set?", 100, img.getAttributeValueAsInteger("height"));
  }

  // TESTS FOR FUNCTIONS THAT DELEGATE TO MediaLibHandler (WHERE THEY ARE TESTED IN MORE DETAIL)

  @Test
  public void testGetMediaUrlStandard() {
    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().getMediaUrl(MEDIAITEM_PATH_STANDARD, new MediaArgs());
    assertNotNull("returned url?", url);
    assertEquals("url as expected?", "/content/dam/test/standard.jpg/_jcr_content/renditions/original./standard.jpg", url);
  }

  @Test
  public void testGetMediaUrlNull() {
    // getMediaUrl should handle null mediaRefs and return null
    String url = mediaHandler().getMediaUrl((String)null, new MediaArgs());
    assertNull("returned null?", url);
  }

  @Test
  public void testGetMediaUrlEmpty() {
    // getMediaUrl should handle empty mediaRefs and return null
    String url = mediaHandler().getMediaUrl("", new MediaArgs());
    assertNull("returned null?", url);
  }

  @Test
  public void testGetMediaUrlIntegrator() throws Exception {

    // activate integrator mode
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);

    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().getMediaUrl(MEDIAITEM_PATH_STANDARD, new MediaArgs());
    assertNotNull("returned url?", url);
    assertEquals("url as expected?", "http://www.dummysite.org/content/dam/test/standard.jpg/_jcr_content/renditions/original./standard.jpg", url);

  }

  @Test
  public void testGetMediaProperties() {
    // get the properties of the first rendition of the 'standard' media item
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(MEDIAITEM_PATH_STANDARD, new MediaArgs());
    ValueMap props = mediaMetadata.getMediaItem().getProperties();
    assertNotNull("returned props?", props);
    assertEquals("are there media item props?", "Editorial Standard 1",
        props.get(DamConstants.DC_TITLE, String[].class)[0]);
  }

  @Test
  public void testGetRenditionProperties() {
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(MEDIAITEM_PATH_STANDARD, new MediaArgs());
    ValueMap props = mediaMetadata.getRendition().getProperties();
    assertNotNull("returned props?", props);
    assertEquals("are there rendition props?", 1, props.size());
  }

  @Test
  public void testGetMediaItemInfoVideo() {
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(MEDIAITEM_VIDEO, MediaArgs.mediaFormat("video_2col"));
    assertTrue("valid", mediaMetadata.isValid());
    assertNull("no invalid reason", mediaMetadata.getMediaInvalidReason());

    MediaItem mediaItem = mediaMetadata.getMediaItem();
    assertNotNull("returned null?", mediaItem);

    Rendition rendition = mediaMetadata.getRendition();
    assertEquals("ref-path", "/content/dam/test/movie.wmf/jcr:content/renditions/cq5dam.video.firefoxhq.ogg", rendition.getPath());
  }

  @Test
  public void testGetMediaItemInfoVideoAsImage() {
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(MEDIAITEM_VIDEO, MediaArgs.mediaFormat("standard_2col"));
    assertFalse("valid", mediaMetadata.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MATCHING_RENDITION, mediaMetadata.getMediaInvalidReason());

    MediaItem mediaItem = mediaMetadata.getMediaItem();
    assertNotNull("returned null?", mediaItem);
  }

  @Test
  public void testDownloadMediaElement() {
    MediaArgsType mediaArgs = new MediaArgs().setForceDownload(true);
    MediaMetadata mediaMetadata = mediaHandler().getMediaMetadata(MEDIAITEM_PATH_STANDARD, mediaArgs);
    assertTrue("valid?", mediaMetadata.isValid());
    assertNotNull("mediaItem?", mediaMetadata.getMediaItem());
    assertNotNull("rendition?", mediaMetadata.getRendition());
    assertEquals("rendition.mediaUrl",
        "/content/dam/test/standard.jpg/_jcr_content/renditions/original.media_file.download_attachment.file/standard.jpg",
        mediaMetadata.getRendition().getMediaUrl());
  }

}
