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
package io.wcm.handler.media.format.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatBuilder;
import io.wcm.handler.media.spi.MediaFormatProvider;
import io.wcm.testing.mock.aem.junit.AemContext;

import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableSortedSet;

@RunWith(MockitoJUnitRunner.class)
public class MediaFormatProviderManagerImplTest {

  private static final String APP_ID_1 = "/apps/app1";
  private static final String APP_ID_2 = "/apps/app2";

  private static final SortedSet<MediaFormat> MEDIAFORMATS_1 = ImmutableSortedSet.of(
      MediaFormatBuilder.create("mf11", APP_ID_1).build(),
      MediaFormatBuilder.create("mf12", APP_ID_1).build()
      );

  private static final SortedSet<MediaFormat> MEDIAFORMATS_2 = ImmutableSortedSet.of(
      MediaFormatBuilder.create("mf21", APP_ID_2).build()
      );

  public AemContext context = new AemContext();

  @Mock
  private MediaFormatProvider provider1;
  @Mock
  private MediaFormatProvider provider2;

  private MediaFormatProviderManager underTest;

  @Before
  public void setUp() {
    when(provider1.getMediaFormats()).thenReturn(MEDIAFORMATS_1);
    when(provider2.getMediaFormats()).thenReturn(MEDIAFORMATS_2);

    context.registerService(MediaFormatProvider.class, provider1);
    context.registerService(MediaFormatProvider.class, provider2);

    underTest = context.registerInjectActivateService(new MediaFormatProviderManagerImpl());
  }

  @Test
  public void testGetMediaFormats() {
    assertEquals(MEDIAFORMATS_1, underTest.getMediaFormats(APP_ID_1));
    assertEquals(MEDIAFORMATS_2, underTest.getMediaFormats(APP_ID_2));
  }

  @Test
  public void testInvalidApplicationId() {
    assertEquals(MEDIAFORMATS_1, underTest.getMediaFormats(APP_ID_1));
    assertEquals(MEDIAFORMATS_2, underTest.getMediaFormats(APP_ID_2));
  }

}
