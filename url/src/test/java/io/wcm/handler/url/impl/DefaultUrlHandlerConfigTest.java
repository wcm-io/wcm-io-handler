/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.handler.url.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.handler.url.SiteRootDetector;
import io.wcm.handler.url.spi.UrlHandlerConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class DefaultUrlHandlerConfigTest {

  private static final int ROOT_LEVEL = 2;

  private final AemContext context = new AemContext();

  @Mock
  private SiteRootDetector siteRootDetector;

  private UrlHandlerConfig underTest;

  @BeforeEach
  void setUp() throws Exception {
    context.registerService(SiteRootDetector.class, siteRootDetector);
    underTest = context.registerInjectActivateService(new DefaultUrlHandlerConfig());
  }

  @Test
  void testGetSiteRootLevel() {
    Resource resource = context.create().resource("/content/test1");
    when(siteRootDetector.getSiteRootLevel(resource)).thenReturn(ROOT_LEVEL);
    assertEquals(ROOT_LEVEL, underTest.getSiteRootLevel(resource));
  }

}
