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
package io.wcm.handler.richtext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.jdom2.Content;
import org.jdom2.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import io.wcm.handler.commons.dom.Div;
import io.wcm.handler.commons.dom.Span;

class RichTextTest {

  private RichTextRequest richTextRequest;

  @BeforeEach
  void setUp() {
    richTextRequest = new RichTextRequest(null, null, null, null, null);
  }

  @Test
  void testWithContent() {
    List<Content> content = ImmutableList.<Content>of(new Span("wurst"), new Text("  "), new Div().setCssClass("abc"));
    RichText underTest = new RichText(richTextRequest, content);

    assertSame(richTextRequest, underTest.getRichTextRequest());
    assertTrue(underTest.isValid());
    assertEquals(content, underTest.getContent());
    assertEquals("<span>wurst</span>  <div class=\"abc\"></div>", underTest.getMarkup());
  }

  @Test
  void testWithoutContent() {
    RichText underTest = new RichText(richTextRequest, ImmutableList.<Content>of());

    assertFalse(underTest.isValid());
    assertTrue(underTest.getContent().isEmpty());
    assertNull(underTest.getMarkup());
  }

  @Test
  void testWithNullContent() {
    RichText underTest = new RichText(richTextRequest, null);

    assertFalse(underTest.isValid());
    assertNull(underTest.getContent());
    assertNull(underTest.getMarkup());
  }

  @Test
  void testToString() throws Exception {
    RichText underTest = new RichText(richTextRequest, null);
    assertEquals("RichText[richTextRequest=RichTextRequest[]]", underTest.toString());
  }

}
