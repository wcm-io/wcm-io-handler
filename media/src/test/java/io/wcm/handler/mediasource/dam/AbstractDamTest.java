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

import static org.junit.Assert.assertNotNull;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.testcontext.MediasourceDamAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.commons.contenttype.FileExtension;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;

import com.day.cq.wcm.api.Page;

/**
 * Unit tests with DAM sample content
 */
public abstract class AbstractDamTest {

  @Rule
  public final AemContext context = MediasourceDamAemContext.newAemContext();

  protected static final String ROOTPATH_DAM = MediasourceDamAemContext.DAM_PATH;
  protected static final String ROOTPATH_CONTENT = MediasourceDamAemContext.ROOTPATH_CONTENT + "/test";
  protected static final String MEDIAFORMATS_PATH = MediasourceDamAemContext.MEDIAFORMATS_PATH;

  // a media item with three renditions of the 'standard_image' rendition group
  protected static final String MEDIAITEM_PATH_STANDARD = ROOTPATH_DAM + "/standard.jpg";

  // path to a media item that does not exist
  protected static final String MEDIAITEM_PATH_NONEXISTANT = ROOTPATH_DAM + "/non_existant.jpg";

  // a media item without altText, rendition dimensions & fileSize - example for incomplete medialib content
  protected static final String MEDIAITEM_PATH_IMAGE_NOALT_NODIMENSIONS = ROOTPATH_DAM + "/imageNoAltNoDimensions.jpg";

  // a media item with only a single flash rendition in format standard_1col
  protected static final String MEDIAITEM_PATH_FLASH_WITHOUT_FALLBACK = ROOTPATH_DAM + "/flashWithoutFallback.swf";

  // a media item with DAM video assets
  protected static final String MEDIAITEM_VIDEO = ROOTPATH_DAM + "/movie.wmf";

  // test page
  protected Page testPage;

  // paragraph with mediaRef property pointing to {@link #MEDIAITEM_PATH_STANDARD}
  protected Resource parStandardMediaRef;

  // paragraph with mediaRef property pointing to {@link #MEDIAITEM_PATH_STANDARD} with cropping attributes
  protected Resource parStandardMediaRefCrop;

  // paragraph with mediaRef property pointing to {@link #MEDIAITEM_PATH_STANDARD} with editorial alt.text
  protected Resource parStandardMediaRefAltText;

  // paragraph with mediaRef property pointing to {@link #MEDIAITEM_PATH_NONEXISTANT}
  protected Resource parInvalidMediaRef;

  // paragraph with mediaRef property containing an empty string
  protected Resource parNullMediaRef;

  // paragraph with missing mediaRef property
  protected Resource parEmptyMediaRef;

  // paragraph with *mediaRef2* property pointing to {@link #MEDIAITEM_PATH_STANDARD}
  protected Resource parStandardMediaRef2;

  // paragraph with mediaRef property pointing to {@link #MEDIAITEM_PATH_IMAGE_NOALT_NODIMENSIONS}
  protected Resource parImgNoAltNoDimension;

  // paragraph with mediaRef property pointing to {@link #MEDIAITEM_PATH_FLASH_WITH_FALLBACK}
  protected Resource parFlashWithFallbackMediaRef;

  // paragraph with mediaRef property pointing to {@link #MEDIAITEM_PATH_FLASH_WITHOUT_FALLBACK}
  protected Resource parFlashWithoutFallbackMediaRef;

  // paragraph with inline image
  protected Resource parInlineImage;

  private MediaHandler mediaHandler;

  @Before
  public final void setUpDamEnvironment() throws Exception {

    // simulate HTML requests for integrator mode
    context.requestPathInfo().setExtension(FileExtension.HTML);

    // all test-content (test content and media-item-pages) are defined in json files in src/test/resources
    context.load().json("/mediasource/dam/damcontent-sample.json", ROOTPATH_DAM);
    context.load().json("/mediasource/dam/sitecontent-sample.json", ROOTPATH_CONTENT);

    testPage = context.pageManager().getPage(ROOTPATH_CONTENT);
    context.currentPage(testPage);

    // get paragraphs from test page and ensure they all actually exist
    parStandardMediaRef = testPage.getContentResource("content/par_standard_mediaRef");
    assertNotNull("par_standard_mediaRef exists?", parStandardMediaRef);

    parStandardMediaRefCrop = testPage.getContentResource("content/par_standard_mediaRef_crop");
    assertNotNull("par_standard_mediaRef_crop exists?", parStandardMediaRefCrop);

    parStandardMediaRefAltText = testPage.getContentResource("content/par_standard_mediaRef_altText");
    assertNotNull("par_standard_mediaRef_altText exists?", parStandardMediaRefAltText);

    parInvalidMediaRef = testPage.getContentResource("content/par_invalid_mediaRef");
    assertNotNull("par_invalid_mediaRef exists?", parInvalidMediaRef);

    parNullMediaRef = testPage.getContentResource("content/par_null_mediaRef");
    assertNotNull("par_null_mediaRef exists?", parNullMediaRef);

    parEmptyMediaRef = testPage.getContentResource("content/par_empty_mediaRef");
    assertNotNull("par_empty_mediaRef exists?", parEmptyMediaRef);

    parStandardMediaRef2 = testPage.getContentResource("content/par_standard_mediaRef2");
    assertNotNull("par_empty_mediaRef2 exists?", parStandardMediaRef2);

    parImgNoAltNoDimension = testPage.getContentResource("content/par_imageNoAltNoDimensions_mediaRef");
    assertNotNull("par_imageNoAltNoDimensions_mediaRef", parImgNoAltNoDimension);

    parFlashWithFallbackMediaRef = testPage.getContentResource("content/par_flashWithFallback_mediaRef");
    assertNotNull("par_flashWithFallback_mediaRef exists?", parFlashWithFallbackMediaRef);

    parFlashWithoutFallbackMediaRef = testPage.getContentResource("content/par_flashWithoutFallback_mediaRef");
    assertNotNull("par_flashWithoutFallback_mediaRef exists?", parFlashWithoutFallbackMediaRef);

    parInlineImage = testPage.getContentResource("content/par_inlineImage");
    assertNotNull("par_inlineImage exists?", parInlineImage);
  }

  protected MediaHandler mediaHandler() {
    if (this.mediaHandler == null) {
      this.mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
    }
    return this.mediaHandler;
  }

}
