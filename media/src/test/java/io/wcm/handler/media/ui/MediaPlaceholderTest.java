/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.handler.media.ui;

import static io.wcm.handler.media.testcontext.AppAemContext.ROOTPATH_CONTENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.EditConfig;
import com.day.cq.wcm.api.components.EditContext;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediaPlaceholderTest {

  private final AemContext context = AppAemContext.newAemContext();

  @Mock
  private ComponentContext wcmComponentContext;
  private Resource resource;

  @BeforeEach
  void setUp() {
    resource = context.create().resource(ROOTPATH_CONTENT + "/jcr:content/media",
        ImmutableValueMap.of(MediaNameConstants.PN_MEDIA_REF, "/content/dam/invalid"));

    // simulate component context
    context.request().setAttribute(ComponentContext.CONTEXT_ATTR_NAME, wcmComponentContext);
    when(wcmComponentContext.getResource()).thenReturn(resource);
    when(wcmComponentContext.getEditContext()).thenReturn(mock(EditContext.class));
    when(wcmComponentContext.getEditContext().getEditConfig()).thenReturn(mock(EditConfig.class));

    WCMMode.EDIT.toRequest(context.request());
  }

  @Test
  void testInvalidMedia() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
    Media media = mediaHandler.get(resource).build();
    context.request().setAttribute("media", media);

    MediaPlaceholder underTest = AdaptTo.notNull(context.request(), MediaPlaceholder.class);
    assertEquals("cq-dd-mediaRef", underTest.getClassAppend());
    assertEquals("io.wcm.handler.media.invalidReason.MEDIA_REFERENCE_INVALID", underTest.getMediaInvalidReason());
  }

  @Test
  void testWithMissingMediaParam() {
    MediaPlaceholder underTest = AdaptTo.notNull(context.request(), MediaPlaceholder.class);
    assertNull(underTest.getClassAppend());
    assertNull(underTest.getMediaInvalidReason());
  }

  @Test
  void testWithInvalidMediaParam() {
    context.request().setAttribute("media", new Object());
    MediaPlaceholder underTest = AdaptTo.notNull(context.request(), MediaPlaceholder.class);
    assertNull(underTest.getClassAppend());
    assertNull(underTest.getMediaInvalidReason());
  }

  @Test
  void testInvalidMedia_MergeClassAppends() {
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
    Media media = mediaHandler.get(resource).build();
    context.request().setAttribute("media", media);
    context.request().setAttribute("classAppend", "class1 cq-dd-mediaRef class2");

    MediaPlaceholder underTest = AdaptTo.notNull(context.request(), MediaPlaceholder.class);
    assertEquals("cq-dd-mediaRef class1 class2", underTest.getClassAppend());
  }

}
