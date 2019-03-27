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
package io.wcm.handler.commons.servlets.impl;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HtxPageExtensionMapperTest {

  private final AemContext context = new AemContext();

  @Mock
  private SlingHttpServletRequest request;
  @Mock
  private RequestPathInfo requestPathInfo;
  @Mock
  private SlingHttpServletResponse response;
  @Mock
  private RequestDispatcher requestDispatcher;

  @BeforeEach
  void setUp() {
    when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
    when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
  }

  @Test
  void testDisabled() throws Exception {
    HtxPageExtensionMapper underTest = context.registerInjectActivateService(new HtxPageExtensionMapper(), "enabled", false);

    underTest.doGet(request, response);
    verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    verifyNoMoreInteractions(response);
  }

  @Test
  void testSimpleUrl() throws Exception {
    HtxPageExtensionMapper underTest = context.registerInjectActivateService(new HtxPageExtensionMapper(), "enabled", true);

    when(requestPathInfo.getResourcePath()).thenReturn("/my/path");
    when(requestPathInfo.getExtension()).thenReturn("html");

    underTest.doGet(request, response);

    verify(request).getRequestDispatcher("/my/path.html");
    verify(requestDispatcher).include(request, response);
    verifyNoMoreInteractions(response);
  }

  @Test
  void testComplexUrl() throws Exception {
    HtxPageExtensionMapper underTest = context.registerInjectActivateService(new HtxPageExtensionMapper(), "enabled", true);

    when(requestPathInfo.getResourcePath()).thenReturn("/my/path");
    when(requestPathInfo.getExtension()).thenReturn("html");
    when(requestPathInfo.getSelectorString()).thenReturn("sel1.sel2");
    when(requestPathInfo.getSuffix()).thenReturn("suffix1/suffix2.html");

    underTest.doGet(request, response);

    verify(request).getRequestDispatcher("/my/path.sel1.sel2.html/suffix1/suffix2.html");
    verify(requestDispatcher).include(request, response);
    verifyNoMoreInteractions(response);
  }

}
