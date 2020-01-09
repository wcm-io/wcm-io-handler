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
package io.wcm.handler.richtext.ui;

import static io.wcm.handler.richtext.RichTextNameConstants.PN_TEXT;
import static io.wcm.handler.richtext.testcontext.AppAemContext.ROOTPATH_CONTENT;
import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;

import io.wcm.handler.richtext.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class ResourceRichTextTest {

  private final AemContext context = AppAemContext.newAemContext();

  private Page page;

  @BeforeEach
  void setUp() {
    page = context.create().page(ROOTPATH_CONTENT + "/page1");
  }

  @Test
  void testRichText() {
    context.currentResource(context.create().resource(page, "richtext",
        PROPERTY_RESOURCE_TYPE, "/dummy/resourcetype",
        PN_TEXT, "<p>my <strong>rich</strong> text</p>"));

    ResourceRichText underTest = context.request().adaptTo(ResourceRichText.class);
    assertTrue(underTest.isValid());
    assertEquals("<p>my <strong>rich</strong> text</p>", underTest.getMarkup());
  }

  @Test
  void testMissingRichText() {
    context.currentResource(context.create().resource(page, "richtext",
        PROPERTY_RESOURCE_TYPE, "/dummy/resourcetype"));

    ResourceRichText underTest = context.request().adaptTo(ResourceRichText.class);
    assertFalse(underTest.isValid());
    assertNull(underTest.getMarkup());
  }

  @Test
  void testPlainText() {
    context.currentResource(context.create().resource(page, "richtext",
        PROPERTY_RESOURCE_TYPE, "/dummy/resourcetype",
        "customTextProp", "Line 1\nLine 2"));

    context.request().setAttribute("propertyName", "customTextProp");
    context.request().setAttribute("isRichText", false);

    ResourceRichText underTest = context.request().adaptTo(ResourceRichText.class);
    assertTrue(underTest.isValid());
    assertEquals("Line 1<br />Line 2", underTest.getMarkup());
  }

}
