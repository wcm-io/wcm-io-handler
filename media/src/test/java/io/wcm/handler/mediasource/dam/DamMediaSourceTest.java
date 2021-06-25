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
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_STAGE_SMALL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.FIXEDHEIGHT_UNCONSTRAINED;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.IMAGE_UNCONSTRAINED;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.MATERIAL_TILE;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.NORATIO_LARGE_MINWIDTH;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.PRODUCT_CUTOUT_LARGE;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO2;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_CONTROLS_SCALE1;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.VIDEO_2COL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Constants;

import com.day.cq.dam.api.DamConstants;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.DropTarget;
import com.day.cq.wcm.api.components.EditConfig;
import com.day.cq.wcm.api.components.EditContext;
import com.day.cq.wcm.api.components.InplaceEditingConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import io.wcm.handler.commons.dom.Div;
import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaArgs.MediaFormatOption;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatBuilder;
import io.wcm.handler.media.imagemap.impl.ImageMapParserImplTest;
import io.wcm.handler.media.impl.ipeconfig.IPEConfigResourceProvider;
import io.wcm.handler.media.markup.DragDropSupport;
import io.wcm.handler.media.spi.ImageMapLinkResolver;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.spi.MediaMarkupBuilder;
import io.wcm.handler.media.testcontext.DummyImageMapLinkResolver;
import io.wcm.handler.media.testcontext.DummyMediaHandlerConfig;
import io.wcm.handler.url.integrator.IntegratorHandler;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Test {@link DamMediaSource}
 */
@SuppressWarnings("null")
class DamMediaSourceTest extends AbstractDamTest {

