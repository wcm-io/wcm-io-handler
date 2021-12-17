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
package io.wcm.handler.richtext.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("null")
class RichTextUtilTest {

  @Test
  void testIsEmpty() {

    assertTrue(RichTextUtil.isEmpty(null));
    assertTrue(RichTextUtil.isEmpty(""));
    assertTrue(RichTextUtil.isEmpty(" "));
    assertTrue(RichTextUtil.isEmpty("    "));
    assertTrue(RichTextUtil.isEmpty("  \n \n  "));

    assertTrue(RichTextUtil.isEmpty("<p></p>"));
    assertTrue(RichTextUtil.isEmpty("<p>   </p>"));
    assertTrue(RichTextUtil.isEmpty("<p>\n  \r</p>"));
    assertTrue(RichTextUtil.isEmpty("   <p></p>  "));
    assertTrue(RichTextUtil.isEmpty(" <p>\n  \r</p>  "));

    assertTrue(RichTextUtil.isEmpty("<p>&nbsp;</p>"));
    assertTrue(RichTextUtil.isEmpty("<p>&#160;</p>"));

    assertFalse(RichTextUtil.isEmpty("a"));
    assertFalse(RichTextUtil.isEmpty("<p>a</p>"));
    assertFalse(RichTextUtil.isEmpty("<br/>"));

    // this is too long (more than default = 20 characters)
    assertFalse(RichTextUtil.isEmpty("<p>                                                            </p>"));

  }

  @Test
  void testParseText() throws Exception {

    // parse valid xhtml
    Element element = RichTextUtil.parseText("<p>Der <strong>Jodelkaiser</strong></p>"
        + "<p align=\"center\">aus dem Ötztal</p>");

    assertNotNull(element);
    assertEquals(2, element.getContentSize(), "child-count");

    Element p1 = (Element)element.getContent().get(0);
    assertEquals("p", p1.getName(), "p1-name");
    assertEquals(2, p1.getContentSize(), "p1-child-count");
    assertEquals("Der ", ((Text)p1.getContent().get(0)).getText(), "p1-text");
    Element strong = (Element)p1.getContent().get(1);
    assertEquals("strong", strong.getName(), "strong-name");
    assertEquals("Jodelkaiser", strong.getText(), "strong-text");
    assertEquals(0, p1.getAttributes().size(), "p1-attrs");

    Element p2 = (Element)element.getContent().get(1);
    assertEquals("p", p2.getName(), "p2-name");
    assertEquals(1, p2.getContentSize(), "p2-child-count");
    assertEquals("aus dem Ötztal", ((Text)p2.getContent().get(0)).getText(), "p2-text");
    assertEquals(1, p2.getAttributes().size(), "p2-attrs");
    assertEquals("center", p2.getAttributeValue("align"), "p2-align");

    // parse invalid xhtml
    boolean exception = false;
    try {
      RichTextUtil.parseText("Der <br>Jodelkaiser");
    }
    catch (JDOMException ex) {
      exception = true;
    }
    assertTrue(exception, "invalid-xhtml");

  }

  @Test
  void testRewriteContent() throws Exception {

    assertEquals("", rewriteContent(""),
        "empty");

    assertEquals("<test1 /><test2 />",
        rewriteContent("<test1 /><test2 />"),
        "nop");

    assertEquals("<test1 /><test2 />",
        rewriteContent("<test1 /><to-remove /><test2 />"),
        "to-remove");

    assertEquals("<test1 /><to-keep /><test2 />",
        rewriteContent("<test1 /><to-keep /><test2 />"),
        "to-keep");

    assertEquals("<test1 /><to-keep-attribute attr=\"testx\" /><test2 />",
        rewriteContent("<test1 /><to-keep-attribute /><test2 />"),
        "to-keep-attribute");

    assertEquals("<test1 /><replaced-element /><test2 />",
        rewriteContent("<test1 /><to-replace-single /><test2 />"),
        "to-replace-single");

    assertEquals("<test1 /><replaced-element-1 /><replaced-element-2 /><test2 />",
        rewriteContent("<test1 /><to-replace-multiple /><test2 />"),
        "to-replace-multiple");

    assertEquals("<test1 /><replaced-element><test-3 /></replaced-element><test2 />",
        rewriteContent("<test1 /><to-replace-single><test-3 /></to-replace-single><test2 />"),
        "to-replace-single-children-1");

    assertEquals("<test1 /><replaced-element><test-3 /><test-4 /></replaced-element><test2 />",
        rewriteContent("<test1 /><to-replace-single><test-3 /><to-remove /><test-4 /></to-replace-single><test2 />"),
        "to-replace-single-children-2");

    assertEquals("<test1 /><replaced-element /><!-- comment --><test2 />",
        rewriteContent("<test1 /><to-replace-single /><!-- comment --><test2 />"),
        "to-replace-single-commeont");

  }

