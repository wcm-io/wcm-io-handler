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
package io.wcm.handler.media.testcontext;

import static io.wcm.testing.mock.wcmio.config.ContextPlugins.WCMIO_CONFIG;
import static io.wcm.testing.mock.wcmio.sling.ContextPlugins.WCMIO_SLING;

import org.osgi.framework.Constants;

import io.wcm.config.spi.ApplicationProvider;
import io.wcm.config.spi.ConfigurationFinderStrategy;
import io.wcm.config.spi.ParameterProvider;
import io.wcm.handler.media.format.impl.MediaFormatProviderManagerImpl;
import io.wcm.handler.media.spi.MediaFormatProvider;
import io.wcm.handler.url.UrlParams;
import io.wcm.handler.url.impl.UrlHandlerParameterProviderImpl;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;
import io.wcm.testing.mock.aem.junit.AemContextCallback;
import io.wcm.testing.mock.wcmio.config.MockConfig;

/**
 * Sets up {@link AemContext} for unit tests in this application.
 */
public final class AppAemContext {

  /**
   * Appliation ID
   */
  public static final String APPLICATION_ID = "/apps/test";

  /**
   * DAM root path
   */
  public static final String DAM_PATH = "/content/dam/test";

  /**
   * Content root path
   */
  public static final String ROOTPATH_CONTENT = "/content/unittest/de_test/brand/de";

  private AppAemContext() {
    // static methods only
  }

  public static AemContext newAemContext() {
    return newAemContext(null);
  }

  public static AemContext newAemContext(AemContextCallback callback) {
    return new AemContextBuilder()
        .plugin(WCMIO_SLING, WCMIO_CONFIG)
        .afterSetUp(callback)
        .afterSetUp(SETUP_CALLBACK)
        .build();
  }

  /**
   * Custom set up rules required in all unit tests.
   */
  private static final AemContextCallback SETUP_CALLBACK = new AemContextCallback() {
    @Override
    public void execute(AemContext context) throws Exception {

      // URL handler-specific parameter definitions
      context.registerService(ParameterProvider.class, new UrlHandlerParameterProviderImpl());

      // application provider
      context.registerService(ApplicationProvider.class,
          MockConfig.applicationProvider(APPLICATION_ID, "/content"),
          ImmutableValueMap.of(Constants.SERVICE_RANKING, 1000));

      // configuration finder strategy
      context.registerService(ConfigurationFinderStrategy.class,
          MockConfig.configurationFinderStrategyAbsoluteParent(APPLICATION_ID,
              DummyUrlHandlerConfig.SITE_ROOT_LEVEL));

      // media formats
      context.registerService(MediaFormatProvider.class, new DummyMediaFormatProvider());
      context.registerInjectActivateService(new MediaFormatProviderManagerImpl());

      // sling models registration
      context.addModelsForPackage(
          "io.wcm.handler.media",
          "io.wcm.handler.mediasource.dam",
          "io.wcm.handler.mediasource.inline");

      // create current page in site context
      context.currentPage(context.create().page(ROOTPATH_CONTENT,
          DummyAppTemplate.CONTENT.getTemplatePath()));

      // default site config
      MockConfig.writeConfiguration(context, ROOTPATH_CONTENT,
          ImmutableValueMap.builder()
          .put(UrlParams.SITE_URL.getName(), "http://www.dummysite.org")
          .put(UrlParams.SITE_URL_SECURE.getName(), "https://www.dummysite.org")
          .put(UrlParams.SITE_URL_AUTHOR.getName(), "https://author.dummysite.org")
          .build());
    }
  };

}
