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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Image;
import io.wcm.handler.media.MediaItem;
import io.wcm.handler.media.MediaMarkupBuilder;
import io.wcm.handler.media.MediaMetadata;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaReference;
import io.wcm.handler.media.MediaSource;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.args.MediaArgs;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit.AemContext;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test {@link SimpleImageMediaMarkupBuilder}
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleImageMediaMarkupBuilderTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  @Mock
  private MediaSource mediaSource;
  @Mock
  private MediaItem mediaItem;
  @Mock
  private Rendition rendition;

  @Test
  public void testAccepts() {
    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), SimpleImageMediaMarkupBuilder.class);

    MediaReference mediaReference = new MediaReference("/media/dummy", new MediaArgs());
    MediaMetadata mediaMetadata = new MediaMetadata(mediaReference, mediaReference, mediaSource);
    when(rendition.isImage()).thenReturn(false);

    assertFalse("no rendition", builder.accepts(mediaMetadata));

    mediaMetadata.setMediaItem(mediaItem);
    mediaMetadata.setRendition(rendition);

    assertFalse("invalid rendition", builder.accepts(mediaMetadata));

    when(rendition.getFileName()).thenReturn("movie.swf");
    when(rendition.isImage()).thenReturn(false);

    assertFalse("non-image rendition", builder.accepts(mediaMetadata));

    when(rendition.getFileName()).thenReturn("image.gif");
    when(rendition.isImage()).thenReturn(true);

    assertTrue("image rendition", builder.accepts(mediaMetadata));

  }

  @Test
  public void testBuild() {
    MediaMarkupBuilder builder = new SimpleImageMediaMarkupBuilder();

    MediaReference mediaReference = new MediaReference("/media/dummy", new MediaArgs());
    mediaReference.getMediaArgs().setMediaFormat("dummyformat");
    MediaMetadata mediaMetadata = new MediaMetadata(mediaReference, mediaReference, mediaSource);

    assertNull("no rendition", builder.build(mediaMetadata));

    mediaMetadata.setMediaItem(mediaItem);
    mediaMetadata.setRendition(rendition);

    assertNull("invalid rendition", builder.build(mediaMetadata));

    when(rendition.getMediaUrl()).thenReturn("/mediay/dummy.gif");

    HtmlElement<?> media = builder.build(mediaMetadata);
    assertNotNull("valid rendition", media);
    assertEquals("src", "/mediay/dummy.gif", media.getAttributeValue("src"));
    assertEquals("width", null, media.getAttributeValue("width"));
    assertEquals("height", null, media.getAttributeValue("height"));
    assertEquals("alt", null, media.getAttributeValue("alt"));

    when(rendition.getWidth()).thenReturn(100);
    when(rendition.getHeight()).thenReturn(50);
    when(mediaItem.getAltText()).thenReturn("Der Jodelkaiser");

    media = builder.build(mediaMetadata);
    assertNotNull("valid rendition with additional attributes", media);
    assertEquals("src", "/mediay/dummy.gif", media.getAttributeValue("src"));
    assertEquals("width", 100, media.getAttributeValueAsInteger("width"));
    assertEquals("height", 50, media.getAttributeValueAsInteger("height"));
    assertEquals("alt", "Der Jodelkaiser", media.getAttributeValue("alt"));

  }

  @Test
  public void testIsValidMedia() {
    MediaMarkupBuilder builder = new SimpleImageMediaMarkupBuilder();

    assertFalse(builder.isValidMedia(null));
    assertFalse(builder.isValidMedia(new Image()));
    assertTrue(builder.isValidMedia(new Image("/any/path.gif")));
    assertFalse(builder.isValidMedia(new Image(MediaMarkupBuilder.DUMMY_IMAGE).setCssClass(MediaNameConstants.CSS_DUMMYIMAGE)));

  }

}
