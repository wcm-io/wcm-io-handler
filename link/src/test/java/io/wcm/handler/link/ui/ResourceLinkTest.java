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

import static io.wcm.handler.link.testcontext.AppAemContext.ROOTPATH_CONTENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.handler.link.type.ExternalLinkType;
import io.wcm.handler.link.type.InternalLinkType;
import io.wcm.testing.mock.aem.junit.AemContext;

@SuppressWarnings("null")
public class ResourceLinkTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  @Before
  public void setUp() {
    context.create().page(ROOTPATH_CONTENT + "/page1");
  }

  @Test
  public void testValidLink() {
    context.currentResource(context.create().resource(ROOTPATH_CONTENT + "/page1/jcr:content/validLink",
        LinkNameConstants.PN_LINK_TYPE, ExternalLinkType.ID,
        LinkNameConstants.PN_LINK_EXTERNAL_REF, "http://www.dummysite.org"));

    ResourceLink underTest = context.request().adaptTo(ResourceLink.class);
    assertTrue(underTest.isValid());
    assertEquals("http://www.dummysite.org", underTest.getMetadata().getUrl());
    assertEquals("http://www.dummysite.org", underTest.getAttributes().get("href"));
  }

  @Test
  public void testInvalidLink() {
    context.currentResource(context.create().resource(ROOTPATH_CONTENT + "/page1/jcr:content/invalidLink",
        LinkNameConstants.PN_LINK_TYPE, InternalLinkType.ID,
        LinkNameConstants.PN_LINK_CONTENT_REF, "/invalid/link"));

    ResourceLink underTest = context.request().adaptTo(ResourceLink.class);
    assertFalse(underTest.isValid());
  }

}