  @Test
  void testGetAssetInfoStringExisting() {
    // get AssetInfo for an existing item - should return the info object
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    assertTrue(media.isValid(), "valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    Asset info = media.getAsset();
    assertNotNull(info, "returned info?");
    assertEquals(MEDIAITEM_PATH_STANDARD, info.getPath(), "path equals?");
  }

  @Test
  void testGetAssetInfoStringNonExistant() {
    // get AssetInfo for an item that does not exist - should return null
    Media media = mediaHandler().get(MEDIAITEM_PATH_NONEXISTANT).build();
    assertFalse(media.isValid(), "valid");
    assertEquals(MediaInvalidReason.MEDIA_REFERENCE_INVALID, media.getMediaInvalidReason(), "invalid reason");
    Asset info = media.getAsset();
    assertNull(info, "returned null?");
  }

  @Test
  void testGetAssetInfoStringEmpty() {
    // get AssetInfo for empty string path - should not crash but return null
    Media media = mediaHandler().get("").build();
    assertFalse(media.isValid(), "valid");
    assertEquals(MediaInvalidReason.MEDIA_REFERENCE_MISSING, media.getMediaInvalidReason(), "invalid reason");
    Asset info = media.getAsset();
    assertNull(info, "returned null?");
  }

  @Test
  void testGetAssetInfoStringNull() {
    // get AssetInfo for null path - should not crash but return null
    Media media = mediaHandler().get((String)null).build();
    assertFalse(media.isValid(), "valid");
    assertEquals(MediaInvalidReason.MEDIA_REFERENCE_MISSING, media.getMediaInvalidReason(), "invalid reason");
    Asset info = media.getAsset();
    assertNull(info, "returned null?");
  }

  @Test
  void testGetAssetInfoResource() {
    // get the info for the paragraph that contains a mediaRef to the 'standard' media item
    Media media = mediaHandler().get(parStandardMediaRef).build();
    assertTrue(media.isValid(), "valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    Asset info = media.getAsset();
    assertNotNull(info, "returned info?");
    assertEquals(MEDIAITEM_PATH_STANDARD, info.getPath(), "mediaRef correctly resolved?");
    assertEquals("Editorial Standard 1", info.getAltText(), "alt text from medialib?");
  }

  @Test
  void testGetAssetInfoResourceCrop() {
    // get the info for the paragraph that contains a mediaRef to the 'standard' media item
    Media media = mediaHandler().get(parStandardMediaRefCrop).build();
    assertTrue(media.isValid(), "valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    Asset info = media.getAsset();
    assertNotNull(info, "returned info?");
    assertEquals(MEDIAITEM_PATH_STANDARD, info.getPath(), "mediaRef correctly resolved?");
  }

  @Test
  void testGetAssetInfoResourceAltText() {
    // get the info for the paragraph that contains a mediaRef to the 'standard' media item & editorial alt.Text
    Media media = mediaHandler().get(parStandardMediaRefAltText).build();
    assertTrue(media.isValid(), "valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    Asset info = media.getAsset();
    assertNotNull(info, "returned info?");
    assertEquals(MEDIAITEM_PATH_STANDARD, info.getPath(), "mediaRef correctly resolved?");
    assertEquals("Alt. Text from Paragraph", info.getAltText(), "alt text from paragraph?");
  }

  @Test
  void testGetAssetInfoResourceInvalid() {
    // get the info for the paragraph that contains a invalid mediaRef - should return null
    Media media = mediaHandler().get(parInvalidMediaRef).build();
    assertFalse(media.isValid(), "valid");
    assertEquals(MediaInvalidReason.MEDIA_REFERENCE_INVALID, media.getMediaInvalidReason(), "invalid reason");
    Asset info = media.getAsset();
    assertNull(info, "returned null?");
  }

  @Test
  void testGetAssetInfoResourceEmpty() {
    // get the info for the paragraph that contains an empty mediaRef - should return null
    Media media = mediaHandler().get(parEmptyMediaRef).build();
    assertFalse(media.isValid(), "valid");
    assertEquals(MediaInvalidReason.MEDIA_REFERENCE_MISSING, media.getMediaInvalidReason(), "invalid reason");
    Asset info = media.getAsset();
    assertNull(info, "returned null?");
  }

  @Test
  void testGetAssetInfoResourceNull() {
    // get the info for the paragraph that contains a null mediaRef - should return null
    Media media = mediaHandler().get(parNullMediaRef).build();
    assertFalse(media.isValid(), "valid");
    assertEquals(MediaInvalidReason.MEDIA_REFERENCE_MISSING, media.getMediaInvalidReason(), "invalid reason");
    Asset info = media.getAsset();
    assertNull(info, "returned null?");
  }

  @Test
  void testGetAssetInfoResourceString() {
    // get the info for the paragraph that contains a mediaRef2 to the 'standard' media item
    Media media = mediaHandler().get(parStandardMediaRef2).refProperty("mediaRef2").build();
    assertTrue(media.isValid(), "valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");
    Asset info = media.getAsset();
    assertNotNull(info, "returned info?");
    assertEquals(MEDIAITEM_PATH_STANDARD, info.getPath(), "mediaRef correctly resolved?");
    assertEquals("Editorial Standard 1", info.getAltText(), "alt text from medialib?");
  }

  // TEST METHODS GENERATING HTML ELEMENTS *****************************************************************************

  @Test
  void testGetMediaElementInvalid() {
    // create img element for a paragraph with invalid media ref - should not crash but return null
    HtmlElement img = mediaHandler().get(parInvalidMediaRef).buildElement();
    assertNull(img, "returned null?");
  }

  @Test
  void testGetMediaElementEmpty() {
    // create img element for a paragraph with empty media ref - should not crash but return null
    HtmlElement img = mediaHandler().get(parEmptyMediaRef).buildElement();
    assertNull(img, "returned null?");
  }

  @Test
  void testGetMediaElementNull() {
    // create img element for a paragraph with null media ref - should not crash but return null
    HtmlElement img = mediaHandler().get(parNullMediaRef).buildElement();
    assertNull(img, "returned null?");
  }

  @Test
  void testGetMediaElementNullResource() {
    // pass-in null for the paragraph resource - should not crash but return null
    HtmlElement img = mediaHandler().get((Resource)null).buildElement();
    assertNull(img, "returned null?");
  }

  @Test
  void testGetMediaElementImageAltTextFromAssetWithTitleOnly() {
    // create img element for the first rendition of the 'standard' media-item
    HtmlElement img = mediaHandler().get(parStandardMediaRef).buildElement();
    assertNotNull(img, "returned html element?");
    assertEquals("img", img.getName(), "is img?");
    assertEquals(mediaHandler().get(MEDIAITEM_PATH_STANDARD).buildUrl(), img.getAttributeValue("src"), "src set?");
    assertEquals(215, img.getAttributeValueAsInteger("width"), "width set?");
    assertEquals(102, img.getAttributeValueAsInteger("height"), "height set?");
    assertEquals("Editorial Standard 1", img.getAttributeValue("alt"), "alt text from medialib?");
  }

  @Test
  void testGetMediaElementImageAltTextFromAssetWithDescription() {
    HtmlElement img = mediaHandler().get(parSixteenTenMediaRefCrop).buildElement();
    assertNotNull(img, "returned html element?");
    assertEquals("img", img.getName(), "is img?");
    assertEquals("Description for 16:10 Image", img.getAttributeValue("alt"), "alt text from medialib?");
  }

  @Test
  void testGetMediaElementImageAltTextFromAssetDecorative() {
    HtmlElement img = mediaHandler().get(parSixteenTenMediaRefCrop)
        .decorative(true)
        .buildElement();
    assertNotNull(img, "returned html element?");
    assertEquals("img", img.getName(), "is img?");
    assertEquals("", img.getAttributeValue("alt"), "alt text from medialib?");
  }

  @Test
  void testGetMediaElementImageAltTextFromParagraph() {
    // create img element for the paragraph that has a editorial alt-text defined in the paragraph
    HtmlElement img = mediaHandler().get(parStandardMediaRefAltText).buildElement();
    assertNotNull(img, "returned html element?");
    assertEquals("Alt. Text from Paragraph", img.getAttributeValue("alt"), "alt text from paragraph?");
  }

  @Test
  void testGetMediaElementImageAltTextOverride() {
    // define alt-text-override via MediaArgs and check if it is appears in the img-tag
    MediaArgs args = new MediaArgs();
    args.altText("Alt. Text Override!");
    HtmlElement img = mediaHandler().get(parStandardMediaRef, args).buildElement();
    assertNotNull(img, "returned html element?");
    assertEquals("Alt. Text Override!", img.getAttributeValue("alt"), "alt text from override?");
  }

  @Test
  void testGetMediaElementImageAltTextForceFromAsset() {
    // define alt-text-override via MediaArgs and check if it is appears in the img-tag
    HtmlElement img = mediaHandler().get(parStandardMediaRef)
        .altText("Alt. Text Override!")
        .forceAltValueFromAsset(true)
        .buildElement();
    assertNotNull(img, "returned html element?");
    assertEquals("Editorial Standard 1", img.getAttributeValue("alt"), "alt text from asset?");
  }

  @Test
  void testGetMediaElementImageNoAltTextNoDimensions() {
    // create img-tag for the medialib-item that has no alt-text, and its rendition lacks dimension information
    HtmlElement img = mediaHandler().get(parImgNoAltNoDimension).buildElement();
    assertNotNull(img, "returned html element?");
    assertEquals(mediaHandler().get(MEDIAITEM_PATH_IMAGE_NOALT_NODIMENSIONS).buildUrl(), img.getAttributeValue("src"), "src set?");

    assertEquals("Image with no altText and a rendition w/o fileSize & dimensions", img.getAttributeValue("alt"), "alt text");

    assertEquals(0, img.getAttributeValueAsInteger("width"), "width from mediaformat?");
    assertEquals(0, img.getAttributeValueAsInteger("height"), "height from mediaformat?");
  }

  @Test
  void testGetMediaElementImageSpecificMediaFormat() {
    // create img element for rendition with standard_2col media format
    MediaArgs args = new MediaArgs(EDITORIAL_2COL);
    Media media = mediaHandler().get(parStandardMediaRef, args).build();
    HtmlElement img = media.getElement();
    assertNotNull(img, "returned html element?");
    assertEquals("img", img.getName(), "is img?");
    // check that this is the requested mediaformat via width/height-attributes of the img-tag
    assertEquals(450, img.getAttributeValueAsInteger("width"), "width set?");
    assertEquals(213, img.getAttributeValueAsInteger("height"), "height set?");
    assertEquals(EDITORIAL_2COL, media.getRendition().getMediaFormat());
    assertEquals(ContentType.JPEG, media.getRendition().getMimeType());
  }

  @Test
  void testGetMediaElementImageSpecificMediaFormat_Resize() {
    // create img element for rendition with standard_2col media format
    MediaArgs args = new MediaArgs(SHOWROOM_CONTROLS_SCALE1);
    Media media = mediaHandler().get(parStandardMediaRef, args).build();
    HtmlElement img = media.getElement();
    assertNotNull(img, "returned html element?");
    assertEquals("img", img.getName(), "is img?");
    // check that this is the requested mediaformat via width/height-attributes of the img-tag
    assertEquals(64, img.getAttributeValueAsInteger("width"), "width set?");
    assertEquals(30, img.getAttributeValueAsInteger("height"), "height set?");
    assertEquals(SHOWROOM_CONTROLS_SCALE1, media.getRendition().getMediaFormat());
  }

  @Test
  void testGetMediaElementImageSpecificMediaFormatCrop() {
    // create img element for rendition with standard_2col media format
    MediaArgs args = new MediaArgs(SHOWROOM_CONTROLS_SCALE1);
    Media media = mediaHandler().get(parStandardMediaRefCrop, args).build();
    HtmlElement img = media.getElement();
    assertNotNull(img, "returned html element?");
    assertEquals("img", img.getName(), "is img?");
    // check that this is the requested mediaformat via width/height-attributes of the img-tag
    assertEquals(64, img.getAttributeValueAsInteger("width"), "width set?");
    assertEquals(30, img.getAttributeValueAsInteger("height"), "height set?");
    assertEquals(SHOWROOM_CONTROLS_SCALE1, media.getRendition().getMediaFormat());
  }

  @Test
  void testGetMediaElementImageSpecificMediaFormatCropInvalid() {
    // create img element for rendition with standard_2col media format
    // fallback to match without cropping because cropping params do not match
    MediaArgs args = new MediaArgs(EDITORIAL_2COL);
    Media media = mediaHandler().get(parStandardMediaRefCrop, args).build();
    HtmlElement img = media.getElement();
    assertNotNull(img, "returned html element?");
    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/cq5dam.web.450.213.jpg./cq5dam.web.450.213.jpg", media.getUrl());
  }

  @Test
  void testGetMediaElementImageSpecificMediaFormatCropInvalidWithoutFallback() {
    // create img element for rendition with standard_2col media format
    MediaArgs args = new MediaArgs(PRODUCT_CUTOUT_LARGE);
    HtmlElement img = mediaHandler().get(parStandardMediaRefCrop, args).buildElement();
    assertNull(img, "returned html element?");
  }

  @Test
  void testGetMediaElementImageInvalidMediaFormat() {
    // create img element in a mediaFormat for which there is no rendition is available - returns any rendition
    MediaArgs args = new MediaArgs(MediaFormatBuilder.create("someotherformat").build());
    HtmlElement img = mediaHandler().get(parStandardMediaRef, args).buildElement();
    assertNotNull(img, "returned null?");
  }

  @Test
  void testGetMediaElementFlashWithoutFallback() {
    // create media-element from a flash mediaRef with no fallback image - should return a div
    Media media = mediaHandler().get(parFlashWithoutFallbackMediaRef).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertNotNull(media.getRendition(), "rendition?");
    assertEquals("/content/dam/test/flashWithoutFallback.swf/_jcr_content/renditions/original./flashWithoutFallback.swf",
        media.getRendition().getUrl(), "rendition.mediaUrl");
  }

  @Test
  void testGetMediaElementEditModeDummyImage() {
    if (!(adaptable() instanceof SlingHttpServletRequest)) {
      return;
    }

    // simulate edit-mode
    WCMMode.EDIT.toRequest(context.request());

    // dummy image is added only if a specific media format is requested
    MediaArgs args = new MediaArgs(EDITORIAL_2COL);
    HtmlElement img = mediaHandler().get(parInvalidMediaRef, args).buildElement();

    assertNotNull(img, "returned element?");
    assertEquals("img", img.getName(), "is img?");
    assertEquals(MediaMarkupBuilder.DUMMY_IMAGE, img.getAttributeValue("src"), "src set?");
    assertEquals(450, img.getAttributeValueAsInteger("width"), "width set?");
    assertEquals(213, img.getAttributeValueAsInteger("height"), "height set?");
    assertTrue(img.getCssClass().contains(MediaNameConstants.CSS_DUMMYIMAGE), "has dummy css class?");
  }

  @Test
  void testGetMediaElementEditModeDummyImageThumbnail() {
    if (!(adaptable() instanceof SlingHttpServletRequest)) {
      return;
    }

    // simulate edit-mode
    WCMMode.EDIT.toRequest(context.request());

    // if fixed dimensions are specified, the image must have exactly the specified size
    MediaArgs args = new MediaArgs(EDITORIAL_2COL);
    args.fixedDimension(100, 100);
    HtmlElement img = mediaHandler().get(parNullMediaRef, args).buildElement();

    assertNotNull(img, "returned element?");
    assertEquals(100, img.getAttributeValueAsInteger("width"), "width set?");
    assertEquals(100, img.getAttributeValueAsInteger("height"), "height set?");
  }

  // TESTS FOR FUNCTIONS THAT DELEGATE TO MediaHandler (WHERE THEY ARE TESTED IN MORE DETAIL)

  @Test
  void testGetMediaUrlStandard() {
    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().get(MEDIAITEM_PATH_STANDARD, EDITORIAL_1COL).buildUrl();
    assertNotNull(url, "returned url?");
    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/original./standard.jpg", url, "url as expected?");
  }

  @Test
  void testGetMediaUrlStandard_enforceVirtualRendition() {
    // enfore virtual renditions
    context.registerService(MediaHandlerConfig.class, new DummyMediaHandlerConfig() {
      @Override
      public boolean enforceVirtualRenditions() {
        return true;
      }
    }, Constants.SERVICE_RANKING, 1000);

    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().get(MEDIAITEM_PATH_STANDARD, EDITORIAL_1COL).buildUrl();
    assertNotNull(url, "returned url?");
    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/original.image_file.215.102.file/standard.jpg", url, "url as expected?");
  }

  @Test
  void testGetMediaUrlStandard_enforceOutputFileExtensions() {
    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().get(MEDIAITEM_PATH_STANDARD, EDITORIAL_1COL)
        .enforceOutputFileExtension(FileExtension.PNG)
        .buildUrl();
    assertNotNull(url, "returned url?");
    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/original.image_file.215.102.file/standard.png", url, "url as expected?");
  }

  @Test
  void testGetMediaUrlStandard_Resize() {
    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().get(MEDIAITEM_PATH_STANDARD, SHOWROOM_CONTROLS_SCALE1).buildUrl();
    assertNotNull(url, "returned url?");
    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/original.image_file.64.30.file/standard.jpg", url, "url as expected?");
  }

  @Test
  void testGetMediaUrlStandard_Resize_Download() {
    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().get(MEDIAITEM_PATH_STANDARD, new MediaArgs(SHOWROOM_CONTROLS_SCALE1).contentDispositionAttachment(true)).buildUrl();
    assertNotNull(url, "returned url?");
    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/original.image_file.64.30.download_attachment.file/standard.jpg",
        url, "url as expected?");
  }

  @Test
  void testGetMediaUrlStandard_FixedDimension_ExactMatch() {
    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().get(MEDIAITEM_PATH_STANDARD, new MediaArgs().fixedDimension(450, 213)).buildUrl();
    assertNotNull(url, "returned url?");
    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/cq5dam.web.450.213.jpg./cq5dam.web.450.213.jpg", url,
        "url as expected?");
  }

  @Test
  void testGetMediaUrlStandard_FixedDimension_Resize() {
    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().get(MEDIAITEM_PATH_STANDARD, new MediaArgs().fixedDimension(64, 30)).buildUrl();
    assertNotNull(url, "returned url?");
    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/original.image_file.64.30.file/standard.jpg", url,
        "url as expected?");
  }

  @Test
  void testGetMediaUrlNull() {
    // getMediaUrl should handle null mediaRefs and return null
    String url = mediaHandler().get((String)null).buildUrl();
    assertNull(url, "returned null?");
  }

  @Test
  void testGetMediaUrlEmpty() {
    // getMediaUrl should handle empty mediaRefs and return null
    String url = mediaHandler().get("").buildUrl();
    assertNull(url, "returned null?");
  }

  @Test
  void testGetMediaUrlIntegrator() {
    if (!(adaptable() instanceof SlingHttpServletRequest)) {
      return;
    }

    // activate integrator mode
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);

    // construct url to an existing media item - should resolve to the first rendition
    String url = mediaHandler().get(MEDIAITEM_PATH_STANDARD).buildUrl();
    assertNotNull(url, "returned url?");
    assertEquals("http://www.dummysite.org/content/dam/test/standard.jpg/_jcr_content/renditions/original./standard.jpg", url,
        "url as expected?");
  }

