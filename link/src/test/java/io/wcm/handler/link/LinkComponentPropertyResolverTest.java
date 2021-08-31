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

import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.commons.jcr.JcrConstants.NT_UNSTRUCTURED;
import static io.wcm.handler.link.LinkNameConstants.PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY;
import static io.wcm.handler.link.LinkNameConstants.PN_COMPONENT_LINK_TARGET_WINDOW_TARGET_FALLBACK_PROPERTY;
import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.component.ComponentPropertyResolverFactory;

@ExtendWith(AemContextExtension.class)
class LinkComponentPropertyResolverTest {

  private static final String RESOURCE_TYPE = "/apps/app1/components/comp1";

  private final AemContext context = AppAemContext.newAemContext();

  private ComponentPropertyResolverFactory componentPropertyResolverFactory;

  @BeforeEach
  void setUp() {
    componentPropertyResolverFactory = context.getService(ComponentPropertyResolverFactory.class);
  }

  @Test
  void testGetLinkTargetUrlFallbackProperty_Default() throws Exception {
    Resource resource = context.create().resource("/content/r1");

    try (LinkComponentPropertyResolver underTest = new LinkComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertNull(underTest.getLinkTargetUrlFallbackProperty());
      assertNull(underTest.getLinkTargetWindowTargetFallbackProperty());
    }
  }

  @Test
  void testGetLinkTargetUrlFallbackProperty_Component() throws Exception {
    context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY, "property1",
        PN_COMPONENT_LINK_TARGET_WINDOW_TARGET_FALLBACK_PROPERTY, new String[] { "prop2a", "prop2b" });
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);

    try (LinkComponentPropertyResolver underTest = new LinkComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertArrayEquals(new String[] { "property1" }, underTest.getLinkTargetUrlFallbackProperty());
      assertArrayEquals(new String[] { "prop2a", "prop2b" }, underTest.getLinkTargetWindowTargetFallbackProperty());
    }
  }

  @Test
  void testGetLinkTargetUrlFallbackProperty_Component_Policy() throws Exception {
    context.contentPolicyMapping(RESOURCE_TYPE,
        PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY, new String[] { "property2", "property3" });

    context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY, "property1");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);

    try (LinkComponentPropertyResolver underTest = new LinkComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
      assertArrayEquals(new String[] { "property2", "property3" }, underTest.getLinkTargetUrlFallbackProperty());
    }
  }

  @Test
  void testGetLinkTargetUrlFallbackProperty_Component_SubResource() throws Exception {
    context.create().resource(RESOURCE_TYPE,
        PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY, "property1");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
    Resource subResource1 = context.create().resource(resource, "subResource1",
        JCR_PRIMARYTYPE, NT_UNSTRUCTURED);
    Resource subResource2 = context.create().resource(subResource1, "subResource2");

    try (LinkComponentPropertyResolver underTest = new LinkComponentPropertyResolver(subResource2, componentPropertyResolverFactory)) {
      assertArrayEquals(new String[] { "property1" }, underTest.getLinkTargetUrlFallbackProperty());
    }
  }

}
