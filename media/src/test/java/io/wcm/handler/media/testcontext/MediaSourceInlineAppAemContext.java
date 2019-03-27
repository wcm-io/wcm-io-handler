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

import static io.wcm.testing.mock.wcmio.caconfig.ContextPlugins.WCMIO_CACONFIG;
import static io.wcm.testing.mock.wcmio.sling.ContextPlugins.WCMIO_SLING;
import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;

import java.io.IOException;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.testing.mock.caconfig.MockContextAwareConfig;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.jetbrains.annotations.NotNull;

import io.wcm.handler.media.format.impl.MediaFormatProviderManagerImpl;
import io.wcm.handler.media.impl.DefaultMediaHandlerConfig;
import io.wcm.handler.media.impl.MediaHandlerConfigAdapterFactory;
import io.wcm.handler.media.spi.MediaFormatProvider;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.url.SiteConfig;
import io.wcm.handler.url.impl.DefaultUrlHandlerConfig;
import io.wcm.handler.url.impl.SiteRootDetectorImpl;
import io.wcm.handler.url.impl.UrlHandlerAdapterFactory;
import io.wcm.handler.url.spi.UrlHandlerConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextBuilder;
import io.wcm.testing.mock.aem.junit5.AemContextCallback;
import io.wcm.testing.mock.wcmio.caconfig.MockCAConfig;

/**
 * Sets up {@link AemContext} for unit tests in this application.
 */
public final class MediaSourceInlineAppAemContext {

  /**
   * DAM root path
   */
  public static final String DAM_PATH = "/content/dam/test";

  /**
   * Content root path
   */
  public static final String ROOTPATH_CONTENT = "/content/unittest/de_test/brand/de";

  private MediaSourceInlineAppAemContext() {
    // static methods only
  }

  public static AemContext newAemContext() {
    return new AemContextBuilder()
        .plugin(CACONFIG)
        .plugin(WCMIO_SLING, WCMIO_CACONFIG)
        .afterSetUp(SETUP_CALLBACK)
        // TODO: add support for multiple resource resolver types?
        //.resourceResolverType(ResourceResolverType.RESOURCERESOLVER_MOCK, ResourceResolverType.JCR_MOCK)
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
      context.registerInjectActivateService(new DefaultUrlHandlerConfig());
      context.registerService(UrlHandlerConfig.class, new DummyUrlHandlerConfig());
      context.registerInjectActivateService(new MediaHandlerConfigAdapterFactory());
      context.registerInjectActivateService(new DefaultMediaHandlerConfig());
      context.registerService(MediaHandlerConfig.class, new DummyMediaHandlerConfig());

      // context path strategy
      MockCAConfig.contextPathStrategyAbsoluteParent(context, DummyUrlHandlerConfig.SITE_ROOT_LEVEL);

      // media formats
      context.registerService(MediaFormatProvider.class, new DummyMediaFormatProvider());
      context.registerInjectActivateService(new MediaFormatProviderManagerImpl());

      // sling models registration
      context.addModelsForPackage("io.wcm.handler.media",
          "io.wcm.handler.mediasource.dam",
          "io.wcm.handler.mediasource.inline");

      // create current page in site context
      context.currentPage(context.create().page(ROOTPATH_CONTENT,
          DummyAppTemplate.CONTENT.getTemplatePath()));

      // default site config
      MockContextAwareConfig.writeConfiguration(context, ROOTPATH_CONTENT, SiteConfig.class.getName(),
          "siteUrl", "http://www.dummysite.org",
          "siteUrlSecure", "https://www.dummysite.org",
          "siteUrlAuthor", "https://author.dummysite.org");
    }
  };

}