  @Test
  void testGetMediaProperties() {
    // get the properties of the first rendition of the 'standard' media item
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    ValueMap props = media.getAsset().getProperties();
    assertNotNull(props, "returned props?");
    assertEquals("Editorial Standard 1", props.get(DamConstants.DC_TITLE, String[].class)[0],
        "are there media item props?");
  }

  @Test
  void testGetRenditionProperties() {
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    ValueMap props = media.getRendition().getProperties();
    assertNotNull(props, "returned props?");
    assertEquals(1, props.size(), "are there rendition props?");
  }

  @Test
  void testGetAssetInfoVideo() {
    Media media = mediaHandler().get(MEDIAITEM_VIDEO, new MediaArgs(VIDEO_2COL)).build();
    assertTrue(media.isValid(), "valid");
    assertNull(media.getMediaInvalidReason(), "no invalid reason");

    Asset asset = media.getAsset();
    assertNotNull(asset, "returned null?");

    Rendition rendition = media.getRendition();
    assertEquals("/content/dam/test/movie.wmf/jcr:content/renditions/cq5dam.video.firefoxhq.ogg", rendition.getPath(), "ref-path");
  }

  @Test
  void testGetAssetInfoVideoAsImage() {
    Media media = mediaHandler().get(MEDIAITEM_VIDEO, new MediaArgs(EDITORIAL_2COL)).build();
    assertFalse(media.isValid(), "valid");
    assertEquals(MediaInvalidReason.NO_MATCHING_RENDITION, media.getMediaInvalidReason(), "invalid reason");

    Asset asset = media.getAsset();
    assertNotNull(asset, "returned null?");
  }