  @Test
  void testRewriteContentOnce() throws Exception {

    assertEquals("<test1 /><replaced-element-once /><test2 />",
        rewriteContent("<test1 /><to-replace-once /><test2 />"));

    assertEquals("<test1 /><replaced-element-once /><to-replace-once /><test2 />",
        rewriteContent("<test1 /><to-replace-once /><to-replace-once /><test2 />"));

  }

  @Test
  void testXhtmlEntities() throws Exception {

    Element element = RichTextUtil.parseText("<span title=\"Das &Ouml;tztal\">Der Jodelkaiser aus dem &Ouml;tztal.</span>", true);
    Element span = element.getChild("span");
    assertEquals("Der Jodelkaiser aus dem Ötztal.", span.getText());
    assertEquals("Das Ötztal", span.getAttributeValue("title"));

  }

  @Test
  void testRemovalOfUnallowedChars() throws Exception {
    // build a string with all invalid control characters < #x20
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 0x20; i++) {
      sb.append((char)i);
    }
    // add some more unicode chars that should not be stripped out
    sb.append("abcäöü€😁🇿🇼🚁");

    Element element = RichTextUtil.parseText(sb.toString(), true);

    // ensure only allowed are in the actual XHTML string
    assertEquals("\t\nabcäöü€😁🇿🇼🚁", element.getText());
  }

  /**
   * It is never allowed to inject DOCTYPE declarations into XHTML that is parsed.
   * This prevents XXE attacks: https://portswigger.net/web-security/xxe
   * @param xhtmlEntities Parse with XHTML entities
   */
  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void testInjectDoctype_WithoutXhtmlEntities(boolean xhtmlEntities) throws Exception {
    String xml = "<!DOCTYPE foo [ <!ELEMENT foo ANY > ]>\n"
        + "<foo></foo>";

    // ensure our test XML is valid and could be parsed by an unprotected SAXBuilder
    SAXBuilder ownSaxBuilder = new SAXBuilder();
    assertNotNull(ownSaxBuilder.build(new StringReader(xml)));

    // but parsing XML with any DOCTYPE declaration using RichTextUtil will fail
    // because it's always wrapped in a <root>...</root> element
    assertThrows(JDOMException.class, () -> {
      RichTextUtil.parseText(xml, xhtmlEntities);
    });
  }

  private String rewriteContent(String input) throws Exception {
    Element root = RichTextUtil.parseText(input);
    RichTextUtil.rewriteContent(root, new TestRewriteContentHandler());
    return toStringContentOnly(root);
  }

  /**
   * Serializes all content/children of this element.
   * @return Serialized content.
   */
  private String toStringContentOnly(Element element) {
    StringBuilder sb = new StringBuilder();
    XMLOutputter xmlOutputter = new XMLOutputter();
    for (Object content : element.getContent()) {
      if (content instanceof org.jdom2.Element) {
        sb.append(xmlOutputter.outputString((org.jdom2.Element)content));
      }
      else if (content instanceof org.jdom2.Text) {
        sb.append(xmlOutputter.outputString((org.jdom2.Text)content));
      }
      else if (content instanceof org.jdom2.CDATA) {
        sb.append(xmlOutputter.outputString((org.jdom2.CDATA)content));
      }
      else if (content instanceof org.jdom2.Comment) {
        sb.append(xmlOutputter.outputString((org.jdom2.Comment)content));
      }
      else if (content instanceof org.jdom2.EntityRef) {
        sb.append(xmlOutputter.outputString((org.jdom2.EntityRef)content));
      }
    }
    return sb.toString();
  }

  static class TestRewriteContentHandler implements RewriteContentHandler {

    private boolean mReplaceOnce;

    @Override
    public List<Content> rewriteElement(Element element) {

      if (StringUtils.equals(element.getName(), "to-remove")) {
        List<Content> content = new ArrayList<Content>();
        return content;
      }

      else if (StringUtils.equals(element.getName(), "to-keep")) {
        List<Content> content = new ArrayList<Content>();
        content.add(element);
        return content;
      }

      else if (StringUtils.equals(element.getName(), "to-keep-attribute")) {
        List<Content> content = new ArrayList<Content>();
        element.setAttribute("attr", "testx");
        content.add(element);
        return content;
      }

      else if (StringUtils.equals(element.getName(), "to-replace-single")) {
        List<Content> content = new ArrayList<Content>();
        content.add(new Element("replaced-element").addContent(element.cloneContent()));
        return content;
      }

      else if (StringUtils.equals(element.getName(), "to-replace-multiple")) {
        List<Content> content = new ArrayList<Content>();
        content.add(new Element("replaced-element-1"));
        content.add(new Element("replaced-element-2"));
        return content;
      }

      else if (StringUtils.equals(element.getName(), "to-replace-once")) {
        if (!mReplaceOnce) {
          List<Content> content = new ArrayList<Content>();
          content.add(new Element("replaced-element-once").addContent(element.cloneContent()));
          mReplaceOnce = true;
          return content;
        }
        else {
          return null;
        }
      }

      return null;
    }

    @Override
    public List<Content> rewriteText(Text text) {
      // noting to do
      return null;
    }

  }

}
