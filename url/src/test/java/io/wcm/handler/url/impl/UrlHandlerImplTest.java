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
package io.wcm.handler.url.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import io.wcm.handler.url.UrlHandler;
import io.wcm.handler.url.UrlMode;
import io.wcm.handler.url.UrlModes;
import io.wcm.handler.url.integrator.IntegratorPlaceholder;
import io.wcm.handler.url.testcontext.AppAemContext;
import io.wcm.handler.url.testcontext.DummyAppTemplate;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit.AemContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableSet;

/**
 * Test {@link UrlHandlerImpl} methods.
 */
public class UrlHandlerImplTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  protected Adaptable adaptable() {
    return context.request();
  }

  /**
   * Test {@link UrlHandler#rewritePathToContext(String)} with current site context
   */
  @Test
  public void testRewritePathToContext_SiteContext() {

    // create current page in site context
    context.currentPage(context.create().page("/content/unittest/de_test/brand/de/section/page",
        DummyAppTemplate.CONTENT.getTemplatePath()));

    UrlHandler urlHandler = AdaptTo.notNull(adaptable(), UrlHandler.class);

    // paths from current site
    assertEquals("site-current-1", "/content/unittest/de_test/brand/de",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/de"));
    assertEquals("site-current-2", "/content/unittest/de_test/brand/de/section/page",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/de/section/page"));
    assertEquals("site-current-3", "/content/unittest/de_test/brand/de/somewhat/other/page",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/de/somewhat/other/page"));

    // paths from other language
    assertEquals("site-otherlang-1", "/content/unittest/de_test/brand/de",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/fr"));
    assertEquals("site-otherlang-2", "/content/unittest/de_test/brand/de/section/page",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/en/section/page"));
    assertEquals("site-otherlang-3", "/content/unittest/de_test/brand/de/somewhat/other/page",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/it/somewhat/other/page"));

    // paths from other sites (for example a dealersite)
    assertEquals("site-othersite-1", "/content/unittest/de_test/brand/de",
        urlHandler.rewritePathToContext("/content/unittest/de_sample_master/brand/de"));

    // paths from other markets/languages
    assertEquals("site-othermarket-1", "/content/unittest/de_test/brand/de",
        urlHandler.rewritePathToContext("/content/unittest/fr_xxx/brand/fr"));
    assertEquals("site-othermarket-2", "/content/unittest/de_test/brand/de/section/page",
        urlHandler.rewritePathToContext("/content/unittest/en_yyy/brand/en/section/page"));
    assertEquals("site-othermarket-3", "/content/unittest/de_test/brand/de/somewhat/other/page",
        urlHandler.rewritePathToContext("/content/unittest/it/brand/it_123/somewhat/other/page"));
    assertEquals("site-othermarket-4", "/content/unittest/de_test/brand/de/somewhat/other/page",
        urlHandler.rewritePathToContext("/content/otherunittest/it/brand/it_123/somewhat/other/page"));

    // invalid paths
    assertEquals("invalid-1", "/content/unittest/xxx",
        urlHandler.rewritePathToContext("/content/unittest/xxx"));
    assertEquals("invalid-2", "/content/xxx",
        urlHandler.rewritePathToContext("/content/xxx"));
    assertEquals("invalid-3", "/etc/aa/bb/cc/dd/ee/ff",
        urlHandler.rewritePathToContext("/etc/aa/bb/cc/dd/ee/ff"));
    assertEquals("invalid-4", "/content/unittest",
        urlHandler.rewritePathToContext("/content/unittest"));
    assertEquals("invalid-5", "/content",
        urlHandler.rewritePathToContext("/content"));
    assertEquals("invalid-6", "",
        urlHandler.rewritePathToContext(""));
    assertNull("invalid-7", urlHandler.rewritePathToContext(null));

  }

  /**
   * Test {@link UrlHandler#rewritePathToContext(String)} with current invalid context
   */
  @Test
  public void testRewritePathToContext_InvalidContext() {

    // create current page with invalid context
    context.currentPage(context.create().page("/etc/xxx/yyy/zzz",
        DummyAppTemplate.CONTENT.getTemplatePath()));

    UrlHandler urlHandler = AdaptTo.notNull(adaptable(), UrlHandler.class);

    // paths from current site
    assertEquals("site-current-1", "/content/unittest/de_test/brand/de",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/de"));
    assertEquals("site-current-2", "/content/unittest/de_test/brand/de/section/page",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/de/section/page"));
    assertEquals("site-current-3", "/content/unittest/de_test/brand/de/somewhat/other/page",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/de/somewhat/other/page"));

    // paths from other language
    assertEquals("site-otherlang-1", "/content/unittest/de_test/brand/fr",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/fr"));
    assertEquals("site-otherlang-2", "/content/unittest/de_test/brand/en/section/page",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/en/section/page"));
    assertEquals("site-otherlang-3", "/content/unittest/de_test/brand/it/somewhat/other/page",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/it/somewhat/other/page"));

    // paths from other markets/languages
    assertEquals("site-othermarket-1", "/content/unittest/fr_xxx/brand/fr",
        urlHandler.rewritePathToContext("/content/unittest/fr_xxx/brand/fr"));
    assertEquals("site-othermarket-2", "/content/unittest/en_yyy/brand/en/section/page",
        urlHandler.rewritePathToContext("/content/unittest/en_yyy/brand/en/section/page"));
    assertEquals("site-othermarket-3", "/content/unittest/it/brand/it_123/somewhat/other/page",
        urlHandler.rewritePathToContext("/content/unittest/it/brand/it_123/somewhat/other/page"));
    assertEquals("site-othermarket-3", "/content/otherunittest/it/brand/it_123/somewhat/other/page",
        urlHandler.rewritePathToContext("/content/otherunittest/it/brand/it_123/somewhat/other/page"));

    // invalid paths
    assertEquals("invalid-1", "/content/unittest/xxx",
        urlHandler.rewritePathToContext("/content/unittest/xxx"));
    assertEquals("invalid-2", "/content/xxx",
        urlHandler.rewritePathToContext("/content/xxx"));
    assertEquals("invalid-3", "/etc/aa/bb/cc/dd/ee/ff",
        urlHandler.rewritePathToContext("/etc/aa/bb/cc/dd/ee/ff"));
    assertEquals("invalid-4", "/content/unittest",
        urlHandler.rewritePathToContext("/content/unittest"));
    assertEquals("invalid-5", "/content",
        urlHandler.rewritePathToContext("/content"));
    assertEquals("invalid-6", "",
        urlHandler.rewritePathToContext(""));
    assertNull("invalid-7", urlHandler.rewritePathToContext(null));

  }

  /**
   * Test {@link UrlHandler#rewritePathToContext(String)} without current context, given context
   */
  @Test
  public void testRewritePathToContext_SpecContext() {

    context.currentPage(context.create().page("/content", DummyAppTemplate.STRUCTURE_ELEMENT.getTemplatePath()));
    UrlHandler urlHandler = AdaptTo.notNull(adaptable(), UrlHandler.class);

    // paths from current site
    assertEquals("site-current-1", "/content/unittest/de_test/brand/de",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/de",
            "/content/unittest/de_test/brand/de/section/page"));
    assertEquals("site-current-2", "/content/unittest/de_test/brand/de/section/page",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/de/section/page",
            "/content/unittest/de_test/brand/de/section/page"));
    assertEquals("site-current-3", "/content/unittest/de_test/brand/de/somewhat/other/page",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/de/somewhat/other/page",
            "/content/unittest/de_test/brand/de/section/page"));
    assertEquals("site-current-4", "/content/unittest/de_test/brand/de/somewhat/other/page",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/de/somewhat/other/page", null));

    // paths from other language
    assertEquals("site-otherlang-1", "/content/unittest/de_test/brand/de",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/fr",
            "/content/unittest/de_test/brand/de/section/page"));
    assertEquals("site-otherlang-2", "/content/unittest/de_test/brand/de/section/page",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/en/section/page",
            "/content/unittest/de_test/brand/de/section/page"));
    assertEquals("site-otherlang-3", "/content/unittest/de_test/brand/de/somewhat/other/page",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/it/somewhat/other/page",
            "/content/unittest/de_test/brand/de/section/page"));
    assertEquals("site-otherlang-4", "/content/unittest/de_test/brand/it/somewhat/other/page",
        urlHandler.rewritePathToContext("/content/unittest/de_test/brand/it/somewhat/other/page", null));

    // paths from other sites (for example a dealersite)
    assertEquals("site-othersite-1", "/content/unittest/de_test/brand/de",
        urlHandler.rewritePathToContext("/content/unittest/de_sample_master/brand/de",
            "/content/unittest/de_test/brand/de/section/page"));

    // paths from other markets/languages
    assertEquals("site-othermarket-1", "/content/unittest/de_test/brand/de",
        urlHandler.rewritePathToContext("/content/unittest/fr_xxx/brand/fr",
            "/content/unittest/de_test/brand/de/section/page"));
    assertEquals("site-othermarket-2", "/content/unittest/de_test/brand/de/section/page",
        urlHandler.rewritePathToContext("/content/unittest/en_yyy/brand/en/section/page",
            "/content/unittest/de_test/brand/de/section/page"));
    assertEquals("site-othermarket-3", "/content/unittest/de_test/brand/de/somewhat/other/page",
        urlHandler.rewritePathToContext("/content/unittest/it/brand/it_123/somewhat/other/page",
            "/content/unittest/de_test/brand/de/section/page"));
    assertEquals("site-othermarket-4", "/content/unittest/de_test/brand/de/somewhat/other/page",
        urlHandler.rewritePathToContext("/content/otherunittest/it/brand/it_123/somewhat/other/page",
            "/content/unittest/de_test/brand/de/section/page"));
    assertEquals("site-othermarket-5", "/content/otherunittest/it/brand/it_123/somewhat/other/page",
        urlHandler.rewritePathToContext("/content/otherunittest/it/brand/it_123/somewhat/other/page", null));

    // invalid paths
    assertEquals("invalid-1", "/content/unittest/xxx",
        urlHandler.rewritePathToContext("/content/unittest/xxx",
            "/content/unittest/de_test/brand/de/section/page"));
    assertEquals("invalid-2", "/content/xxx",
        urlHandler.rewritePathToContext("/content/xxx",
            "/content/unittest/de_test/brand/de/section/page"));
    assertEquals("invalid-3", "/etc/aa/bb/cc/dd/ee/ff",
        urlHandler.rewritePathToContext("/etc/aa/bb/cc/dd/ee/ff",
            "/content/unittest/de_test/brand/de/section/page"));
    assertEquals("invalid-4", "/content/unittest",
        urlHandler.rewritePathToContext("/content/unittest",
            "/content/unittest/de_test/brand/de/section/page"));
    assertEquals("invalid-5", "/content",
        urlHandler.rewritePathToContext("/content",
            "/content/unittest/de_test/brand/de/section/page"));
    assertEquals("invalid-6", "",
        urlHandler.rewritePathToContext("",
            "/content/unittest/de_test/brand/de/section/page"));
    assertNull("invalid-7", urlHandler.rewritePathToContext(null,
        "/content/unittest/de_test/brand/de/section/page"));
    assertNull("invalid-8", urlHandler.rewritePathToContext(null, null));

  }

  /**
   * Test UrlHandler#externalizeLinkUrl(String, com.day.cq.wcm.api.Page, io.wcm.handler.url.UrlMode)
   */
  @Test
  public void testExternalizeLinkUrl() {
    // create current page in site context
    context.currentPage(context.create().page("/content/unittest/de_test/brand/de/section/page",
        DummyAppTemplate.CONTENT.getTemplatePath()));

    // create more pages to simulate internal link
    Page targetPage = context.create().page("/content/unittest/de_test/brand/de/section2/page2",
        DummyAppTemplate.CONTENT.getTemplatePath());
    Page secureTargetPage = context.create().page("/content/unittest/de_test/brand/de/section2/page3",
        DummyAppTemplate.CONTENT_SECURE.getTemplatePath());

    UrlHandler urlHandler = AdaptTo.notNull(adaptable(), UrlHandler.class);

    // invalid parameters
    assertEquals(null, externalizeLinkUrl(urlHandler, null, null));
    assertEquals(null, externalizeLinkUrl(urlHandler, null, targetPage));
    assertEquals(null, externalizeLinkUrl(urlHandler, "", null));
    assertEquals(null, externalizeLinkUrl(urlHandler, "", targetPage));

    // urls that are already externalized
    assertEquals("http://xyz/abc", externalizeLinkUrl(urlHandler, "http://xyz/abc", null));
    assertEquals("http://xyz/abc", externalizeLinkUrl(urlHandler, "http://xyz/abc", targetPage));
    assertEquals("ftp://xyz/abc", externalizeLinkUrl(urlHandler, "ftp://xyz/abc", null));
    assertEquals("ftp://xyz/abc", externalizeLinkUrl(urlHandler, "ftp://xyz/abc", targetPage));
    assertEquals("//xyz/abc", externalizeLinkUrl(urlHandler, "//xyz/abc", null));
    assertEquals("//xyz/abc", externalizeLinkUrl(urlHandler, "//xyz/abc", targetPage));
    assertEquals("mailto:aa@bb.cc", externalizeLinkUrl(urlHandler, "mailto:aa@bb.cc", null));
    assertEquals("mailto:aa@bb.cc", externalizeLinkUrl(urlHandler, "mailto:aa@bb.cc", targetPage));
    assertEquals("#anchor", externalizeLinkUrl(urlHandler, "#anchor", null));
    assertEquals("#anchor", externalizeLinkUrl(urlHandler, "#anchor", targetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT, externalizeLinkUrl(urlHandler, IntegratorPlaceholder.URL_CONTENT, null));
    assertEquals(IntegratorPlaceholder.URL_CONTENT, externalizeLinkUrl(urlHandler, IntegratorPlaceholder.URL_CONTENT, targetPage));

    // simple externalization
    assertEquals("http://de.dummysite.org/content/unittest/de_test/brand/de/section2/page2.html",
        externalizeLinkUrl(urlHandler, targetPage.getPath() + ".html", null));
    assertEquals("http://de.dummysite.org/content/unittest/de_test/brand/de/section2/page2.html",
        externalizeLinkUrl(urlHandler, targetPage.getPath() + ".html", targetPage));
    assertEquals("http://de.dummysite.org/content/unittest/de_test/brand/de/section2/page2/_jcr_content.html",
        externalizeLinkUrl(urlHandler, targetPage.getContentResource().getPath() + ".html", targetPage));
    assertEquals("/content/unittest/de_test/brand/de/section2/page2/_jcr_content.html",
        externalizeLinkUrl(urlHandler, targetPage.getContentResource().getPath() + ".html", targetPage, UrlModes.NO_HOSTNAME));
    assertEquals("http://de.dummysite.org/content/unittest/de_test/brand/de/section2/page3.html",
        externalizeLinkUrl(urlHandler, secureTargetPage.getPath() + ".html", null));
    assertEquals("https://de.dummysite.org/content/unittest/de_test/brand/de/section2/page3.html",
        externalizeLinkUrl(urlHandler, secureTargetPage.getPath() + ".html", secureTargetPage));
    assertEquals("/content/unittest/de_test/brand/de/section2/page3.html",
        externalizeLinkUrl(urlHandler, secureTargetPage.getPath() + ".html", secureTargetPage, UrlModes.NO_HOSTNAME));

    // test with query params and fragment
    assertEquals("http://de.dummysite.org/content/unittest/de_test/brand/de/section2/page2.html?param=1",
        externalizeLinkUrl(urlHandler, targetPage.getPath() + ".html?param=1", null));
    assertEquals("http://de.dummysite.org/content/unittest/de_test/brand/de/section2/page2.html#hash",
        externalizeLinkUrl(urlHandler, targetPage.getPath() + ".html#hash", null));

    if (context.request() != null) {
      // externalization with context path and mapping
      MockSlingHttpServletRequest request = applySimpleMapping(context.request());
      request.setContextPath("/cms");
      urlHandler = AdaptTo.notNull(request, UrlHandler.class);

      assertEquals("http://de.dummysite.org/cms/de/section2/page2.html",
          externalizeLinkUrl(urlHandler, targetPage.getPath() + ".html", null));
      assertEquals("http://de.dummysite.org/cms/de/section2/page2.html",
          externalizeLinkUrl(urlHandler, targetPage.getPath() + ".html", targetPage));
      assertEquals("http://de.dummysite.org/cms/de/section2/page2/_jcr_content.html",
          externalizeLinkUrl(urlHandler, targetPage.getContentResource().getPath() + ".html", targetPage));
      assertEquals("/cms/de/section2/page2/_jcr_content.html",
          externalizeLinkUrl(urlHandler, targetPage.getContentResource().getPath() + ".html", targetPage, UrlModes.NO_HOSTNAME));
      assertEquals("http://de.dummysite.org/cms/de/section2/page3.html",
          externalizeLinkUrl(urlHandler, secureTargetPage.getPath() + ".html", null));
      assertEquals("https://de.dummysite.org/cms/de/section2/page3.html",
          externalizeLinkUrl(urlHandler, secureTargetPage.getPath() + ".html", secureTargetPage));
      assertEquals("/cms/de/section2/page3.html",
          externalizeLinkUrl(urlHandler, secureTargetPage.getPath() + ".html", secureTargetPage, UrlModes.NO_HOSTNAME));

      // externalization with context path and mapping with page from other site as current page
      request = applySimpleMapping(context.request());
      request.setContextPath(null);
      urlHandler = AdaptTo.notNull(request, UrlHandler.class);
      context.currentPage(context.create().page("/content/unittest/de_test/brand/en/section/page",
          DummyAppTemplate.CONTENT.getTemplatePath()));

      assertEquals("http://de.dummysite.org/de/section2/page2.html",
          externalizeLinkUrl(urlHandler, targetPage.getPath() + ".html", null));
      assertEquals("http://de.dummysite.org/de/section2/page2.html",
          externalizeLinkUrl(urlHandler, targetPage.getPath() + ".html", targetPage));
      assertEquals("http://de.dummysite.org/de/section2/page2/_jcr_content.html",
          externalizeLinkUrl(urlHandler, targetPage.getContentResource().getPath() + ".html", targetPage));
      assertEquals("/de/section2/page2/_jcr_content.html",
          externalizeLinkUrl(urlHandler, targetPage.getContentResource().getPath() + ".html", targetPage, UrlModes.NO_HOSTNAME));
      assertEquals("http://de.dummysite.org/de/section2/page3.html",
          externalizeLinkUrl(urlHandler, secureTargetPage.getPath() + ".html", null));
      assertEquals("https://de.dummysite.org/de/section2/page3.html",
          externalizeLinkUrl(urlHandler, secureTargetPage.getPath() + ".html", secureTargetPage));
      assertEquals("/de/section2/page3.html",
          externalizeLinkUrl(urlHandler, secureTargetPage.getPath() + ".html", secureTargetPage, UrlModes.NO_HOSTNAME));
    }

  }

  /**
   * Test UrlHandler#externalizeResourceUrl(String, io.wcm.handler.url.UrlMode)
   */
  @Test
  public void testExternalizeResourceUrl() {
    // create current page in site context
    context.currentPage(context.create().page("/content/unittest/de_test/brand/de/section/page",
        DummyAppTemplate.CONTENT.getTemplatePath()));

    // create more pages to simulate internal link
    Page targetPage = context.create().page("/content/unittest/de_test/brand/de/section2/page2",
        DummyAppTemplate.CONTENT.getTemplatePath());

    UrlHandler urlHandler = AdaptTo.notNull(adaptable(), UrlHandler.class);

    // invalid parameters
    assertEquals(null, externalizeResourceUrl(urlHandler, null));
    assertEquals(null, externalizeResourceUrl(urlHandler, ""));

    // urls that are already externalized
    assertEquals("http://xyz/abc", externalizeResourceUrl(urlHandler, "http://xyz/abc"));
    assertEquals("ftp://xyz/abc", externalizeResourceUrl(urlHandler, "ftp://xyz/abc"));
    assertEquals("//xyz/abc", externalizeResourceUrl(urlHandler, "//xyz/abc"));
    assertEquals("mailto:aa@bb.cc", externalizeResourceUrl(urlHandler, "mailto:aa@bb.cc"));
    assertEquals("#anchor", externalizeResourceUrl(urlHandler, "#anchor"));
    assertEquals(IntegratorPlaceholder.URL_CONTENT, externalizeResourceUrl(urlHandler, IntegratorPlaceholder.URL_CONTENT));

    // simple externalization
    assertEquals("/apps/testapp/docroot/img.png",
        externalizeResourceUrl(urlHandler, "/apps/testapp/docroot/img.png"));
    assertEquals("http://de.dummysite.org/apps/testapp/docroot/img.png",
        externalizeResourceUrl(urlHandler, "/apps/testapp/docroot/img.png", UrlModes.FULL_URL));
    assertEquals("/content/unittest/de_test/brand/de/section2/page2.png",
        externalizeResourceUrl(urlHandler, targetPage.getPath() + ".png"));
    assertEquals("/content/unittest/de_test/brand/de/section2/page2/_jcr_content.png",
        externalizeResourceUrl(urlHandler, targetPage.getContentResource().getPath() + ".png"));
    assertEquals("http://de.dummysite.org/content/unittest/de_test/brand/de/section2/page2/_jcr_content.png",
        externalizeResourceUrl(urlHandler, targetPage.getContentResource().getPath() + ".png", UrlModes.FULL_URL));

    // with query parameter or fragment
    assertEquals("/apps/testapp/docroot/img.png?param=1",
        externalizeResourceUrl(urlHandler, "/apps/testapp/docroot/img.png?param=1"));
    assertEquals("/apps/testapp/docroot/img.png#hash",
        externalizeResourceUrl(urlHandler, "/apps/testapp/docroot/img.png#hash"));

    if (context.request() != null) {

      // externalization with context path and mapping
      MockSlingHttpServletRequest request = applySimpleMapping(context.request());
      request.setContextPath("/cms");
      urlHandler = AdaptTo.notNull(request, UrlHandler.class);

      assertEquals("/cms/apps/testapp/docroot/img.png",
          externalizeResourceUrl(urlHandler, "/apps/testapp/docroot/img.png"));
      assertEquals("http://de.dummysite.org/cms/apps/testapp/docroot/img.png",
          externalizeResourceUrl(urlHandler, "/apps/testapp/docroot/img.png", UrlModes.FULL_URL));
      assertEquals("/cms/de/section2/page2.png",
          externalizeResourceUrl(urlHandler, targetPage.getPath() + ".png"));
      assertEquals("/cms/de/section2/page2/_jcr_content.png",
          externalizeResourceUrl(urlHandler, targetPage.getContentResource().getPath() + ".png"));
      assertEquals("http://de.dummysite.org/cms/de/section2/page2/_jcr_content.png",
          externalizeResourceUrl(urlHandler, targetPage.getContentResource().getPath() + ".png", UrlModes.FULL_URL));

      // externalization with context path and mapping with page from other site as current page
      context.currentPage(context.create().page("/content/unittest/de_test/brand/en/section/page",
          DummyAppTemplate.CONTENT.getTemplatePath()));
      request = applySimpleMapping(context.request());
      request.setContextPath(null);
      urlHandler = AdaptTo.notNull(request, UrlHandler.class);

      assertEquals("/apps/testapp/docroot/img.png",
          externalizeResourceUrl(urlHandler, "/apps/testapp/docroot/img.png"));
      assertEquals("http://en.dummysite.org/apps/testapp/docroot/img.png",
          externalizeResourceUrl(urlHandler, "/apps/testapp/docroot/img.png", UrlModes.FULL_URL));
      assertEquals("/content/unittest/de_test/brand/de/section2/page2.png",
          externalizeResourceUrl(urlHandler, targetPage.getPath() + ".png"));
      assertEquals("/content/unittest/de_test/brand/de/section2/page2/_jcr_content.png",
          externalizeResourceUrl(urlHandler, targetPage.getContentResource().getPath() + ".png"));
      assertEquals("http://en.dummysite.org/content/unittest/de_test/brand/de/section2/page2/_jcr_content.png",
          externalizeResourceUrl(urlHandler, targetPage.getContentResource().getPath() + ".png", UrlModes.FULL_URL));

    }

  }

  /**
   * Test UrlHandler#buildUrl(String, String, String, String)
   */
  @Test
  public void testBuildUrl() {
    UrlHandler urlHandler = AdaptTo.notNull(adaptable(), UrlHandler.class);

    // permuations
    assertEquals("/the/path.ext", buildUrl(urlHandler, "/the/path", null, "ext", null));
    assertEquals("/the/path.selector.ext", buildUrl(urlHandler, "/the/path", "selector", "ext", null));
    assertEquals("/the/path.selector.ext", buildUrl(urlHandler, "/the/path", ".selector", "ext", null));
    assertEquals("/the/path.sel1.sel2.ext", buildUrl(urlHandler, "/the/path", "sel1.sel2", "ext", null));
    assertEquals("/the/path.suffix.ext/suffix1.ext", buildUrl(urlHandler, "/the/path", null, "ext", "suffix1"));
    assertEquals("/the/path.selector.suffix.ext/suffix1.ext", buildUrl(urlHandler, "/the/path", "selector", "ext", "suffix1"));
    assertEquals("/the/path.selector.suffix.ext/suffix1.ext", buildUrl(urlHandler, "/the/path", ".selector", "ext", "suffix1"));
    assertEquals("/the/path.sel1.sel2.suffix.ext/suffix1.ext", buildUrl(urlHandler, "/the/path", "sel1.sel2", "ext", "suffix1"));
    assertEquals("/the/path.suffix.ext/suffix2.ext", buildUrl(urlHandler, "/the/path", null, "ext", "/suffix2"));
    assertEquals("/the/path.selector.suffix.ext/suffix2.ext", buildUrl(urlHandler, "/the/path", "selector", "ext", "/suffix2"));
    assertEquals("/the/path.selector.suffix.ext/suffix2.ext", buildUrl(urlHandler, "/the/path", ".selector", "ext", "/suffix2"));
    assertEquals("/the/path.sel1.sel2.suffix.ext/suffix2.ext", buildUrl(urlHandler, "/the/path", "sel1.sel2", "ext", "/suffix2"));
    assertEquals("/the/path.suffix.ext/suffix3/suffix4.ext", buildUrl(urlHandler, "/the/path", null, "ext", "suffix3/suffix4"));
    assertEquals("/the/path.selector.suffix.ext/suffix3/suffix4.ext", buildUrl(urlHandler, "/the/path", "selector", "ext", "suffix3/suffix4"));
    assertEquals("/the/path.selector.suffix.ext/suffix3/suffix4.ext", buildUrl(urlHandler, "/the/path", ".selector", "ext", "suffix3/suffix4"));
    assertEquals("/the/path.sel1.sel2.suffix.ext/suffix3/suffix4.ext", buildUrl(urlHandler, "/the/path", "sel1.sel2", "ext", "suffix3/suffix4"));
    assertEquals("/the/path.suffix.ext/suffix1.myext", buildUrl(urlHandler, "/the/path", null, "ext", "suffix1.myext"));
    assertEquals("/the/path.selector.suffix.ext/suffix1.myext", buildUrl(urlHandler, "/the/path", "selector", "ext", "suffix1.myext"));
    assertEquals("/the/path.selector.suffix.ext/suffix1.myext", buildUrl(urlHandler, "/the/path", ".selector", "ext", "suffix1.myext"));
    assertEquals("/the/path.sel1.sel2.suffix.ext/suffix1.myext", buildUrl(urlHandler, "/the/path", "sel1.sel2", "ext", "suffix1.myext"));
    assertEquals("/the/path.suffix.ext/suffix2.myext", buildUrl(urlHandler, "/the/path", null, "ext", "/suffix2.myext"));
    assertEquals("/the/path.selector.suffix.ext/suffix2.myext", buildUrl(urlHandler, "/the/path", "selector", "ext", "/suffix2.myext"));
    assertEquals("/the/path.selector.suffix.ext/suffix2.myext", buildUrl(urlHandler, "/the/path", ".selector", "ext", "/suffix2.myext"));
    assertEquals("/the/path.sel1.sel2.suffix.ext/suffix2.myext", buildUrl(urlHandler, "/the/path", "sel1.sel2", "ext", "/suffix2.myext"));
    assertEquals("/the/path.suffix.ext/suffix3/suffix4.myext", buildUrl(urlHandler, "/the/path", null, "ext", "suffix3/suffix4.myext"));
    assertEquals("/the/path.selector.suffix.ext/suffix3/suffix4.myext", buildUrl(urlHandler, "/the/path", "selector", "ext", "suffix3/suffix4.myext"));
    assertEquals("/the/path.selector.suffix.ext/suffix3/suffix4.myext", buildUrl(urlHandler, "/the/path", ".selector", "ext", "suffix3/suffix4.myext"));
    assertEquals("/the/path.sel1.sel2.suffix.ext/suffix3/suffix4.myext", buildUrl(urlHandler, "/the/path", "sel1.sel2", "ext", "suffix3/suffix4.myext"));

    // invalid arguments
    assertNull(buildUrl(urlHandler, null, null, null, null));
    assertEquals("/the/path", buildUrl(urlHandler, "/the/path", null, null, null));
    assertNull(buildUrl(urlHandler, null, null, "ext", null));
    assertNull(buildUrl(urlHandler, "", "", "", ""));

  }

  /**
   * Test UrlHandler#appendQueryString(String, String)
   */
  @Test
  public void testAppendQueryString() {
    UrlHandler urlHandler = AdaptTo.notNull(adaptable(), UrlHandler.class);

    assertEquals("/the/path", appendQueryString(urlHandler, "/the/path", null));
    assertEquals("/the/path", appendQueryString(urlHandler, "/the/path", ""));
    assertEquals("/the/path?abc", appendQueryString(urlHandler, "/the/path", "abc"));
    assertEquals("/the/path?param1=xyz", appendQueryString(urlHandler, "/the/path", "param1=xyz"));

    assertEquals("/the/path?def=ghi", appendQueryString(urlHandler, "/the/path?def=ghi", null));
    assertEquals("/the/path?def=ghi", appendQueryString(urlHandler, "/the/path?def=ghi", ""));
    assertEquals("/the/path?def=ghi&abc", appendQueryString(urlHandler, "/the/path?def=ghi", "abc"));
    assertEquals("/the/path?def=ghi&param1=xyz", appendQueryString(urlHandler, "/the/path?def=ghi", "param1=xyz"));

    assertEquals("/the/path?def=ghi&p1=ab&p1=cd", appendQueryString(urlHandler, "/the/path?def=ghi", "p1=ab&p1=cd"));
    assertEquals("/the/path?def=ghi&def=jkl&p1=ab&p1=cd", appendQueryString(urlHandler, "/the/path?def=ghi&def=jkl", "p1=ab&p1=cd"));

    // invalid url
    assertEquals(null, appendQueryString(urlHandler, null, null));
    assertEquals(null, appendQueryString(urlHandler, null, "aa"));
    assertEquals(null, appendQueryString(urlHandler, "", null));
    assertEquals(null, appendQueryString(urlHandler, "", "aa"));

  }

  /**
   * Test UrlHandler#appendQueryString(String, String, java.util.Set)
   */
  @Test
  public void testAppendQueryStringWithInheritance() {
    UrlHandler urlHandler = AdaptTo.notNull(adaptable(), UrlHandler.class);

    Set<String> params = ImmutableSet.of("i1", "i2");

    // test without request url params
    assertEquals("/the/path", appendQueryString(urlHandler, "/the/path", null, params));
    assertEquals("/the/path", appendQueryString(urlHandler, "/the/path", "", params));
    assertEquals("/the/path?abc", appendQueryString(urlHandler, "/the/path", "abc", params));
    assertEquals("/the/path?param1=xyz", appendQueryString(urlHandler, "/the/path", "param1=xyz", params));

    assertEquals("/the/path?def=ghi", appendQueryString(urlHandler, "/the/path?def=ghi", null, params));
    assertEquals("/the/path?def=ghi", appendQueryString(urlHandler, "/the/path?def=ghi", "", params));
    assertEquals("/the/path?def=ghi&abc", appendQueryString(urlHandler, "/the/path?def=ghi", "abc", params));
    assertEquals("/the/path?def=ghi&param1=xyz", appendQueryString(urlHandler, "/the/path?def=ghi", "param1=xyz", params));

    assertEquals(null, appendQueryString(urlHandler, null, null, params));
    assertEquals(null, appendQueryString(urlHandler, null, "aa", params));
    assertEquals(null, appendQueryString(urlHandler, "", null, params));
    assertEquals(null, appendQueryString(urlHandler, "", "aa", params));

    if (adaptable() instanceof SlingHttpServletRequest) {
      // test with request url params
      context.request().setQueryString("abc=xyz&i1=123&i1=456&def=jkl&def=mno");

      assertEquals("/the/path?i1=123&i1=456", appendQueryString(urlHandler, "/the/path", null, params));
      assertEquals("/the/path?i1=123&i1=456", appendQueryString(urlHandler, "/the/path", "", params));
      assertEquals("/the/path?abc&i1=123&i1=456", appendQueryString(urlHandler, "/the/path", "abc", params));
      assertEquals("/the/path?param1=xyz&i1=123&i1=456", appendQueryString(urlHandler, "/the/path", "param1=xyz", params));

      assertEquals("/the/path?def=ghi&i1=123&i1=456", appendQueryString(urlHandler, "/the/path?def=ghi", null, params));
      assertEquals("/the/path?def=ghi&i1=123&i1=456", appendQueryString(urlHandler, "/the/path?def=ghi", "", params));
      assertEquals("/the/path?def=ghi&abc&i1=123&i1=456", appendQueryString(urlHandler, "/the/path?def=ghi", "abc", params));
      assertEquals("/the/path?def=ghi&param1=xyz&i1=123&i1=456", appendQueryString(urlHandler, "/the/path?def=ghi", "param1=xyz", params));

      assertEquals("/the/path?def=ghi&i1=4567&param1=xyz", appendQueryString(urlHandler, "/the/path?def=ghi&i1=4567", "param1=xyz", params));

      assertEquals(null, appendQueryString(urlHandler, null, null, params));
      assertEquals(null, appendQueryString(urlHandler, null, "aa", params));
      assertEquals(null, appendQueryString(urlHandler, "", null, params));
      assertEquals(null, appendQueryString(urlHandler, "", "aa", params));
    }

  }

  /**
   * Test UrlHandler#setFragment(String, String)
   */
  @Test
  public void testSetAnchor() {
    UrlHandler urlHandler = AdaptTo.notNull(adaptable(), UrlHandler.class);

    // test invalid parameters
    assertEquals(null, setFragment(urlHandler, null, null));
    assertEquals(null, setFragment(urlHandler, null, ""));
    assertEquals(null, setFragment(urlHandler, "", null));
    assertEquals(null, setFragment(urlHandler, "", ""));
    assertEquals("/the/path", setFragment(urlHandler, "/the/path", null));
    assertEquals("/the/path", setFragment(urlHandler, "/the/path", ""));

    // test valid parameters
    assertEquals("/the/path#anchor", setFragment(urlHandler, "/the/path", "anchor"));
    assertEquals("/the/path#anchor", setFragment(urlHandler, "/the/path", "#anchor"));
    assertEquals("/the/path#anchor", setFragment(urlHandler, "/the/path#othernachor", "#anchor"));

  }

  private static String externalizeLinkUrl(UrlHandler urlHandler, String url, Page targetPage) {
    return urlHandler.get(url).buildExternalLinkUrl(targetPage);
  }

  private static String externalizeLinkUrl(UrlHandler urlHandler, String url, Page targetPage, UrlMode urlMode) {
    return urlHandler.get(url).urlMode(urlMode).buildExternalLinkUrl(targetPage);
  }

  private static String externalizeResourceUrl(UrlHandler urlHandler, String url) {
    return urlHandler.get(url).buildExternalResourceUrl();
  }

  private static String externalizeResourceUrl(UrlHandler urlHandler, String url, UrlMode urlMode) {
    return urlHandler.get(url).urlMode(urlMode).buildExternalResourceUrl();
  }

  private static String buildUrl(UrlHandler urlHandler, String path, String selectors, String extension, String suffix) {
    return urlHandler.get(path).selectors(selectors).extension(extension).suffix(suffix).build();
  }

  private static String appendQueryString(UrlHandler urlHandler, String path, String queryString) {
    return urlHandler.get(path).queryString(queryString).build();
  }

  private static String appendQueryString(UrlHandler urlHandler, String path, String queryString,
      Set<String> inheritableParameterNames) {
    return urlHandler.get(path).queryString(queryString, inheritableParameterNames).build();
  }

  private static String setFragment(UrlHandler urlHandler, String path, String fragment) {
    return urlHandler.get(path).fragment(fragment).build();
  }

  /**
   * Simulate mapping:
   * - /content/unittest/de_test/brand/de -> /de
   * - /content/* -> /*
   */
  private static MockSlingHttpServletRequest applySimpleMapping(SlingHttpServletRequest request) {
    ResourceResolver spyResolver = spy(request.getResourceResolver());
    Answer<String> mappingAnswer = new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) {
        SlingHttpServletRequest mapRequest;
        String path;
        if (invocation.getArguments()[0] instanceof SlingHttpServletRequest) {
          mapRequest = (SlingHttpServletRequest)invocation.getArguments()[0];
          path = (String)invocation.getArguments()[1];
        }
        else {
          mapRequest = null;
          path = (String)invocation.getArguments()[0];
        }
        if (StringUtils.startsWith(path, "/content/unittest/de_test/brand/")) {
          path = "/" + StringUtils.substringAfter(path, "/content/unittest/de_test/brand/");
        }
        if (StringUtils.startsWith(path, "/content/")) {
          path = "/" + StringUtils.substringAfter(path, "/content/");
        }
        if (mapRequest != null) {
          path = StringUtils.defaultString(mapRequest.getContextPath()) + path;
        }
        path = Externalizer.mangleNamespaces(path);
        try {
          return URLEncoder.encode(path, CharEncoding.UTF_8);
        }
        catch (UnsupportedEncodingException ex) {
          throw new RuntimeException(ex);
        }
      }
    };
    when(spyResolver.map(anyString())).thenAnswer(mappingAnswer);
    when(spyResolver.map(any(SlingHttpServletRequest.class), anyString())).thenAnswer(mappingAnswer);
    MockSlingHttpServletRequest newRequest = new MockSlingHttpServletRequest(spyResolver);
    newRequest.setResource(request.getResource());
    newRequest.setContextPath(request.getContextPath());
    return newRequest;
  }

}
