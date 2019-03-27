/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.handler.url.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.url.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class SiteRootTest {

  private final AemContext context = AppAemContext.newAemContext();

  @BeforeEach
  void setUp() {
    context.create().page("/content/unittest/de_test/brand/de/conference");
    context.create().page("/content/launches/2018/01/01/my-launch/content/unittest/de_test/brand/de/conference");
  }

  @Test
  void testGetRootPath() {
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertEquals("/content/unittest/de_test/brand/de", underTest.getRootPath());

    // check that SiteRoot also works with resources
    underTest = context.currentResource().adaptTo(SiteRoot.class);
    assertEquals("/content/unittest/de_test/brand/de", underTest.getRootPath());
  }

  @Test
  void testGetRootPath_NoCurrentPage() {
    context.currentPage((String)null);
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertNull(underTest.getRootPath());
  }

  @Test
  void testGetRootPage() {
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertEquals("/content/unittest/de_test/brand/de", underTest.getRootPage().getPath());

    // check that SiteRoot also works with resources
    underTest = context.currentResource().adaptTo(SiteRoot.class);
    assertEquals("/content/unittest/de_test/brand/de", underTest.getRootPage().getPath());
  }

  @Test
  void testGetRootPage_NoCurrentPage() {
    context.currentPage((String)null);
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertNull(underTest.getRootPage());
  }

  @Test
  void testGetRelativePage() {
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertEquals("/content/unittest/de_test/brand/de/conference", underTest.getRelativePage("/conference").getPath());
  }

  @Test
  void testGetRelativePage_NoCurrentPage() {
    context.currentPage((String)null);
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertNull(underTest.getRelativePage("/conference"));
  }

  @Test
  void testIsSiteRootPage() {
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertTrue(underTest.isRootPage(context.pageManager().getPage("/content/unittest/de_test/brand/de")));
    assertFalse(underTest.isRootPage(context.pageManager().getPage("/content/unittest/de_test/brand/de/conference")));
  }

  @Test
  void testIsSiteRootPage_NoCurrentPage() {
    context.currentPage((String)null);
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertFalse(underTest.isRootPage(context.pageManager().getPage("/content/unittest/de_test/brand/de")));
    assertFalse(underTest.isRootPage(context.pageManager().getPage("/content/unittest/de_test/brand/de/conference")));
  }

  @Test
  void testGetRootPathForLaunch() {
    context.currentPage("/content/launches/2018/01/01/my-launch/content/unittest/de_test/brand/de/conference");
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertEquals("/content/launches/2018/01/01/my-launch/content/unittest/de_test/brand/de", underTest.getRootPath());

    // check that SiteRoot also works with resources
    underTest = context.currentResource().adaptTo(SiteRoot.class);
    assertEquals("/content/launches/2018/01/01/my-launch/content/unittest/de_test/brand/de", underTest.getRootPath());
  }

  @Test
  void testGetRelativePageForLaunch() {
    context.currentPage("/content/launches/2018/01/01/my-launch/content/unittest/de_test/brand/de/conference");
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertEquals("/content/launches/2018/01/01/my-launch/content/unittest/de_test/brand/de/conference", underTest.getRelativePage("/conference").getPath());
  }

}
