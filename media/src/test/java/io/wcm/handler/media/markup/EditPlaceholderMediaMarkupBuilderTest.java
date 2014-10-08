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
import io.wcm.handler.media.MediaItem;
import io.wcm.handler.media.MediaMetadata;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaReference;
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
  private MediaItem mediaItem;
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

    MediaReference mediaReference = new MediaReference("/media/dummy", new MediaArgs());
    MediaMetadata mediaMetadata = new MediaMetadata(mediaReference, mediaReference, mediaSource);

    // test with wcm modes, without rendition, without mediaformat, no suppress
    mediaReference.getMediaArgs().setMediaFormat(null).setNoDummyImage(false);
    assertFalse(builder.accepts(mediaMetadata));

    // test with wcm modes, without rendition, with mediaformat, no suppress
    mediaReference.getMediaArgs().setMediaFormat(null).setNoDummyImage(false);
    mediaReference.getMediaArgs().setMediaFormat(DUMMY_FORMAT);
    assertFalse(builder.accepts(mediaMetadata));

    // test with wcm modes, without rendition, with mediaformat, with suppress
    mediaReference.getMediaArgs().setMediaFormat(DUMMY_FORMAT).setNoDummyImage(true);
    assertFalse(builder.accepts(mediaMetadata));

    // test with wcm modes, with rendition, with mediaformat, no suppress
    mediaReference.getMediaArgs().setMediaFormat(DUMMY_FORMAT).setNoDummyImage(false);
    mediaMetadata.setMediaItem(mediaItem);
    mediaMetadata.setRendition(rendition);
    assertFalse(builder.accepts(mediaMetadata));

  }

  @Test
  public void testAccepts_PREVIEW() {
    WCMMode.PREVIEW.toRequest(context.request());

    MediaMarkupBuilder builder = context.request().adaptTo(EditPlaceholderMediaMarkupBuilder.class);

    MediaReference mediaReference = new MediaReference("/media/dummy", new MediaArgs());
    MediaMetadata mediaMetadata = new MediaMetadata(mediaReference, mediaReference, mediaSource);

    // test with wcm modes, without rendition, without mediaformat, no suppress
    mediaReference.getMediaArgs().setMediaFormat(null).setNoDummyImage(false);
    assertFalse(builder.accepts(mediaMetadata));

    // test with wcm modes, without rendition, with mediaformat, no suppress
    mediaReference.getMediaArgs().setMediaFormat(null).setNoDummyImage(false);
    mediaReference.getMediaArgs().setMediaFormat(DUMMY_FORMAT);
    assertFalse(builder.accepts(mediaMetadata));

    // test with wcm modes, without rendition, with mediaformat, with suppress
    mediaReference.getMediaArgs().setMediaFormat(DUMMY_FORMAT).setNoDummyImage(true);
    assertFalse(builder.accepts(mediaMetadata));

    // test with wcm modes, with rendition, with mediaformat, no suppress
    mediaReference.getMediaArgs().setMediaFormat(DUMMY_FORMAT).setNoDummyImage(false);
    mediaMetadata.setMediaItem(mediaItem);
    mediaMetadata.setRendition(rendition);
    assertFalse(builder.accepts(mediaMetadata));

  }

  @Test
  public void testAccepts_EDIT() {
    WCMMode.EDIT.toRequest(context.request());

    MediaMarkupBuilder builder = context.request().adaptTo(EditPlaceholderMediaMarkupBuilder.class);

    MediaReference mediaReference = new MediaReference("/media/dummy", new MediaArgs());
    MediaMetadata mediaMetadata = new MediaMetadata(mediaReference, mediaReference, mediaSource);

    // test with wcm modes, without rendition, without mediaformat, no suppress
    mediaReference.getMediaArgs().setMediaFormat(null).setNoDummyImage(false);
    assertFalse(builder.accepts(mediaMetadata));

    // test with wcm modes, without rendition, with mediaformat, no suppress
    mediaReference.getMediaArgs().setMediaFormat(null).setNoDummyImage(false);
    mediaReference.getMediaArgs().setMediaFormat(DUMMY_FORMAT);
    assertTrue(builder.accepts(mediaMetadata));

    // test with wcm modes, without rendition, with mediaformat, with suppress
    mediaReference.getMediaArgs().setMediaFormat(DUMMY_FORMAT).setNoDummyImage(true);
    assertFalse(builder.accepts(mediaMetadata));

    // test with wcm modes, with rendition, with mediaformat, no suppress
    mediaReference.getMediaArgs().setMediaFormat(DUMMY_FORMAT).setNoDummyImage(false);
    mediaMetadata.setMediaItem(mediaItem);
    mediaMetadata.setRendition(rendition);
    assertFalse(builder.accepts(mediaMetadata));

  }

  @Test
  public void testBuild() {
    MediaMarkupBuilder builder = context.request().adaptTo(EditPlaceholderMediaMarkupBuilder.class);

    MediaReference mediaReference = new MediaReference("/invalid/media", new MediaArgs());
    mediaReference.getMediaArgs().setMediaFormat(DUMMY_FORMAT);
    MediaMetadata mediaMetadata = new MediaMetadata(mediaReference, mediaReference, mediaSource);

    HtmlElement<?> media = builder.build(mediaMetadata);

    assertNotNull(media);
    assertEquals(MediaMarkupBuilder.DUMMY_IMAGE, media.getAttributeValue("src"));
    assertEquals(MediaMarkupBuilder.DUMMY_MIN_DIMENSION, media.getAttributeValueAsInteger("width"));
    assertEquals(MediaMarkupBuilder.DUMMY_MIN_DIMENSION, media.getAttributeValueAsInteger("height"));
    assertEquals(MediaNameConstants.CSS_DUMMYIMAGE, media.getAttributeValue("class"));

  }

  @Test
  public void testBuildWithUrlMode() {
    MediaMarkupBuilder builder = context.request().adaptTo(EditPlaceholderMediaMarkupBuilder.class);

    MediaReference mediaReference = new MediaReference("/invalid/media", MediaArgs.urlMode(UrlModes.FULL_URL));
    mediaReference.getMediaArgs().setMediaFormat(DUMMY_FORMAT);
    MediaMetadata mediaMetadata = new MediaMetadata(mediaReference, mediaReference, mediaSource);

    HtmlElement<?> media = builder.build(mediaMetadata);

    assertNotNull(media);
    assertEquals("http://www.dummysite.org" + MediaMarkupBuilder.DUMMY_IMAGE, media.getAttributeValue("src"));
    assertEquals(MediaMarkupBuilder.DUMMY_MIN_DIMENSION, media.getAttributeValueAsInteger("width"));
    assertEquals(MediaMarkupBuilder.DUMMY_MIN_DIMENSION, media.getAttributeValueAsInteger("height"));
    assertEquals(MediaNameConstants.CSS_DUMMYIMAGE, media.getAttributeValue("class"));

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

    MediaReference mediaReference = new MediaReference("/invalid/media", new MediaArgs());
    mediaReference.getMediaArgs().setMediaFormat(EDITORIAL_1COL);
    MediaMetadata mediaMetadata = new MediaMetadata(mediaReference, mediaReference, mediaSource);

    HtmlElement<?> media = builder.build(mediaMetadata);

    assertNotNull(media);
    assertEquals(MediaMarkupBuilder.DUMMY_IMAGE, media.getAttributeValue("src"));
    assertEquals(215, media.getAttributeValueAsInteger("width"));
    assertEquals(102, media.getAttributeValueAsInteger("height"));
    assertEquals(MediaNameConstants.CSS_DUMMYIMAGE, media.getAttributeValue("class"));

  }

}
