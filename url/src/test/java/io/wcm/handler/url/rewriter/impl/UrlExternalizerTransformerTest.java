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
package io.wcm.handler.url.rewriter.impl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.wcm.handler.url.integrator.IntegratorHandler;
import io.wcm.handler.url.testcontext.AppAemContext;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Transformer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

@RunWith(MockitoJUnitRunner.class)
public class UrlExternalizerTransformerTest {

  @Rule
  public AemContext context = AppAemContext.newAemContext();

  @Mock
  private ContentHandler contentHandler;
  @Mock
  private ProcessingContext processingContext;
  @Mock
  private ProcessingComponentConfiguration processingComponentConfiguration;

  private Transformer underTest;

  @Before
  public void setUp() {
    when(processingContext.getRequest()).thenReturn(context.request());
    when(processingComponentConfiguration.getConfiguration()).thenReturn(ImmutableValueMap.builder()
        .put(UrlExternalizerTransformerConfig.PN_REWRITE_ELEMENTS, new String[] {
            "element1:attr1",
            "element2:attr2"
        })
        .build());

    UrlExternalizerTransformerFactory factory = context.registerInjectActivateService(new UrlExternalizerTransformerFactory());

    underTest = factory.createTransformer();
    underTest.setContentHandler(contentHandler);
  }

  @After
  public void tearDown() {
    underTest.dispose();
  }

  @Test
  public void testRewriteUnknownElement() {
    callTransformer("unknownElement", "attr1", "/my/url");
    verifyTransformer("unknownElement", "attr1", "/my/url");
  }

  @Test
  public void testRewriteUnknownElement_IntegratorTemplateMode() {
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    callTransformer("unknownElement", "attr1", "/my/url");
    verifyTransformer("unknownElement", "attr1", "/my/url");
  }

  @Test
  public void testRewriteKnownElement() {
    callTransformer("element1", "attr1", "/my/url");
    verifyTransformer("element1", "attr1", "/my/url");
  }

  @Test
  public void testRewriteKnownElement_IntegratorTemplateMode() {
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    callTransformer("element1", "attr1", "/my/url");
    verifyTransformer("element1", "attr1", "http://de.dummysite.org/my/url");
  }

  @Test
  public void testRewriteKnownElement_MissingAttr_IntegratorTemplateMode() {
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    callTransformer("element1", "attr5", "/my/url");
    verifyTransformer("element1", "attr5", "/my/url");
  }

  @Test
  public void testRewriteKnownElement_EmptyAttr_IntegratorTemplateMode() {
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    callTransformer("element1", "attr1", "");
    verifyTransformer("element1", "attr1", "");
  }

  @Test
  public void testRewriteWithSpecialChars() {
    callTransformer("element1", "attr1", "/my/url%20with%20space?param1=value%20with%20space&param2=with%26amp");
    verifyTransformer("element1", "attr1", "/my/url%20with%20space?param1=value%20with%20space&param2=with%26amp");
  }

  @Test
  public void testRewriteWithSpecialCharsAndHtmlEscaping() {
    callTransformer("element1", "attr1", "/my/url%20with%20space?param1=value%20with%20space&amp;param2=with%26amp");
    verifyTransformer("element1", "attr1", "/my/url%20with%20space?param1=value%20with%20space&amp;param2=with%26amp");
  }

  @Test
  public void testRewriteAnchorOnly() {
    callTransformer("element1", "attr1", "#my-anchor");
    verifyTransformer("element1", "attr1", "#my-anchor");
  }

  private void callTransformer(String elementName, String... attributes) {
    try {
      underTest.init(processingContext, processingComponentConfiguration);
      underTest.startElement(null, elementName, null, toAttributes(attributes));
    }
    catch (IOException | SAXException ex) {
      throw new RuntimeException(ex);
    }
  }

  private void verifyTransformer(String elementName, String... attributes) {
    try {
      verify(contentHandler).startElement(null, elementName, null, toAttributes(attributes));
    }
    catch (SAXException ex) {
      throw new RuntimeException(ex);
    }
  }

  private Attributes toAttributes(String... attributes) {
    Map<String, String> attributesMap = new HashMap<>();
    for (int i = 0; i < attributes.length - 1; i += 2) {
      attributesMap.put(attributes[i], attributes[i + 1]);
    }
    ComparableAttributes attrs = new ComparableAttributes();
    for (Map.Entry<String, String> entry : attributesMap.entrySet()) {
      attrs.addAttribute(null, entry.getKey(), entry.getKey(), "xs:string", entry.getValue());
    }
    return attrs;
  }

}
