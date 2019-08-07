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

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.spi.LinkType;
import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * Test {@link ExternalLinkType} methods.
 */
@ExtendWith(AemContextExtension.class)
class ExternalLinkTypeTest {

  final AemContext context = AppAemContext.newAemContext();

  protected Adaptable adaptable() {
    return context.request();
  }

  @Test
  void testInvalidLink() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        "/content/dummy-path",
        ImmutableValueMap.builder()
            .put(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID)
            .put(LinkNameConstants.PN_LINK_EXTERNAL_REF, "")
            .build());

    Link link = linkHandler.get(linkResource).build();

    assertFalse(link.isValid(), "link valid");
    assertNull(link.getUrl(), "link url");
    assertNull(link.getAnchor(), "anchor");
  }

  @Test
  void testValidLink() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        "/content/dummy-path",
        ImmutableValueMap.builder()
            .put(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID)
            .put(LinkNameConstants.PN_LINK_EXTERNAL_REF, "http://xyz/abc")
            .build());

    Link link = linkHandler.get(linkResource).build();

    assertTrue(link.isValid(), "link valid");
    assertEquals("http://xyz/abc", link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

  @Test
  void testGetSyntheticLinkResource() {
    Resource resource = ExternalLinkType.getSyntheticLinkResource(context.resourceResolver(),
        "/content/dummy-path",
        "http://dummy");
    ValueMap expected = ImmutableValueMap.of(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID,
        LinkNameConstants.PN_LINK_EXTERNAL_REF, "http://dummy");
    assertEquals(expected, ImmutableValueMap.copyOf(resource.getValueMap()));
  }

  @Test
  @SuppressWarnings("deprecation")
  void testGetSyntheticLinkResource_Deprecated() {
    Resource resource = ExternalLinkType.getSyntheticLinkResource(context.resourceResolver(),
        "http://dummy");
    ValueMap expected = ImmutableValueMap.of(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID,
        LinkNameConstants.PN_LINK_EXTERNAL_REF, "http://dummy");
    assertEquals(expected, ImmutableValueMap.copyOf(resource.getValueMap()));
  }

  @Test
  void testAccepts() throws Exception {
    LinkType underTest = AdaptTo.notNull(adaptable(), ExternalLinkType.class);

    assertTrue(underTest.accepts("http://hostname"));
    assertTrue(underTest.accepts("https://hostname"));
    assertTrue(underTest.accepts("mailto:abc@xx.yy"));
    assertTrue(underTest.accepts("tel:+49 123 45678"));
    assertFalse(underTest.accepts("/relative/path"));
    assertFalse(underTest.accepts("anystring"));
  }

  @Test
  void testReferenceAutoDetection() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    Link link = linkHandler.get("http://xyz/abc").build();

    assertEquals(ExternalLinkType.ID, link.getLinkType().getId());
    assertTrue(link.isValid(), "link valid");
    assertEquals("http://xyz/abc", link.getUrl(), "link url");
    assertNotNull(link.getAnchor(), "anchor");
  }

}
