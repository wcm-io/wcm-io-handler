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
package io.wcm.handler.media.impl.ipeconfig;

import static io.wcm.handler.media.impl.ipeconfig.CroppingRatios.MEDIAFORMAT_FREE_CROP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableSet;

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatBuilder;
import io.wcm.handler.media.format.MediaFormatHandler;

@ExtendWith(MockitoExtension.class)
class CroppingRatiosTest {

  @Mock(lenient = true)
  private MediaFormatHandler mediaFormatHandler;
  private SortedSet<MediaFormat> mediaFormats = new TreeSet<>();

  private MediaFormat ratio16_9;
  private MediaFormat ratio32_10;
  private MediaFormat ratio2_1a;

  private CroppingRatios underTest;

  @BeforeEach
  void setUp() {
    mediaFormats = new TreeSet<>();

    // image media formats with ratio
    ratio16_9 = addMediaFormat(MediaFormatBuilder.create("ratio16_9").extensions("jpg").ratio(16, 9));
    addMediaFormat(MediaFormatBuilder.create("ratio4_3").extensions("jpg").ratio(4, 3).minWidth(500));
    ratio32_10 = addMediaFormat(MediaFormatBuilder.create("ratio32_10").extensions("jpg").ratio(32, 10));
    addMediaFormat(MediaFormatBuilder.create("ratio32_10_fixeddim").extensions("jpg").fixedDimension(640, 200));

    // multiple image media formats with same ratio
    ratio2_1a = addMediaFormat(MediaFormatBuilder.create("ratio2_1a").extensions("jpg").fixedDimension(200, 100));
    addMediaFormat(MediaFormatBuilder.create("ratio2_1b").extensions("jpg").fixedDimension(100, 50));
    addMediaFormat(MediaFormatBuilder.create("ratio2_1c").extensions("jpg").fixedDimension(400, 200));

    // image media format without ratio
    addMediaFormat(MediaFormatBuilder.create("minwidth").extensions("jpg").minWidth(500));

    // no image media format
    addMediaFormat(MediaFormatBuilder.create("download").extensions("pdf", "txt"));

    when(mediaFormatHandler.getMediaFormats()).thenReturn(mediaFormats);

    underTest = new CroppingRatios(mediaFormatHandler);
  }

  @Test
  void testGetMediaFormatsForCropping_MediaArgsWithMediaFormats() {
    MediaRequest mediaRequest = new MediaRequest("/content/dam/test.jpg", new MediaArgs()
        .mediaFormats(ratio16_9, ratio32_10, ratio2_1a));

    Set<String> result = underTest.getMediaFormatsForCropping(mediaRequest);

    assertEquals(ImmutableSet.of("ratio16_9", "ratio32_10", "ratio2_1a"), result);
  }

  @Test
  void testGetMediaFormatsForCropping_AllMediaFormats() {
    MediaRequest mediaRequest = new MediaRequest("/content/dam/test.jpg", new MediaArgs());

    Set<String> result = underTest.getMediaFormatsForCropping(mediaRequest);

    assertEquals(ImmutableSet.of(MEDIAFORMAT_FREE_CROP.getName(), "ratio16_9", "ratio4_3", "ratio32_10", "ratio2_1c"), result);
  }

  private MediaFormat addMediaFormat(MediaFormatBuilder mediaFormatBuilder) {
    MediaFormat mediaFormat = mediaFormatBuilder.build();
    mediaFormats.add(mediaFormat);
    return mediaFormat;
  }

}
