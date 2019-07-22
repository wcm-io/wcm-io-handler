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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.WCMMode;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * Test {@link MediaLinkType}
 */
@ExtendWith(AemContextExtension.class)
class MediaLinkTypeTest {

  final AemContext context = AppAemContext.newAemContext();

  protected Adaptable adaptable() {
    return context.request();
  }

  @Test
  void testEmptyLink() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        "/content/dummy-path",
        ImmutableValueMap.builder()
            .put(LinkNameConstants.PN_LINK_TYPE, MediaLinkType.ID)
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
        "/content/dummy-path",
        ImmutableValueMap.builder()
            .put(LinkNameConstants.PN_LINK_TYPE, MediaLinkType.ID)
            .put(LinkNameConstants.PN_LINK_MEDIA_REF, "/invalid/media/link")
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
        "/content/dummy-path",
        ImmutableValueMap.builder()
            .put(LinkNameConstants.PN_LINK_TYPE, MediaLinkType.ID)
            .put(LinkNameConstants.PN_LINK_MEDIA_REF, "/invalid/media/link")
            .build());

    Link link = linkHandler.get(linkResource).dummyLink(true).dummyLinkUrl("/my/dummy/url").build();

    assertFalse(link.isValid(), "link valid");
    assertTrue(link.isLinkReferenceInvalid(), "link ref invalid");
    assertNull(link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
    assertEquals("/my/dummy/url", link.getAnchor().getHRef(), "anchor.href");
  }

  // --> does not work because dummy implementation does not support download media format detection
  //@Test
  //void testInvalidImageLink() {
  //  LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);
  //
  //  SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
  //      "/content/dummy-path",
  //      ImmutableValueMap.builder()
  //          .put(LinkNameConstants.PN_LINK_TYPE, MediaLinkType.ID)
  //          .put(LinkNameConstants.PN_LINK_MEDIA_REF, "/content/dummymedia/image1")
  //          .build());
  //
  //  Link link = linkHandler.get(linkResource).build();
  //
  //  assertFalse("link invalid", link.isValid());
  //  assertNull("link url", link.getUrl());
  //  assertNull("anchor", link.getAnchor());
  //}

  @Test
  void testValidPdfLink() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        "/content/dummy-path",
        ImmutableValueMap.builder()
            .put(LinkNameConstants.PN_LINK_TYPE, MediaLinkType.ID)
            .put(LinkNameConstants.PN_LINK_MEDIA_REF, "/content/dummymedia/pdf1")
            .build());

    Link link = linkHandler.get(linkResource).build();

    assertTrue(link.isValid(), "link valid");
    assertEquals("/content/dummymedia/pdf1.pdf", link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testGetSyntheticLinkResource() {
    Resource resource = MediaLinkType.getSyntheticLinkResource(context.resourceResolver(),
        "/content/dummy-path",
        "/media/ref");
    ValueMap expected = ImmutableValueMap.of(LinkNameConstants.PN_LINK_TYPE, MediaLinkType.ID,
        LinkNameConstants.PN_LINK_MEDIA_REF, "/media/ref");
    assertEquals(expected, ImmutableValueMap.copyOf(resource.getValueMap()));
  }

  @Test
  @SuppressWarnings("deprecation")
  void testGetSyntheticLinkResource_Deprecated() {
    Resource resource = MediaLinkType.getSyntheticLinkResource(context.resourceResolver(),
        "/media/ref");
    ValueMap expected = ImmutableValueMap.of(LinkNameConstants.PN_LINK_TYPE, MediaLinkType.ID,
        LinkNameConstants.PN_LINK_MEDIA_REF, "/media/ref");
    assertEquals(expected, ImmutableValueMap.copyOf(resource.getValueMap()));
  }

}
