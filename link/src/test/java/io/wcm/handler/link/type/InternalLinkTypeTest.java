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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.LinkMetadata;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.LinkReference;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.args.LinkArgs;
import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.handler.link.testcontext.DummyAppTemplate;
import io.wcm.handler.url.UrlModes;
import io.wcm.handler.url.integrator.IntegratorModes;
import io.wcm.handler.url.integrator.IntegratorNameConstants;
import io.wcm.handler.url.integrator.IntegratorProtocol;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.commons.contenttype.FileExtension;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;
import com.google.common.collect.ImmutableMap;

/**
 * Test {@link InternalLinkType} methods.
 */
public class InternalLinkTypeTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  private Page targetPage;

  @Before
  public void setUp() throws Exception {

    // create current page in site context
    context.currentPage(context.create().page("/content/unittest/de_test/brand/de/section/page",
        DummyAppTemplate.CONTENT.getTemplatePath()));

    // create internal pages for unit tests
    targetPage = context.create().page("/content/unittest/de_test/brand/de/section/content",
        DummyAppTemplate.CONTENT.getTemplatePath());

  }

  @Test
  public void testEmptyLink() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID)
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(linkResource);

    assertFalse("link valid", linkMetadata.isValid());
    assertFalse("link ref invalid", linkMetadata.isLinkReferenceInvalid());
    assertNull("link url", linkMetadata.getLinkUrl());
    assertNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testEmptyLink_EditMode() {
    WCMMode.EDIT.toRequest(context.request());

    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID)
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(linkResource);

    assertFalse("link valid", linkMetadata.isValid());
    assertFalse("link ref invalid", linkMetadata.isLinkReferenceInvalid());
    assertNull("link url", linkMetadata.getLinkUrl());
    assertNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testInvalidLink() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, "/invalid/content/path")
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(linkResource);

    assertFalse("link valid", linkMetadata.isValid());
    assertTrue("link ref invalid", linkMetadata.isLinkReferenceInvalid());
    assertNull("link url", linkMetadata.getLinkUrl());
    assertNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testInvalidLink_EditMode() {
    WCMMode.EDIT.toRequest(context.request());

    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, "/invalid/content/path")
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(linkResource);

    assertFalse("link valid", linkMetadata.isValid());
    assertTrue("link ref invalid", linkMetadata.isLinkReferenceInvalid());
    assertNull("link url", linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());
    assertEquals("anchor.href", LinkHandler.INVALID_LINK, linkMetadata.getAnchor().getHRef());
  }

  @Test
  public void testTargetPage() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(linkResource);

    assertTrue("link valid", linkMetadata.isValid());
    assertFalse("link ref invalid", linkMetadata.isLinkReferenceInvalid());
    assertEquals("link url", "http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html", linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testStructureElement() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    Page structureElementPage = context.create().page("/content/unittest/de_test/brand/de/section/structureElement",
        DummyAppTemplate.STRUCTURE_ELEMENT.getTemplatePath());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(structureElementPage);

    assertFalse("link valid", linkMetadata.isValid());
    assertNull("link url", linkMetadata.getLinkUrl());
    assertNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testSecureTargetPage() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    Page secureTargetPage = context.create().page("/content/unittest/de_test/brand/de/section/contentSecure",
        DummyAppTemplate.CONTENT_SECURE.getTemplatePath());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(secureTargetPage);

    assertTrue("link valid", linkMetadata.isValid());
    assertEquals("link url", "https://www.dummysite.org/content/unittest/de_test/brand/de/section/contentSecure.html", linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testRedirectInternal() throws Exception {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    Page redirectInternalPage = context.create().page("/content/unittest/de_test/brand/de/section/redirectInternal",
        DummyAppTemplate.REDIRECT.getTemplatePath(), ImmutableMap.<String,Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(redirectInternalPage);

    assertTrue("link valid", linkMetadata.isValid());
    assertEquals("link url", "http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html", linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testRedirectInternal_EditMode() throws Exception {
    WCMMode.EDIT.toRequest(context.request());
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    Page redirectInternalPage = context.create().page("/content/unittest/de_test/brand/de/section/redirectInternal",
        DummyAppTemplate.REDIRECT.getTemplatePath(), ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(redirectInternalPage);

    assertTrue("link valid", linkMetadata.isValid());
    assertEquals("link url", "http://www.dummysite.org/content/unittest/de_test/brand/de/section/redirectInternal.html", linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testRedirectRedirectInternal() throws Exception {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    Page redirectInternalPage = context.create().page("/content/unittest/de_test/brand/de/section/redirectInternal",
        DummyAppTemplate.REDIRECT.getTemplatePath(), ImmutableMap.<String,Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .build());

    Page redirectRedirectInternalPage = context.create().page("/content/unittest/de_test/brand/de/section/redirectRedirectInternal",
        DummyAppTemplate.REDIRECT.getTemplatePath(), ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, redirectInternalPage.getPath())
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(redirectRedirectInternalPage);

    assertTrue("link valid", linkMetadata.isValid());
    assertEquals("link url", "http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html", linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testRedirectExternal() throws Exception {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    Page redirectExternalPage = context.create().page("/content/unittest/de_test/brand/de/section/redirectExternal",
        DummyAppTemplate.REDIRECT.getTemplatePath(), ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_EXTERNAL_REF, "http://xyz/abc")
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(redirectExternalPage);

    assertTrue("link valid", linkMetadata.isValid());
    assertEquals("link url", "http://xyz/abc", linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testRedirectCyclic() throws Exception {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    String redirectInternalCyclic1Path = "/content/unittest/de_test/brand/de/section/redirectInternalCyclic1";
    String redirectInternalCyclic2Path = "/content/unittest/de_test/brand/de/section/redirectInternalCyclic2";

    Page redirectInternalCyclic1Page = context.create().page(redirectInternalCyclic1Path,
        DummyAppTemplate.REDIRECT.getTemplatePath(), ImmutableMap.<String,Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, redirectInternalCyclic2Path)
        .build());
    Page redirectInternalCyclic2Page = context.create().page(redirectInternalCyclic2Path,
        DummyAppTemplate.REDIRECT.getTemplatePath(), ImmutableMap.<String,Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, redirectInternalCyclic1Path)
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(redirectInternalCyclic1Page);

    assertFalse("link valid", linkMetadata.isValid());
    assertNull("link url", linkMetadata.getLinkUrl());
    assertNull("anchor", linkMetadata.getAnchor());

    linkMetadata = linkHandler.getLinkMetadata(redirectInternalCyclic2Page);

    assertFalse("link valid", linkMetadata.isValid());
    assertNull("link url", linkMetadata.getLinkUrl());
    assertNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testIntegrator() throws Exception {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    Page integratorPage = context.create().page("/content/unittest/de_test/brand/de/section/integrator",
        DummyAppTemplate.INTEGRATOR.getTemplatePath(), ImmutableMap.<String, Object>builder()
        .put(IntegratorNameConstants.PN_INTEGRATOR_MODE, IntegratorModes.SIMPLE.getId())
        .put(IntegratorNameConstants.PN_INTEGRATOR_PROTOCOL, IntegratorProtocol.HTTP.name())
        .put(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_EXTERNAL_REF, "http://xyz/app")
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(integratorPage);

    assertTrue("link valid", linkMetadata.isValid());
    assertEquals("link url", "http://xyz/app", linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testIntegrator_EditMode() throws Exception {
    WCMMode.EDIT.toRequest(context.request());
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    Page integratorPage = context.create().page("/content/unittest/de_test/brand/de/section/integrator",
        DummyAppTemplate.INTEGRATOR.getTemplatePath(), ImmutableMap.<String, Object>builder()
        .put(IntegratorNameConstants.PN_INTEGRATOR_MODE, IntegratorModes.SIMPLE.getId())
        .put(IntegratorNameConstants.PN_INTEGRATOR_PROTOCOL, IntegratorProtocol.HTTP.name())
        .put(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_EXTERNAL_REF, "http://xyz/app")
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(integratorPage);

    assertTrue("link valid", linkMetadata.isValid());
    assertEquals("link url", "http://www.dummysite.org/content/unittest/de_test/brand/de/section/integrator.html", linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testTargetPageOtherSite() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, "/content/unittest/en_test/brand/en/section/content")
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(linkResource);

    assertTrue("link valid", linkMetadata.isValid());
    assertEquals("link url", "http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html",
        linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testTargetPageLinkUrlVariants() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html",
        linkHandler.getLinkUrl(targetPage));
    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.sel1.html",
        linkHandler.getLinkUrl(targetPage, "sel1"));
    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.htx",
        linkHandler.getLinkUrl(targetPage, null, FileExtension.HTML_UNCACHED));
    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.sel1.htx",
        linkHandler.getLinkUrl(targetPage, "sel1", FileExtension.HTML_UNCACHED));
    assertEquals("http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.sel1.suffix.htx/suf1/suf2.htx",
        linkHandler.getLinkUrl(targetPage, "sel1", FileExtension.HTML_UNCACHED, "suf1/suf2"));
    assertEquals("/content/unittest/de_test/brand/de/section/content.sel1.suffix.htx/suf1/suf2.htx",
        linkHandler.getLinkUrl(targetPage, "sel1", FileExtension.HTML_UNCACHED, "suf1/suf2", UrlModes.NO_HOSTNAME));
  }

  @Test
  public void testTargetPageWithQueryParams_Resource() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .put(LinkNameConstants.PN_LINK_QUERY_PARAM, "p1=abc&p2=def")
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(linkResource);

    assertTrue("link valid", linkMetadata.isValid());
    assertEquals("link url", "http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html?p1=abc&p2=def",
        linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testTargetPageWithFragment_Resource() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .put(LinkNameConstants.PN_LINK_ANCHOR_NAME, "anchor1")
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(linkResource);

    assertTrue("link valid", linkMetadata.isValid());
    assertEquals("link url", "http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html#anchor1",
        linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testTargetPageWithQueryParamsFragment_Resource() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .put(LinkNameConstants.PN_LINK_QUERY_PARAM, "p1=abc&p2=def")
        .put(LinkNameConstants.PN_LINK_ANCHOR_NAME, "anchor1")
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(linkResource);

    assertTrue("link valid", linkMetadata.isValid());
    assertEquals("link url", "http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html?p1=abc&p2=def#anchor1",
        linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testTargetPageWithQueryParamsFragment_LinkArgs() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .build());

    LinkReference linkRef = new LinkReference(linkResource,
        new LinkArgs().setQueryString("p5=abc&p6=xyz").setFragment("anchor2"));
    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(linkRef);

    assertTrue("link valid", linkMetadata.isValid());
    assertEquals("link url", "http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html?p5=abc&p6=xyz#anchor2",
        linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());
  }

  @Test
  public void testTargetPageWithQueryParamsFragment_LinkArgs_Resource() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, targetPage.getPath())
        .put(LinkNameConstants.PN_LINK_QUERY_PARAM, "p1=abc&p2=def")
        .put(LinkNameConstants.PN_LINK_ANCHOR_NAME, "anchor1")
        .build());

    LinkReference linkRef = new LinkReference(linkResource,
        new LinkArgs().setQueryString("p5=abc&p6=xyz").setFragment("anchor2"));
    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(linkRef);

    assertTrue("link valid", linkMetadata.isValid());
    assertEquals("link url", "http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html?p1=abc&p2=def#anchor1",
        linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());
  }

}
