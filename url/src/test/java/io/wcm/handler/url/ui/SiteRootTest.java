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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.handler.url.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class SiteRootTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  @Before
  public void setUp() {
    context.create().page("/content/unittest/de_test/brand/de/conference");
  }

  @Test
  public void testGetRootPath() {
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertEquals("/content/unittest/de_test/brand/de", underTest.getRootPath());
  }

  @Test
  public void testGetRootPath_NoCurrentPage() {
    context.currentPage((String)null);
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertNull(underTest.getRootPath());
  }

  @Test
  public void testGetRootPage() {
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertEquals("/content/unittest/de_test/brand/de", underTest.getRootPage().getPath());
  }

  @Test
  public void testGetRootPage_NoCurrentPage() {
    context.currentPage((String)null);
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertNull(underTest.getRootPage());
  }

  @Test
  public void testGetRelativePage() {
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertEquals("/content/unittest/de_test/brand/de/conference", underTest.getRelativePage("/conference").getPath());
  }

  @Test
  public void testGetRelativePage_NoCurrentPage() {
    context.currentPage((String)null);
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertNull(underTest.getRelativePage("/conference"));
  }

  @Test
  public void testIsSiteRootPage() {
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertTrue(underTest.isRootPage(context.pageManager().getPage("/content/unittest/de_test/brand/de")));
    assertFalse(underTest.isRootPage(context.pageManager().getPage("/content/unittest/de_test/brand/de/conference")));
  }

  @Test
  public void testIsSiteRootPage_NoCurrentPage() {
    context.currentPage((String)null);
    SiteRoot underTest = context.request().adaptTo(SiteRoot.class);
    assertFalse(underTest.isRootPage(context.pageManager().getPage("/content/unittest/de_test/brand/de")));
    assertFalse(underTest.isRootPage(context.pageManager().getPage("/content/unittest/de_test/brand/de/conference")));
  }

}
