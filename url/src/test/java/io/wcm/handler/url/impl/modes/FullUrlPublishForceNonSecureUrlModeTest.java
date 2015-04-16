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
package io.wcm.handler.url.impl.modes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import io.wcm.handler.url.UrlMode;
import io.wcm.handler.url.UrlModes;
import io.wcm.handler.url.integrator.IntegratorHandler;
import io.wcm.handler.url.integrator.IntegratorPlaceholder;
import io.wcm.wcm.commons.util.RunMode;

import org.junit.Test;

public class FullUrlPublishForceNonSecureUrlModeTest extends AbstractUrlModeTest {

  @Override
  protected UrlMode urlMode() {
    return UrlModes.FULL_URL_PUBLISH_FORCENONSECURE;
  }

  /**
   * Test with site urls
   */
  @Test
  public void testSiteUrls() {

    context.runMode(RunMode.PUBLISH);
    assertEquals("http://de.dummysite.org", urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("http://de.dummysite.org", urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("http://de.dummysite.org", urlMode().getResourceUrlPrefix(adaptable(), runModes(), currentPage, null));

    context.runMode(RunMode.AUTHOR);
    assertEquals("http://de.dummysite.org", urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("http://de.dummysite.org", urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("http://de.dummysite.org", urlMode().getResourceUrlPrefix(adaptable(), runModes(), currentPage, null));

  }

  /**
   * Test without site urls
   */
  @Test
  public void testNoSiteUrls() {

    setSiteConfigNoUrl();

    context.runMode(RunMode.PUBLISH);
    assertNull(urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertNull(urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertNull(urlMode().getResourceUrlPrefix(adaptable(), runModes(), currentPage, null));

    context.runMode(RunMode.AUTHOR);
    assertNull(urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertNull(urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertNull(urlMode().getResourceUrlPrefix(adaptable(), runModes(), currentPage, null));

  }

  /**
   * Test in simple integrator template mode
   */
  @Test
  public void testIntegratorModeSimple() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    context.currentPage(integratorPageSimple);

    context.runMode(RunMode.PUBLISH);
    assertEquals("http://de.dummysite.org", urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("http://de.dummysite.org", urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("http://de.dummysite.org", urlMode().getResourceUrlPrefix(adaptable(), runModes(), currentPage, null));

    context.runMode(RunMode.AUTHOR);
    assertEquals("http://de.dummysite.org", urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("http://de.dummysite.org", urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("http://de.dummysite.org", urlMode().getResourceUrlPrefix(adaptable(), runModes(), currentPage, null));

  }

  /**
   * Test in secure simple integrator templates mode
   */
  @Test
  public void testIntegratorModeSimpleSecure() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE_SECURE);
    context.currentPage(integratorPageSimpleSecure);

    context.runMode(RunMode.PUBLISH);
    assertEquals("http://de.dummysite.org", urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("http://de.dummysite.org", urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("http://de.dummysite.org", urlMode().getResourceUrlPrefix(adaptable(), runModes(), currentPage, null));

    context.runMode(RunMode.AUTHOR);
    assertEquals("http://de.dummysite.org", urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("http://de.dummysite.org", urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("http://de.dummysite.org", urlMode().getResourceUrlPrefix(adaptable(), runModes(), currentPage, null));

  }

  /**
   * Test in extended integrator template mode
   */
  @Test
  public void testIntegratorModeExtended() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    context.currentPage(integratorPageExtended);

    context.runMode(RunMode.PUBLISH);
    assertEquals(IntegratorPlaceholder.URL_CONTENT, urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT, urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_PROXY, urlMode().getResourceUrlPrefix(adaptable(), runModes(), currentPage, null));

    context.runMode(RunMode.AUTHOR);
    assertEquals(IntegratorPlaceholder.URL_CONTENT, urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT, urlMode().getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_PROXY, urlMode().getResourceUrlPrefix(adaptable(), runModes(), currentPage, null));

  }

}
