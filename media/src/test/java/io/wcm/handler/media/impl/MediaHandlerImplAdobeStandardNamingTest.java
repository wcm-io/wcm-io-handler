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

import static io.wcm.handler.media.MediaNameConstants.PN_MEDIA_CROP_STANDARD;
import static io.wcm.handler.media.MediaNameConstants.PN_MEDIA_REF_STANDARD;
import static io.wcm.handler.media.testcontext.AppAemContext.ROOTPATH_CONTENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Constants;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataListenerService;
import io.wcm.handler.mediasource.dam.impl.metadata.AssetSynchonizationService;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextCallback;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.util.RunMode;

/**
 * Test {@link MediaHandlerImpl} methods with adobe standard naming.
 */
@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class MediaHandlerImplAdobeStandardNamingTest {

  private final AemContext context = AppAemContext.newAemContext(new AemContextCallback() {
    @Override
    public void execute(AemContext callbackContext) {
      callbackContext.registerService(MediaHandlerConfig.class, new TestMediaHandlerConfig(),
          Constants.SERVICE_RANKING, 1000);
    }
  });

  @Test
  void testMediaResolve() {

    // register DamRenditionMetadataService (which is only active on author run mode) to generate rendition metadata
    context.runMode(RunMode.AUTHOR);
    context.registerInjectActivateService(new AssetSynchonizationService());
    context.registerInjectActivateService(new RenditionMetadataListenerService());

    Asset asset = context.create().asset("/content/dam/test.jpg", 20, 20, ContentType.JPEG);

    // create crop rendition as expected by CropRenditionHandler
    context.create().assetRendition(asset, "cq5dam.web.10.10.jpg", 10, 10, ContentType.JPEG);

    Resource resource = context.create().resource(ROOTPATH_CONTENT + "/media",
        PN_MEDIA_REF_STANDARD, asset.getPath(),
        PN_MEDIA_CROP_STANDARD, new CropDimension(2, 2, 4, 4).getCropString());

    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(resource).build();
    assertTrue(media.isValid());
    assertEquals("/content/dam/test.jpg/_jcr_content/renditions/original.image_file.8.8.4,4,12,12.file/test.jpg",
        media.getElement().getAttributeValue("src"));

  }


  static class TestMediaHandlerConfig extends MediaHandlerConfig {
    @Override
    public boolean useAdobeStandardNames() {
      // switch to adobe standard naming
      return true;
    }
  };

}
