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

import static io.wcm.handler.media.format.MediaFormatBuilder.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Image;
import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.spi.MediaMarkupBuilder;
import io.wcm.handler.media.spi.MediaSource;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

import org.apache.sling.api.resource.Resource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.day.cq.wcm.api.WCMMode;
import com.google.common.collect.ImmutableList;

/**
 * Test {@link SimpleImageMediaMarkupBuilder}
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleImageMediaMarkupBuilderTest {

  private static final MediaFormat DUMMY_FORMAT = create("dummyformat", AppAemContext.APPLICATION_ID).build();

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  @Mock
  private MediaSource mediaSource;
  @Mock
  private Asset asset;
  @Mock
  private Rendition rendition;
  @Mock
  private Resource resource;

  @Test
  public void testAccepts() {
    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    Media media = new Media(mediaSource, mediaRequest);
    when(rendition.isImage()).thenReturn(false);

    assertFalse("no rendition", builder.accepts(media));

    media.setAsset(asset);
    media.setRenditions(ImmutableList.of(rendition));

    assertFalse("invalid rendition", builder.accepts(media));

    when(rendition.getFileName()).thenReturn("movie.swf");
    when(rendition.isImage()).thenReturn(false);

    assertFalse("non-image rendition", builder.accepts(media));

    when(rendition.getFileName()).thenReturn("image.gif");
    when(rendition.isImage()).thenReturn(true);

    assertTrue("image rendition", builder.accepts(media));

    media.setMediaInvalidReason(MediaInvalidReason.NOT_ENOUGH_MATCHING_RENDITIONS);

    assertFalse("media is invalid", builder.accepts(media));

  }

  @Test
  public void testBuild() {
    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    mediaRequest.getMediaArgs().mediaFormat(DUMMY_FORMAT);
    Media media = new Media(mediaSource, mediaRequest);

    assertNull("no rendition", builder.build(media));

    media.setAsset(asset);
    media.setRenditions(ImmutableList.of(rendition));

    assertNull("invalid rendition", builder.build(media));

    when(rendition.getUrl()).thenReturn("/media/dummy.gif");

    HtmlElement<?> element = builder.build(media);
    assertNotNull("valid rendition", element);
    assertEquals("src", "/media/dummy.gif", element.getAttributeValue("src"));
    assertEquals("width", null, element.getAttributeValue("width"));
    assertEquals("height", null, element.getAttributeValue("height"));
    assertEquals("alt", null, element.getAttributeValue("alt"));

    when(rendition.getWidth()).thenReturn(100L);
    when(rendition.getHeight()).thenReturn(50L);
    when(asset.getAltText()).thenReturn("Der Jodelkaiser");

    element = builder.build(media);
    assertNotNull("valid rendition with additional attributes", element);
    assertEquals("src", "/media/dummy.gif", element.getAttributeValue("src"));
    assertEquals("width", 100, element.getAttributeValueAsInteger("width"));
    assertEquals("height", 50, element.getAttributeValueAsInteger("height"));
    assertEquals("alt", "Der Jodelkaiser", element.getAttributeValue("alt"));

  }

  @Test
  public void testBuild_EditMode() {
    WCMMode.EDIT.toRequest(context.request());

    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest(resource, new MediaArgs());
    when(resource.getValueMap()).thenReturn(ImmutableValueMap.of(MediaNameConstants.PN_MEDIA_REF, "/media/dummy"));
    mediaRequest.getMediaArgs().mediaFormat(DUMMY_FORMAT);
    Media media = new Media(mediaSource, mediaRequest);
    media.setAsset(asset);
    media.setRenditions(ImmutableList.of(rendition));
    when(rendition.getUrl()).thenReturn("/media/dummy.gif");

    HtmlElement<?> element = builder.build(media);
    verify(mediaSource).enableMediaDrop(element, mediaRequest);
  }

  @Test
  public void testBuild_PreviewMode_DiffDecoration() {
    WCMMode.PREVIEW.toRequest(context.request());

    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest(resource, new MediaArgs());
    when(resource.getValueMap()).thenReturn(ImmutableValueMap.of(MediaNameConstants.PN_MEDIA_REF, "/media/dummy"));
    mediaRequest.getMediaArgs().mediaFormat(DUMMY_FORMAT);
    Media media = new Media(mediaSource, mediaRequest);
    media.setAsset(asset);
    media.setRenditions(ImmutableList.of(rendition));
    when(rendition.getUrl()).thenReturn("/media/dummy.gif");

    builder.build(media);
  }

  @Test
  public void testIsValidMedia() {
    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    assertFalse(builder.isValidMedia(null));
    assertFalse(builder.isValidMedia(new Image()));
    assertTrue(builder.isValidMedia(new Image("/any/path.gif")));
    assertFalse(builder.isValidMedia(new Image(MediaMarkupBuilder.DUMMY_IMAGE).setCssClass(MediaNameConstants.CSS_DUMMYIMAGE)));

  }

}
