/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
import static org.junit.Assert.assertTrue;

import org.apache.sling.api.adapter.Adaptable;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.day.cq.wcm.api.Page;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.handler.link.testcontext.DummyAppTemplate;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;

/**
 * Test {@link InternalCrossContextLinkType} methods.
 * Most of the test cases are identical to {@link InternalLinkTypeTest}, so they are not duplicated here.
 */
public class InternalCrossContextLinkTypeTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  private Page targetPage;

  protected Adaptable adaptable() {
    return context.request();
  }

  @BeforeEach
  public void setUp() throws Exception {

    // create current page in site context
    context.currentPage(context.create().page("/content/unittest/de_test/brand/de/section/page",
        DummyAppTemplate.CONTENT.getTemplatePath()));

    // create internal pages for unit tests
    targetPage = context.create().page("/content/unittest/de_test/brand/de/section/content",
        DummyAppTemplate.CONTENT.getTemplatePath());
    context.create().page("/content/unittest/en_test/brand/en/section/content",
        DummyAppTemplate.CONTENT.getTemplatePath());

  }

  @Test
  public void testTargetPage() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
            .put(LinkNameConstants.PN_LINK_TYPE, InternalCrossContextLinkType.ID)
            .put(LinkNameConstants.PN_LINK_CROSSCONTEXT_CONTENT_REF, targetPage.getPath())
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertTrue("link valid", link.isValid());
    assertFalse("link ref invalid", link.isLinkReferenceInvalid());
    assertEquals("link url", "http://www.dummysite.org/content/unittest/de_test/brand/de/section/content.html", link.getUrl());
    assertNotNull("anchor", link.getAnchor());
  }

  @Test
  public void testTargetPageOtherSite() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
            .put(LinkNameConstants.PN_LINK_TYPE, InternalCrossContextLinkType.ID)
            .put(LinkNameConstants.PN_LINK_CROSSCONTEXT_CONTENT_REF, "/content/unittest/en_test/brand/en/section/content")
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertTrue("link valid", link.isValid());
    assertEquals("link url", "http://en.dummysite.org/content/unittest/en_test/brand/en/section/content.html",
        link.getUrl());
    assertNotNull("anchor", link.getAnchor());
  }

}
