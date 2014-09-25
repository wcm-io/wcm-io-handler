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
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit.AemContext;

import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * Test {@link ExternalLinkType} methods.
 */
public class ExternalLinkTypeTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  @Test
  public void testInvalidLink() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_EXTERNAL_REF, "")
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(linkResource);

    assertFalse("link valid", linkMetadata.isValid());
    assertNull("link url", linkMetadata.getLinkUrl());
    assertNull("anchor", linkMetadata.getAnchor());

  }

  @Test
  public void testValidLink() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableMap.<String, Object>builder()
        .put(LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID)
        .put(LinkNameConstants.PN_LINK_EXTERNAL_REF, "http://xyz/abc")
        .build());

    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(linkResource);

    assertTrue("link valid", linkMetadata.isValid());
    assertEquals("link url", "http://xyz/abc", linkMetadata.getLinkUrl());
    assertNotNull("anchor", linkMetadata.getAnchor());

  }

}
