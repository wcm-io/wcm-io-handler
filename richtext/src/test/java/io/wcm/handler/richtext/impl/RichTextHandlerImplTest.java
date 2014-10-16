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
import static org.junit.Assert.assertNull;
import io.wcm.handler.richtext.RichText;
import io.wcm.handler.richtext.RichTextHandler;
import io.wcm.handler.richtext.RichTextNameConstants;
import io.wcm.handler.richtext.TextMode;
import io.wcm.handler.richtext.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
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

  private static final String PLAINTEXT_FRAGMENT = "Der Jodelkaiser\naus dem Ötztal\nist wieder daheim.";
  private static final String PLAINTEXT_FRAGMENT_REWRITTEN = "Der Jodelkaiser<br />aus dem Ötztal<br />ist wieder daheim.";

  protected Adaptable adaptable() {
    return context.request();
  }

  @Test
  public void testNull() {
    RichTextHandler richTextHandler = AdaptTo.notNull(adaptable(), RichTextHandler.class);
    RichText richText = richTextHandler.get((String)null).build();
    assertNull(richText.getMarkup());
  }

  @Test
  public void testContentIllegal() {
    RichTextHandler richTextHandler = AdaptTo.notNull(adaptable(), RichTextHandler.class);
    RichText richText = richTextHandler.get("<wurst").build();
    assertNull(richText.getMarkup());
  }

  @Test
  public void testContent() {
    RichTextHandler richTextHandler = AdaptTo.notNull(adaptable(), RichTextHandler.class);
    RichText richText = richTextHandler.get(RICHTEXT_FRAGMENT).build();
    assertEquals(RICHTEXT_FRAGMENT_REWRITTEN, richText.getMarkup());
  }

  @Test
  public void testContent_LegacyData() {
    RichTextHandler richTextHandler = AdaptTo.notNull(adaptable(), RichTextHandler.class);
    RichText richText = richTextHandler.get(RICHTEXT_FRAGMENT_LEGACY_DATA).build();
    assertEquals(RICHTEXT_FRAGMENT_REWRITTEN, richText.getMarkup());
  }

  @Test
  public void testContent_LegacyRel() {
    RichTextHandler richTextHandler = AdaptTo.notNull(adaptable(), RichTextHandler.class);
    RichText richText = richTextHandler.get(RICHTEXT_FRAGMENT_LEGACY_REL).build();
    assertEquals(RICHTEXT_FRAGMENT_REWRITTEN, richText.getMarkup());
  }

  @Test
  public void testPlainTextContent() {
    RichTextHandler richTextHandler = AdaptTo.notNull(adaptable(), RichTextHandler.class);
    RichText richText = richTextHandler.get(PLAINTEXT_FRAGMENT).textMode(TextMode.PLAIN).build();
    assertEquals(PLAINTEXT_FRAGMENT_REWRITTEN, richText.getMarkup());
  }

  @Test
  public void testContentFromResource() {
    RichTextHandler richTextHandler = AdaptTo.notNull(adaptable(), RichTextHandler.class);

    Resource resource = context.create().resource("/text/resource",
        ImmutableValueMap.of(RichTextNameConstants.PN_TEXT, RICHTEXT_FRAGMENT));
    RichText richText = richTextHandler.get(resource).build();
    assertEquals(RICHTEXT_FRAGMENT_REWRITTEN, richText.getMarkup());
  }

  @Test
  public void testPlainTextContentFromResource() {
    RichTextHandler richTextHandler = AdaptTo.notNull(adaptable(), RichTextHandler.class);

    Resource resource = context.create().resource("/text/resource",
        ImmutableValueMap.of(RichTextNameConstants.PN_TEXT, PLAINTEXT_FRAGMENT,
            RichTextNameConstants.PN_TEXT_IS_RICH, false));
    RichText richText = richTextHandler.get(resource).build();
    assertEquals(PLAINTEXT_FRAGMENT_REWRITTEN, richText.getMarkup());
  }

}
