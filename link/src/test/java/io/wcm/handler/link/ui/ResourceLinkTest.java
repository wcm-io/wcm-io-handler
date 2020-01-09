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
package io.wcm.handler.link.ui;

import static io.wcm.handler.link.LinkNameConstants.PN_LINK_CONTENT_REF;
import static io.wcm.handler.link.LinkNameConstants.PN_LINK_EXTERNAL_REF;
import static io.wcm.handler.link.LinkNameConstants.PN_LINK_TYPE;
import static io.wcm.handler.link.testcontext.AppAemContext.ROOTPATH_CONTENT;
import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.handler.link.type.ExternalLinkType;
import io.wcm.handler.link.type.InternalLinkType;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class ResourceLinkTest {

  private final AemContext context = AppAemContext.newAemContext();

  @BeforeEach
  void setUp() {
    context.create().page(ROOTPATH_CONTENT + "/page1");
  }

  @Test
  void testValidLink() {
    context.currentResource(context.create().resource(ROOTPATH_CONTENT + "/page1/jcr:content/validLink",
        PROPERTY_RESOURCE_TYPE, "/dummy/resourcetype",
        PN_LINK_TYPE, ExternalLinkType.ID,
        PN_LINK_EXTERNAL_REF, "http://www.dummysite.org"));

    ResourceLink underTest = context.request().adaptTo(ResourceLink.class);
    assertTrue(underTest.isValid());
    assertEquals("http://www.dummysite.org", underTest.getMetadata().getUrl());
    assertEquals("http://www.dummysite.org", underTest.getAttributes().get("href"));
  }

  @Test
  void testInvalidLink() {
    context.currentResource(context.create().resource(ROOTPATH_CONTENT + "/page1/jcr:content/invalidLink",
        PROPERTY_RESOURCE_TYPE, "/dummy/resourcetype",
        PN_LINK_TYPE, InternalLinkType.ID,
        PN_LINK_CONTENT_REF, "/invalid/link"));

    ResourceLink underTest = context.request().adaptTo(ResourceLink.class);
    assertFalse(underTest.isValid());
  }

}
