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
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Image;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.Asset;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.args.MediaArgs;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.spi.MediaMarkupBuilder;
import io.wcm.handler.media.spi.MediaSource;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.url.UrlModes;
import io.wcm.testing.mock.aem.junit.AemContext;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.day.cq.wcm.api.WCMMode;

/**
 * Test {@link EditPlaceholderMediaMarkupBuilder}
 */
@RunWith(MockitoJUnitRunner.class)
public class EditPlaceholderMediaMarkupBuilderTest {

  private static final MediaFormat DUMMY_FORMAT = create("dummyformat", AppAemContext.APPLICATION_ID).build();

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  @Mock
  private MediaSource mediaSource;
  @Mock
  private Asset asset;
  @Mock
  private Rendition rendition;

  @Before
  public void setUp() {
    when(rendition.isImage()).thenReturn(true);
  }

  @Test
  public void testAccepts_DISABLED() {
    WCMMode.DISABLED.toRequest(context.request());

    MediaMarkupBuilder builder = context.request().adaptTo(EditPlaceholderMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    Media media = new Media(mediaSource, mediaRequest);

    // test with wcm modes, without rendition, without mediaformat, no suppress
    mediaRequest.getMediaArgs().setMediaFormat(null).setNoDummyImage(false);
    assertFalse(builder.accepts(media));

    // test with wcm modes, without rendition, with mediaformat, no suppress
    mediaRequest.getMediaArgs().setMediaFormat(null).setNoDummyImage(false);
    mediaRequest.getMediaArgs().setMediaFormat(DUMMY_FORMAT);
    assertFalse(builder.accepts(media));

    // test with wcm modes, without rendition, with mediaformat, with suppress
    mediaRequest.getMediaArgs().setMediaFormat(DUMMY_FORMAT).setNoDummyImage(true);
    assertFalse(builder.accepts(media));

    // test with wcm modes, with rendition, with mediaformat, no suppress
    mediaRequest.getMediaArgs().setMediaFormat(DUMMY_FORMAT).setNoDummyImage(false);
    media.setAsset(asset);
    media.setRendition(rendition);
    assertFalse(builder.accepts(media));

  }

  @Test
  public void testAccepts_PREVIEW() {
    WCMMode.PREVIEW.toRequest(context.request());

    MediaMarkupBuilder builder = context.request().adaptTo(EditPlaceholderMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    Media media = new Media(mediaSource, mediaRequest);

    // test with wcm modes, without rendition, without mediaformat, no suppress
    mediaRequest.getMediaArgs().setMediaFormat(null).setNoDummyImage(false);
    assertFalse(builder.accepts(media));

    // test with wcm modes, without rendition, with mediaformat, no suppress
    mediaRequest.getMediaArgs().setMediaFormat(null).setNoDummyImage(false);
    mediaRequest.getMediaArgs().setMediaFormat(DUMMY_FORMAT);
    assertFalse(builder.accepts(media));

    // test with wcm modes, without rendition, with mediaformat, with suppress
    mediaRequest.getMediaArgs().setMediaFormat(DUMMY_FORMAT).setNoDummyImage(true);
    assertFalse(builder.accepts(media));

    // test with wcm modes, with rendition, with mediaformat, no suppress
    mediaRequest.getMediaArgs().setMediaFormat(DUMMY_FORMAT).setNoDummyImage(false);
    media.setAsset(asset);
    media.setRendition(rendition);
    assertFalse(builder.accepts(media));

  }

  @Test
  public void testAccepts_EDIT() {
    WCMMode.EDIT.toRequest(context.request());

    MediaMarkupBuilder builder = context.request().adaptTo(EditPlaceholderMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    Media media = new Media(mediaSource, mediaRequest);

    // test with wcm modes, without rendition, without mediaformat, no suppress
    mediaRequest.getMediaArgs().setMediaFormat(null).setNoDummyImage(false);
    assertFalse(builder.accepts(media));

    // test with wcm modes, without rendition, with mediaformat, no suppress
    mediaRequest.getMediaArgs().setMediaFormat(null).setNoDummyImage(false);
    mediaRequest.getMediaArgs().setMediaFormat(DUMMY_FORMAT);
    assertTrue(builder.accepts(media));

    // test with wcm modes, without rendition, with mediaformat, with suppress
    mediaRequest.getMediaArgs().setMediaFormat(DUMMY_FORMAT).setNoDummyImage(true);
    assertFalse(builder.accepts(media));

    // test with wcm modes, with rendition, with mediaformat, no suppress
    mediaRequest.getMediaArgs().setMediaFormat(DUMMY_FORMAT).setNoDummyImage(false);
    media.setAsset(asset);
    media.setRendition(rendition);
    assertFalse(builder.accepts(media));

  }

  @Test
  public void testBuild() {
    MediaMarkupBuilder builder = context.request().adaptTo(EditPlaceholderMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/invalid/media", new MediaArgs());
    mediaRequest.getMediaArgs().setMediaFormat(DUMMY_FORMAT);
    Media media = new Media(mediaSource, mediaRequest);

    HtmlElement<?> element = builder.build(media);

    assertNotNull(element);
    assertEquals(MediaMarkupBuilder.DUMMY_IMAGE, element.getAttributeValue("src"));
    assertEquals(MediaMarkupBuilder.DUMMY_MIN_DIMENSION, element.getAttributeValueAsInteger("width"));
    assertEquals(MediaMarkupBuilder.DUMMY_MIN_DIMENSION, element.getAttributeValueAsInteger("height"));
    assertEquals(MediaNameConstants.CSS_DUMMYIMAGE, element.getAttributeValue("class"));

  }

  @Test
  public void testBuildWithUrlMode() {
    MediaMarkupBuilder builder = context.request().adaptTo(EditPlaceholderMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/invalid/media", MediaArgs.urlMode(UrlModes.FULL_URL));
    mediaRequest.getMediaArgs().setMediaFormat(DUMMY_FORMAT);
    Media media = new Media(mediaSource, mediaRequest);

    HtmlElement<?> element = builder.build(media);

    assertNotNull(element);
    assertEquals("http://www.dummysite.org" + MediaMarkupBuilder.DUMMY_IMAGE, element.getAttributeValue("src"));
    assertEquals(MediaMarkupBuilder.DUMMY_MIN_DIMENSION, element.getAttributeValueAsInteger("width"));
    assertEquals(MediaMarkupBuilder.DUMMY_MIN_DIMENSION, element.getAttributeValueAsInteger("height"));
    assertEquals(MediaNameConstants.CSS_DUMMYIMAGE, element.getAttributeValue("class"));

  }

  @Test
  public void testIsValidMedia() {
    MediaMarkupBuilder builder = context.request().adaptTo(EditPlaceholderMediaMarkupBuilder.class);

    assertFalse(builder.isValidMedia(null));
    assertFalse(builder.isValidMedia(new Image()));
    assertFalse(builder.isValidMedia(new Image("/any/path.gif")));
    assertFalse(builder.isValidMedia(new Image(MediaMarkupBuilder.DUMMY_IMAGE)));

  }

  @Test
  public void testWithMediaFormat() {

    MediaMarkupBuilder builder = context.request().adaptTo(EditPlaceholderMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/invalid/media", new MediaArgs());
    mediaRequest.getMediaArgs().setMediaFormat(EDITORIAL_1COL);
    Media media = new Media(mediaSource, mediaRequest);

    HtmlElement<?> element = builder.build(media);

    assertNotNull(element);
    assertEquals(MediaMarkupBuilder.DUMMY_IMAGE, element.getAttributeValue("src"));
    assertEquals(215, element.getAttributeValueAsInteger("width"));
    assertEquals(102, element.getAttributeValueAsInteger("height"));
    assertEquals(MediaNameConstants.CSS_DUMMYIMAGE, element.getAttributeValue("class"));

  }

}
