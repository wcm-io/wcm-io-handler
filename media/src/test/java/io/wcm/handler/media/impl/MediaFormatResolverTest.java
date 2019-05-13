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
package io.wcm.handler.media.impl;

import static io.wcm.handler.media.impl.MediaFormatResolver.MEDIAFORMAT_NAME_SEPARATOR;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_2COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.FIXEDWIDTH_UNCONSTRAINED;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO2;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaArgs.ImageSizes;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatHandler;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediaFormatResolverTest {

  @Mock
  private MediaFormatHandler mediaFormatHandler;

  private MediaFormatResolver underTest;

  @BeforeEach
  void setUp() {
    when(mediaFormatHandler.getMediaFormat(EDITORIAL_1COL.getName())).thenReturn(EDITORIAL_1COL);
    when(mediaFormatHandler.getMediaFormat(EDITORIAL_2COL.getName())).thenReturn(EDITORIAL_2COL);

    underTest = new MediaFormatResolver(mediaFormatHandler);
  }

  @Test
  void testMediaFormats() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormats(EDITORIAL_1COL, EDITORIAL_2COL);

    assertTrue(underTest.resolve(mediaArgs));

    assertArrayEquals(new MediaFormat[] { EDITORIAL_1COL, EDITORIAL_2COL }, mediaArgs.getMediaFormats());
  }

  @Test
  void testMediaFormatNamesAllValid() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormatNames(EDITORIAL_1COL.getName(), EDITORIAL_2COL.getName());

    assertTrue(underTest.resolve(mediaArgs));

    assertArrayEquals(new MediaFormat[] { EDITORIAL_1COL, EDITORIAL_2COL }, mediaArgs.getMediaFormats());
  }

  @Test
  void testMediaFormatNamesSomeInvalid() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormatNames(EDITORIAL_1COL.getName(), "invalid", EDITORIAL_2COL.getName());

    assertFalse(underTest.resolve(mediaArgs));

    assertArrayEquals(new MediaFormat[] { EDITORIAL_1COL, null, EDITORIAL_2COL }, mediaArgs.getMediaFormats());
  }

  @Test
  void testMediaFormatNamesAllInvalid() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormatNames("invalid1", "invalid2");

    assertFalse(underTest.resolve(mediaArgs));

    assertArrayEquals(new MediaFormat[] { null, null }, mediaArgs.getMediaFormats());
  }

  @Test
  void testImageSizes() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormat(RATIO)
        .imageSizes(new ImageSizes("size1", 10, 20));

    assertTrue(underTest.resolve(mediaArgs));

    MediaFormat[] mediaFormats = mediaArgs.getMediaFormats();
    assertEquals(3, mediaFormats.length);
    assertEquals(RATIO, mediaFormats[0]);
    assertResponsiveMediaFormat(RATIO, 10, mediaFormats[1]);
    assertResponsiveMediaFormat(RATIO, 20, mediaFormats[2]);
  }

  @Test
  void testImageSizes_NoRatioMediaFormat() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormat(FIXEDWIDTH_UNCONSTRAINED)
        .imageSizes(new ImageSizes("size1", 10, 20));

    assertTrue(underTest.resolve(mediaArgs));

    MediaFormat[] mediaFormats = mediaArgs.getMediaFormats();
    assertEquals(3, mediaFormats.length);
    assertEquals(FIXEDWIDTH_UNCONSTRAINED, mediaFormats[0]);
    assertResponsiveMediaFormat(FIXEDWIDTH_UNCONSTRAINED, 10, mediaFormats[1]);
    assertResponsiveMediaFormat(FIXEDWIDTH_UNCONSTRAINED, 20, mediaFormats[2]);
  }

  @Test
  void testPictureSources_DifferentRatio() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormat(RATIO)
        .pictureSources(new PictureSource[] {
            new PictureSource(RATIO, "media1", 20, 30),
            new PictureSource(RATIO2, null, 10, 20)
        });

    assertTrue(underTest.resolve(mediaArgs));

    MediaFormat[] mediaFormats = mediaArgs.getMediaFormats();
    assertEquals(5, mediaFormats.length);
    assertEquals(RATIO, mediaFormats[0]);
    assertResponsiveMediaFormat(RATIO, 20, mediaFormats[1]);
    assertResponsiveMediaFormat(RATIO, 30, mediaFormats[2]);
    assertResponsiveMediaFormat(RATIO2, 10, mediaFormats[3]);
    assertResponsiveMediaFormat(RATIO2, 20, mediaFormats[4]);
  }

  @Test
  void testPictureSources_SameRatio() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormat(RATIO)
        .pictureSources(new PictureSource[] {
            new PictureSource(RATIO, "media1", 20, 30),
            new PictureSource(RATIO, null, 10, 20)
        });

    assertTrue(underTest.resolve(mediaArgs));

    MediaFormat[] mediaFormats = mediaArgs.getMediaFormats();
    assertEquals(4, mediaFormats.length);
    assertEquals(RATIO, mediaFormats[0]);
    assertResponsiveMediaFormat(RATIO, 20, mediaFormats[1]);
    assertResponsiveMediaFormat(RATIO, 30, mediaFormats[2]);
    assertResponsiveMediaFormat(RATIO, 10, mediaFormats[3]);
  }

  private void assertResponsiveMediaFormat(MediaFormat baseMediaFormat, long width, MediaFormat actualMediaFormat) {
    assertEquals(baseMediaFormat.getName() + MEDIAFORMAT_NAME_SEPARATOR + width, actualMediaFormat.getName());
    assertEquals(width, actualMediaFormat.getWidth());
    assertEquals(baseMediaFormat.getRatio(), actualMediaFormat.getRatio(), 0.001);
  }

}
