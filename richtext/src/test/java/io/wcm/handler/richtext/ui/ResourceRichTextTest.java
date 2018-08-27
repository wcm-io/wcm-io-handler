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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.handler.richtext.RichTextNameConstants;
import io.wcm.handler.richtext.testcontext.AppAemContext;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

@SuppressWarnings("null")
public class ResourceRichTextTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  private Resource validRichtextResource;
  private Resource invalidRichtextResource;

  @Before
  public void setUp() {
    context.create().page(ROOTPATH_CONTENT + "/page1");

    validRichtextResource = context.create().resource(ROOTPATH_CONTENT + "/page1/jcr:content/validRichtext",
        ImmutableValueMap.builder()
        .put(RichTextNameConstants.PN_TEXT, "<p>my <strong>rich</strong> text</p>")
        .build());

    invalidRichtextResource = context.create().resource(ROOTPATH_CONTENT + "/page1/jcr:content/invalidRichtext",
        ImmutableValueMap.builder()
        .build());
  }

  @Test
  public void testRichText() {
    context.currentResource(validRichtextResource);
    ResourceRichText underTest = context.request().adaptTo(ResourceRichText.class);
    assertTrue(underTest.isValid());
    assertTrue(StringUtils.isNotBlank(underTest.getMarkup()));
  }

  @Test
  public void testInvalidRichText() {
    context.currentResource(invalidRichtextResource);
    ResourceRichText underTest = context.request().adaptTo(ResourceRichText.class);
    assertFalse(underTest.isValid());
    assertNull(underTest.getMarkup());
  }

}