  @Test
  void testDownloadMediaElement() {
    MediaArgs mediaArgs = new MediaArgs().contentDispositionAttachment(true);
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertNotNull(media.getRendition(), "rendition?");
    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/original.media_file.download_attachment.file/standard.jpg",
        media.getRendition().getUrl(), "rendition.mediaUrl");
  }

  @Test
  void testGetMediaMarkup() {
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    assertEquals(
        "<img src=\"/content/dam/test/standard.jpg/_jcr_content/renditions/original./standard.jpg\" alt=\"Editorial Standard 1\" height=\"102\" width=\"215\" />",
        media.getMarkup());
  }

  @Test
  void testIsValidElement() {
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    assertTrue(mediaHandler().isValidElement(media.getElement()));
    assertFalse(mediaHandler().isValidElement(new Div()));
    assertFalse(mediaHandler().isValidElement(null));
  }

  @Test
  void testMultipleMediaMediaFormats() {
    MediaArgs mediaArgs = new MediaArgs(EDITORIAL_1COL, EDITORIAL_2COL, EDITORIAL_3COL);
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(1, media.getRenditions().size(), "renditions");
    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/original./standard.jpg",
        media.getUrl(), "rendition.mediaUrl");
    assertEquals(EDITORIAL_1COL, media.getRendition().getMediaFormat());
  }

  @Test
  void testMultipleMandatoryMediaFormats() {
    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(EDITORIAL_1COL, EDITORIAL_2COL, EDITORIAL_3COL);
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(3, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/original./standard.jpg",
        renditions.get(0).getUrl(), "rendition.mediaUrl.1");
    assertEquals(EDITORIAL_1COL, renditions.get(0).getMediaFormat());

    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/cq5dam.web.450.213.jpg./cq5dam.web.450.213.jpg",
        renditions.get(1).getUrl(), "rendition.mediaUrl.2");
    assertEquals(EDITORIAL_2COL, renditions.get(1).getMediaFormat());

    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/cq5dam.web.685.325.jpg./cq5dam.web.685.325.jpg",
        renditions.get(2).getUrl(), "rendition.mediaUrl.3");
    assertEquals(EDITORIAL_3COL, renditions.get(2).getMediaFormat());
  }

  @Test
  void testMultipleMandatoryMediaFormatsWithCropping() {
    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(SHOWROOM_CONTROLS_SCALE1, MATERIAL_TILE);
    Media media = mediaHandler().get(parResponsiveMediaRefCrop).args(mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(2, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/original.image_file.64.30.2,2,86,42.file/standard.jpg",
        renditions.get(0).getUrl(), "rendition.mediaUrl.1");
    assertEquals(SHOWROOM_CONTROLS_SCALE1, renditions.get(0).getMediaFormat());

    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/original.image_file.84.40.2,2,86,42.file/standard.jpg",
        renditions.get(1).getUrl(), "rendition.mediaUrl.2");
    assertEquals(MATERIAL_TILE, renditions.get(1).getMediaFormat());
  }

  @Test
  void testMultipleMandatoryMediaFormatsWithCropping_AlsoMatchOriginal() {
    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(SHOWROOM_CONTROLS, RATIO);
    Media media = mediaHandler().get(parSixteenTenMediaRefCrop).args(mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(2, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.84.40.0,0,840,400.file/sixteen-ten.jpg",
        renditions.get(0).getUrl(), "rendition.mediaUrl.1");
    assertEquals(SHOWROOM_CONTROLS, renditions.get(0).getMediaFormat());

    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original./sixteen-ten.jpg",
        renditions.get(1).getUrl(), "rendition.mediaUrl.2");
    assertEquals(RATIO, renditions.get(1).getMediaFormat());
  }

  @Test
  void testMultipleMandatoryMediaFormatsWithCropping_AlsoMatchOriginal_AutoCrop() {
    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(SHOWROOM_CONTROLS, RATIO, RATIO2, EDITORIAL_STAGE_SMALL)
        .autoCrop(true);
    Media media = mediaHandler().get(parSixteenTenMediaRefCrop).args(mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(4, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.84.40.0,0,840,400.file/sixteen-ten.jpg",
        renditions.get(0).getUrl(), "rendition.mediaUrl.1");
    assertEquals(SHOWROOM_CONTROLS, renditions.get(0).getMediaFormat());

    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original./sixteen-ten.jpg",
        renditions.get(1).getUrl(), "rendition.mediaUrl.2");
    assertEquals(RATIO, renditions.get(1).getMediaFormat());

    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.1333.1000.134,0,1467,1000.file/sixteen-ten.jpg",
        renditions.get(2).getUrl(), "rendition.mediaUrl.1");
    assertEquals(RATIO2, renditions.get(2).getMediaFormat());

    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.960.150.0,375,1600,625.file/sixteen-ten.jpg",
        renditions.get(3).getUrl(), "rendition.mediaUrl.1");
    assertEquals(EDITORIAL_STAGE_SMALL, renditions.get(3).getMediaFormat());
  }

  /**
   * If there are multiple non-mandatory media formats requests with an explicit cropping, and
   * the original image matches with the first media format (uncropped), and the cropped image with the
   * second media format, the cropped image should be preferred because the user explitely defined the cropping.
   */
  @Test
  void testMultipleMediaFormatsWithCropping_PreferCroppingOverFallback() {
    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(RATIO, SHOWROOM_CONTROLS);
    Media media = mediaHandler().get(parSixteenTenMediaRefCrop).args(mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(2, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.84.40.0,0,840,400.file/sixteen-ten.jpg",
        renditions.get(0).getUrl(), "rendition.mediaUrl.1");
    assertEquals(SHOWROOM_CONTROLS, renditions.get(0).getMediaFormat());

    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original./sixteen-ten.jpg",
        renditions.get(1).getUrl(), "rendition.mediaUrl.2");
    assertEquals(RATIO, renditions.get(1).getMediaFormat());
  }

  @Test
  void testMultipleMandatoryMediaFormatsNotAllMatch() {
    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(VIDEO_2COL, EDITORIAL_2COL, EDITORIAL_3COL);
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD, mediaArgs).build();
    assertFalse(media.isValid(), "valid?");
    assertEquals(MediaInvalidReason.NOT_ENOUGH_MATCHING_RENDITIONS, media.getMediaInvalidReason());
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(2, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/cq5dam.web.450.213.jpg./cq5dam.web.450.213.jpg",
        renditions.get(0).getUrl(), "rendition.mediaUrl.1");
    assertEquals(EDITORIAL_2COL, renditions.get(0).getMediaFormat());

    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/cq5dam.web.685.325.jpg./cq5dam.web.685.325.jpg",
        renditions.get(1).getUrl(), "rendition.mediaUrl.2");
    assertEquals(EDITORIAL_3COL, renditions.get(1).getMediaFormat());
  }

  @Test
  void testMultipleMandatoryMediaFormatsNotAllMatch_MixedMandatory() {
    MediaArgs mediaArgs = new MediaArgs().mediaFormatOptions(
        new MediaFormatOption(VIDEO_2COL, false),
        new MediaFormatOption(EDITORIAL_2COL, true),
        new MediaFormatOption(EDITORIAL_3COL, false));
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(2, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/cq5dam.web.450.213.jpg./cq5dam.web.450.213.jpg",
        renditions.get(0).getUrl(), "rendition.mediaUrl.2");
    assertEquals(EDITORIAL_2COL, renditions.get(0).getMediaFormat());

    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/cq5dam.web.685.325.jpg./cq5dam.web.685.325.jpg",
        renditions.get(1).getUrl(), "rendition.mediaUrl.3");
    assertEquals(EDITORIAL_3COL, renditions.get(1).getMediaFormat());
  }

  @Test
  void testOptionalMediaFormatNotMatch_ResponsiveChildFormatsMatch() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormat(NORATIO_LARGE_MINWIDTH) // does not match any rendition: too wide
        .imageSizes(new MediaArgs.ImageSizes("sizes",
            new MediaArgs.WidthOption(2000, false), // does not match any rendition: too wide
            new MediaArgs.WidthOption(500, false), // matches the rendition 685x325
            new MediaArgs.WidthOption(300, false))); // matches the rendition 450x213
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(2, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/cq5dam.web.685.325.jpg.image_file.500.237.file/cq5dam.web.685.325.jpg",
        renditions.get(0).getUrl(), "Virtual rendition for width option 500px should match");
    assertEquals(NORATIO_LARGE_MINWIDTH.getName() + "___500", renditions.get(0).getMediaFormat().getName());

    assertEquals("/content/dam/test/standard.jpg/_jcr_content/renditions/cq5dam.web.450.213.jpg.image_file.300.142.file/cq5dam.web.450.213.jpg",
        renditions.get(1).getUrl(), "Virtual rendition for width option 300px should match");
    assertEquals(NORATIO_LARGE_MINWIDTH.getName() + "___300", renditions.get(1).getMediaFormat().getName());
  }

  @Test
  void testEnableMediaDrop() {
    // simulate component context
    ComponentContext wcmComponentContext = mock(ComponentContext.class);
    context.request().setAttribute(ComponentContext.CONTEXT_ATTR_NAME, wcmComponentContext);
    when(wcmComponentContext.getResource()).thenReturn(parStandardMediaRef);
    when(wcmComponentContext.getEditContext()).thenReturn(mock(EditContext.class));
    when(wcmComponentContext.getEditContext().getEditConfig()).thenReturn(mock(EditConfig.class));

    WCMMode.EDIT.toRequest(context.request());
    HtmlElement div = new Div();
    MediaRequest mediaRequest = new MediaRequest(parStandardMediaRef, new MediaArgs().dragDropSupport(DragDropSupport.ALWAYS));
    DamMediaSource underTest = context.request().adaptTo(DamMediaSource.class);

    underTest.enableMediaDrop(div, mediaRequest);
    assertEquals(DropTarget.CSS_CLASS_PREFIX + MediaNameConstants.PN_MEDIA_REF, div.getCssClass());
  }

  @Test
  void testCustomizeIPEConfig() {
    // simulate component context
    ComponentContext wcmComponentContext = mock(ComponentContext.class);
    context.request().setAttribute(ComponentContext.CONTEXT_ATTR_NAME, wcmComponentContext);
    when(wcmComponentContext.getResource()).thenReturn(parStandardMediaRef);
    when(wcmComponentContext.getEditContext()).thenReturn(mock(EditContext.class));

    EditConfig editConfig = mock(EditConfig.class);
    when(wcmComponentContext.getEditContext().getEditConfig()).thenReturn(editConfig);

    // build ipe config with a fixed path set
    InplaceEditingConfig ipeConfig = mock(InplaceEditingConfig.class);
    when(editConfig.getInplaceEditingConfig()).thenReturn(ipeConfig);
    when(ipeConfig.getEditorType()).thenReturn("image");
    when(ipeConfig.getConfigPath()).thenReturn("/original/ipeconfig/path");

    // simulate request with media formats
    WCMMode.EDIT.toRequest(context.request());
    HtmlElement div = new Div();
    MediaRequest mediaRequest = new MediaRequest(parStandardMediaRef, new MediaArgs().mediaFormats(EDITORIAL_1COL));
    DamMediaSource underTest = context.request().adaptTo(DamMediaSource.class);

    // ensure a custom ipe config with a path pointing to IPEConfigResourceProvider is set
    String expectedPath = IPEConfigResourceProvider.buildPath(parStandardMediaRef.getPath(),
        ImmutableSet.of("editorial_1col"));
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        InplaceEditingConfig customIpeConfig = invocation.getArgument(0);
        assertEquals(expectedPath, customIpeConfig.getConfigPath());
        return null;
      }
    }).when(editConfig).setInplaceEditingConfig(any(InplaceEditingConfig.class));

    underTest.setCustomIPECropRatios(div, mediaRequest);
  }

  @Test
  @SuppressWarnings("deprecation")
  void testMultipleMandatoryMediaFormats_OnThyFlyMediaFormats() {
    MediaArgs mediaArgs = new MediaArgs().mandatoryMediaFormats(new io.wcm.handler.media.format.ResponsiveMediaFormatsBuilder(RATIO)
        .breakpoint("B1", 160, 100)
        .breakpoint("B2", 320, 200)
        .build());

    Media media = mediaHandler().get(MEDIAITEM_PATH_16_10, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(2, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    Rendition rendition0 = renditions.get(0);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.160.100.file/sixteen-ten.jpg",
        rendition0.getUrl(), "rendition.mediaUrl.1");
    assertEquals(160, rendition0.getWidth());
    assertEquals(100, rendition0.getHeight());
    assertEquals(160d / 100d, rendition0.getRatio(), 0.0001);

    MediaFormat mediaFormat0 = renditions.get(0).getMediaFormat();
    assertEquals(RATIO.getLabel(), mediaFormat0.getLabel());
    assertEquals(RATIO.getRatio(), mediaFormat0.getRatio(), 0.001d);
    assertEquals(160, mediaFormat0.getWidth());
    assertEquals(100, mediaFormat0.getHeight());
    assertEquals("B1", mediaFormat0.getProperties().get(MediaNameConstants.PROP_BREAKPOINT));

    Rendition rendition1 = renditions.get(1);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.320.200.file/sixteen-ten.jpg",
        rendition1.getUrl(), "rendition.mediaUrl.2");
    assertEquals(320, rendition1.getWidth());
    assertEquals(200, rendition1.getHeight());

    MediaFormat mediaFormat1 = renditions.get(1).getMediaFormat();
    assertEquals(RATIO.getLabel(), mediaFormat1.getLabel());
    assertEquals(RATIO.getRatio(), mediaFormat1.getRatio(), 0.001d);
    assertEquals(320, mediaFormat1.getWidth());
    assertEquals(200, mediaFormat1.getHeight());
    assertEquals("B2", mediaFormat1.getProperties().get(MediaNameConstants.PROP_BREAKPOINT));
  }

  @Test
  void testMultipleMandatoryMediaFormats_OnThyFlyMediaFormats_PictureSources() {
    Media media = mediaHandler().get(MEDIAITEM_PATH_16_10)
        .mediaFormat(RATIO)
        .pictureSource(new PictureSource(RATIO).media("media1").widths(160))
        .pictureSource(new PictureSource(RATIO).media("media2").widths(320))
        .build();

    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(3, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    Rendition rendition0 = renditions.get(0);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original./sixteen-ten.jpg",
        rendition0.getUrl(), "rendition.mediaUrl.1");
    assertEquals(1600, rendition0.getWidth());
    assertEquals(1000, rendition0.getHeight());
    assertEquals(160d / 100d, rendition0.getRatio(), 0.0001);

    MediaFormat mediaFormat0 = rendition0.getMediaFormat();
    assertEquals(RATIO.getName(), mediaFormat0.getName());

    Rendition rendition1 = renditions.get(1);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.160.100.file/sixteen-ten.jpg",
        rendition1.getUrl(), "rendition.mediaUrl.2");
    assertEquals(160, rendition1.getWidth());
    assertEquals(100, rendition1.getHeight());
    assertEquals(160d / 100d, rendition1.getRatio(), 0.0001);

    MediaFormat mediaFormat1 = rendition1.getMediaFormat();
    assertEquals(RATIO.getLabel(), mediaFormat1.getLabel());
    assertEquals(RATIO.getRatio(), mediaFormat1.getRatio(), 0.001d);
    assertEquals(160, mediaFormat1.getWidth());

    Rendition rendition2 = renditions.get(2);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.320.200.file/sixteen-ten.jpg",
        rendition2.getUrl(), "rendition.mediaUrl.3");
    assertEquals(320, rendition2.getWidth());
    assertEquals(200, rendition2.getHeight());

    MediaFormat mediaFormat2 = rendition2.getMediaFormat();
    assertEquals(RATIO.getLabel(), mediaFormat2.getLabel());
    assertEquals(RATIO.getRatio(), mediaFormat2.getRatio(), 0.001d);
    assertEquals(320, mediaFormat2.getWidth());
  }

  @Test
  void testMultipleMandatoryMediaFormats_OnThyFlyMediaFormats_PictureSources_NoRatio() {
    Media media = mediaHandler().get(MEDIAITEM_PATH_16_10)
        .mediaFormat(FIXEDHEIGHT_UNCONSTRAINED)
        .pictureSource(new PictureSource(FIXEDHEIGHT_UNCONSTRAINED).media("media1").widths(160))
        .pictureSource(new PictureSource(FIXEDHEIGHT_UNCONSTRAINED).media("media2").widths(320))
        .build();

    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(3, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    Rendition rendition0 = renditions.get(0);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original./sixteen-ten.jpg",
        rendition0.getUrl(), "rendition.mediaUrl.1");
    assertEquals(1600, rendition0.getWidth());
    assertEquals(1000, rendition0.getHeight());
    assertEquals(160d / 100d, rendition0.getRatio(), 0.0001);

    Rendition rendition1 = renditions.get(1);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.160.100.file/sixteen-ten.jpg",
        rendition1.getUrl(), "rendition.mediaUrl.2");
    assertEquals(160, rendition1.getWidth());
    assertEquals(100, rendition1.getHeight());
    assertEquals(160d / 100d, rendition1.getRatio(), 0.0001);

    MediaFormat mediaFormat1 = rendition1.getMediaFormat();
    assertEquals(FIXEDHEIGHT_UNCONSTRAINED.getLabel(), mediaFormat1.getLabel());
    assertEquals(160, mediaFormat1.getWidth());

    Rendition rendition2 = renditions.get(2);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.320.200.file/sixteen-ten.jpg",
        rendition2.getUrl(), "rendition.mediaUrl.3");
    assertEquals(320, rendition2.getWidth());
    assertEquals(200, rendition2.getHeight());

    MediaFormat mediaFormat2 = rendition2.getMediaFormat();
    assertEquals(FIXEDHEIGHT_UNCONSTRAINED.getLabel(), mediaFormat2.getLabel());
    assertEquals(320, mediaFormat2.getWidth());
  }

  @Test
  void testMultipleMediaFormats_ImageSizes() {
    Media media = mediaHandler().get(MEDIAITEM_PATH_16_10)
        .mediaFormats(RATIO2, RATIO) // <- only second media format matches
        .imageSizes("sizes", 160, 320)
        .build();

    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(3, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    Rendition rendition0 = renditions.get(0);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original./sixteen-ten.jpg",
        rendition0.getUrl(), "rendition.mediaUrl.1");
    assertEquals(1600, rendition0.getWidth());
    assertEquals(1000, rendition0.getHeight());
    assertEquals(160d / 100d, rendition0.getRatio(), 0.0001);

    MediaFormat mediaFormat0 = rendition0.getMediaFormat();
    assertEquals(RATIO.getName(), mediaFormat0.getName());

    Rendition rendition1 = renditions.get(1);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.160.100.file/sixteen-ten.jpg",
        rendition1.getUrl(), "rendition.mediaUrl.2");
    assertEquals(160, rendition1.getWidth());
    assertEquals(100, rendition1.getHeight());
    assertEquals(160d / 100d, rendition1.getRatio(), 0.0001);

    MediaFormat mediaFormat1 = rendition1.getMediaFormat();
    assertEquals(RATIO.getLabel(), mediaFormat1.getLabel());
    assertEquals(RATIO.getRatio(), mediaFormat1.getRatio(), 0.001d);
    assertEquals(160, mediaFormat1.getWidth());

    Rendition rendition2 = renditions.get(2);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.320.200.file/sixteen-ten.jpg",
        rendition2.getUrl(), "rendition.mediaUrl.3");
    assertEquals(320, rendition2.getWidth());
    assertEquals(200, rendition2.getHeight());

    MediaFormat mediaFormat2 = rendition2.getMediaFormat();
    assertEquals(RATIO.getLabel(), mediaFormat2.getLabel());
    assertEquals(RATIO.getRatio(), mediaFormat2.getRatio(), 0.001d);
    assertEquals(320, mediaFormat2.getWidth());
  }

  @Test
  void testImageSizes_UnconstrainedMediaFormatWithoutRatioOrMinSizes() {
    Media media = mediaHandler().get(MEDIAITEM_PATH_16_10)
        .mediaFormats(IMAGE_UNCONSTRAINED)
        .imageSizes("sizes", 160, 320)
        .build();

    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(3, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    Rendition rendition0 = renditions.get(0);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original./sixteen-ten.jpg",
        rendition0.getUrl(), "rendition.mediaUrl.1");
    assertEquals(1600, rendition0.getWidth());
    assertEquals(1000, rendition0.getHeight());
    assertEquals(160d / 100d, rendition0.getRatio(), 0.0001);

    MediaFormat mediaFormat0 = rendition0.getMediaFormat();
    assertEquals(IMAGE_UNCONSTRAINED.getName(), mediaFormat0.getName());

    Rendition rendition1 = renditions.get(1);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.160.100.file/sixteen-ten.jpg",
        rendition1.getUrl(), "rendition.mediaUrl.2");
    assertEquals(160, rendition1.getWidth());
    assertEquals(100, rendition1.getHeight());
    assertEquals(160d / 100d, rendition1.getRatio(), 0.0001);

    MediaFormat mediaFormat1 = rendition1.getMediaFormat();
    assertEquals(IMAGE_UNCONSTRAINED.getLabel(), mediaFormat1.getLabel());
    assertEquals(160, mediaFormat1.getWidth());

    Rendition rendition2 = renditions.get(2);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.320.200.file/sixteen-ten.jpg",
        rendition2.getUrl(), "rendition.mediaUrl.3");
    assertEquals(320, rendition2.getWidth());
    assertEquals(200, rendition2.getHeight());

    MediaFormat mediaFormat2 = rendition2.getMediaFormat();
    assertEquals(IMAGE_UNCONSTRAINED.getLabel(), mediaFormat2.getLabel());
    assertEquals(320, mediaFormat2.getWidth());
  }

  @Test
  void testPictureSources_UnconstrainedMediaFormatWithoutRatioOrMinSizes() {
    Media media = mediaHandler().get(MEDIAITEM_PATH_16_10)
        .mediaFormat(IMAGE_UNCONSTRAINED)
        .pictureSource(new PictureSource(IMAGE_UNCONSTRAINED).media("media1").widths(160))
        .pictureSource(new PictureSource(IMAGE_UNCONSTRAINED).media("media2").widths(320))
        .build();

    assertTrue(media.isValid(), "valid?");
    assertNotNull(media.getAsset(), "asset?");
    assertEquals(3, media.getRenditions().size(), "renditions");
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());

    Rendition rendition0 = renditions.get(0);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original./sixteen-ten.jpg",
        rendition0.getUrl(), "rendition.mediaUrl.1");
    assertEquals(1600, rendition0.getWidth());
    assertEquals(1000, rendition0.getHeight());
    assertEquals(160d / 100d, rendition0.getRatio(), 0.0001);

    Rendition rendition1 = renditions.get(1);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.160.100.file/sixteen-ten.jpg",
        rendition1.getUrl(), "rendition.mediaUrl.2");
    assertEquals(160, rendition1.getWidth());
    assertEquals(100, rendition1.getHeight());
    assertEquals(160d / 100d, rendition1.getRatio(), 0.0001);

    MediaFormat mediaFormat1 = rendition1.getMediaFormat();
    assertEquals(IMAGE_UNCONSTRAINED.getLabel(), mediaFormat1.getLabel());
    assertEquals(160, mediaFormat1.getWidth());

    Rendition rendition2 = renditions.get(2);
    assertEquals("/content/dam/test/sixteen-ten.jpg/_jcr_content/renditions/original.image_file.320.200.file/sixteen-ten.jpg",
        rendition2.getUrl(), "rendition.mediaUrl.3");
    assertEquals(320, rendition2.getWidth());
    assertEquals(200, rendition2.getHeight());

    MediaFormat mediaFormat2 = rendition2.getMediaFormat();
    assertEquals(IMAGE_UNCONSTRAINED.getLabel(), mediaFormat2.getLabel());
    assertEquals(320, mediaFormat2.getWidth());
  }

  @Test
  void testImageMap() {
    context.registerService(ImageMapLinkResolver.class, new DummyImageMapLinkResolver(context));

    // put map string in resource
    ModifiableValueMap props = parStandardMediaRef.adaptTo(ModifiableValueMap.class);
    props.put(MediaNameConstants.PN_MEDIA_MAP, ImageMapParserImplTest.MAP_STRING);

    Media media = mediaHandler().get(parStandardMediaRef).build();
    assertTrue(media.isValid(), "valid");

    // assert map
    assertEquals(ImageMapParserImplTest.EXPECTED_AREAS_RESOLVED, media.getMap());
    assertTrue(StringUtils.startsWith(media.getMarkup(), "<img "));
    assertTrue(StringUtils.endsWith(media.getMarkup(), "</map>"));
  }

  @Test
  void testImageMap_CustomProperty() {
    context.registerService(ImageMapLinkResolver.class, new DummyImageMapLinkResolver(context));

    // put map string in resource
    ModifiableValueMap props = parStandardMediaRef.adaptTo(ModifiableValueMap.class);
    props.put("customMapProperty", ImageMapParserImplTest.MAP_STRING);

    Media media = mediaHandler().get(parStandardMediaRef)
        .mapProperty("customMapProperty")
        .build();
    assertTrue(media.isValid(), "valid");

    // assert map
    assertEquals(ImageMapParserImplTest.EXPECTED_AREAS_RESOLVED, media.getMap());
    assertTrue(StringUtils.startsWith(media.getMarkup(), "<img "));
    assertTrue(StringUtils.endsWith(media.getMarkup(), "</map>"));
  }

  @Test
  void testMinWidthHeight_1() {
    MediaArgs mediaArgs = new MediaArgs(MediaFormatBuilder.create("medium_minWithHeight_1")
        .extensions("gif", "jpg", "png")
        .minWidthHeight(1000)
        .build());
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
  }

  @Test
  void testMinWidthHeight_2() {
    MediaArgs mediaArgs = new MediaArgs(MediaFormatBuilder.create("medium_minWithHeight_2")
        .extensions("gif", "jpg", "png")
        .minWidthHeight(1200)
        .build());
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD, mediaArgs).build();
    assertTrue(media.isValid(), "valid?");
  }

  @Test
  void testMinWidthHeight_NotFulfilled() {
    MediaArgs mediaArgs = new MediaArgs(MediaFormatBuilder.create("large_minWithHeight")
        .extensions("gif", "jpg", "png")
        .minWidthHeight(2000)
        .build());
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD, mediaArgs).build();
    assertFalse(media.isValid(), "valid?");
  }

}
