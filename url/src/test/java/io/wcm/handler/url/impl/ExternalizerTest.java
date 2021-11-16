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
package io.wcm.handler.url.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import io.wcm.handler.url.integrator.IntegratorPlaceholder;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class ExternalizerTest {

  private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

  @Test
  void testExternalizeUrl_SimplePath() {
    assertExternalizeWithoutRequest("/the/path", "/the/path");
    assertExternalizeWithoutRequest("/the/path?param=1", "/the/path?param=1");
    assertExternalizeWithoutRequest("/the/path#hash", "/the/path#hash");
  }

  @Test
  void testExternalizeUrl_MangleNamespaces() {
    assertExternalizeWithoutRequest("/the/_ns_path", "/the/ns:path");
    assertExternalizeWithoutRequest("/the/_ns_path?ns:param=1", "/the/ns:path?ns:param=1");
    assertExternalizeWithoutRequest("/the/_ns_path#ns:hash", "/the/ns:path#ns:hash");
  }

  @Test
  void testExternalizeUrl_ServletContextPath() {
    context.request().setContextPath("/context");

    // use mocked resolver because request context path change is not respected by sling-mock resolver
    ResourceResolver mockedResolver = mock(ResourceResolver.class);
    when(mockedResolver.map(same(context.request()), anyString())).then(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) {
        return "/context" + (String)invocation.getArguments()[1];
      }
    });

    assertExternalize("/context/the/path", "/the/path", mockedResolver);
    assertExternalize("/context/the/path?param=1", "/the/path?param=1", mockedResolver);
    assertExternalize("/context/the/path#hash", "/the/path#hash", mockedResolver);
  }

  @Test
  void testExternalizeUrl_SpecialChars() {
    assertExternalize("/the/path%20with%20spaces", "/the/path with spaces");
    assertExternalize("/the/path%20with%20spaces?param with spaces=1", "/the/path with spaces?param with spaces=1");
    assertExternalize("/the/path%20with%20spaces#hash with spaces", "/the/path with spaces#hash with spaces");

    assertExternalize("/the/path%C3%A4%C3%B6%C3%BC%C3%9F%E2%82%AC", "/the/pathäöüß€");
    assertExternalize("/the/path%C3%A4%C3%B6%C3%BC%C3%9F%E2%82%AC?paramäöüß€=1", "/the/pathäöüß€?paramäöüß€=1");
    assertExternalize("/the/path%C3%A4%C3%B6%C3%BC%C3%9F%E2%82%AC#hashäöüß€", "/the/pathäöüß€#hashäöüß€");
  }

  @Test
  void testExternalizeUrl_SpecialChars_WithoutReqeust() {
    assertExternalizeWithoutRequest("/the/path%20with%20spaces", "/the/path with spaces");
    assertExternalizeWithoutRequest("/the/path%20with%20spaces?param with spaces=1", "/the/path with spaces?param with spaces=1");
    assertExternalizeWithoutRequest("/the/path%20with%20spaces#hash with spaces", "/the/path with spaces#hash with spaces");

    assertExternalizeWithoutRequest("/the/path%C3%A4%C3%B6%C3%BC%C3%9F%E2%82%AC", "/the/pathäöüß€");
    assertExternalizeWithoutRequest("/the/path%C3%A4%C3%B6%C3%BC%C3%9F%E2%82%AC?paramäöüß€=1", "/the/pathäöüß€?paramäöüß€=1");
    assertExternalizeWithoutRequest("/the/path%C3%A4%C3%B6%C3%BC%C3%9F%E2%82%AC#hashäöüß€", "/the/pathäöüß€#hashäöüß€");
  }

  private void assertExternalize(String expected, String url) {
    assertExternalize(expected, url, context.resourceResolver());
  }

  private void assertExternalize(String expected, String url, ResourceResolver resolver) {
    assertEquals(expected, Externalizer.externalizeUrl(url, resolver, context.request()));
    assertEquals(expected, Externalizer.externalizeUrlWithoutMapping(url, context.request()));
  }

  private void assertExternalizeWithoutRequest(String expected, String url) {
    assertEquals(expected, Externalizer.externalizeUrl(url, context.resourceResolver(), null));
    assertEquals(expected, Externalizer.externalizeUrlWithoutMapping(url, null));
  }

  @Test
  void testExternalizeUrlWithHost() {

    // use mocked resolver to mock sling mapping
    ResourceResolver mockedResolver = mock(ResourceResolver.class);
    when(mockedResolver.map(same(context.request()), anyString())).then(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) {
        return "http://www.domain.com/context" + (String)invocation.getArguments()[1];
      }
    });

    assertExternalizeUrlWithHost("http://www.domain.com/context/the/path", "/the/path", mockedResolver);
    assertExternalizeUrlWithHost("http://www.domain.com/context/the/path?param=1", "/the/path?param=1", mockedResolver);
    assertExternalizeUrlWithHost("http://www.domain.com/context/the/path#hash", "/the/path#hash", mockedResolver);
  }

  @Test
  void testExternalizeUrlWithHostWithoutActualSlingMappingConfiguration() {
    assertExternalizeUrlWithHost("/the/path", "/the/path", context.resourceResolver());
    assertExternalizeUrlWithHost("/the/path?param=1", "/the/path?param=1", context.resourceResolver());
    assertExternalizeUrlWithHost("/the/path#hash", "/the/path#hash", context.resourceResolver());
  }

  @Test
  void testIsExternalized() {
    assertFalse(Externalizer.isExternalized("/absolute/path"));
    assertFalse(Externalizer.isExternalized("/ns:absolute/path"));
    assertFalse(Externalizer.isExternalized("relative/path"));
    assertFalse(Externalizer.isExternalized("relative/ns:path"));

    // ideally this one should be false - but then it's not possible to detect other special protocols in a generic way
    assertTrue(Externalizer.isExternalized("ns:relative/ns:path"));

    assertTrue(Externalizer.isExternalized("http://www.heise.de/path1"));
    assertTrue(Externalizer.isExternalized("https://www.heise.de/path1"));
    assertTrue(Externalizer.isExternalized("mailto:info@jodelkaiser.de"));
    assertTrue(Externalizer.isExternalized("tel:+123456"));
    assertTrue(Externalizer.isExternalized("javascript:print()"));
    assertTrue(Externalizer.isExternalized("//www.heise.de"));
    assertTrue(Externalizer.isExternalized("ftp://ftp.heise.de"));
    assertTrue(Externalizer.isExternalized(IntegratorPlaceholder.URL_CONTENT + "/path1"));
  }

  @Test
  void testMangleNamespaces() {
    assertEquals("/content/aa/bb/content.png", Externalizer.mangleNamespaces("/content/aa/bb/content.png"));
    assertEquals("/content/aa/bb/_jcr_content.png", Externalizer.mangleNamespaces("/content/aa/bb/jcr:content.png"));
  }

  @Test
  void testIsExternalizable() {
    assertFalse(Externalizer.isExternalizable(""));
    assertFalse(Externalizer.isExternalizable("abc"));
    assertTrue(Externalizer.isExternalizable("/abc"));
  }

  private void assertExternalizeUrlWithHost(String expected, String url, ResourceResolver resolver) {
    assertEquals(expected, Externalizer.externalizeUrlWithHost(url, resolver, context.request()));
  }

}
