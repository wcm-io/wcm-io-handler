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
package io.wcm.handler.media.spi.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import io.wcm.handler.media.markup.DummyImageMediaMarkupBuilder;
import io.wcm.handler.media.markup.SimpleImageMediaMarkupBuilder;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.mediasource.dam.DamMediaSource;

import org.junit.Test;

public class AbstractMediaHandlerConfigTest {

  private MediaHandlerConfig underTest = new AbstractMediaHandlerConfig() {
    // not overrides
  };

  @Test
  public void testGetSources() {
    assertEquals(1, underTest.getSources().size());
    assertEquals(DamMediaSource.class, underTest.getSources().get(0));
  }

  @Test
  public void testGetMarkupBuilders() {
    assertEquals(2, underTest.getMarkupBuilders().size());
    assertEquals(SimpleImageMediaMarkupBuilder.class, underTest.getMarkupBuilders().get(0));
    assertEquals(DummyImageMediaMarkupBuilder.class, underTest.getMarkupBuilders().get(1));
  }

  @Test
  public void testGetProcessors() {
    assertTrue(underTest.getPreProcessors().isEmpty());
    assertTrue(underTest.getPostProcessors().isEmpty());
  }

  @Test
  public void testGetDownloadMediaFormats() {
    assertTrue(underTest.getDownloadMediaFormats().isEmpty());
  }

  @Test
  public void testGetDefaultImageQuality() {
    assertEquals(1d, underTest.getDefaultImageQuality("image/png"), 0.001d);
    assertEquals(256d, underTest.getDefaultImageQuality("image/gif"), 0.001d);
    assertEquals(MediaHandlerConfig.DEFAULT_JPEG_QUALITY, underTest.getDefaultImageQuality("image/jpg"), 0.001d);
    assertEquals(MediaHandlerConfig.DEFAULT_JPEG_QUALITY, underTest.getDefaultImageQuality("IMAGE/JPEG"), 0.001d);
    assertEquals(1d, underTest.getDefaultImageQuality(""), 0.001d);
    assertEquals(1d, underTest.getDefaultImageQuality(null), 0.001d);
  }

}
