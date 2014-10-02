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
import io.wcm.handler.link.LinkMetadata;
import io.wcm.handler.link.LinkMetadataProcessor;
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
    LinkMetadataProcessor postProcessor = AdaptTo.notNull(context.request(), DefaultInternalLinkInheritUrlParamLinkPostProcessor.class);

    LinkMetadata linkMetadata = new LinkMetadata(null, null, new InternalLinkType());
    linkMetadata.setLinkUrl("/sample.html");
    linkMetadata.setAnchor(new Anchor().setHRef("/sample.html"));

    // test without url parameters
    postProcessor.process(linkMetadata);
    assertEquals("/sample.html", linkMetadata.getLinkUrl());

    // test with url parameters
    context.request().setQueryString("debugClientLibs=true&abc=123");

    postProcessor.process(linkMetadata);
    assertEquals("/sample.html?debugClientLibs=true", linkMetadata.getLinkUrl());
  }

  @Test
  public void testInternalLinkWithCustomParameterList() {
    LinkMetadataProcessor postProcessor = AdaptTo.notNull(context.request(), AbcInternalLinkInheritUrlParamLinkPostProcessor.class);

    LinkMetadata linkMetadata = new LinkMetadata(null, null, new InternalLinkType());
    linkMetadata.setLinkUrl("/sample.html");
    linkMetadata.setAnchor(new Anchor().setHRef("/sample.html"));

    // test without url parameters
    postProcessor.process(linkMetadata);
    assertEquals("/sample.html", linkMetadata.getLinkUrl());

    // test with url parameters
    context.request().setQueryString("debugClientLibs=true&abc=123");

    postProcessor.process(linkMetadata);
    assertEquals("/sample.html?abc=123", linkMetadata.getLinkUrl());
  }

  @Test
  public void testExternalLinkWithDefaultParameterList() {
    LinkMetadataProcessor postProcessor = AdaptTo.notNull(context.request(), DefaultInternalLinkInheritUrlParamLinkPostProcessor.class);

    LinkMetadata linkMetadata = new LinkMetadata(null, null, new ExternalLinkType());
    linkMetadata.setLinkUrl("/sample.html");
    linkMetadata.setAnchor(new Anchor().setHRef("/sample.html"));

    // test without url parameters
    postProcessor.process(linkMetadata);
    assertEquals("/sample.html", linkMetadata.getLinkUrl());

    // test with url parameters
    context.request().setQueryString("debugClientLibs=true&abc=123");

    postProcessor.process(linkMetadata);
    assertEquals("/sample.html", linkMetadata.getLinkUrl());
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
