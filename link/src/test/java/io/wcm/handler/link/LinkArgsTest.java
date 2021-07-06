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
package io.wcm.handler.link;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

import io.wcm.handler.url.UrlModes;
import io.wcm.sling.commons.resource.ImmutableValueMap;

class LinkArgsTest {

  @Test
  void testProperties() {
    LinkArgs linkArgs = new LinkArgs()
        .urlMode(UrlModes.FULL_URL)
        .dummyLink(true)
        .dummyLinkUrl("/test/url")
        .selectors("sel1")
        .extension("ext1")
        .suffix("suffix1")
        .queryString("query1")
        .fragment("fragment1")
        .windowTarget("_parent")
        .linkTargetUrlFallbackProperty("property1");

    assertEquals(UrlModes.FULL_URL, linkArgs.getUrlMode());
    assertTrue(linkArgs.isDummyLink());
    assertEquals("/test/url", linkArgs.getDummyLinkUrl());
    assertEquals("sel1", linkArgs.getSelectors());
    assertEquals("ext1", linkArgs.getExtension());
    assertEquals("suffix1", linkArgs.getSuffix());
    assertEquals("query1", linkArgs.getQueryString());
    assertEquals("fragment1", linkArgs.getFragment());
    assertEquals("_parent", linkArgs.getWindowTarget());
    assertArrayEquals(new String[] { "property1" }, linkArgs.getLinkTargetUrlFallbackProperty());
  }

  @Test
  void testGetProperties() {
    Map<String, Object> props = ImmutableMap.<String, Object>of("prop1", "value1");

    LinkArgs linkArgs = new LinkArgs()
        .property("prop3", "value3")
        .properties(props)
        .property("prop2", "value2");

    assertEquals(3, linkArgs.getProperties().size());
    assertEquals("value1", linkArgs.getProperties().get("prop1", String.class));
    assertEquals("value2", linkArgs.getProperties().get("prop2", String.class));
    assertEquals("value3", linkArgs.getProperties().get("prop3", String.class));
  }

  @Test
  void testClone() {
    Map<String, Object> props = ImmutableValueMap.of("prop1", "value1", "prop2", "value2");

    LinkArgs linkArgs = new LinkArgs()
        .urlMode(UrlModes.FULL_URL)
        .dummyLink(true)
        .dummyLinkUrl("/test/url")
        .selectors("sel1")
        .extension("ext1")
        .suffix("suffix1")
        .queryString("query1")
        .fragment("fragment1")
        .windowTarget("_blank")
        .linkTargetUrlFallbackProperty("property1")
        .linkTargetWindowTargetFallbackProperty("property2")
        .properties(props);

    LinkArgs clone = linkArgs.clone();
    assertNotSame(linkArgs, clone);
    assertEquals(linkArgs.getUrlMode(), clone.getUrlMode());
    assertEquals(linkArgs.isDummyLink(), clone.isDummyLink());
    assertEquals(linkArgs.getDummyLinkUrl(), clone.getDummyLinkUrl());
    assertEquals(linkArgs.getSelectors(), clone.getSelectors());
    assertEquals(linkArgs.getExtension(), clone.getExtension());
    assertEquals(linkArgs.getSuffix(), clone.getSuffix());
    assertEquals(linkArgs.getQueryString(), clone.getQueryString());
    assertEquals(linkArgs.getFragment(), clone.getFragment());
    assertEquals(linkArgs.getWindowTarget(), clone.getWindowTarget());
    assertArrayEquals(linkArgs.getLinkTargetUrlFallbackProperty(), clone.getLinkTargetUrlFallbackProperty());
    assertArrayEquals(linkArgs.getLinkTargetWindowTargetFallbackProperty(), clone.getLinkTargetWindowTargetFallbackProperty());
    assertEquals(ImmutableValueMap.copyOf(linkArgs.getProperties()), ImmutableValueMap.copyOf(clone.getProperties()));
  }


}
