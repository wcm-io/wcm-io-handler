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
import io.wcm.handler.link.spi.LinkType;
import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test {@link ExternalLinkType} methods.
 */
public class ExternalLinkTypeTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  protected Adaptable adaptable() {
    return context.request();
  }

  @Test
  public void testInvalidLink() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_EXTERNAL_REF, "")
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertFalse("link valid", link.isValid());
    assertNull("link url", link.getUrl());
    assertNull("anchor", link.getAnchor());

  }

  @Test
  public void testValidLink() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_EXTERNAL_REF, "http://xyz/abc")
        .build());

    Link link = linkHandler.get(linkResource).build();

    assertTrue("link valid", link.isValid());
    assertEquals("link url", "http://xyz/abc", link.getUrl());
    assertNotNull("anchor", link.getAnchor());

  }

  @Test
  public void testGetSyntheticLinkResource() {
    Resource resource = ExternalLinkType.getSyntheticLinkResource(context.resourceResolver(), "http://dummy");
    ValueMap expected = ImmutableValueMap.of(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID,
        LinkNameConstants.PN_LINK_EXTERNAL_REF, "http://dummy");
    assertEquals(expected, ImmutableValueMap.copyOf(resource.getValueMap()));
  }

  @Test
  public void testAccepts() throws Exception {
    LinkType underTest = AdaptTo.notNull(adaptable(), ExternalLinkType.class);

    assertTrue(underTest.accepts("http://hostname"));
    assertTrue(underTest.accepts("https://hostname"));
    assertTrue(underTest.accepts("mailto:abc@xx.yy"));
    assertFalse(underTest.accepts("/relative/path"));
  }

}
