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

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.handler.richtext.RichTextNameConstants;
import io.wcm.handler.richtext.testcontext.AppAemContext;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

public class ResourceMultilineTextTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  private Resource simpleTextResource;
  private Resource textWithLineBreaksResource;
  private Resource textWithMarkupResource;
  private Resource invalidTextResource;

  @Before
  public void setUp() {
    context.create().page(ROOTPATH_CONTENT + "/page1");

    simpleTextResource = context.create().resource(ROOTPATH_CONTENT + "/page1/jcr:content/simpleText",
        ImmutableValueMap.builder()
        .put(RichTextNameConstants.PN_TEXT, "Simple Text")
        .build());

    textWithLineBreaksResource = context.create().resource(ROOTPATH_CONTENT + "/page1/jcr:content/textWithLineBreaks",
        ImmutableValueMap.builder()
        .put(RichTextNameConstants.PN_TEXT, "Simple Text\nwith\nLine Breaks")
        .build());

    textWithMarkupResource = context.create().resource(ROOTPATH_CONTENT + "/page1/jcr:content/textWithMarkup",
        ImmutableValueMap.builder()
        .put(RichTextNameConstants.PN_TEXT, "<p>Text with <strong>Markup</strong></p>")
        .build());

    invalidTextResource = context.create().resource(ROOTPATH_CONTENT + "/page1/jcr:content/invalidText",
        ImmutableValueMap.builder()
        .build());
  }

  @Test
  public void testSimpleText() {
    context.currentResource(simpleTextResource);
    ResourceMultilineText underTest = context.request().adaptTo(ResourceMultilineText.class);
    assertTrue(underTest.isValid());
    assertEquals("Simple Text", underTest.getMarkup());
  }

  @Test
  public void testTextWithLineBreaks() {
    context.currentResource(textWithLineBreaksResource);
    ResourceMultilineText underTest = context.request().adaptTo(ResourceMultilineText.class);
    assertTrue(underTest.isValid());
    assertEquals("Simple Text<br />with<br />Line Breaks", underTest.getMarkup());
  }

  @Test
  public void testTextWithMarkupResource() {
    context.currentResource(textWithMarkupResource);
    ResourceMultilineText underTest = context.request().adaptTo(ResourceMultilineText.class);
    assertTrue(underTest.isValid());
    assertEquals("&lt;p&gt;Text with &lt;strong&gt;Markup&lt;/strong&gt;&lt;/p&gt;", underTest.getMarkup());
  }

  @Test
  public void testInvalidText() {
    context.currentResource(invalidTextResource);
    ResourceMultilineText underTest = context.request().adaptTo(ResourceMultilineText.class);
    assertFalse(underTest.isValid());
    assertNull(underTest.getMarkup());
  }

}
