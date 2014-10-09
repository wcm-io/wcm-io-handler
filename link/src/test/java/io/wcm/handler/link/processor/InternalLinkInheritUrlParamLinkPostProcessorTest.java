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
package io.wcm.handler.link.processor;

import static org.junit.Assert.assertEquals;
import io.wcm.handler.commons.dom.Anchor;
import io.wcm.handler.link.Link;
import io.wcm.handler.link.spi.LinkProcessor;
import io.wcm.handler.link.testcontext.AppAemContext;
import io.wcm.handler.link.type.ExternalLinkType;
import io.wcm.handler.link.type.InternalLinkType;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit.AemContext;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Test {@link AbstractInternalLinkInheritUrlParamLinkPostProcessor}.
 */
public class InternalLinkInheritUrlParamLinkPostProcessorTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  @Test
  public void testInternalLinkWithDefaultParameterList() {
    LinkProcessor postProcessor = AdaptTo.notNull(context.request(), DefaultInternalLinkInheritUrlParamLinkPostProcessor.class);

    Link link = new Link(new InternalLinkType(), null);
    link.setUrl("/sample.html");
    link.setAnchor(new Anchor().setHRef("/sample.html"));

    // test without url parameters
    postProcessor.process(link);
    assertEquals("/sample.html", link.getUrl());

    // test with url parameters
    context.request().setQueryString("debugClientLibs=true&abc=123");

    postProcessor.process(link);
    assertEquals("/sample.html?debugClientLibs=true", link.getUrl());
  }

  @Test
  public void testInternalLinkWithCustomParameterList() {
    LinkProcessor postProcessor = AdaptTo.notNull(context.request(), AbcInternalLinkInheritUrlParamLinkPostProcessor.class);

    Link link = new Link(new InternalLinkType(), null);
    link.setUrl("/sample.html");
    link.setAnchor(new Anchor().setHRef("/sample.html"));

    // test without url parameters
    postProcessor.process(link);
    assertEquals("/sample.html", link.getUrl());

    // test with url parameters
    context.request().setQueryString("debugClientLibs=true&abc=123");

    postProcessor.process(link);
    assertEquals("/sample.html?abc=123", link.getUrl());
  }

  @Test
  public void testExternalLinkWithDefaultParameterList() {
    LinkProcessor postProcessor = AdaptTo.notNull(context.request(), DefaultInternalLinkInheritUrlParamLinkPostProcessor.class);

    Link link = new Link(new ExternalLinkType(), null);
    link.setUrl("/sample.html");
    link.setAnchor(new Anchor().setHRef("/sample.html"));

    // test without url parameters
    postProcessor.process(link);
    assertEquals("/sample.html", link.getUrl());

    // test with url parameters
    context.request().setQueryString("debugClientLibs=true&abc=123");

    postProcessor.process(link);
    assertEquals("/sample.html", link.getUrl());
  }


  @Model(adaptables = {
      SlingHttpServletRequest.class, Resource.class
  })
  public static class AbcInternalLinkInheritUrlParamLinkPostProcessor extends AbstractInternalLinkInheritUrlParamLinkPostProcessor {
    public AbcInternalLinkInheritUrlParamLinkPostProcessor() {
      super(ImmutableSet.of("abc"));
    }
  }

}
