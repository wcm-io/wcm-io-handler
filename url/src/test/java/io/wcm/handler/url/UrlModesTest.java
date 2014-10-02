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
package io.wcm.handler.url;

import static org.junit.Assert.assertEquals;
import io.wcm.handler.url.integrator.IntegratorHandler;
import io.wcm.handler.url.integrator.IntegratorModes;
import io.wcm.handler.url.integrator.IntegratorNameConstants;
import io.wcm.handler.url.integrator.IntegratorPlaceholder;
import io.wcm.handler.url.integrator.IntegratorProtocol;
import io.wcm.handler.url.testcontext.AppAemContext;
import io.wcm.handler.url.testcontext.DummyAppTemplate;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.wcmio.config.MockConfig;
import io.wcm.wcm.commons.util.RunMode;

import java.util.Set;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.settings.SlingSettingsService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;

public class UrlModesTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  private Page currentPage;
  private Page targetPage;
  private Page secureTargetPage;
  private Page integratorPageSimple;
  private Page integratorPageSimpleSecure;
  private Page integratorPageExtended;
  private SlingSettingsService slingSettings;

  @Before
  public void setUp() throws Exception {

    // create current page in site context
    currentPage = context.create().page("/content/unittest/de_test/brand/de/section/page",
        DummyAppTemplate.CONTENT.getTemplatePath());
    context.currentPage(currentPage);

    // create more pages to simulate internal link
    targetPage = context.create().page("/content/unittest/de_test/brand/de/section2/page2",
        DummyAppTemplate.CONTENT.getTemplatePath());
    secureTargetPage = context.create().page("/content/unittest/de_test/brand/de/section2/page3",
        DummyAppTemplate.CONTENT_SECURE.getTemplatePath());

    integratorPageSimple = context.create().page("/content/unittest/de_test/brand/de/section2/page4",
        DummyAppTemplate.INTEGRATOR.getTemplatePath(),
        ImmutableValueMap.builder()
        .put(IntegratorNameConstants.PN_INTEGRATOR_MODE, IntegratorModes.SIMPLE.getId())
        .put(IntegratorNameConstants.PN_INTEGRATOR_PROTOCOL, IntegratorProtocol.HTTP.name())
        .build());

    integratorPageSimpleSecure = context.create().page("/content/unittest/de_test/brand/de/section2/page5",
        DummyAppTemplate.INTEGRATOR.getTemplatePath(),
        ImmutableValueMap.builder()
        .put(IntegratorNameConstants.PN_INTEGRATOR_MODE, IntegratorModes.SIMPLE.getId())
        .put(IntegratorNameConstants.PN_INTEGRATOR_PROTOCOL, IntegratorProtocol.HTTPS.name())
        .build());

    integratorPageExtended = context.create().page("/content/unittest/de_test/brand/de/section2/page6",
        DummyAppTemplate.INTEGRATOR.getTemplatePath(),
        ImmutableValueMap.builder()
        .put(IntegratorNameConstants.PN_INTEGRATOR_MODE, IntegratorModes.EXTENDED.getId())
        .build());

    slingSettings = context.getService(SlingSettingsService.class);

  }

  private Adaptable adaptable() {
    return context.request();
  }

  private Set<String> runModes() {
    return slingSettings.getRunModes();
  }

  private void setSiteConfigNoUrl() {
    MockConfig.writeConfiguration(context, "/content/unittest/de_test/brand/de", ImmutableValueMap.of());
  }

  // ==================================================================================================================

  /**
   * Test {@link UrlModes#DEFAULT} with site urls
   */
  @Test
  public void testDEFAULT_SiteUrls() {

    context.runMode(RunMode.PUBLISH);
    assertEquals("http://de.dummysite.org", UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://de.dummysite.org", UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.DEFAULT.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals("https://author.dummysite.org", UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://author.dummysite.org", UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.DEFAULT.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#DEFAULT} without site urls
   */
  @Test
  public void testDEFAULT_NoSiteUrls() {

    setSiteConfigNoUrl();

    context.runMode(RunMode.PUBLISH);
    assertEquals(null, UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.DEFAULT.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals(null, UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.DEFAULT.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#DEFAULT} in simple integrator template mode
   */
  @Test
  public void testDEFAULT_IntegratorModeSimple() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    context.currentPage(integratorPageSimple);

    context.runMode(RunMode.PUBLISH);
    assertEquals("http://de.dummysite.org", UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://de.dummysite.org", UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("http://de.dummysite.org", UrlModes.DEFAULT.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals("https://author.dummysite.org", UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://author.dummysite.org", UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://author.dummysite.org", UrlModes.DEFAULT.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#DEFAULT} in secure simple integrator templates mode
   */
  @Test
  public void testDEFAULT_IntegratorModeSimpleSecure() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE_SECURE);
    context.currentPage(integratorPageSimpleSecure);

    context.runMode(RunMode.PUBLISH);
    assertEquals("http://de.dummysite.org", UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://de.dummysite.org", UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://de.dummysite.org", UrlModes.DEFAULT.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals("https://author.dummysite.org", UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://author.dummysite.org", UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://author.dummysite.org", UrlModes.DEFAULT.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#DEFAULT} in extended integrator template mode
   */
  @Test
  public void testDEFAULT_IntegratorModeExtended() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    context.currentPage(integratorPageExtended);

    context.runMode(RunMode.PUBLISH);
    assertEquals(IntegratorPlaceholder.URL_CONTENT, UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_SECURE, UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_PROXY, UrlModes.DEFAULT.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals(IntegratorPlaceholder.URL_CONTENT, UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_SECURE, UrlModes.DEFAULT.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_PROXY, UrlModes.DEFAULT.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  // ==================================================================================================================

  /**
   * Test {@link UrlModes#NO_HOSTNAME} with site urls
   */
  @Test
  public void testNO_HOSTNAME_SiteUrls() {

    context.runMode(RunMode.PUBLISH);
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#NO_HOSTNAME} without site urls
   */
  @Test
  public void testNO_HOSTNAME_NoSiteUrls() {

    setSiteConfigNoUrl();

    context.runMode(RunMode.PUBLISH);
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#NO_HOSTNAME} in simple integrator template mode
   */
  @Test
  public void testNO_HOSTNAME_IntegratorModeSimple() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    context.currentPage(integratorPageSimple);

    context.runMode(RunMode.PUBLISH);
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#NO_HOSTNAME} in secure simple integrator templates mode
   */
  @Test
  public void testNO_HOSTNAME_IntegratorModeSimpleSecure() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE_SECURE);
    context.currentPage(integratorPageSimpleSecure);

    context.runMode(RunMode.PUBLISH);
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#NO_HOSTNAME} in extended integrator template mode
   */
  @Test
  public void testNO_HOSTNAME_IntegratorModeExtended() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    context.currentPage(integratorPageExtended);

    context.runMode(RunMode.PUBLISH);
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.NO_HOSTNAME.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  // ==================================================================================================================

  /**
   * Test {@link UrlModes#FULL_URL} with site urls
   */
  @Test
  public void testFULL_URL_SiteUrls() {

    context.runMode(RunMode.PUBLISH);
    assertEquals("http://de.dummysite.org", UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://de.dummysite.org", UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("http://de.dummysite.org", UrlModes.FULL_URL.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#FULL_URL} without site urls
   */
  @Test
  public void testFULL_URL_NoSiteUrls() {

    setSiteConfigNoUrl();

    context.runMode(RunMode.PUBLISH);
    assertEquals(null, UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.FULL_URL.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals(null, UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.FULL_URL.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#FULL_URL} in simple integrator template mode
   */
  @Test
  public void testFULL_URL_IntegratorModeSimple() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    context.currentPage(integratorPageSimple);

    context.runMode(RunMode.PUBLISH);
    assertEquals("http://de.dummysite.org", UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://de.dummysite.org", UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("http://de.dummysite.org", UrlModes.FULL_URL.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#FULL_URL} in secure simple integrator templates mode
   */
  @Test
  public void testFULL_URL_IntegratorModeSimpleSecure() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE_SECURE);
    context.currentPage(integratorPageSimpleSecure);

    context.runMode(RunMode.PUBLISH);
    assertEquals("http://de.dummysite.org", UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://de.dummysite.org", UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://de.dummysite.org", UrlModes.FULL_URL.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#FULL_URL} in extended integrator template mode
   */
  @Test
  public void testFULL_URL_IntegratorModeExtended() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    context.currentPage(integratorPageExtended);

    context.runMode(RunMode.PUBLISH);
    assertEquals(IntegratorPlaceholder.URL_CONTENT, UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_SECURE, UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_PROXY, UrlModes.FULL_URL.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals(IntegratorPlaceholder.URL_CONTENT, UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_SECURE, UrlModes.FULL_URL.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_PROXY, UrlModes.FULL_URL.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  // ==================================================================================================================

  /**
   * Test {@link UrlModes#FULL_URL_FORCENONSECURE} with site urls
   */
  @Test
  public void testFULL_URL_FORCENONSECURE_SiteUrls() {

    context.runMode(RunMode.PUBLISH);
    assertEquals("http://de.dummysite.org", UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("http://de.dummysite.org", UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("http://de.dummysite.org", UrlModes.FULL_URL_FORCENONSECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://author.dummysite.org",
        UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_FORCENONSECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#FULL_URL_FORCENONSECURE} without site urls
   */
  @Test
  public void testFULL_URL_FORCENONSECURE_NoSiteUrls() {

    setSiteConfigNoUrl();

    context.runMode(RunMode.PUBLISH);
    assertEquals(null, UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.FULL_URL_FORCENONSECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals(null, UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.FULL_URL_FORCENONSECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#FULL_URL_FORCENONSECURE} in simple integrator template mode
   */
  @Test
  public void testFULL_URL_FORCENONSECURE_IntegratorModeSimple() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    context.currentPage(integratorPageSimple);

    context.runMode(RunMode.PUBLISH);
    assertEquals("http://de.dummysite.org", UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("http://de.dummysite.org", UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("http://de.dummysite.org", UrlModes.FULL_URL_FORCENONSECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://author.dummysite.org",
        UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_FORCENONSECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#FULL_URL_FORCENONSECURE} in secure simple integrator templates mode
   */
  @Test
  public void testFULL_URL_FORCENONSECURE_IntegratorModeSimpleSecure() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE_SECURE);
    context.currentPage(integratorPageSimpleSecure);

    context.runMode(RunMode.PUBLISH);
    assertEquals("http://de.dummysite.org", UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("http://de.dummysite.org", UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("http://de.dummysite.org", UrlModes.FULL_URL_FORCENONSECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://author.dummysite.org",
        UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_FORCENONSECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#FULL_URL_FORCENONSECURE} in extended integrator template mode
   */
  @Test
  public void testFULL_URL_FORCENONSECURE_IntegratorModeExtended() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    context.currentPage(integratorPageExtended);

    context.runMode(RunMode.PUBLISH);
    assertEquals(IntegratorPlaceholder.URL_CONTENT, UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT,
        UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_PROXY, UrlModes.FULL_URL_FORCENONSECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals(IntegratorPlaceholder.URL_CONTENT, UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT,
        UrlModes.FULL_URL_FORCENONSECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_PROXY, UrlModes.FULL_URL_FORCENONSECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  // ==================================================================================================================

  /**
   * Test {@link UrlModes#FULL_URL_FORCESECURE} with site urls
   */
  @Test
  public void testFULL_URL_FORCESECURE_SiteUrls() {

    context.runMode(RunMode.PUBLISH);
    assertEquals("https://de.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://de.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://de.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#FULL_URL_FORCESECURE} without site urls
   */
  @Test
  public void testFULL_URL_FORCESECURE_NoSiteUrls() {

    setSiteConfigNoUrl();

    context.runMode(RunMode.PUBLISH);
    assertEquals(null, UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.FULL_URL_FORCESECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals(null, UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(null, UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(null, UrlModes.FULL_URL_FORCESECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#FULL_URL_FORCESECURE} in simple integrator template mode
   */
  @Test
  public void testFULL_URL_FORCESECURE_IntegratorModeSimple() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    context.currentPage(integratorPageSimple);

    context.runMode(RunMode.PUBLISH);
    assertEquals("https://de.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://de.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://de.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#FULL_URL_FORCESECURE} in secure simple integrator templates mode
   */
  @Test
  public void testFULL_URL_FORCESECURE_IntegratorModeSimpleSecure() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE_SECURE);
    context.currentPage(integratorPageSimpleSecure);

    context.runMode(RunMode.PUBLISH);
    assertEquals("https://de.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://de.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://de.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals("https://author.dummysite.org", UrlModes.FULL_URL_FORCESECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  /**
   * Test {@link UrlModes#FULL_URL_FORCESECURE} in extended integrator template mode
   */
  @Test
  public void testFULL_URL_FORCESECURE_IntegratorModeExtended() {

    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    context.currentPage(integratorPageExtended);

    context.runMode(RunMode.PUBLISH);
    assertEquals(IntegratorPlaceholder.URL_CONTENT_SECURE,
        UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_SECURE,
        UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_PROXY, UrlModes.FULL_URL_FORCESECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

    context.runMode(RunMode.AUTHOR);
    assertEquals(IntegratorPlaceholder.URL_CONTENT_SECURE,
        UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, targetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_SECURE,
        UrlModes.FULL_URL_FORCESECURE.getLinkUrlPrefix(adaptable(), runModes(), currentPage, secureTargetPage));
    assertEquals(IntegratorPlaceholder.URL_CONTENT_PROXY, UrlModes.FULL_URL_FORCESECURE.getResourceUrlPrefix(adaptable(), runModes(), currentPage));

  }

  // ==================================================================================================================

  /**
   * Test {@link UrlModes#FULL_URL_PROTOCOLRELATIVE} with site urls
   */
  @Test
  public void testFULL_URL_PROTOCOLRELATIVE_SiteUrls() {

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
   * Test {@link UrlModes#FULL_URL_PROTOCOLRELATIVE} without site urls
   */
  @Test
  public void testFULL_URL_PROTOCOLRELATIVE_NoSiteUrls() {

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
   * Test {@link UrlModes#FULL_URL_PROTOCOLRELATIVE} in simple integrator template mode
   */
  @Test
  public void testFULL_URL_PROTOCOLRELATIVE_IntegratorModeSimple() {

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
   * Test {@link UrlModes#FULL_URL_PROTOCOLRELATIVE} in secure simple integrator templates mode
   */
  @Test
  public void testFULL_URL_PROTOCOLRELATIVE_IntegratorModeSimpleSecure() {

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
   * Test {@link UrlModes#FULL_URL_PROTOCOLRELATIVE} in extended integrator template mode
   */
  @Test
  public void testFULL_URL_PROTOCOLRELATIVE_IntegratorModeExtended() {

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
