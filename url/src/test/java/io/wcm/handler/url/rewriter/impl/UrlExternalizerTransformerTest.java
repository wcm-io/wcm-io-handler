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

import com.google.common.collect.ImmutableMap;

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
  public void testRewriteUnknownElement() throws Exception {
    callTransformer("unknownElement", ImmutableMap.of("attr1", "/my/url"));
    verifyTransformer("unknownElement", ImmutableMap.of("attr1", "/my/url"));
  }

  @Test
  public void testRewriteUnknownElement_IntegratorTemplateMode() throws Exception {
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    callTransformer("unknownElement", ImmutableMap.of("attr1", "/my/url"));
    verifyTransformer("unknownElement", ImmutableMap.of("attr1", "/my/url"));
  }

  @Test
  public void testRewriteKnownElement() throws Exception {
    callTransformer("element1", ImmutableMap.of("attr1", "/my/url"));
    verifyTransformer("element1", ImmutableMap.of("attr1", "/my/url"));
  }

  @Test
  public void testRewriteKnownElement_IntegratorTemplateMode() throws Exception {
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    callTransformer("element1", ImmutableMap.of("attr1", "/my/url"));
    verifyTransformer("element1", ImmutableMap.of("attr1", "http://de.dummysite.org/my/url"));
  }

  @Test
  public void testRewriteKnownElement_MissingAttr_IntegratorTemplateMode() throws Exception {
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    callTransformer("element1", ImmutableMap.of("attr5", "/my/url"));
    verifyTransformer("element1", ImmutableMap.of("attr5", "/my/url"));
  }

  @Test
  public void testRewriteKnownElement_EmptyAttr_IntegratorTemplateMode() throws Exception {
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    callTransformer("element1", ImmutableMap.of("attr1", ""));
    verifyTransformer("element1", ImmutableMap.of("attr1", ""));
  }

  private void callTransformer(String elementName, Map<String, String> attributes) throws Exception {
    underTest.init(processingContext, processingComponentConfiguration);
    underTest.startElement(null, elementName, null, toAttributes(attributes));
  }

  private void verifyTransformer(String elementName, Map<String, String> attributes) throws Exception {
    verify(contentHandler).startElement(null, elementName, null, toAttributes(attributes));
  }

  private Attributes toAttributes(Map<String, String> attributes) {
    ComparableAttributes attrs = new ComparableAttributes();
    for (Map.Entry<String, String> entry : attributes.entrySet()) {
      attrs.addAttribute(null, entry.getKey(), entry.getKey(), "xs:string", entry.getValue());
    }
    return attrs;
  }

}
