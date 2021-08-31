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
package io.wcm.handler.link.impl;

import static io.wcm.handler.link.LinkNameConstants.PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY;
import static io.wcm.handler.link.LinkNameConstants.PN_COMPONENT_LINK_TARGET_WINDOW_TARGET_FALLBACK_PROPERTY;
import static io.wcm.handler.link.LinkNameConstants.PN_LINK_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Constants;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkArgs;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.LinkRequest;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.spi.LinkHandlerConfig;
import io.wcm.handler.link.spi.LinkProcessor;
import io.wcm.handler.link.spi.LinkType;
import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.handler.url.UrlModes;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextCallback;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * Test {@link LinkHandlerImpl} methods.
 */
@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class LinkHandlerImplTest {

  static final String APP_ID = "linkHandlerImplTestApp";

  final AemContext context = AppAemContext.newAemContext(new AemContextCallback() {
    @Override
    public void execute(AemContext callbackContext) {
      callbackContext.registerService(LinkHandlerConfig.class, new TestLinkHandlerConfig(),
          Constants.SERVICE_RANKING, 1000);
    }
  });

  protected Adaptable adaptable() {
    return context.request();
  }

  /**
   * Test LinkHandler.processLinkRequest pipelining implementation
   */
  @Test
  void testPipelining() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    // test pipelining and resolve link
    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        "/content/dummy-path",
        ImmutableValueMap.builder()
            .put(PN_LINK_TYPE, "dummy")
            .put("dummyLinkRef", "/path1")
            .build());
    LinkRequest linkRequest = new LinkRequest(linkResource, null, new LinkArgs().urlMode(UrlModes.DEFAULT));
    Link link = linkHandler.get(linkRequest).build();

    // make sure initial link reference is unmodified
    assertEquals("dummy", linkRequest.getResourceProperties().get(PN_LINK_TYPE, String.class));
    assertEquals("/path1", linkRequest.getResourceProperties().get("dummyLinkRef", String.class));
    assertEquals(UrlModes.DEFAULT, linkRequest.getLinkArgs().getUrlMode());

    // check preprocessed link reference
    assertEquals("/path1/pre1", link.getLinkRequest().getResourceProperties().get("dummyLinkRef", String.class));
    assertEquals(UrlModes.FULL_URL, link.getLinkRequest().getLinkArgs().getUrlMode());

    // check final link url and html element
    assertEquals(true, link.isValid());
    assertEquals("http://xyz/path1/pre1/post1", link.getUrl());
    assertNotNull(link.getAnchor());
    assertEquals("http://xyz/path1/pre1", link.getAnchor().getHRef());
  }

  @Test
  void testInvalid() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    Link link = linkHandler.invalid();
    assertFalse(link.isValid());
  }

  @Test
  void testLinkTargetUrlFallbackProperty() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    // build resource with fallbackproperty and component that has a fallback property name defined
    Resource componentResource = context.create().resource("/apps/app1/components/comp1",
        PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY, "fallbackProperty",
        PN_COMPONENT_LINK_TARGET_WINDOW_TARGET_FALLBACK_PROPERTY, "fallbackWindowTarget");
    Resource linkResource = context.create().resource("/content/dummy-path",
        ResourceResolver.PROPERTY_RESOURCE_TYPE, componentResource.getPath(),
        "fallbackProperty", "/fallbackpath1",
        "fallbackWindowTarget", "_blank");

    Link link = linkHandler.get(linkResource).build();

    // check final link url and html element
    assertEquals(true, link.isValid());
    assertEquals("http://xyz/fallbackpath1/post1", link.getUrl());
    assertNotNull(link.getAnchor());
    assertEquals(ImmutableMap.of("href", "http://xyz/fallbackpath1", "target", "_blank"), link.getAnchorAttributes());
  }

  @Test
  void testLinkTargetUrlFallbackProperty_MultipleProperties() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    // build resource with fallbackproperty and component that has a fallback property name defined
    Resource componentResource = context.create().resource("/apps/app1/components/comp1",
        PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY, new String[] { "fallbackProperty1", "fallbackProperty2" });
    Resource linkResource = context.create().resource("/content/dummy-path",
        ResourceResolver.PROPERTY_RESOURCE_TYPE, componentResource.getPath(),
        "fallbackProperty2", "/fallbackpath1");

    Link link = linkHandler.get(linkResource).build();

    // check final link url and html element
    assertEquals(true, link.isValid());
    assertEquals("http://xyz/fallbackpath1/post1", link.getUrl());
    assertNotNull(link.getAnchor());
    assertEquals(ImmutableMap.of("href", "http://xyz/fallbackpath1"), link.getAnchorAttributes());
  }

  @Test
  void testLinkTargetUrlFallbackProperty_IgnoreWhenValidLinkSet() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    // build resource with fallbackproperty and component that has a fallback property name defined
    Resource componentResource = context.create().resource("/apps/app1/components/comp1",
        PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY, "fallbackProperty");
    Resource linkResource = context.create().resource("/content/dummy-path",
        ResourceResolver.PROPERTY_RESOURCE_TYPE, componentResource.getPath(),
        "fallbackProperty", "/fallbackpath1",
        PN_LINK_TYPE, "dummy",
        "dummyLinkRef", "/path1");

    Link link = linkHandler.get(linkResource).build();

    // check final link url and html element
    assertEquals(true, link.isValid());
    assertEquals("http://xyz/path1/pre1/post1", link.getUrl());
    assertNotNull(link.getAnchor());
    assertEquals("http://xyz/path1/pre1", link.getAnchor().getHRef());
  }


  public static class TestLinkHandlerConfig extends LinkHandlerConfig {

    @Override
    public List<Class<? extends LinkType>> getLinkTypes() {
      return ImmutableList.<Class<? extends LinkType>>of(TestLinkType.class);
    }

    @Override
    public List<Class<? extends LinkProcessor>> getPreProcessors() {
      return ImmutableList.<Class<? extends LinkProcessor>>of(TestLinkPreProcessor.class);
    }

    @Override
    public List<Class<? extends LinkProcessor>> getPostProcessors() {
      return ImmutableList.<Class<? extends LinkProcessor>>of(TestLinkPostProcessor.class);
    }

  };

  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  })
  public static class TestLinkPreProcessor implements LinkProcessor {
    @Override
    public Link process(Link link) {
      LinkRequest linkRequest = link.getLinkRequest();
      // add path part "/pre1" to content ref
      String contentRef = linkRequest.getResourceProperties().get("dummyLinkRef", String.class);
      if (contentRef == null) {
        return link;
      }
      contentRef = contentRef + "/pre1";

      LinkRequest newLinkRequest = new LinkRequest(
          linkRequest.getResource(),
          linkRequest.getPage(),
          new LinkArgs().urlMode(UrlModes.FULL_URL));
      newLinkRequest.getResourceProperties().put("dummyLinkRef", contentRef);
      link.setLinkRequest(newLinkRequest);
      return link;
    }
  }

  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  })
  public static class TestLinkType extends LinkType {

    @Override
    public String getId() {
      return "dummy";
    }

    @Override
    public String getPrimaryLinkRefProperty() {
      return "dummyLinkRef";
    }

    @Override
    public boolean accepts(String linkRef) {
      return StringUtils.startsWith(linkRef, "/");
    }

    @Override
    public Link resolveLink(Link link) {
      String contentRef = link.getLinkRequest().getResourceProperties().get("dummyLinkRef", link.getLinkRequest().getReference());
      link.setUrl("http://xyz" + contentRef);
      return link;
    }

  }

  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  })
  public static class TestLinkPostProcessor implements LinkProcessor {
    @Override
    public Link process(Link link) {
      String linkUrl = StringUtils.defaultString(link.getUrl()) + "/post1";
      link.setUrl(linkUrl);
      return link;
    }
  }

}
