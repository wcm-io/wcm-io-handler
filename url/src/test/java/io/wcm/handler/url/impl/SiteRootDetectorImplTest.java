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

import static io.wcm.testing.mock.wcmio.caconfig.ContextPlugins.WCMIO_CACONFIG;
import static io.wcm.testing.mock.wcmio.sling.ContextPlugins.WCMIO_SLING;
import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.handler.url.SiteRootDetector;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;
import io.wcm.testing.mock.wcmio.caconfig.MockCAConfig;

public class SiteRootDetectorImplTest {

  private static final int ROOT_LEVEL = 2;

  @Rule
  public AemContext context = new AemContextBuilder()
      .plugin(CACONFIG)
      .plugin(WCMIO_SLING, WCMIO_CACONFIG)
      .build();

  private SiteRootDetector underTest;

  @Before
  public void setUp() throws Exception {
    MockCAConfig.contextPathStrategyAbsoluteParent(context, ROOT_LEVEL);
    underTest = context.registerInjectActivateService(new SiteRootDetectorImpl());
  }

  @Test
  public void testGetSiteRootLevel() {
    assertEquals(-1, underTest.getSiteRootLevel(context.create().resource("/content")));
    assertEquals(ROOT_LEVEL, underTest.getSiteRootLevel(context.create().resource("/content/test1/test2")));
    assertEquals(ROOT_LEVEL, underTest.getSiteRootLevel(context.create().resource("/content/test1/test2/test3")));
    assertEquals(ROOT_LEVEL, underTest.getSiteRootLevel(context.create().resource("/content/test1/test2/test3/test4/test5")));
    assertEquals(-1, underTest.getSiteRootLevel(null));
  }

}
