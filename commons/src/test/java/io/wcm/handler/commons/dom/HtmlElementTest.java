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
package io.wcm.handler.commons.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;

import org.jdom2.Content;
import org.jdom2.Namespace;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class HtmlElementTest {

  private static final Namespace NS_TEST = Namespace.getNamespace("test", "http://test");

  private HtmlElement<?> underTest;

  @Before
  public void setUp() {
    underTest = new HtmlElement("test");
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testName() {
    assertEquals("test", underTest.getName());

    underTest.setName("test2");
    assertEquals("test2", underTest.getName());
  }

  @Test
  public void testAttributeValueAsInteger() {
    underTest.setAttributeValueAsInteger("attr1", 25);
    assertEquals(25, underTest.getAttributeValueAsInteger("attr1"));
  }

  @Test
  public void testAddContent() {
    underTest.addContent(ImmutableSet.of(new Image(), new Div()));
    underTest.addContent((Collection)null);

    underTest.addContent(new Image());
    underTest.addContent((Content)null);

    underTest.addContent("text");
    underTest.addContent((String)null);

    underTest.addContent(0, ImmutableSet.of(new Image(), new Div()));
    underTest.addContent(0, (Collection)null);

    underTest.addContent(0, new Image());
    underTest.addContent(0, (Content)null);

    underTest.add(new Image());
  }

  @Test
  public void testText() {
    underTest.setText("abc");
    assertEquals("abc", underTest.getText());

    underTest.setText(null);
    assertEquals("", underTest.getText());
  }

  @Test
  public void testSetAttribute() {
    underTest.setAttribute("attr1", "value1");
    underTest.setAttribute("attr1", null);

    underTest.setAttribute("attr1", "value1", NS_TEST);
    underTest.setAttribute("attr1", null, NS_TEST);
  }

  @Test
  public void testCommonAttributes() {
    underTest.setId("id1");
    assertEquals("id1", underTest.getId());

    underTest.setTitle("title1");
    assertEquals("title1", underTest.getTitle());
  }

  @Test
  public void testCssClass() {
    assertNull(underTest.getCssClass());

    underTest.addCssClass("class1");
    assertEquals("class1", underTest.getCssClass());

    underTest.addCssClass("class2");
    assertEquals("class1 class2", underTest.getCssClass());

    underTest.setCssClass("class3");
    assertEquals("class3", underTest.getCssClass());

    underTest.setCssClass(null);
    assertNull(underTest.getCssClass());
  }

  @Test
  public void testStyle() {
    assertNull(underTest.getStyleString());

    underTest.setStyleString("test1:value1;test2:value2;");
    assertEquals("value1", underTest.getStyle("test1"));

    underTest.setStyle("test2", "value21");
    assertEquals("test1:value1;test2:value21;", underTest.getStyleString());
  }

  @Test
  public void testData() {
    underTest.setData("attr1", "value1");
    assertEquals("value1", underTest.getData("attr1"));

    underTest.setData("attr1", null);
    assertNull(underTest.getData("attr1"));
  }

  @Test
  public void testToString() {
    underTest.setId("id1");
    underTest.setData("attr1", "value1");
    underTest.addContent(new Span("abc"));
    assertEquals("<test id=\"id1\" data-attr1=\"value1\"><span>abc</span></test>", underTest.toString());
    assertEquals("<span>abc</span>", underTest.toStringContentOnly());
  }

  @Test
  public void testFactory() {
    underTest.create("sub1");
    underTest.createComment("text");
    underTest.createDiv();
    underTest.createSpan();
    underTest.createSpan("text");
    underTest.createAnchor();
    underTest.createAnchor("href");
    underTest.createAnchor("href", "target");
    underTest.createImage();
    underTest.createImage("src");
    underTest.createImage("src", "alt");
    underTest.createImage("src", 20, 30);
    underTest.createImage("src", "alt", 20, 30);
    underTest.createScript();
    underTest.createScript("text");
    underTest.createNoScript();
    underTest.createFigure();
    underTest.createFigCaption();
    underTest.createVideo();
    underTest.createAudio();
    underTest.createSource();
  }

}
