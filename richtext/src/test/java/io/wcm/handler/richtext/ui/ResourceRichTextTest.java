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

import static io.wcm.handler.richtext.testcontext.AppAemContext.ROOTPATH_CONTENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.day.cq.wcm.api.Page;

import io.wcm.handler.richtext.RichTextNameConstants;
import io.wcm.handler.richtext.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;

@SuppressWarnings("null")
public class ResourceRichTextTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  private Page page;

  @BeforeEach
  public void setUp() {
    page = context.create().page(ROOTPATH_CONTENT + "/page1");
  }

  @Test
  public void testRichText() {
    context.currentResource(context.create().resource(page, "richtext",
        RichTextNameConstants.PN_TEXT, "<p>my <strong>rich</strong> text</p>"));

    ResourceRichText underTest = context.request().adaptTo(ResourceRichText.class);
    assertTrue(underTest.isValid());
    assertEquals("<p>my <strong>rich</strong> text</p>", underTest.getMarkup());
  }

  @Test
  public void testMissingRichText() {
    context.currentResource(context.create().resource(page, "richtext"));

    ResourceRichText underTest = context.request().adaptTo(ResourceRichText.class);
    assertFalse(underTest.isValid());
    assertNull(underTest.getMarkup());
  }

  @Test
  public void testPlainText() {
    context.currentResource(context.create().resource(page, "richtext",
        "customTextProp", "Line 1\nLine 2"));

    context.request().setAttribute("propertyName", "customTextProp");
    context.request().setAttribute("isRichText", false);

    ResourceRichText underTest = context.request().adaptTo(ResourceRichText.class);
    assertTrue(underTest.isValid());
    assertEquals("Line 1<br />Line 2", underTest.getMarkup());
  }

}
