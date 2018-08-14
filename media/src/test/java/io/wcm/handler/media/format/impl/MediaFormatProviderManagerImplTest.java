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

import java.util.SortedSet;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;

import com.google.common.collect.ImmutableSortedSet;

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatBuilder;
import io.wcm.handler.media.spi.MediaFormatProvider;
import io.wcm.sling.commons.caservice.impl.ContextAwareServiceResolverImpl;
import io.wcm.testing.mock.aem.junit.AemContext;

@RunWith(MockitoJUnitRunner.Silent.class)
@SuppressWarnings("null")
public class MediaFormatProviderManagerImplTest {

  private static final MediaFormat MF11 = MediaFormatBuilder.create("mf11").description("desc-from-1").build();
  private static final MediaFormat MF12 = MediaFormatBuilder.create("mf12").description("desc-from-1").build();
  private static final SortedSet<MediaFormat> MEDIAFORMATS_1 = ImmutableSortedSet.of(MF11, MF12);

  private static final MediaFormat MF11_FROM2 = MediaFormatBuilder.create("mf11").description("desc-from-2").build();
  private static final MediaFormat MF21 = MediaFormatBuilder.create("mf21").description("desc-from-2").build();
  private static final SortedSet<MediaFormat> MEDIAFORMATS_2 = ImmutableSortedSet.of(MF11_FROM2, MF21);

  @Rule
  public AemContext context = new AemContext();

  @Mock
  private MediaFormatProvider provider1;
  @Mock
  private MediaFormatProvider provider2;

  private Resource resource;
  private MediaFormatProviderManager underTest;

  @Before
  public void setUp() {
    resource = context.create().resource("/content/test");

    context.registerInjectActivateService(new ContextAwareServiceResolverImpl());

    when(provider1.getMediaFormats()).thenReturn(MEDIAFORMATS_1);
    when(provider2.getMediaFormats()).thenReturn(MEDIAFORMATS_2);

    context.registerService(MediaFormatProvider.class, provider1,
        Constants.SERVICE_RANKING, 200);
    context.registerService(MediaFormatProvider.class, provider2,
        Constants.SERVICE_RANKING, 100);

    underTest = context.registerInjectActivateService(new MediaFormatProviderManagerImpl());
  }

  @Test
  public void testWithResource() {
    SortedSet<MediaFormat> result = underTest.getMediaFormats(resource);
    assertEquals(ImmutableSortedSet.of(MF11, MF12, MF21), result);

    MediaFormat first = result.iterator().next();
    assertEquals("mf11", first.getName());
    // make sure when multiplie providers define formats with the same name the one with the highest ranking wins
    assertEquals("desc-from-1", first.getDescription());
  }

  @Test
  public void testNullResource() {
    assertEquals(ImmutableSortedSet.of(), underTest.getMediaFormats(null));
  }

}
