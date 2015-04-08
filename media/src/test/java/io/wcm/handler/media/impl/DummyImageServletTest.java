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
package io.wcm.handler.media.impl;

import static org.junit.Assert.assertEquals;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.commons.contenttype.FileExtension;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

// TODO: enable this unit tests and test futher variants when AEM mocks supports mocking Designer
@Ignore
public class DummyImageServletTest {

  @Rule
  public AemContext context = AppAemContext.newAemContext();

  private DummyImageServlet underTest;

  @Before
  public void setUp() {
    underTest = new DummyImageServlet();
    ((MockRequestPathInfo)context.request().getRequestPathInfo()).setExtension(FileExtension.PNG);
  }

  @Test
  public void testGet_NoSuffix() throws Exception {
    underTest.service(context.request(), context.response());
    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
  }

}
