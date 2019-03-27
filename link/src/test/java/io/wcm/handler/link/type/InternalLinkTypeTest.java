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
package io.wcm.handler.link.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.handler.link.testcontext.DummyAppTemplate;
import io.wcm.handler.url.UrlModes;
import io.wcm.handler.url.integrator.IntegratorModes;
import io.wcm.handler.url.integrator.IntegratorNameConstants;
import io.wcm.handler.url.integrator.IntegratorProtocol;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Test {@link InternalLinkType} methods.
 */
@ExtendWith(AemContextExtension.class)
class InternalLinkTypeTest {

  final AemContext context = AppAemContext.newAemContext();

  private Page targetPage;

  protected Adaptable adaptable() {
    return context.request();
  }

  @BeforeEach
  void setUp() throws Exception {

    // create current page in site context
    context.currentPage(context.create().page("/content/unittest/de_test/brand/de/section/page",
        DummyAppTemplate.CONTENT.getTemplatePath()));

    // create internal pages for unit tests
    targetPage = context.create().page("/content/unittest/de_test/brand/de/section/content",
        DummyAppTemplate.CONTENT.getTemplatePath());

  }

  @Test
  void testEmptyLink() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertFalse(link.isValid(), "link valid");
    assertFalse(link.isLinkReferenceInvalid(), "link ref invalid");
    assertNull(link.getUrl(), "link url");
    assertNull(link.getAnchor(), "anchor");
  }

  @Test
  void testEmptyLink_EditMode() {
    WCMMode.EDIT.toRequest(context.request());

    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
            .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertFalse(link.isValid(), "link valid");
    assertFalse(link.isLinkReferenceInvalid(), "link ref invalid");
    assertNull(link.getUrl(), "link url");
    assertNull(link.getAnchor(), "anchor");
  }

  @Test
  void testInvalidLink() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, "/invalid/content/path")
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertFalse(link.isValid(), "link valid");
    assertTrue(link.isLinkReferenceInvalid(), "link ref invalid");
    assertNull(link.getUrl(), "link url");
    assertNull(link.getAnchor(), "anchor");
  }

  @Test
  void testInvalidLink_EditMode() {
    if (!(adaptable() instanceof SlingHttpServletRequest)) {
      return;
    }

    WCMMode.EDIT.toRequest(context.request());

    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, "/invalid/content/path")
        .build());

    Link link = linkHandler.get(linkResource).dummyLink(true).build();

    assertFalse(link.isValid(), "link valid");
    assertTrue(link.isLinkReferenceInvalid(), "link ref invalid");
    assertNull(link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
    assertEquals(LinkHandler.INVALID_LINK, link.getAnchor().getHRef(), "anchor.href");
  }

  @Test
  void testTargetPage() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertTrue(link.isValid(), "link valid");
    assertFalse(link.isLinkReferenceInvalid(), "link ref invalid");
    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html", link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testStructureElement() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    Page structureElementPage = context.create().page("/content/unittest/de_test/brand/de/section/structureElement",
        DummyAppTemplate.STRUCTURE_ELEMENT.getTemplatePath());

    Link link = linkHandler.get(structureElementPage).build();

    assertFalse(link.isValid(), "link valid");
    assertNull(link.getUrl(), "link url");
    assertNull(link.getAnchor(), "anchor");
  }

  @Test
  void testSecureTargetPage() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    Page secureTargetPage = context.create().page("/content/unittest/de_test/brand/de/section/contentSecure",
        DummyAppTemplate.CONTENT_SECURE.getTemplatePath());

    Link link = linkHandler.get(secureTargetPage).build();

    assertTrue(link.isValid(), "link valid");
    assertEquals("https://www.dummysite.org/content/unittest/de_test/brand/de/section/contentSecure.html", link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testRedirectInternal() throws Exception {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    Page redirectInternalPage = context.create().page("/content/unittest/de_test/brand/de/section/redirectInternal",
        DummyAppTemplate.REDIRECT.getTemplatePath(), ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .build());

    Link link = linkHandler.get(redirectInternalPage).build();

    assertTrue(link.isValid(), "link valid");
    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html", link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testRedirectInternal_EditMode() throws Exception {
    if (!(adaptable() instanceof SlingHttpServletRequest)) {
      return;
    }

    WCMMode.EDIT.toRequest(context.request());
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    Page redirectInternalPage = context.create().page("/content/unittest/de_test/brand/de/section/redirectInternal",
        DummyAppTemplate.REDIRECT.getTemplatePath(), ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .build());

    Link link = linkHandler.get(redirectInternalPage).build();

    assertTrue(link.isValid(), "link valid");
    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/redirectInternal.html", link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testRedirectRedirectInternal() throws Exception {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    Page redirectInternalPage = context.create().page("/content/unittest/de_test/brand/de/section/redirectInternal",
        DummyAppTemplate.REDIRECT.getTemplatePath(), ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .build());

    Page redirectRedirectInternalPage = context.create().page("/content/unittest/de_test/brand/de/section/redirectRedirectInternal",
        DummyAppTemplate.REDIRECT.getTemplatePath(), ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, redirectInternalPage.getPath())
        .build());

    Link link = linkHandler.get(redirectRedirectInternalPage).build();

    assertTrue(link.isValid(), "link valid");
    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html", link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testRedirectExternal() throws Exception {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    Page redirectExternalPage = context.create().page("/content/unittest/de_test/brand/de/section/redirectExternal",
        DummyAppTemplate.REDIRECT.getTemplatePath(), ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_EXTERNAL_REF, "http://xyz/abc")
        .build());

    Link link = linkHandler.get(redirectExternalPage).build();

    assertTrue(link.isValid(), "link valid");
    assertEquals("http://xyz/abc", link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testRedirectCyclic() throws Exception {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    String redirectInternalCyclic1Path = "/content/unittest/de_test/brand/de/section/redirectInternalCyclic1";
    String redirectInternalCyclic2Path = "/content/unittest/de_test/brand/de/section/redirectInternalCyclic2";

    Page redirectInternalCyclic1Page = context.create().page(redirectInternalCyclic1Path,
        DummyAppTemplate.REDIRECT.getTemplatePath(), ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, redirectInternalCyclic2Path)
        .build());
    Page redirectInternalCyclic2Page = context.create().page(redirectInternalCyclic2Path,
        DummyAppTemplate.REDIRECT.getTemplatePath(), ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, redirectInternalCyclic1Path)
        .build());

    Link link = linkHandler.get(redirectInternalCyclic1Page).build();

    assertFalse(link.isValid(), "link valid");
    assertNull(link.getUrl(), "link url");
    assertNull(link.getAnchor(), "anchor");

    link = linkHandler.get(redirectInternalCyclic2Page).build();

    assertFalse(link.isValid(), "link valid");
    assertNull(link.getUrl(), "link url");
    assertNull(link.getAnchor(), "anchor");
  }

  @Test
  void testIntegrator() throws Exception {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    Page integratorPage = context.create().page("/content/unittest/de_test/brand/de/section/integrator",
        DummyAppTemplate.INTEGRATOR.getTemplatePath(), ImmutableValueMap.builder()
        .put(IntegratorNameConstants.PN_INTEGRATOR_MODE, IntegratorModes.SIMPLE.getId())
        .put(IntegratorNameConstants.PN_INTEGRATOR_PROTOCOL, IntegratorProtocol.HTTP.name())
        .put(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_EXTERNAL_REF, "http://xyz/app")
        .build());

    Link link = linkHandler.get(integratorPage).build();

    assertTrue(link.isValid(), "link valid");
    assertEquals("http://xyz/app", link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testIntegrator_EditMode() throws Exception {
    if (!(adaptable() instanceof SlingHttpServletRequest)) {
      return;
    }

    WCMMode.EDIT.toRequest(context.request());
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    Page integratorPage = context.create().page("/content/unittest/de_test/brand/de/section/integrator",
        DummyAppTemplate.INTEGRATOR.getTemplatePath(), ImmutableValueMap.builder()
        .put(IntegratorNameConstants.PN_INTEGRATOR_MODE, IntegratorModes.SIMPLE.getId())
        .put(IntegratorNameConstants.PN_INTEGRATOR_PROTOCOL, IntegratorProtocol.HTTP.name())
        .put(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_EXTERNAL_REF, "http://xyz/app")
        .build());

    Link link = linkHandler.get(integratorPage).build();

    assertTrue(link.isValid(), "link valid");
    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/integrator.html", link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testTargetPageOtherSite() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, "/content/unittest/en_test/brand/en/section/content")
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertTrue(link.isValid(), "link valid");
    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html",
        link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testTargetPageLinkUrlVariants() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html",
        linkHandler.get(targetPage).buildUrl());

    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.sel1.html",
        linkHandler.get(targetPage).selectors("sel1").buildUrl());

    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.htx",
        linkHandler.get(targetPage).extension(FileExtension.HTML_UNCACHED).buildUrl());

    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.sel1.htx",
        linkHandler.get(targetPage).selectors("sel1").extension(FileExtension.HTML_UNCACHED).buildUrl());

    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.sel1.suffix.htx/suf1/suf2.htx",
        linkHandler.get(targetPage).selectors("sel1").extension(FileExtension.HTML_UNCACHED).suffix("suf1/suf2").buildUrl());

    assertEquals("/content/unittest/de_test/brand/de/section/content.sel1.suffix.htx/suf1/suf2.htx",
        linkHandler.get(targetPage).selectors("sel1").extension(FileExtension.HTML_UNCACHED).suffix("suf1/suf2").urlMode(UrlModes.NO_HOSTNAME).buildUrl());
  }

  @Test
  void testTargetPageWithQueryParams_Resource() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .put(LinkNameConstants.PN_LINK_QUERY_PARAM, "p1=abc&p2=def")
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertTrue(link.isValid(), "link valid");
    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html?p1=abc&p2=def",
        link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testTargetPageWithFragment_Resource() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .put(LinkNameConstants.PN_LINK_FRAGMENT, "anchor1")
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertTrue(link.isValid(), "link valid");
    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html#anchor1",
        link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testTargetPageWithQueryParamsFragment_Resource() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .put(LinkNameConstants.PN_LINK_QUERY_PARAM, "p1=abc&p2=def")
        .put(LinkNameConstants.PN_LINK_FRAGMENT, "anchor1")
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertTrue(link.isValid(), "link valid");
    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html?p1=abc&p2=def#anchor1",
        link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testTargetPageWithQueryParamsFragment_LinkArgs() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .build());

    Link link = linkHandler.get(linkResource).queryString("p5=abc&p6=xyz").fragment("anchor2").build();

    assertTrue(link.isValid(), "link valid");
    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html?p5=abc&p6=xyz#anchor2",
        link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testTargetPageWithQueryParamsFragment_LinkArgs_Resource() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .put(LinkNameConstants.PN_LINK_QUERY_PARAM, "p1=abc&p2=def")
        .put(LinkNameConstants.PN_LINK_FRAGMENT, "anchor1")
        .build());

    Link link = linkHandler.get(linkResource).queryString("p5=abc&p6=xyz").fragment("anchor2").build();

    assertTrue(link.isValid(), "link valid");
    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html?p1=abc&p2=def#anchor1",
        link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testGetSyntheticLinkResource() {
    Resource resource = InternalLinkType.getSyntheticLinkResource(context.resourceResolver(), "/page/ref");
    ValueMap expected = ImmutableValueMap.of(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID,
        LinkNameConstants.PN_LINK_CONTENT_REF, "/page/ref");
    assertEquals(expected, ImmutableValueMap.copyOf(resource.getValueMap()));
  }

}
