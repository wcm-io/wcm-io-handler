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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;

public class RichTextUtilTest {

  @Test
  public void testIsEmpty() {

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
  public void testParseText() throws Exception {

    // parse valid xhtml
    Element element = RichTextUtil.parseText("<p>Der <strong>Jodelkaiser</strong></p>"
        + "<p align=\"center\">aus dem Ötztal</p>");

    assertNotNull(element);
    assertEquals("child-count", 2, element.getContentSize());

    Element p1 = (Element)element.getContent().get(0);
    assertEquals("p1-name", "p", p1.getName());
    assertEquals("p1-child-count", 2, p1.getContentSize());
    assertEquals("p1-text", "Der ", ((Text)p1.getContent().get(0)).getText());
    Element strong = (Element)p1.getContent().get(1);
    assertEquals("strong-name", "strong", strong.getName());
    assertEquals("strong-text", "Jodelkaiser", strong.getText());
    assertEquals("p1-attrs", 0, p1.getAttributes().size());

    Element p2 = (Element)element.getContent().get(1);
    assertEquals("p2-name", "p", p2.getName());
    assertEquals("p2-child-count", 1, p2.getContentSize());
    assertEquals("p2-text", "aus dem Ötztal", ((Text)p2.getContent().get(0)).getText());
    assertEquals("p2-attrs", 1, p2.getAttributes().size());
    assertEquals("p2-align", "center", p2.getAttributeValue("align"));

    // parse invalid xhtml
    boolean exception = false;
    try {
      RichTextUtil.parseText("Der <br>Jodelkaiser");
    }
    catch (JDOMException ex) {
      exception = true;
    }
    assertTrue("invalid-xhtml", exception);

  }

  @Test
  public void testRewriteContent() throws Exception {

    assertEquals("empty", "", rewriteContent(""));

    assertEquals("nop",
        "<test1 /><test2 />",
        rewriteContent("<test1 /><test2 />"));

    assertEquals("to-remove",
        "<test1 /><test2 />",
        rewriteContent("<test1 /><to-remove /><test2 />"));

    assertEquals("to-keep",
        "<test1 /><to-keep /><test2 />",
        rewriteContent("<test1 /><to-keep /><test2 />"));

    assertEquals("to-keep-attribute",
        "<test1 /><to-keep-attribute attr=\"testx\" /><test2 />",
        rewriteContent("<test1 /><to-keep-attribute /><test2 />"));

    assertEquals("to-replace-single",
        "<test1 /><replaced-element /><test2 />",
        rewriteContent("<test1 /><to-replace-single /><test2 />"));

    assertEquals("to-replace-multiple",
        "<test1 /><replaced-element-1 /><replaced-element-2 /><test2 />",
        rewriteContent("<test1 /><to-replace-multiple /><test2 />"));

    assertEquals("to-replace-single-children-1",
        "<test1 /><replaced-element><test-3 /></replaced-element><test2 />",
        rewriteContent("<test1 /><to-replace-single><test-3 /></to-replace-single><test2 />"));

    assertEquals("to-replace-single-children-2",
        "<test1 /><replaced-element><test-3 /><test-4 /></replaced-element><test2 />",
        rewriteContent("<test1 /><to-replace-single><test-3 /><to-remove /><test-4 /></to-replace-single><test2 />"));

    assertEquals("to-replace-single-commeont",
        "<test1 /><replaced-element /><!-- comment --><test2 />",
        rewriteContent("<test1 /><to-replace-single /><!-- comment --><test2 />"));

  }

  @Test
  public void testRewriteContentOnce() throws Exception {

    assertEquals("to-replace-single",
        "<test1 /><replaced-element-once /><test2 />",
        rewriteContent("<test1 /><to-replace-once /><test2 />"));

    assertEquals("to-replace-single",
        "<test1 /><replaced-element-once /><to-replace-once /><test2 />",
        rewriteContent("<test1 /><to-replace-once /><to-replace-once /><test2 />"));

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

  @Test
  public void testXhtmlEntities() throws Exception {

    Element element = RichTextUtil.parseText("Der Jodelkaiser aus dem &Ouml;tztal.", true);
    assertEquals("text", "Der Jodelkaiser aus dem Ötztal.", element.getText());

  }

}
