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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.service.event.EventHandler;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamEvent;
import com.day.cq.dam.api.Rendition;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.mediasource.dam.impl.DamRenditionMetadataService;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextCallback;
import io.wcm.wcm.commons.contenttype.ContentType;

/**
 * Test {@link MediaHandlerImpl} methods with adobe standard naming.
 */
@SuppressWarnings("null")
public class MediaHandlerImplAdobeStandardNamingTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext(new AemContextCallback() {
    @Override
    public void execute(AemContext callbackContext) {
      callbackContext.registerService(MediaHandlerConfig.class, new TestMediaHandlerConfig(),
          Constants.SERVICE_RANKING, 1000);
    }
  });

  protected Adaptable adaptable() {
    return context.request();
  }

  @Test
  public void testMediaResolve() {

    // register DamRenditionMetadataService which is only active on author run mode
    context.runMode("author");
    EventHandler eventHandler = context.registerInjectActivateService(new DamRenditionMetadataService());

    Asset asset = context.create().asset("/content/dam/test.jpg", 20, 20, ContentType.JPEG);

    // create crop rendition as expected by CropRenditionHandler
    Rendition rendition = context.create().assetRendition(asset, "cq5dam.web.10.10.jpg", 10, 10, ContentType.JPEG);
    // generate rendition metadata
    eventHandler.handleEvent(DamEvent.renditionUpdated(asset.getPath(), "admin", rendition.getPath()).toEvent());

    Resource resource = context.create().resource(ROOTPATH_CONTENT + "/media",
        PN_MEDIA_REF_STANDARD, asset.getPath(),
        PN_MEDIA_CROP_STANDARD, new CropDimension(2, 2, 4, 4).getCropString());

    MediaHandler mediaHandler = AdaptTo.notNull(adaptable(), MediaHandler.class);

    Media media = mediaHandler.get(resource).build();
    assertTrue(media.isValid());
    assertEquals("/content/dam/test.jpg/_jcr_content/renditions/original.image_file.8.8.4,4,12,12.file/test.jpg",
        media.getElement().getAttributeValue("src"));

  }


  public static class TestMediaHandlerConfig extends MediaHandlerConfig {

    @Override
    public boolean useAdobeStandardNames() {
      // switch to adobe standard naming
      return true;
    }

  };

}
