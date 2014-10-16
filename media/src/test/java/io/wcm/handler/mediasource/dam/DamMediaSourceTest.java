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

import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_2COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_3COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.HOME_TEASER_SCALE1;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.VIDEO_2COL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import io.wcm.handler.commons.dom.Div;
import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.format.MediaFormatBuilder;
import io.wcm.handler.media.spi.MediaMarkupBuilder;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.url.integrator.IntegratorHandler;

import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Test;

import com.day.cq.dam.api.DamConstants;
import com.day.cq.wcm.api.WCMMode;
import com.google.common.collect.ImmutableList;

/**
 * Test {@link DamMediaSource}
 */
public class DamMediaSourceTest extends AbstractDamTest {

  @Test
  public void testGetAssetInfoStringExisting() {
    // get AssetInfo for an existing item - should return the info object
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    assertTrue("valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    Asset info = media.getAsset();
    assertNotNull("returned info?", info);
    assertEquals("path equals?", MEDIAITEM_PATH_STANDARD, info.getPath());
  }

  @Test
  public void testGetAssetInfoStringNonExistant() {
    // get AssetInfo for an item that does not exist - should return null
    Media media = mediaHandler().get(MEDIAITEM_PATH_NONEXISTANT).build();
    assertFalse("valid", media.isValid());
    assertEquals("invalid reason", MediaInvalidReason.MEDIA_REFERENCE_INVALID, media.getMediaInvalidReason());
    Asset info = media.getAsset();
    assertNull("returned null?", info);
  }

  @Test
  public void testGetAssetInfoStringEmpty() {
    // get AssetInfo for empty string path - should not crash but return null
    Media media = mediaHandler().get("").build();
    assertFalse("valid", media.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MEDIA_SOURCE, media.getMediaInvalidReason());
    Asset info = media.getAsset();
    assertNull("returned null?", info);
  }

  @Test
  public void testGetAssetInfoStringNull() {
    // get AssetInfo for null path - should not crash but return null
    Media media = mediaHandler().get((String)null).build();
    assertFalse("valid", media.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MEDIA_SOURCE, media.getMediaInvalidReason());
    Asset info = media.getAsset();
    assertNull("returned null?", info);
  }

  @Test
  public void testGetAssetInfoResource() {
    // get the info for the paragraph that contains a mediaRef to the 'standard' media item
    Media media = mediaHandler().get(parStandardMediaRef).build();
    assertTrue("valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    Asset info = media.getAsset();
    assertNotNull("returned info?", info);
    assertEquals("mediaRef correctly resolved?", MEDIAITEM_PATH_STANDARD, info.getPath());
    assertEquals("alt text from medialib?", "Editorial Standard 1", info.getAltText());
  }

  @Test
  public void testGetAssetInfoResourceCrop() {
    // get the info for the paragraph that contains a mediaRef to the 'standard' media item
    Media media = mediaHandler().get(parStandardMediaRefCrop).build();
    assertTrue("valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    Asset info = media.getAsset();
    assertNotNull("returned info?", info);
    assertEquals("mediaRef correctly resolved?", MEDIAITEM_PATH_STANDARD, info.getPath());
  }

  @Test
  public void testGetAssetInfoResourceAltText() {
    // get the info for the paragraph that contains a mediaRef to the 'standard' media item & editorial alt.Text
    Media media = mediaHandler().get(parStandardMediaRefAltText).build();
    assertTrue("valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    Asset info = media.getAsset();
    assertNotNull("returned info?", info);
    assertEquals("mediaRef correctly resolved?", MEDIAITEM_PATH_STANDARD, info.getPath());
    assertEquals("alt text from paragraph?", "Alt. Text from Paragraph", info.getAltText());
  }

  @Test
  public void testGetAssetInfoResourceInvalid() {
    // get the info for the paragraph that contains a invalid mediaRef - should return null
    Media media = mediaHandler().get(parInvalidMediaRef).build();
    assertFalse("valid", media.isValid());
    assertEquals("invalid reason", MediaInvalidReason.MEDIA_REFERENCE_INVALID, media.getMediaInvalidReason());
    Asset info = media.getAsset();
    assertNull("returned null?", info);
  }

  @Test
  public void testGetAssetInfoResourceEmpty() {
    // get the info for the paragraph that contains an empty mediaRef - should return null
    Media media = mediaHandler().get(parEmptyMediaRef).build();
    assertFalse("valid", media.isValid());
    assertEquals("invalid reason", MediaInvalidReason.MEDIA_REFERENCE_MISSING, media.getMediaInvalidReason());
    Asset info = media.getAsset();
    assertNull("returned null?", info);
  }

  @Test
  public void testGetAssetInfoResourceNull() {
    // get the info for the paragraph that contains a null mediaRef - should return null
    Media media = mediaHandler().get(parNullMediaRef).build();
    assertFalse("valid", media.isValid());
    assertEquals("invalid reason", MediaInvalidReason.MEDIA_REFERENCE_MISSING, media.getMediaInvalidReason());
    Asset info = media.getAsset();
    assertNull("returned null?", info);
  }

  @Test
  public void testGetAssetInfoResourceString() {
    // get the info for the paragraph that contains a mediaRef2 to the 'standard' media item
    Media media = mediaHandler().get(parStandardMediaRef2).refProperty("mediaRef2").build();
    assertTrue("valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());
    Asset info = media.getAsset();
    assertNotNull("returned info?", info);
    assertEquals("mediaRef correctly resolved?", MEDIAITEM_PATH_STANDARD, info.getPath());
    assertEquals("alt text from medialib?", "Editorial Standard 1", info.getAltText());
  }

  // TEST METHODS GENERATING HTML ELEMENTS *****************************************************************************

  @Test
  public void testGetMediaElementInvalid() {
    // create img element for a paragraph with invalid media ref - should not crash but return null
    HtmlElement img = mediaHandler().get(parInvalidMediaRef).buildElement();
    assertNull("returned null?", img);
  }

  @Test
  public void testGetMediaElementEmpty() {
    // create img element for a paragraph with empty media ref - should not crash but return null
    HtmlElement img = mediaHandler().get(parEmptyMediaRef).buildElement();
    assertNull("returned null?", img);
  }

  @Test
  public void testGetMediaElementNull() {
    // create img element for a paragraph with null media ref - should not crash but return null
    HtmlElement img = mediaHandler().get(parNullMediaRef).buildElement();
    assertNull("returned null?", img);
  }

  @Test
  public void testGetMediaElementNullResource() {
    // pass-in null for the paragraph resource - should not crash but return null
    HtmlElement img = mediaHandler().get((Resource)null).buildElement();
    assertNull("returned null?", img);
  }

  @Test
  public void testGetMediaElementImageAltTextFromMediaLib() {
    // create img element for the first rendition of the 'standard' media-item
    MediaArgs args = new MediaArgs();
    HtmlElement img = mediaHandler().get(parStandardMediaRef, args).buildElement();
    assertNotNull("returned html element?", img);
    assertEquals("is img?", "img", img.getName());
    assertEquals("src set?", mediaHandler().get(MEDIAITEM_PATH_STANDARD, args).buildUrl(), img.getAttributeValue("src"));
    assertEquals("width set?", 215, img.getAttributeValueAsInteger("width"));
    assertEquals("height set?", 102, img.getAttributeValueAsInteger("height"));
    assertEquals("alt text from medialib?", "Editorial Standard 1", img.getAttributeValue("alt"));
  }

  @Test
  public void testGetMediaElementImageAltTextFromParagraph() {
    // create img element for the paragraph that has a editorial alt-text defined in the paragraph
    HtmlElement img = mediaHandler().get(parStandardMediaRefAltText).buildElement();
    assertNotNull("returned html element?", img);
    assertEquals("alt text from paragraph?", "Alt. Text from Paragraph", img.getAttributeValue("alt"));
  }

  @Test
  public void testGetMediaElementImageAltTextOverride() {
    // define alt-text-override via MediaArgs and check if it is appears in the img-tag
    MediaArgs args = new MediaArgs();
    args.altText("Alt. Text Override!");
    HtmlElement img = mediaHandler().get(parStandardMediaRef, args).buildElement();
    assertNotNull("returned html element?", img);
    assertEquals("alt text from override?", "Alt. Text Override!", img.getAttributeValue("alt"));
  }

  @Test
  public void testGetMediaElementImageNoAltTextNoDimensions() {
    // create img-tag for the medialib-item that has no alt-text, and its rendition lacks dimension information
    HtmlElement img = mediaHandler().get(parImgNoAltNoDimension).buildElement();
    assertNotNull("returned html element?", img);
    assertEquals("src set?", mediaHandler().get(MEDIAITEM_PATH_IMAGE_NOALT_NODIMENSIONS).buildUrl(), img.getAttributeValue("src"));

    assertEquals("alt text", "Image with no altText and a rendition w/o fileSize & dimensions", img.getAttributeValue("alt"));

    assertEquals("width from mediaformat?", 0, img.getAttributeValueAsInteger("width"));
    assertEquals("height from mediaformat?", 0, img.getAttributeValueAsInteger("height"));
  }

  @Test
  public void testGetMediaElementImageSpecificMediaFormat() {
    // create img element for rendition with standard_2col media format
    MediaArgs args = new MediaArgs(EDITORIAL_2COL);
    Media media = mediaHandler().get(parStandardMediaRef, args).build();
    HtmlElement img = media.getElement();
    assertNotNull("returned html element?", img);
    assertEquals("is img?", "img", img.getName());
    // check that this is the requested mediaformat via width/height-attributes of the img-tag
    assertEquals("width set?", 450, img.getAttributeValueAsInteger("width"));
    assertEquals("height set?", 213, img.getAttributeValueAsInteger("height"));
    assertEquals(EDITORIAL_2COL, media.getRendition().getMediaFormat());
  }

  @Test
  public void testGetMediaElementImageSpecificMediaFormat_Resize() {
    // create img element for rendition with standard_2col media format
    MediaArgs args = new MediaArgs(SHOWROOM_CONTROLS_SCALE1);
    Media media = mediaHandler().get(parStandardMediaRef, args).build();
    HtmlElement img = media.getElement();
    assertNotNull("returned html element?", img);
    assertEquals("is img?", "img", img.getName());
    // check that this is the requested mediaformat via width/height-attributes of the img-tag
    assertEquals("width set?", 64, img.getAttributeValueAsInteger("width"));
    assertEquals("height set?", 30, img.getAttributeValueAsInteger("height"));
    assertEquals(SHOWROOM_CONTROLS_SCALE1, media.getRendition().getMediaFormat());
  }

  @Test
  public void testGetMediaElementImageSpecificMediaFormatCrop() {
    // create img element for rendition with standard_2col media format
    MediaArgs args = new MediaArgs(HOME_TEASER_SCALE1);
    Media media = mediaHandler().get(parStandardMediaRefCrop, args).build();
    HtmlElement img = media.getElement();
    assertNotNull("returned html element?", img);
    assertEquals("is img?", "img", img.getName());
    // check that this is the requested mediaformat via width/height-attributes of the img-tag
    assertEquals("width set?", 158, img.getAttributeValueAsInteger("width"));
    assertEquals("height set?", 80, img.getAttributeValueAsInteger("height"));
    assertEquals(HOME_TEASER_SCALE1, media.getRendition().getMediaFormat());
  }

  @Test
  public void testGetMediaElementImageSpecificMediaFormatCropInvalid() {
    // create img element for rendition with standard_2col media format
    MediaArgs args = new MediaArgs(EDITORIAL_2COL);
    HtmlElement img = mediaHandler().get(parStandardMediaRefCrop, args).buildElement();
    assertNull("returned html element?", img);
  }

  @Test
  public void testGetMediaElementImageInvalidMediaFormat() {
    // create img element in a mediaFormat for which there is no rendition is available - returns any rendition
    MediaArgs args = new MediaArgs(MediaFormatBuilder.create("someotherformat", AppAemContext.APPLICATION_ID).build());
    HtmlElement img = mediaHandler().get(parStandardMediaRef, args).buildElement();
    assertNotNull("returned null?", img);
  }

  @Test
  public void testGetMediaElementFlashWithoutFallback() {
    // create media-element from a flash mediaRef with no fallback image - should return a div
    Media media = mediaHandler().get(parFlashWithoutFallbackMediaRef).build();
    assertTrue("valid?", media.isValid());
    assertNotNull("asset?", media.getAsset());
    assertNotNull("rendition?", media.getRendition());
    assertEquals("rendition.mediaUrl", "/content/dam/test/flashWithoutFallback.swf/_jcr_content/renditions/original./flashWithoutFallback.swf",
        media.getRendition().getUrl());
  }

  @Test
  public void testGetMediaElementEditModeDummyImage() {
    if (!(adaptable() instanceof SlingHttpServletRequest)) {
      return;
    }

    // simulate edit-mode
    WCMMode.EDIT.toRequest(context.request());

    // dummy image is added only if a specific media format is requested
    MediaArgs args = new MediaArgs(EDITORIAL_2COL);
    HtmlElement img = mediaHandler().get(parInvalidMediaRef, args).buildElement();

    assertNotNull("returned element?", img);
    assertEquals("is img?", "img", img.getName());
    assertEquals("src set?", MediaMarkupBuilder.DUMMY_IMAGE, img.getAttributeValue("src"));
    assertEquals("width set?", 450, img.getAttributeValueAsInteger("width"));
    assertEquals("height set?", 213, img.getAttributeValueAsInteger("height"));
    assertTrue("has dummy css class?", img.getCssClass().contains(MediaNameConstants.CSS_DUMMYIMAGE));
  }

  @Test
  public void testGetMediaElementEditModeDummyImageThumbnail() {
    if (!(adaptable() instanceof SlingHttpServletRequest)) {
      return;
    }

    // simulate edit-mode
    WCMMode.EDIT.toRequest(context.request());

    // if fixed dimensions are specified, the image must have exactly the specified size
    MediaArgs args = new MediaArgs(EDITORIAL_2COL);
    args.fixedDimension(100, 100);
    HtmlElement img = mediaHandler().get(parNullMediaRef, args).buildElement();

    assertNotNull("returned element?", img);
    assertEquals("width set?", 100, img.getAttributeValueAsInteger("width"));
    assertEquals("height set?", 100, img.getAttributeValueAsInteger("height"));
  }

  // TESTS FOR FUNCTIONS THAT DELEGATE TO MediaHandler (WHERE THEY ARE TESTED IN MORE DETAIL)

  @Test
  public void testGetMediaUrlStandard() {
    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().get(MEDIAITEM_PATH_STANDARD).buildUrl();
    assertNotNull("returned url?", url);
    assertEquals("url as expected?", "/content/dam/test/standard.jpg/_jcr_content/renditions/original./standard.jpg", url);
  }

  @Test
  public void testGetMediaUrlStandard_Resize() {
    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().get(MEDIAITEM_PATH_STANDARD, SHOWROOM_CONTROLS_SCALE1).buildUrl();
    assertNotNull("returned url?", url);
    assertEquals("url as expected?", "/content/dam/test/standard.jpg/_jcr_content/renditions/original.image_file.64.30.file/standard.jpg", url);
  }

  @Test
  public void testGetMediaUrlStandard_Resize_Download() {
    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().get(MEDIAITEM_PATH_STANDARD, new MediaArgs(SHOWROOM_CONTROLS_SCALE1).forceDownload(true)).buildUrl();
    assertNotNull("returned url?", url);
    assertEquals("url as expected?", "/content/dam/test/standard.jpg/_jcr_content/renditions/original.image_file.64.30.download_attachment.file/standard.jpg",
        url);
  }

  @Test
  public void testGetMediaUrlStandard_FixedDimension_ExactMatch() {
    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().get(MEDIAITEM_PATH_STANDARD, new MediaArgs().fixedDimension(450, 213)).buildUrl();
    assertNotNull("returned url?", url);
    assertEquals("url as expected?",
        "/content/dam/test/standard.jpg/_jcr_content/renditions/cq5dam.web.450.213.jpg./cq5dam.web.450.213.jpg", url);
  }

  @Test
  public void testGetMediaUrlStandard_FixedDimension_Resize() {
    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().get(MEDIAITEM_PATH_STANDARD, new MediaArgs().fixedDimension(64, 30)).buildUrl();
    assertNotNull("returned url?", url);
    assertEquals("url as expected?", "/content/dam/test/standard.jpg/_jcr_content/renditions/original.image_file.64.30.file/standard.jpg", url);
  }

  @Test
  public void testGetMediaUrlNull() {
    // getMediaUrl should handle null mediaRefs and return null
    String url = mediaHandler().get((String)null).buildUrl();
    assertNull("returned null?", url);
  }

  @Test
  public void testGetMediaUrlEmpty() {
    // getMediaUrl should handle empty mediaRefs and return null
    String url = mediaHandler().get("").buildUrl();
    assertNull("returned null?", url);
  }

  @Test
  public void testGetMediaUrlIntegrator() {
    if (!(adaptable() instanceof SlingHttpServletRequest)) {
      return;
    }

    // activate integrator mode
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);

    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().get(MEDIAITEM_PATH_STANDARD).buildUrl();
    assertNotNull("returned url?", url);
    assertEquals("url as expected?", "http://www.dummysite.org/content/dam/test/standard.jpg/_jcr_content/renditions/original./standard.jpg", url);

  }

  @Test
  public void testGetMediaProperties() {
    // get the properties of the first rendition of the 'standard' media item
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    ValueMap props = media.getAsset().getProperties();
    assertNotNull("returned props?", props);
    assertEquals("are there media item props?", "Editorial Standard 1",
        props.get(DamConstants.DC_TITLE, String[].class)[0]);
  }

  @Test
  public void testGetRenditionProperties() {
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    ValueMap props = media.getRendition().getProperties();
    assertNotNull("returned props?", props);
    assertEquals("are there rendition props?", 1, props.size());
  }

  @Test
  public void testGetAssetInfoVideo() {
    Media media = mediaHandler().get(MEDIAITEM_VIDEO, new MediaArgs(VIDEO_2COL)).build();
    assertTrue("valid", media.isValid());
    assertNull("no invalid reason", media.getMediaInvalidReason());

    Asset asset = media.getAsset();
    assertNotNull("returned null?", asset);

    Rendition rendition = media.getRendition();
    assertEquals("ref-path", "/content/dam/test/movie.wmf/jcr:content/renditions/cq5dam.video.firefoxhq.ogg", rendition.getPath());
  }

  @Test
  public void testGetAssetInfoVideoAsImage() {
    Media media = mediaHandler().get(MEDIAITEM_VIDEO, new MediaArgs(EDITORIAL_2COL)).build();
    assertFalse("valid", media.isValid());
    assertEquals("invalid reason", MediaInvalidReason.NO_MATCHING_RENDITION, media.getMediaInvalidReason());

    Asset asset = media.getAsset();
    assertNotNull("returned null?", asset);
  }

  @Test
  public void testDownloadMediaElement() {
    MediaArgs mediaArgs = new MediaArgs().forceDownload(true);
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD, mediaArgs).build();
    assertTrue("valid?", media.isValid());
    assertNotNull("asset?", media.getAsset());
    assertNotNull("rendition?", media.getRendition());
    assertEquals("rendition.mediaUrl",
        "/content/dam/test/standard.jpg/_jcr_content/renditions/original.media_file.download_attachment.file/standard.jpg",
        media.getRendition().getUrl());
  }

  @Test
  public void testGetMediaMarkup() {
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    assertEquals(
        "<img src=\"/content/dam/test/standard.jpg/_jcr_content/renditions/original./standard.jpg\" alt=\"Editorial Standard 1\" height=\"102\" width=\"215\" />",
        media.getMarkup());
  }

  @Test
  public void testIsValidElement() {
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    assertTrue(mediaHandler().isValidElement(media.getElement()));
    assertFalse(mediaHandler().isValidElement(new Div()));
    assertFalse(mediaHandler().isValidElement(null));
  }

  @Test
  public void testMultipleMediaMediaFormats() {
    MediaArgs mediaArgs = new MediaArgs(EDITORIAL_1COL, EDITORIAL_2COL, EDITORIAL_3COL);
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD, mediaArgs).build();
    assertTrue("valid?", media.isValid());
    assertNotNull("asset?", media.getAsset());
    assertEquals("renditions", 1, media.getRenditions().size());
    assertEquals("rendition.mediaUrl",
        "/content/dam/test/standard.jpg/_jcr_content/renditions/original./standard.jpg",
        media.getUrl());
    assertEquals(EDITORIAL_1COL, media.getRendition().getMediaFormat());
  }

  @Test
  public void testMultipleMandatoryMediaFormats() {
    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(EDITORIAL_1COL, EDITORIAL_2COL, EDITORIAL_3COL);
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD, mediaArgs).build();
    assertTrue("valid?", media.isValid());
    assertNotNull("asset?", media.getAsset());
    assertEquals("renditions", 3, media.getRenditions().size());
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals("rendition.mediaUrl.1",
        "/content/dam/test/standard.jpg/_jcr_content/renditions/original./standard.jpg",
        renditions.get(0).getUrl());
    assertEquals(EDITORIAL_1COL, renditions.get(0).getMediaFormat());

    assertEquals("rendition.mediaUrl.2",
        "/content/dam/test/standard.jpg/_jcr_content/renditions/cq5dam.web.450.213.jpg./cq5dam.web.450.213.jpg",
        renditions.get(1).getUrl());
    assertEquals(EDITORIAL_2COL, renditions.get(1).getMediaFormat());

    assertEquals("rendition.mediaUrl.3",
        "/content/dam/test/standard.jpg/_jcr_content/renditions/cq5dam.web.685.325.jpg./cq5dam.web.685.325.jpg",
        renditions.get(2).getUrl());
    assertEquals(EDITORIAL_3COL, renditions.get(2).getMediaFormat());
  }

  @Test
  public void testMultipleMandatoryMediaFormatsNotAllMatch() {
    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(VIDEO_2COL, EDITORIAL_2COL, EDITORIAL_3COL);
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD, mediaArgs).build();
    assertFalse("valid?", media.isValid());
    assertEquals(MediaInvalidReason.NOT_ENOUGH_MATCHING_RENDITIONS, media.getMediaInvalidReason());
    assertNotNull("asset?", media.getAsset());
    assertEquals("renditions", 2, media.getRenditions().size());
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals("rendition.mediaUrl.1",
        "/content/dam/test/standard.jpg/_jcr_content/renditions/cq5dam.web.450.213.jpg./cq5dam.web.450.213.jpg",
        renditions.get(0).getUrl());
    assertEquals(EDITORIAL_2COL, renditions.get(0).getMediaFormat());

    assertEquals("rendition.mediaUrl.2",
        "/content/dam/test/standard.jpg/_jcr_content/renditions/cq5dam.web.685.325.jpg./cq5dam.web.685.325.jpg",
        renditions.get(1).getUrl());
    assertEquals(EDITORIAL_3COL, renditions.get(1).getMediaFormat());
  }

}
