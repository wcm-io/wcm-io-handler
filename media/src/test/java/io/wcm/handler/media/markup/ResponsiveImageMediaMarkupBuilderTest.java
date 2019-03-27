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
package io.wcm.handler.media.markup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.wcm.api.WCMMode;
import com.google.common.collect.ImmutableList;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Image;
import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.spi.MediaMarkupBuilder;
import io.wcm.handler.media.spi.MediaSource;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.media.testcontext.DummyMediaFormats;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * Test ResponsiveImageMediaMarkupBuilder
 */
@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "deprecation", "null" })
class ResponsiveImageMediaMarkupBuilderTest {

  private final AemContext context = AppAemContext.newAemContext();

  @Mock
  private MediaSource mediaSource;
  @Mock
  private Asset asset;

  @Mock
  private Rendition renditionL;
  @Mock
  private Rendition renditionS;

  @Mock
  private Resource resource;

  @Test
  void testAccepts() {
    MediaMarkupBuilder underTest = AdaptTo.notNull(context.request(), ResponsiveImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    Media media = new Media(mediaSource, mediaRequest);

    assertFalse(underTest.accepts(media), "no rendition");

    media.setRenditions(ImmutableList.of(renditionL));

    assertFalse(underTest.accepts(media),"media format not mandatory");

    mediaRequest.getMediaArgs().mandatoryMediaFormats(DummyMediaFormats.RESPONSIVE_32_9_L1);

    assertFalse(underTest.accepts(media),"no multiple media formats");

    mediaRequest.getMediaArgs().mandatoryMediaFormats(DummyMediaFormats.RESPONSIVE_32_9_L1, DummyMediaFormats.RESPONSIVE_32_9_M1);

    assertFalse(underTest.accepts(media),"only one rendition");

    media.setRenditions(ImmutableList.of(renditionL, renditionS));

    assertTrue(underTest.accepts(media));

    media.setMediaInvalidReason(MediaInvalidReason.MEDIA_REFERENCE_MISSING);

    assertFalse(underTest.accepts(media));
  }

  @Test
  void testBuild() throws JSONException {
    MediaMarkupBuilder underTest = AdaptTo.notNull(context.request(), ResponsiveImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    Media media = new Media(mediaSource, mediaRequest);
    mediaRequest.getMediaArgs().mandatoryMediaFormats(DummyMediaFormats.RESPONSIVE_32_9_L1, DummyMediaFormats.RESPONSIVE_32_9_M1);

    media.setRenditions(ImmutableList.of(renditionL, renditionS));
    when(renditionL.getUrl()).thenReturn("/media/dummy/1920x600png");
    when(renditionL.getMediaFormat()).thenReturn(DummyMediaFormats.RESPONSIVE_32_9_L1);
    when(renditionS.getUrl()).thenReturn("/media/dummy/120x100png");
    when(renditionS.getMediaFormat()).thenReturn(DummyMediaFormats.RESPONSIVE_32_9_M1);

    HtmlElement image = underTest.build(media);
    assertNotNull(image);

    JSONArray sources = new JSONArray(image.getAttributeValue("data-resp-src"));
    assertNotNull(sources);
    assertEquals(2, sources.length());

    assertEquals("L1", sources.getJSONObject(0).get(MediaNameConstants.PROP_BREAKPOINT));
    assertEquals("/media/dummy/1920x600png", sources.getJSONObject(0).get("src"));

    assertEquals("M1", sources.getJSONObject(1).get(MediaNameConstants.PROP_BREAKPOINT));
    assertEquals("/media/dummy/120x100png", sources.getJSONObject(1).get("src"));
    assertNull(image.getAttributeValue("alt"), "alt");

    when(asset.getAltText()).thenReturn("Alt Text");
    media.setAsset(asset);
    image = underTest.build(media);
    assertEquals("Alt Text", image.getAttributeValue("alt"), "alt");

    // compare whole string
    assertEquals(
        "<img alt=\"Alt Text\" data-resp-src=\"[{&quot;mq&quot;:&quot;L1&quot;,&quot;src&quot;:&quot;/media/dummy/1920x600png&quot;},"
            + "{&quot;mq&quot;:&quot;M1&quot;,&quot;src&quot;:&quot;/media/dummy/120x100png&quot;}]\" />",
        image.toString());
  }

  @Test
  void testBuild_EditMode() {
    WCMMode.EDIT.toRequest(context.request());

    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), ResponsiveImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest(resource, new MediaArgs());
    Media media = new Media(mediaSource, mediaRequest);
    mediaRequest.getMediaArgs().mandatoryMediaFormats(DummyMediaFormats.RESPONSIVE_32_9_L1, DummyMediaFormats.RESPONSIVE_32_9_M1);
    media.setAsset(asset);
    media.setRenditions(ImmutableList.of(renditionL, renditionS));
    when(renditionL.getUrl()).thenReturn("/media/dummy/1920x600png");
    when(renditionL.getMediaFormat()).thenReturn(DummyMediaFormats.RESPONSIVE_32_9_L1);
    when(renditionS.getUrl()).thenReturn("/media/dummy/120x100png");
    when(renditionS.getMediaFormat()).thenReturn(DummyMediaFormats.RESPONSIVE_32_9_M1);

    HtmlElement<?> element = builder.build(media);
    verify(mediaSource).enableMediaDrop(element, mediaRequest);
  }


  @Test
  void testBuild_PreviewMode() {
    WCMMode.PREVIEW.toRequest(context.request());

    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), ResponsiveImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest(resource, new MediaArgs());
    Media media = new Media(mediaSource, mediaRequest);
    mediaRequest.getMediaArgs().mandatoryMediaFormats(DummyMediaFormats.RESPONSIVE_32_9_L1, DummyMediaFormats.RESPONSIVE_32_9_M1);
    media.setAsset(asset);
    media.setRenditions(ImmutableList.of(renditionL, renditionS));
    when(renditionL.getUrl()).thenReturn("/media/dummy/1920x600png");
    when(renditionL.getMediaFormat()).thenReturn(DummyMediaFormats.RESPONSIVE_32_9_L1);
    when(renditionS.getUrl()).thenReturn("/media/dummy/120x100png");
    when(renditionS.getMediaFormat()).thenReturn(DummyMediaFormats.RESPONSIVE_32_9_M1);

    HtmlElement<?> element = builder.build(media);
    verify(mediaSource).enableMediaDrop(element, mediaRequest);
  }

  @Test
  void testIsValidMedia() {
    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), ResponsiveImageMediaMarkupBuilder.class);

    assertFalse(builder.isValidMedia(null));
    assertFalse(builder.isValidMedia(new Image()));
    assertFalse(builder.isValidMedia(new Image("/any/path.gif")));
    assertFalse(builder.isValidMedia(new Image(MediaMarkupBuilder.DUMMY_IMAGE).setCssClass(MediaNameConstants.CSS_DUMMYIMAGE)));

    assertTrue(builder.isValidMedia(new Image().setData("resp-src", "[{'mg': 'test', 'src':'/dummy/img.png'}]")));

  }

}
