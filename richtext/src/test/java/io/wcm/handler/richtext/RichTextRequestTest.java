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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.url.UrlModes;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class RichTextRequestTest {

  private AemContext context = new AemContext();

  @Test
  void testWithResource() {
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
  void testWithText() {
    RichTextRequest underTest = new RichTextRequest(null, "text", null, null, null);
    assertEquals("text", underTest.getText());
    assertNull(underTest.getUrlMode());
    assertNull(underTest.getTextMode());
    assertNull(underTest.getMediaArgs());
    assertTrue(underTest.getResourceProperties().isEmpty());
  }

  @Test
  void testWithBoth() {
    assertThrows(IllegalArgumentException.class, () -> {
      Resource resource = context.create().resource("/test/resource");
      new RichTextRequest(resource, "text", null, null, null);
    });
  }

  @Test
  void testToString() {
    RichTextRequest underTest = new RichTextRequest(null, "text", null, null, null);
    assertEquals("RichTextRequest[text=text]", underTest.toString());
  }

}
