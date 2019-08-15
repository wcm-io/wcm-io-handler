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
package io.wcm.handler.link;

import static io.wcm.handler.link.LinkNameConstants.PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY;
import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class LinkComponentPropertyResolverTest {

  private static final String RESOURCE_TYPE = "/apps/app1/components/comp1";

  private final AemContext context = AppAemContext.newAemContext();

  @Test
  void testGetLinkTargetUrlFallbackProperty_Default() {
    Resource resource = context.create().resource("/content/r1");

    LinkComponentPropertyResolver underTest = new LinkComponentPropertyResolver(resource);
    assertNull(underTest.getLinkTargetUrlFallbackProperty());
  }

  @Test
  void testGetLinkTargetUrlFallbackProperty_Component() {
    context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY, "property1");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);

    LinkComponentPropertyResolver underTest = new LinkComponentPropertyResolver(resource);
    assertEquals("property1", underTest.getLinkTargetUrlFallbackProperty());
  }

  @Test
  void testGetLinkTargetUrlFallbackProperty_Component_Policy() {
    context.contentPolicyMapping(RESOURCE_TYPE,
        PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY, "property2");

    context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY, "property1");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);

    LinkComponentPropertyResolver underTest = new LinkComponentPropertyResolver(resource);
    assertEquals("property2", underTest.getLinkTargetUrlFallbackProperty());
  }

}
