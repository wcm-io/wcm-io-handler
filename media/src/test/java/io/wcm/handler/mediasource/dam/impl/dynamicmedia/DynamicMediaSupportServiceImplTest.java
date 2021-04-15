/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.handler.mediasource.dam.impl.dynamicmedia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.featureflags.impl.ConfiguredFeature;
import org.apache.sling.testing.mock.caconfig.MockContextAwareConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.entitlement.api.EntitlementConstants;
import com.day.cq.dam.scene7.api.constants.Scene7Constants;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.url.SiteConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.testing.mock.wcmio.caconfig.MockCAConfig;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
class DynamicMediaSupportServiceImplTest {

  private final AemContext context = AppAemContext.newAemContext();

  private Asset asset;

  @BeforeEach
  void setUp() {
    asset = context.create().asset("/content/dam/dummy.jpg", 10, 10, ContentType.JPEG,
        Scene7Constants.PN_S7_FILE, "DummyFolder/dummy");
  }

  @Test
  void testDefaultConfig_FeatureDisabled() {
    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl());
    assertFalse(underTest.isDynamicMediaEnabled());
  }

  @Test
  void testDefaultConfig() {
    activateDynamicMediaFeature();
    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl());
    assertTrue(underTest.isDynamicMediaEnabled());
    assertEquals(new Dimension(2000, 2000), underTest.getImageSizeLimit());
    assertEquals("https://dummy.scene7.com", underTest.getDynamicMediaServerUrl(asset));
  }

  @Test
  void testConfigDisabled() {
    activateDynamicMediaFeature();
    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl(),
        "enabled", false);
    assertFalse(underTest.isDynamicMediaEnabled());
  }

  @Test
  void testAuthorPreviewMode() {
    activateDynamicMediaFeature();
    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl(),
        "authorPreviewMode", true,
        "imageSizeLimitWidth", 4000,
        "imageSizeLimitHeight", 3000);
    assertTrue(underTest.isDynamicMediaEnabled());
    assertEquals(new Dimension(4000, 3000), underTest.getImageSizeLimit());
    assertEquals("", underTest.getDynamicMediaServerUrl(asset));
  }

  @Test
  void testAuthorPreviewMode_SiteConfig() {
    activateDynamicMediaFeature();

    MockCAConfig.contextPathStrategyAbsoluteParent(context, 1);
    MockContextAwareConfig.writeConfiguration(context, "/content/dam", SiteConfig.class,
        "siteUrlAuthor", "https://author");

    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl(),
        "authorPreviewMode", true);
    assertTrue(underTest.isDynamicMediaEnabled());
    assertEquals(new Dimension(2000, 2000), underTest.getImageSizeLimit());
    assertEquals("https://author", underTest.getDynamicMediaServerUrl(asset));
  }

  private void activateDynamicMediaFeature() {
    context.registerInjectActivateService(new ConfiguredFeature(),
        "name", EntitlementConstants.ASSETS_SCENE7_FEATURE_FLAG_PID,
        "enabled", true);
  }

}
