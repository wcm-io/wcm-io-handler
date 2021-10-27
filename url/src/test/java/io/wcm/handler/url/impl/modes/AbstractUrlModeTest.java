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

import java.util.Set;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.testing.mock.caconfig.MockContextAwareConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;

import io.wcm.handler.url.SiteConfig;
import io.wcm.handler.url.UrlMode;
import io.wcm.handler.url.integrator.IntegratorModes;
import io.wcm.handler.url.integrator.IntegratorNameConstants;
import io.wcm.handler.url.integrator.IntegratorProtocol;
import io.wcm.handler.url.testcontext.AppAemContext;
import io.wcm.handler.url.testcontext.DummyAppTemplate;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.instancetype.InstanceTypeService;

@ExtendWith(AemContextExtension.class)
abstract class AbstractUrlModeTest {

  final AemContext context = AppAemContext.newAemContext();

  protected Page currentPage;
  protected Page targetPage;
  protected Page secureTargetPage;
  protected Page integratorPageSimple;
  protected Page integratorPageSimpleSecure;
  protected Page integratorPageExtended;
  protected InstanceTypeService instanceTypeService;

  @BeforeEach
  void setUp() {

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

    instanceTypeService = context.getService(InstanceTypeService.class);

  }

  protected Adaptable adaptable() {
    return context.request();
  }

  protected Set<String> runModes() {
    return instanceTypeService.getRunModes();
  }

  protected void setSiteConfigNoUrl() {
    MockContextAwareConfig.writeConfiguration(context, "/content/unittest/de_test/brand/de", SiteConfig.class.getName(),
        ImmutableValueMap.of());
  }

  protected abstract UrlMode urlMode();

}
