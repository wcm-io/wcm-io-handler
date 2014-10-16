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
import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.WCMMode;

/**
 * Test {@link MediaLinkType}
 */
public class MediaLinkTypeTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  @Test
  public void testEmptyLink() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, MediaLinkType.ID)
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertFalse("link valid", link.isValid());
    assertFalse("link ref invalid", link.isLinkReferenceInvalid());
    assertNull("link url", link.getUrl());
    assertNull("anchor", link.getAnchor());
  }

  @Test
  public void testInvalidLink() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, MediaLinkType.ID)
        .put(LinkNameConstants.PN_LINK_MEDIA_REF, "/invalid/media/link")
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertFalse("link valid", link.isValid());
    assertTrue("link ref invalid", link.isLinkReferenceInvalid());
    assertNull("link url", link.getUrl());
    assertNull("anchor", link.getAnchor());
  }

  @Test
  public void testInvalidLink_EditMode() {
    WCMMode.EDIT.toRequest(context.request());

    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, MediaLinkType.ID)
        .put(LinkNameConstants.PN_LINK_MEDIA_REF, "/invalid/media/link")
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertFalse("link valid", link.isValid());
    assertTrue("link ref invalid", link.isLinkReferenceInvalid());
    assertNull("link url", link.getUrl());
    assertNotNull("anchor", link.getAnchor());
    assertEquals("anchor.href", LinkHandler.INVALID_LINK, link.getAnchor().getHRef());
  }

  // --> does not work because dummy implementation does not support download media format detection
  // @Test
  // public void testInvalidImageLink() {
  // LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);
  //
  // SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver());
  // ValueMap linkProps = linkResource.getProperties();
  // .put(LinkNameConstants.PN_LINK_TYPE, DefaultMediaLinkType.ID);
  // .put(LinkNameConstants.PN_LINK_MEDIA_REF, "/content/dummymedia/image1");
  //
  // Link link = linkHandler.get(linkResource).build();
  //
  // assertFalse("link invalid", link.isValid());
  // assertNull("link url", link.getLinkUrl());
  // assertNull("anchor", link.getAnchor());
  // }

  @Test
  public void testValidPdfLink() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, MediaLinkType.ID)
        .put(LinkNameConstants.PN_LINK_MEDIA_REF, "/content/dummymedia/pdf1")
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertTrue("link valid", link.isValid());
    assertEquals("link url", "/content/dummymedia/pdf1.pdf", link.getUrl());
    assertNotNull("anchor", link.getAnchor());
  }

  @Test
  public void testGetSyntheticLinkResource() {
    Resource resource = MediaLinkType.getSyntheticLinkResource(context.resourceResolver(), "/media/ref");
    ValueMap expected = ImmutableValueMap.of(LinkNameConstants.PN_LINK_TYPE, MediaLinkType.ID,
        LinkNameConstants.PN_LINK_MEDIA_REF, "/media/ref");
    assertEquals(expected, ImmutableValueMap.copyOf(resource.getValueMap()));
  }

}
