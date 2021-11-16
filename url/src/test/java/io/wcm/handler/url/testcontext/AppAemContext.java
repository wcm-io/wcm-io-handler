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
import static io.wcm.testing.mock.wcmio.sling.ContextPlugins.WCMIO_SLING;
import static io.wcm.testing.mock.wcmio.wcm.ContextPlugins.WCMIO_WCM;
import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;

import java.io.IOException;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.testing.mock.caconfig.MockContextAwareConfig;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.jetbrains.annotations.NotNull;

import io.wcm.handler.url.SiteConfig;
import io.wcm.handler.url.impl.DefaultUrlHandlerConfig;
import io.wcm.handler.url.impl.SiteRootDetectorImpl;
import io.wcm.handler.url.impl.UrlHandlerAdapterFactory;
import io.wcm.handler.url.impl.clientlib.ClientlibProxyRewriterImpl;
import io.wcm.handler.url.spi.UrlHandlerConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextBuilder;
import io.wcm.testing.mock.aem.junit5.AemContextCallback;
import io.wcm.testing.mock.wcmio.caconfig.MockCAConfig;

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
        .plugin(WCMIO_SLING, WCMIO_WCM, WCMIO_CACONFIG)
        .afterSetUp(SETUP_CALLBACK)
        .resourceResolverType(ResourceResolverType.JCR_MOCK)
        .build();
  }

  /**
   * Custom set up rules required in all unit tests.
   */
  private static final AemContextCallback SETUP_CALLBACK = new AemContextCallback() {
    @Override
    public void execute(@NotNull AemContext context) throws PersistenceException, IOException {

      // handler SPI
      context.registerInjectActivateService(new SiteRootDetectorImpl());
      context.registerInjectActivateService(new UrlHandlerAdapterFactory());
      context.registerInjectActivateService(new ClientlibProxyRewriterImpl());
      context.registerInjectActivateService(new DefaultUrlHandlerConfig());
      context.registerService(UrlHandlerConfig.class, new DummyUrlHandlerConfig());

      // register configuration classes
      MockContextAwareConfig.registerAnnotationClasses(context, SiteConfig.class);

      // context path strategy
      MockCAConfig.contextPathStrategyAbsoluteParent(context, DummyUrlHandlerConfig.SITE_ROOT_LEVEL);

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
      MockContextAwareConfig.writeConfiguration(context, "/content/unittest/de_test/brand/de", SiteConfig.class.getName(),
          "siteUrl", "http://de.dummysite.org",
          "siteUrlSecure", "https://de.dummysite.org",
          "siteUrlAuthor", "https://author.dummysite.org");
      MockContextAwareConfig.writeConfiguration(context, "/content/unittest/de_test/brand/en", SiteConfig.class.getName(),
          "siteUrl", "http://en.dummysite.org",
          "siteUrlSecure", "https://en.dummysite.org",
          "siteUrlAuthor", "https://author.dummysite.org");
    }
  };

}
