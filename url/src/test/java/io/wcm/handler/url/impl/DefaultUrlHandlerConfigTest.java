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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.wcm.handler.url.SiteRootDetector;
import io.wcm.handler.url.spi.UrlHandlerConfig;
import io.wcm.testing.mock.aem.junit.AemContext;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("null")
public class DefaultUrlHandlerConfigTest {

  private static final int ROOT_LEVEL = 2;

  @Rule
  public AemContext context = new AemContext();

  @Mock
  private SiteRootDetector siteRootDetector;

  private UrlHandlerConfig underTest;

  @Before
  public void setUp() throws Exception {
    context.registerService(SiteRootDetector.class, siteRootDetector);
    underTest = context.registerInjectActivateService(new DefaultUrlHandlerConfig());
  }

  @Test
  public void testGetSiteRootLevel() {
    Resource resource = context.create().resource("/content/test1");
    when(siteRootDetector.getSiteRootLevel(resource)).thenReturn(ROOT_LEVEL);
    assertEquals(ROOT_LEVEL, underTest.getSiteRootLevel(resource));
  }

}
