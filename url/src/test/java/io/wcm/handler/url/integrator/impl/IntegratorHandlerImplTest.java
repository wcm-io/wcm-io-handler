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
package io.wcm.handler.url.integrator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import io.wcm.handler.url.integrator.IntegratorHandler;
import io.wcm.handler.url.integrator.IntegratorModes;
import io.wcm.handler.url.integrator.IntegratorNameConstants;
import io.wcm.handler.url.integrator.IntegratorProtocol;
import io.wcm.handler.url.testcontext.AppAemContext;
import io.wcm.handler.url.testcontext.DummyAppTemplate;
import io.wcm.testing.mock.aem.junit.AemContext;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;

public class IntegratorHandlerImplTest {

  private static final String PAGE_PATH = "/content/unittest/de_test/brand/de/section/page";

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  protected Adaptable adaptable() {
    return context.request();
  }

  @Test
  public void testIsIntegratorTemplateModes_NoSelector() {
    IntegratorHandler underTest = adaptable().adaptTo(IntegratorHandler.class);
    assertFalse(underTest.isIntegratorTemplateMode());
    assertFalse(underTest.isIntegratorTemplateSecureMode());
  }

  @Test
  public void testIsIntegratorTemplateModes_IntegratorTemplateSelector() {
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    IntegratorHandler underTest = adaptable().adaptTo(IntegratorHandler.class);
    assertTrue(underTest.isIntegratorTemplateMode());
    assertFalse(underTest.isIntegratorTemplateSecureMode());
  }

  @Test
  public void testIsIntegratorTemplateModes_IntegratorTemplateSecureSelector() {
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE_SECURE);
    IntegratorHandler underTest = adaptable().adaptTo(IntegratorHandler.class);
    assertTrue(underTest.isIntegratorTemplateMode());
    assertTrue(underTest.isIntegratorTemplateSecureMode());
  }

  @Test
  public void testGetIntegratorTemplateSelector_NoSelector() {
    IntegratorHandler underTest = adaptable().adaptTo(IntegratorHandler.class);
    assertEquals(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE, underTest.getIntegratorTemplateSelector());
  }

  @Test
  public void testGetIntegratorTemplateSelector_IntegratorTemplateSelector() {
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE);
    IntegratorHandler underTest = adaptable().adaptTo(IntegratorHandler.class);
    assertEquals(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE, underTest.getIntegratorTemplateSelector());
  }

  @Test
  public void testGetIntegratorTemplateSelector_IntegratorTemplateSecureSelector() {
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE_SECURE);
    IntegratorHandler underTest = adaptable().adaptTo(IntegratorHandler.class);
    assertEquals(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE_SECURE, underTest.getIntegratorTemplateSelector());
  }

  @Test
  public void testNoIntegratorPage() {
    IntegratorHandler underTest = adaptable().adaptTo(IntegratorHandler.class);
    assertEquals(IntegratorModes.SIMPLE, underTest.getIntegratorMode());
    assertEquals(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE, underTest.getIntegratorTemplateSelector());
  }

  @Test
  public void testIntegratorPage() {
    Page integratorPage = context.create().page(PAGE_PATH, DummyAppTemplate.INTEGRATOR.getTemplatePath());
    context.currentPage(integratorPage);
    IntegratorHandler underTest = adaptable().adaptTo(IntegratorHandler.class);
    assertEquals(IntegratorModes.SIMPLE, underTest.getIntegratorMode());
    assertEquals(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE, underTest.getIntegratorTemplateSelector());
  }

  @Test
  public void testIntegratorPageExtended() throws PersistenceException {
    Page integratorPage = context.create().page(PAGE_PATH, DummyAppTemplate.INTEGRATOR.getTemplatePath());
    ModifiableValueMap props = integratorPage.getContentResource().adaptTo(ModifiableValueMap.class);
    props.put(IntegratorNameConstants.PN_INTEGRATOR_MODE, IntegratorModes.EXTENDED.getId());
    context.resourceResolver().commit();
    context.currentPage(integratorPage);
    IntegratorHandler underTest = adaptable().adaptTo(IntegratorHandler.class);
    assertEquals(IntegratorModes.EXTENDED, underTest.getIntegratorMode());
    assertEquals(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE, underTest.getIntegratorTemplateSelector());
  }

  @Test
  public void testIntegratorPageNull() {
    context.currentPage((Page)null);
    IntegratorHandler underTest = adaptable().adaptTo(IntegratorHandler.class);
    assertNull(underTest.getIntegratorMode());
    assertEquals(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE, underTest.getIntegratorTemplateSelector());
  }

  @Test
  public void testIntegratorPage_IntegratorTemplateSecureSelector() {
    context.requestPathInfo().setSelectorString(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE_SECURE);
    Page integratorPage = context.create().page(PAGE_PATH, DummyAppTemplate.INTEGRATOR.getTemplatePath());
    context.currentPage(integratorPage);
    IntegratorHandler underTest = adaptable().adaptTo(IntegratorHandler.class);
    assertEquals(IntegratorModes.SIMPLE, underTest.getIntegratorMode());
    assertEquals(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE_SECURE, underTest.getIntegratorTemplateSelector());
  }

  @Test
  public void testIntegratorPage_IntegratorProtocolSecure() throws PersistenceException {
    Page integratorPage = context.create().page(PAGE_PATH, DummyAppTemplate.INTEGRATOR.getTemplatePath());
    ModifiableValueMap props = integratorPage.getContentResource().adaptTo(ModifiableValueMap.class);
    props.put(IntegratorNameConstants.PN_INTEGRATOR_PROTOCOL, IntegratorProtocol.HTTPS.name());
    context.resourceResolver().commit();
    context.currentPage(integratorPage);
    IntegratorHandler underTest = adaptable().adaptTo(IntegratorHandler.class);
    assertEquals(IntegratorModes.SIMPLE, underTest.getIntegratorMode());
    assertEquals(IntegratorHandler.SELECTOR_INTEGRATORTEMPLATE_SECURE, underTest.getIntegratorTemplateSelector());
  }

}
