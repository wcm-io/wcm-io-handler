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

import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.NONFIXED_RAW;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.jdom2.Element;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.wcm.api.WCMMode;
import com.google.common.collect.ImmutableList;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Image;
import io.wcm.handler.commons.dom.Picture;
import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaArgs.ImageSizes;
import io.wcm.handler.media.MediaArgs.PictureSource;
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
import io.wcm.testing.mock.aem.junit5.AemContext;

/**
 * Test {@link SimpleImageMediaMarkupBuilder}
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class SimpleImageMediaMarkupBuilderTest {

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
  public void testBuild_NoRendition() {
    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    mediaRequest.getMediaArgs().mediaFormat(EDITORIAL_1COL);
    Media media = new Media(mediaSource, mediaRequest);

    assertNull("no rendition", builder.build(media));
  }

  @Test
  public void testBuild_InvalidRendition() {
    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    mediaRequest.getMediaArgs().mediaFormat(EDITORIAL_1COL);
    Media media = new Media(mediaSource, mediaRequest);
    media.setAsset(asset);
    media.setRenditions(ImmutableList.of(rendition));

    assertNull("invalid rendition", builder.build(media));
  }

  @Test
  public void testBuild_ValidRendition() {
    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    mediaRequest.getMediaArgs().mediaFormat(EDITORIAL_1COL);
    Media media = new Media(mediaSource, mediaRequest);
    media.setAsset(asset);
    media.setRenditions(ImmutableList.of(rendition));
    when(rendition.getUrl()).thenReturn("/media/dummy.gif");

    HtmlElement<?> element = builder.build(media);
    assertTrue(element instanceof Image);
    assertEquals("src", "/media/dummy.gif", element.getAttributeValue("src"));
    assertNull("width", element.getAttributeValue("width"));
    assertNull("height", element.getAttributeValue("height"));
    assertNull("alt", element.getAttributeValue("alt"));
  }

  @Test
  public void testBuild_ValidRendition_WidthHeightAlt() {
    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    mediaRequest.getMediaArgs().mediaFormat(EDITORIAL_1COL);
    mediaRequest.getMediaArgs().property("custom-property", "value1");
    Media media = new Media(mediaSource, mediaRequest);
    media.setAsset(asset);
    media.setRenditions(ImmutableList.of(rendition));
    when(rendition.getUrl()).thenReturn("/media/dummy.gif");

    when(rendition.getWidth()).thenReturn(100L);
    when(rendition.getHeight()).thenReturn(50L);
    when(asset.getAltText()).thenReturn("Der Jodelkaiser");

    HtmlElement<?> element = builder.build(media);
    assertTrue(element instanceof Image);
    assertEquals("src", "/media/dummy.gif", element.getAttributeValue("src"));
    assertEquals("width", 100, element.getAttributeValueAsInteger("width"));
    assertEquals("height", 50, element.getAttributeValueAsInteger("height"));
    assertEquals("alt", "Der Jodelkaiser", element.getAttributeValue("alt"));
    assertEquals("custom-property", "value1", element.getAttributeValue("custom-property"));
  }

  @Test
  public void testBuild_ImageSizes() {
    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    mediaRequest.getMediaArgs().mediaFormat(RATIO);
    mediaRequest.getMediaArgs().imageSizes(new ImageSizes("sizes1", 64, 32, 16));
    mediaRequest.getMediaArgs().property("custom-property", "value1");
    Media media = new Media(mediaSource, mediaRequest);
    media.setAsset(asset);
    media.setRenditions(ImmutableList.of(rendition(RATIO, 128), rendition(RATIO, 64), rendition(RATIO, 16)));

    HtmlElement<?> element = builder.build(media);
    assertTrue(element instanceof Image);
    assertEquals("src", "/media/dummy.128.png", element.getAttributeValue("src"));
    assertNull("width", element.getAttributeValue("width"));
    assertNull("height", element.getAttributeValue("height"));
    assertEquals("sizes", "sizes1", element.getAttributeValue("sizes"));
    assertEquals("srcset", "/media/dummy.64.png 64w, /media/dummy.16.png 16w", element.getAttributeValue("srcset"));
    assertEquals("custom-property", "value1", element.getAttributeValue("custom-property"));
  }

  @Test
  public void testBuild_ImageSizes_NoRatio() {
    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    mediaRequest.getMediaArgs().mediaFormat(NONFIXED_RAW);
    mediaRequest.getMediaArgs().imageSizes(new ImageSizes("sizes1", 64, 32, 16));
    Media media = new Media(mediaSource, mediaRequest);
    media.setAsset(asset);
    media.setRenditions(ImmutableList.of(rendition(RATIO, 128), rendition(RATIO, 64), rendition(RATIO, 16)));

    HtmlElement<?> element = builder.build(media);
    assertTrue(element instanceof Image);
    assertEquals("src", "/media/dummy.128.png", element.getAttributeValue("src"));
    assertNull("width", element.getAttributeValue("width"));
    assertNull("height", element.getAttributeValue("height"));
    assertNull("sizes", element.getAttributeValue("sizes"));
    assertNull("srcset", element.getAttributeValue("srcset"));
  }

  @Test
  public void testBuild_Picture() {
    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    mediaRequest.getMediaArgs().mediaFormat(RATIO);
    mediaRequest.getMediaArgs().pictureSources(new PictureSource[] {
        new PictureSource(RATIO, "media1", 64, 32),
        new PictureSource(EDITORIAL_1COL, "media2", 215),
        new PictureSource(RATIO2, null, 40),
    });
    mediaRequest.getMediaArgs().property("custom-property", "value1");
    Media media = new Media(mediaSource, mediaRequest);
    media.setAsset(asset);
    media.setRenditions(ImmutableList.of(rendition(RATIO, 128), rendition(RATIO, 64), rendition(RATIO, 32), rendition(RATIO, 16),
        rendition(RATIO2, 40), rendition(RATIO2, 20)));

    HtmlElement<?> picture = builder.build(media);
    assertTrue(picture instanceof Picture);

    List<Element> sources = picture.getChildren("source");
    assertEquals(2, sources.size());
    HtmlElement<?> source1 = (HtmlElement<?>)sources.get(0);
    assertEquals("media1", source1.getAttributeValue("media"));
    assertEquals("/media/dummy.64.png 64w, /media/dummy.32.png 32w", source1.getAttributeValue("srcset"));
    HtmlElement<?> source2 = (HtmlElement<?>)sources.get(1);
    assertNull(source2.getAttributeValue("media"));
    assertEquals("/media/dummy.40.png 40w", source2.getAttributeValue("srcset"));

    HtmlElement<?> img = (HtmlElement<?>)picture.getChild("img");
    assertTrue(img instanceof Image);

    assertEquals("src", "/media/dummy.128.png", img.getAttributeValue("src"));
    assertNull("width", img.getAttributeValue("width"));
    assertNull("height", img.getAttributeValue("height"));
    assertNull("sizes", picture.getAttributeValue("sizes"));
    assertNull("srcset", picture.getAttributeValue("srcset"));
    assertEquals("custom-property", "value1", img.getAttributeValue("custom-property"));
  }

  @Test
  public void testBuild_EditMode() {
    WCMMode.EDIT.toRequest(context.request());

    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest(resource, new MediaArgs());
    when(resource.getValueMap()).thenReturn(ImmutableValueMap.of(MediaNameConstants.PN_MEDIA_REF, "/media/dummy"));
    mediaRequest.getMediaArgs().mediaFormat(EDITORIAL_1COL);
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
    mediaRequest.getMediaArgs().mediaFormat(EDITORIAL_1COL);
    Media media = new Media(mediaSource, mediaRequest);
    media.setAsset(asset);
    media.setRenditions(ImmutableList.of(rendition));
    when(rendition.getUrl()).thenReturn("/media/dummy.gif");

    builder.build(media);
  }

  @Test
  public void testIsValidMedia_Image() {
    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    assertFalse(builder.isValidMedia(null));
    assertFalse(builder.isValidMedia(new Image()));
    assertTrue(builder.isValidMedia(new Image("/any/path.gif")));
    assertFalse(builder.isValidMedia(new Image(MediaMarkupBuilder.DUMMY_IMAGE).setCssClass(MediaNameConstants.CSS_DUMMYIMAGE)));
  }

  @Test
  public void testIsValidMedia_Picture() {
    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    assertFalse(builder.isValidMedia(null));
    assertFalse(builder.isValidMedia(new Picture()));

    Picture picture = new Picture();
    picture.add(new Image("/any/path.gif"));
    assertTrue(builder.isValidMedia(picture));

    picture = new Picture().setCssClass(MediaNameConstants.CSS_DUMMYIMAGE);
    picture.add(new Image("/any/path.gif"));
    assertFalse(builder.isValidMedia(picture));
  }

  private Rendition rendition(MediaFormat mediaFormat, long width) {
    Rendition r = mock(Rendition.class);
    when(r.getWidth()).thenReturn(width);
    when(r.getHeight()).thenReturn(Math.round(width / mediaFormat.getRatio()));
    when(r.getRatio()).thenReturn(mediaFormat.getRatio());
    when(r.getUrl()).thenReturn("/media/dummy." + width + ".png");
    return r;
  }

}
