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
import io.wcm.handler.url.UrlModes;
import io.wcm.handler.url.integrator.IntegratorHandler;
import io.wcm.handler.url.integrator.IntegratorPlaceholder;
import io.wcm.wcm.commons.util.RunMode;

import org.junit.Test;

public class FullUrlProtocolRelativeUrlModeTest extends AbstractUrlModeTest {

  /**
   * Test with site urls
   */
  @Test
  public void testSiteUrls() {

    context.runMode(RunMode.PUBLISH);
    assertEquals("//de.dummysite.org", UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("//de.dummysite.org", UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("//de.dummysite.org", UrlModes.FULL_URL_PROTOCOLRELATIVE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://author.dummysite.org",
        UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_PROTOCOLRELATIVE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test without site urls
   */
  @Test
  public void testNoSiteUrls() {

    setSiteConfigNoUrl();

    context.runMode(RunMode.PUBLISH);
    assertEquals(null, UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.FULL_URL_PROTOCOLRELATIVE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals(null, UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.FULL_URL_PROTOCOLRELATIVE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test in simple integrator template mode
   */
  @Test
  public void testIntegratorModeSimple() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    context.currentPage(integratorPageSimple);

    context.runMode(RunMode.PUBLISH);
    assertEquals("//de.dummysite.org", UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("//de.dummysite.org", UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("//de.dummysite.org", UrlModes.FULL_URL_PROTOCOLRELATIVE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://author.dummysite.org",
        UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_PROTOCOLRELATIVE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test in secure simple integrator templates mode
   */
  @Test
  public void testIntegratorModeSimpleSecure() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE_SECURE);
    context.currentPage(integratorPageSimpleSecure);

    context.runMode(RunMode.PUBLISH);
    assertEquals("//de.dummysite.org", UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("//de.dummysite.org", UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("//de.dummysite.org", UrlModes.FULL_URL_PROTOCOLRELATIVE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://author.dummysite.org",
        UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_PROTOCOLRELATIVE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test in extended integrator template mode
   */
  @Test
  public void testIntegratorModeExtended() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    context.currentPage(integratorPageExtended);

    context.runMode(RunMode.PUBLISH);
    assertEquals(IntegratorPlaceholder.URL_CONTENT, UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_SECURE,
        UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_PROXY, UrlModes.FULL_URL_PROTOCOLRELATIVE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals(IntegratorPlaceholder.URL_CONTENT, UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_SECURE,
        UrlModes.FULL_URL_PROTOCOLRELATIVE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_PROXY, UrlModes.FULL_URL_PROTOCOLRELATIVE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

}
