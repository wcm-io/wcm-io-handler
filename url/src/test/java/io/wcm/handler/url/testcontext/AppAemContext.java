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
package io.wcm.handler.url.testcontext;

import static io.wcm.testing.mock.wcmio.caconfig.ContextPlugins.WCMIO_CACONFIG;
import static io.wcm.testing.mock.wcmio.caconfig.compat.ContextPlugins.WCMIO_CACONFIG_COMPAT;
import static io.wcm.testing.mock.wcmio.sling.ContextPlugins.WCMIO_SLING;
import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;

import java.io.IOException;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.testing.mock.sling.ResourceResolverType;

import io.wcm.caconfig.application.spi.ApplicationProvider;
import io.wcm.config.spi.ConfigurationFinderStrategy;
import io.wcm.config.spi.ParameterProvider;
import io.wcm.handler.url.UrlParams;
import io.wcm.handler.url.impl.ApplicationProviderImpl;
import io.wcm.handler.url.impl.UrlHandlerParameterProviderImpl;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;
import io.wcm.testing.mock.aem.junit.AemContextCallback;
import io.wcm.testing.mock.wcmio.caconfig.compat.MockCAConfig;

/**
 * Sets up {@link AemContext} for unit tests in this application.
 */
public final class AppAemContext {

  private AppAemContext() {
    // static methods only
  }

  public static AemContext newAemContext() {
    return new AemContextBuilder()
        .plugin(CACONFIG)
        .plugin(WCMIO_SLING, WCMIO_CACONFIG, WCMIO_CACONFIG_COMPAT)
        .afterSetUp(SETUP_CALLBACK)
        .resourceResolverType(ResourceResolverType.JCR_MOCK)
        .build();
  }

  /**
   * Custom set up rules required in all unit tests.
   */
  private static final AemContextCallback SETUP_CALLBACK = new AemContextCallback() {
    @Override
    public void execute(AemContext context) throws PersistenceException, IOException {

      // URL handler-specific parameter definitions
      context.registerService(ParameterProvider.class, new UrlHandlerParameterProviderImpl());

      // application provider
      context.registerService(ApplicationProvider.class,
          MockCAConfig.applicationProvider(ApplicationProviderImpl.APPLICATION_ID, "/content"));

      // configuration finder strategy
      context.registerService(ConfigurationFinderStrategy.class,
          MockCAConfig.configurationFinderStrategyAbsoluteParent(ApplicationProviderImpl.APPLICATION_ID,
              DummyUrlHandlerConfig.SITE_ROOT_LEVEL));

      // sling models registration
      context.addModelsForPackage("io.wcm.handler.url");

      // create current page in site context
      context.create().page("/content");
      context.create().page("/content/unittest");
      context.create().page("/content/unittest/de_test");
      context.create().page("/content/unittest/de_test/brand");
      context.currentPage(context.create().page("/content/unittest/de_test/brand/de"));
      context.create().page("/content/unittest/de_test/brand/de/setion");
      context.create().page("/content/unittest/de_test/brand/de/section/page");
      context.create().page("/content/unittest/de_test/brand/de/section2");
      context.create().page("/content/unittest/de_test/brand/de/section2/page2");
      context.create().page("/content/unittest/de_test/brand/de/section2/page3");
      context.create().page("/content/unittest/de_test/brand/en");
      context.create().page("/content/unittest/de_test/brand/en/section");
      context.create().page("/content/unittest/de_test/brand/en/section/page");

      // default site config
      MockCAConfig.writeConfiguration(context, "/content/unittest/de_test/brand/de",
          ImmutableValueMap.builder()
          .put(UrlParams.SITE_URL.getName(), "http://de.dummysite.org")
          .put(UrlParams.SITE_URL_SECURE.getName(), "https://de.dummysite.org")
          .put(UrlParams.SITE_URL_AUTHOR.getName(), "https://author.dummysite.org")
          .build());
      MockCAConfig.writeConfiguration(context, "/content/unittest/de_test/brand/en",
          ImmutableValueMap.builder()
          .put(UrlParams.SITE_URL.getName(), "http://en.dummysite.org")
          .put(UrlParams.SITE_URL_SECURE.getName(), "https://en.dummysite.org")
          .put(UrlParams.SITE_URL_AUTHOR.getName(), "https://author.dummysite.org")
          .build());
    }
  };

}
