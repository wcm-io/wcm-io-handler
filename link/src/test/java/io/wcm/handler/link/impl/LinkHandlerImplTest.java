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
import io.wcm.config.api.annotations.Application;
import io.wcm.config.spi.ApplicationProvider;
import io.wcm.handler.link.AbstractLinkHandlerConfig;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.LinkHandlerConfig;
import io.wcm.handler.link.LinkMetadata;
import io.wcm.handler.link.LinkMetadataProcessor;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.LinkReference;
import io.wcm.handler.link.LinkType;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.args.LinkArgs;
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

  /**
   * Test LinkHandler.processLinkReference pipelining implementation
   */
  @Test
  public void testPipelining() {
    LinkHandler linkHandler = AdaptTo.notNull(context.request(), LinkHandler.class);

    // test pipelining and resolve link
    SyntheticLinkResource linkResource = new SyntheticLinkResource(context.resourceResolver(),
        ImmutableValueMap.builder()
        .put(LinkNameConstants.PN_LINK_TYPE, "dummy")
        .put("dummyLinkRef", "/path1")
        .build());
    LinkReference linkReference = new LinkReference(linkResource, LinkArgs.urlMode(UrlModes.DEFAULT));
    LinkMetadata linkMetadata = linkHandler.getLinkMetadata(linkReference);

    // make sure initial link reference is unmodified
    assertEquals("dummy", linkReference.getResourceProperties().get(LinkNameConstants.PN_LINK_TYPE, String.class));
    assertEquals("/path1", linkReference.getResourceProperties().get("dummyLinkRef", String.class));
    assertEquals(UrlModes.DEFAULT, linkReference.getLinkArgs().getUrlMode());
    assertEquals("dummy", linkMetadata.getOriginalLinkReference().getResourceProperties().get(LinkNameConstants.PN_LINK_TYPE, String.class));
    assertEquals("/path1", linkMetadata.getOriginalLinkReference().getResourceProperties().get("dummyLinkRef", String.class));
    assertEquals(UrlModes.DEFAULT, linkMetadata.getOriginalLinkReference().getLinkArgs().getUrlMode());

    // check preprocessed link reference
    assertEquals("/path1/pre1", linkMetadata.getLinkReference().getResourceProperties().get("dummyLinkRef", String.class));
    assertEquals(UrlModes.FULL_URL, linkMetadata.getLinkReference().getLinkArgs().getUrlMode());

    // check final link url and html element
    assertEquals(true, linkMetadata.isValid());
    assertEquals("http://xyz/path1/pre1/post1", linkMetadata.getLinkUrl());
    assertNotNull(linkMetadata.getAnchor());
    assertEquals("http://xyz/path1/pre1", linkMetadata.getAnchor().getHRef());

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
    public List<Class<? extends LinkMetadataProcessor>> getLinkMetadataPreProcessors() {
      return ImmutableList.<Class<? extends LinkMetadataProcessor>>of(TestLinkMetadataPreProcessor.class);
    }

    @Override
    public List<Class<? extends LinkMetadataProcessor>> getLinkMetadataPostProcessors() {
      return ImmutableList.<Class<? extends LinkMetadataProcessor>>of(TestLinkMetadataPostProcessor.class);
    }

  };

  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  })
  public static class TestLinkMetadataPreProcessor implements LinkMetadataProcessor {
    @Override
    public LinkMetadata process(LinkMetadata pLinkMetadata) {
      // add path part "/pre1" to content ref
      String contentRef = pLinkMetadata.getLinkReference().getResourceProperties().get("dummyLinkRef", String.class);
      contentRef = StringUtils.defaultString(contentRef) + "/pre1";
      pLinkMetadata.getLinkReference().getResourceProperties().put("dummyLinkRef", contentRef);
      pLinkMetadata.getLinkReference().getLinkArgs().setUrlMode(UrlModes.FULL_URL);
      return pLinkMetadata;
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
    public LinkMetadata resolveLink(LinkMetadata pLinkMetadata) {
      String contentRef = pLinkMetadata.getLinkReference().getResourceProperties().get("dummyLinkRef", String.class);
      pLinkMetadata.setLinkUrl("http://xyz" + contentRef);
      return pLinkMetadata;
    }

  }

  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  })
  public static class TestLinkMetadataPostProcessor implements LinkMetadataProcessor {
    @Override
    public LinkMetadata process(LinkMetadata pLinkMetadata) {
      String linkUrl = StringUtils.defaultString(pLinkMetadata.getLinkUrl()) + "/post1";
      pLinkMetadata.setLinkUrl(linkUrl);
      return pLinkMetadata;
    }
  }

}
