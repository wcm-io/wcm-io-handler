/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.handler.mediasource.dam.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.mediasource.dam.AbstractDamTest;

@SuppressWarnings("null")
public class DefaultRenditionHandlerTest extends AbstractDamTest {

  private DefaultRenditionHandler underTest;

  @Before
  public void setUp() throws Exception {
    Asset asset = context.resourceResolver().getResource(MEDIAITEM_PATH_16_10).adaptTo(Asset.class);
    underTest = new DefaultRenditionHandler(asset);
  }

  @Test
  public void testOriginal() throws Exception {
    RenditionMetadata rendition = underTest.getRendition(new MediaArgs());
    assertEquals(1600, rendition.getWidth());
    assertEquals(1000, rendition.getHeight());
  }

  @Test
  public void testInvalidRatio() throws Exception {
    RenditionMetadata rendition = underTest.getRendition(new MediaArgs()
        .fixedWidth(100)
        .fixedHeight(100));
    assertNull(rendition);
  }

  @Test
  public void testFixedWith() throws Exception {
    RenditionMetadata rendition = underTest.getRendition(new MediaArgs()
        .fixedWidth(160));
    assertEquals(160, rendition.getWidth());
    assertEquals(100, rendition.getHeight());
  }

  @Test
  public void testFixedHeight() throws Exception {
    RenditionMetadata rendition = underTest.getRendition(new MediaArgs()
        .fixedHeight(100));
    assertEquals(160, rendition.getWidth());
    assertEquals(100, rendition.getHeight());
  }

}
