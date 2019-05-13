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
package io.wcm.handler.mediasource.dam.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Constants;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.util.RunMode;

/**
 * Tests inclusion and exclusion of AEM-generated asset thumbnails and web renditions.
 */
@ExtendWith(AemContextExtension.class)
class AssertThumbnailWebRenditionMediaHandlerTest {

  private final AemContext context = AppAemContext.newAemContext();

  private MediaHandler mediaHandler;
  private Asset asset;

  @BeforeEach
  void setUp() {
    // register DamRenditionMetadataService (which is only active on author run mode) to generate rendition metadata
    context.runMode(RunMode.AUTHOR);
    context.registerInjectActivateService(new DamRenditionMetadataService());

    // prepare asset with web rendition
    // original uses a different ratio than the other renditions to test only with the other renditions
    asset = context.create().asset("/content/dam/test.jpg", 400, 400, ContentType.JPEG);
    context.create().assetRendition(asset, "rendition1.jpg", 360, 180, ContentType.JPEG);
    context.create().assetRendition(asset, "cq5dam.web.300.150.jpg", 300, 150, ContentType.JPEG);
    context.create().assetRendition(asset, "cq5dam.thumbnail.60.30.jpg", 60, 30, ContentType.JPEG);
  }

  @Test
  void testWithDefaultSettings() {
    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(asset.getPath())
        .fixedDimension(30, 15)
        .build();
    assertTrue(media.isValid());
    Rendition rendition = media.getRendition();
    assertEquals(30, rendition.getWidth());
    assertEquals(15, rendition.getHeight());
    // expected: rendition derived from cqdam.web rendition because is is allowed by default
    assertEquals("/content/dam/test.jpg/_jcr_content/renditions/cq5dam.web.300.150.jpg.image_file.30.15.file/cq5dam.web.300.150.jpg", media.getUrl());
  }

  @Test
  void testWithDisallowWebRenditions() {
    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(asset.getPath())
        .fixedDimension(30, 15)
        .includeAssetWebRenditions(false)
        .build();
    assertTrue(media.isValid());
    Rendition rendition = media.getRendition();
    assertEquals(30, rendition.getWidth());
    assertEquals(15, rendition.getHeight());
    // expected: rendition derived from rendition1 because both web and thumbnail renditions are not allowed
    assertEquals("/content/dam/test.jpg/_jcr_content/renditions/rendition1.jpg.image_file.30.15.file/rendition1.jpg", media.getUrl());
  }

  @Test
  void testWithAllowThumbnailRenditions() {
    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(asset.getPath())
        .fixedDimension(30, 15)
        .includeAssetThumbnails(true)
        .build();
    assertTrue(media.isValid());
    Rendition rendition = media.getRendition();
    assertEquals(30, rendition.getWidth());
    assertEquals(15, rendition.getHeight());
    // expected: rendition derived from cq5dam.thumbnail because both web and thumbnail renditions are allowed
    assertEquals("/content/dam/test.jpg/_jcr_content/renditions/cq5dam.thumbnail.60.30.jpg.image_file.30.15.file/cq5dam.thumbnail.60.30.jpg", media.getUrl());
  }

  @Test
  void testWithDisallowWebRenditionsViaMediaHandlerConfig() {
    context.registerService(MediaHandlerConfig.class, new MediaHandlerConfig() {
      @Override
      public boolean includeAssetWebRenditionsByDefault() {
        return false;
      }
    }, Constants.SERVICE_RANKING, 1000);

    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(asset.getPath())
        .fixedDimension(30, 15)
        .build();
    assertTrue(media.isValid());
    Rendition rendition = media.getRendition();
    assertEquals(30, rendition.getWidth());
    assertEquals(15, rendition.getHeight());
    // expected: rendition derived from rendition1 because both web and thumbnail renditions are not allowed
    assertEquals("/content/dam/test.jpg/_jcr_content/renditions/rendition1.jpg.image_file.30.15.file/rendition1.jpg", media.getUrl());
  }

}
