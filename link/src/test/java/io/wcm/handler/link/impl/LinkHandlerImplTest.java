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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import io.wcm.config.spi.ApplicationProvider;
import io.wcm.config.spi.annotations.Application;
import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkArgs;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.LinkRequest;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.spi.LinkHandlerConfig;
import io.wcm.handler.link.spi.LinkProcessor;
import io.wcm.handler.link.spi.LinkType;
import io.wcm.handler.link.spi.helpers.AbstractLinkHandlerConfig;
import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.handler.link.type.AbstractLinkType;
import io.wcm.handler.url.UrlModes;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextCallback;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.Constants;

import com.google.common.collect.ImmutableList;

/**
 * Test {@link LinkHandlerImpl} methods.
 */
public class LinkHandlerImplTest {

  static final String APP_ID = "linkHandlerImplTestApp";

  @Rule
  public final AemContext context = AppAemContext.newAemContext(new AemContextCallback() {

    @Override
    public void execute(AemContext callbackContext) {
      callbackContext.registerService(ApplicationProvider.class, new TestApplicationProvider(),
          ImmutableValueMap.of(Constants.SERVICE_RANKING, 1));
    }
  });

  protected Adaptable adaptable() {
    return context.request();
  }

  /**
   * Test LinkHandler.processLinkRequest pipelining implementation
   */
  @Test
  public void testPipelining() {
    LinkHandler linkHandler = AdaptTo.notNull(adaptable(), LinkHandler.class);

    // test pipelining and resolve link
    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, "dummy")
        .put("dummyLinkRef", "/path1")
        .build());
    LinkRequest linkRequest = new LinkRequest(linkResource, null, new LinkArgs().urlMode(UrlModes.DEFAULT));
    Link link = linkHandler.get(linkRequest).build();

    // make sure initial link reference is unmodified
    assertEquals("dummy", linkRequest.getResourceProperties().get(LinkNameConstants.PN_LINK_TYPE, String.class));
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


  public static class TestApplicationProvider implements ApplicationProvider {

    @Override
    public String getApplicationId() {
      return APP_ID;
    }

    @Override
    public String getLabel() {
      return null;
    }

    @Override
    public boolean matches(Resource resource) {
      return true;
    }
  }

  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  }, adapters = LinkHandlerConfig.class)
  @Application(APP_ID)
  public static class TestLinkHandlerConfig extends AbstractLinkHandlerConfig {

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
      contentRef = StringUtils.defaultString(contentRef) + "/pre1";

      LinkRequest newLinkRequest = new LinkRequest(
          linkRequest.getResource(),
          linkRequest.getPage(),
          new LinkArgs().urlMode(UrlModes.FULL_URL)
          );
      newLinkRequest.getResourceProperties().put("dummyLinkRef", contentRef);
      link.setLinkRequest(newLinkRequest);
      return link;
    }
  }

  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  })
  public static class TestLinkType extends AbstractLinkType {

    @Override
    public String getId() {
      return "dummy";
    }

    @Override
    public String getPrimaryLinkRefProperty() {
      return "dummyLinkRef";
    }

    @Override
    public boolean accepts(String pLinkRef) {
      return false;
    }

    @Override
    public Link resolveLink(Link link) {
      String contentRef = link.getLinkRequest().getResourceProperties().get("dummyLinkRef", String.class);
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
