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
package io.wcm.handler.link.type.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.LinkRequest;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.handler.link.testcontext.DummyAppTemplate;
import io.wcm.handler.link.type.InternalLinkType;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

/**
 * Most of the logic of {@link InternalLinkResolver} is tested in the InternalLinkTypeTest.
 * This tests only some special feature not used by InternalLinkType.
 */
public class InternalLinkResolverTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  @Before
  public void setUp() throws Exception {

    // create current page in site context
    context.currentPage(context.create().page("/content/unittest/de_test/brand/de/section/page",
        DummyAppTemplate.CONTENT.getTemplatePath()));

    // create internal pages for unit tests
    context.create().page("/content/unittest/de_test/brand/de/section/content",
        DummyAppTemplate.CONTENT.getTemplatePath());
    context.create().page("/content/unittest/en_test/brand/en/section/content",
        DummyAppTemplate.CONTENT.getTemplatePath());

  }


  @Test
  public void testTargetPage_RewritePathToContext() {
    InternalLinkResolver resolver = AdaptTo.notNull(context.request(), InternalLinkResolver.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(), ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, "/content/unittest/en_test/brand/en/section/content")
        .build());

    LinkRequest linkRequest = new LinkRequest(linkResource, null, null);
    Link link = new Link(new InternalLinkType(), linkRequest);

    link = resolver.resolveLink(link, new InternalLinkResolverOptions()
        .rewritePathToContext(true));

    assertTrue("link valid", link.isValid());
    assertFalse("link ref invalid", link.isLinkReferenceInvalid());
    assertEquals("link url", "http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html", link.getUrl());
  }

  @Test
  public void testTargetPageOtherSite_NoRewritePathToContext() {
    InternalLinkResolver resolver = AdaptTo.notNull(context.request(), InternalLinkResolver.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(), ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_CONTENT_REF, "/content/unittest/en_test/brand/en/section/content")
        .build());

    LinkRequest linkRequest = new LinkRequest(linkResource, null, null);
    Link link = new Link(new InternalLinkType(), linkRequest);

    link = resolver.resolveLink(link, new InternalLinkResolverOptions()
        .rewritePathToContext(false));

    assertTrue("link valid", link.isValid());
    assertFalse("link ref invalid", link.isLinkReferenceInvalid());
    assertEquals("link url", "http://en.dummysite.org/content/unittest/en_test/brand/en/section/content.html", link.getUrl());
  }

  @Test
  public void testTargetPage_RewritePathToContext_ExperienceFragment() {
    Page xfPage = context.create().page("/content/experience-fragments/level1/level2/level3/level4/xf1");
    Resource linkResource = context.create().resource(xfPage, "link",
        LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID,
        LinkNameConstants.PN_LINK_CONTENT_REF, "/content/unittest/en_test/brand/en/section/content");

    context.currentPage(xfPage);
    context.currentResource(linkResource);

    InternalLinkResolver resolver = AdaptTo.notNull(context.request(), InternalLinkResolver.class);

    LinkRequest linkRequest = new LinkRequest(linkResource, null, null);
    Link link = new Link(new InternalLinkType(), linkRequest);

    link = resolver.resolveLink(link, new InternalLinkResolverOptions()
        .rewritePathToContext(true));

    assertTrue("link valid", link.isValid());
    assertFalse("link ref invalid", link.isLinkReferenceInvalid());
    assertEquals("link url", "http://en.dummysite.org/content/unittest/en_test/brand/en/section/content.html", link.getUrl());
  }

}
