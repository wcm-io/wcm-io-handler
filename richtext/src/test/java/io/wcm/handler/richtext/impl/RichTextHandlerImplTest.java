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
package io.wcm.handler.richtext.impl;

import static org.junit.Assert.assertEquals;
import io.wcm.handler.commons.dom.Div;
import io.wcm.handler.richtext.RichTextHandler;
import io.wcm.handler.richtext.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit.AemContext;

import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.XMLOutputter;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test {@link RichTextHandler}
 */
public class RichTextHandlerImplTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  private static final String RICHTEXT_FRAGMENT = "<p>Der <strong>Jodelkaiser</strong> "
      + "aus<span/> dem "
      + "<a href='#' data-link-type-id='external' data-link-external-ref='http://www.jodelkaiser.de' "
      + "data-link-window-target='_blank' data-link-window-features='[&quot;resizable&quot;,&quot;toolbar&quot;]'>Ötztal</a> "
      + "ist wieder daheim.</p>";

  private static final String RICHTEXT_FRAGMENT_LEGACY_DATA = "<p>Der <strong>Jodelkaiser</strong> "
      + "aus<span/> dem "
      + "<a href='#' data='{\"linkTypeId\":\"external\",\"linkExternalRef\":\"http://www.jodelkaiser.de\","
      + "\"linkWindowTarget\":\"_blank\",\"linkWindowFeatures\":[\"resizable\",\"toolbar\"]}'>Ötztal</a> "
      + "ist wieder daheim.</p>";

  private static final String RICHTEXT_FRAGMENT_LEGACY_REL = "<p>Der <strong>Jodelkaiser</strong> "
      + "aus<span/> dem "
      + "<a href=\"http://www.jodelkaiser.de\" target=\"_blank\" rel=\"{linktype:'external',linkwindowfeatures:['resizable','toolbar']}\">Ötztal</a> "
      + "ist wieder daheim.</p>";

  private static final String RICHTEXT_FRAGMENT_REWRITTEN = "<p>Der <strong>Jodelkaiser</strong> "
      + "aus<span></span> dem "
      + "<a href=\"http://www.jodelkaiser.de\" target=\"_blank\">Ötztal</a> "
      + "ist wieder daheim.</p>";

  @Test
  public void testAddContentNull() throws JDOMException {
    RichTextHandler richTextHandler = AdaptTo.notNull(context.request(), RichTextHandler.class);
    Div dummy = new Div();
    richTextHandler.addContent(null, dummy);
    assertEquals("<div></div>", dummy.toString());
  }

  @Test(expected = JDOMException.class)
  public void testAddContentIllegal() throws JDOMException {
    RichTextHandler richTextHandler = AdaptTo.notNull(context.request(), RichTextHandler.class);
    Div dummy = new Div();
    richTextHandler.addContent("<wurst", dummy);
  }

  @Test
  public void testAddContentIllegalSuppress() {
    RichTextHandler richTextHandler = AdaptTo.notNull(context.request(), RichTextHandler.class);
    Div dummy = new Div();
    richTextHandler.addContent("<wurst", dummy, true);
    assertEquals("<div></div>", dummy.toString());
  }

  @Test
  public void testAddContent() throws JDOMException {
    RichTextHandler richTextHandler = AdaptTo.notNull(context.request(), RichTextHandler.class);
    Div dummy = new Div();
    richTextHandler.addContent(RICHTEXT_FRAGMENT, dummy);
    assertEquals("<div>" + RICHTEXT_FRAGMENT_REWRITTEN + "</div>", dummy.toString());
  }

  @Test
  public void testGetContent() throws JDOMException {
    RichTextHandler richTextHandler = AdaptTo.notNull(context.request(), RichTextHandler.class);
    List<Content> content = richTextHandler.getContent(RICHTEXT_FRAGMENT);
    assertEquals(1, content.size());
    assertEquals(RICHTEXT_FRAGMENT_REWRITTEN, new XMLOutputter().outputString((Element)content.get(0)));
  }

  @Test
  public void testAddContent_LegacyData() throws JDOMException {
    RichTextHandler richTextHandler = AdaptTo.notNull(context.request(), RichTextHandler.class);
    Div dummy = new Div();
    richTextHandler.addContent(RICHTEXT_FRAGMENT_LEGACY_DATA, dummy);
    assertEquals("<div>" + RICHTEXT_FRAGMENT_REWRITTEN + "</div>", dummy.toString());
  }

  @Test
  public void testGetContent_LegacyData() throws JDOMException {
    RichTextHandler richTextHandler = AdaptTo.notNull(context.request(), RichTextHandler.class);
    List<Content> content = richTextHandler.getContent(RICHTEXT_FRAGMENT_LEGACY_DATA);
    assertEquals(1, content.size());
    assertEquals(RICHTEXT_FRAGMENT_REWRITTEN, new XMLOutputter().outputString((Element)content.get(0)));
  }

  @Test
  public void testAddContent_LegacyRel() throws JDOMException {
    RichTextHandler richTextHandler = AdaptTo.notNull(context.request(), RichTextHandler.class);
    Div dummy = new Div();
    richTextHandler.addContent(RICHTEXT_FRAGMENT_LEGACY_REL, dummy);
    assertEquals("<div>" + RICHTEXT_FRAGMENT_REWRITTEN + "</div>", dummy.toString());
  }

  @Test
  public void testGetContent_LegacyRel() throws JDOMException {
    RichTextHandler richTextHandler = AdaptTo.notNull(context.request(), RichTextHandler.class);
    List<Content> content = richTextHandler.getContent(RICHTEXT_FRAGMENT_LEGACY_REL);
    assertEquals(1, content.size());
    assertEquals(RICHTEXT_FRAGMENT_REWRITTEN, new XMLOutputter().outputString((Element)content.get(0)));
  }

  @Test
  public void testAddPlaintextContent() {
    RichTextHandler richTextHandler = AdaptTo.notNull(context.request(), RichTextHandler.class);
    Div dummy = new Div();
    richTextHandler.addPlaintextContent("Der Jodelkaiser\naus dem Ötztal\nist wieder daheim.", dummy);
    assertEquals("<div>Der Jodelkaiser<br />aus dem Ötztal<br />ist wieder daheim.</div>", dummy.toString());
  }

}
