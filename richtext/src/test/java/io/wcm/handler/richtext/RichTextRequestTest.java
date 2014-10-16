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
package io.wcm.handler.richtext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.url.UrlModes;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RichTextRequestTest {

  @Rule
  public AemContext context = new AemContext();

  @Test
  public void testWithResource() {
    ValueMap props = ImmutableValueMap.of("prop1", "value1");
    Resource resource = context.create().resource("/test/resource", props);

    RichTextRequest underTest = new RichTextRequest(resource, null, UrlModes.FULL_URL, TextMode.PLAIN, new MediaArgs("mf1"));
    assertSame(resource, underTest.getResource());
    assertEquals(UrlModes.FULL_URL, underTest.getUrlMode());
    assertEquals(TextMode.PLAIN, underTest.getTextMode());
    assertEquals("mf1", underTest.getMediaArgs().getMediaFormatNames()[0]);
    assertEquals(props, ImmutableValueMap.copyOf(underTest.getResourceProperties()));

  }

  @Test
  public void testWithText() {
    RichTextRequest underTest = new RichTextRequest(null, "text", null, null, null);
    assertEquals("text", underTest.getText());
    assertNull(underTest.getUrlMode());
    assertNull(underTest.getTextMode());
    assertNull(underTest.getMediaArgs());
    assertTrue(underTest.getResourceProperties().isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithBoth() {
    Resource resource = context.create().resource("/test/resource");
    new RichTextRequest(resource, "text", null, null, null);
  }

}
