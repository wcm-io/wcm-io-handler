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
package io.wcm.handler.media;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import io.wcm.handler.commons.dom.Div;
import io.wcm.handler.media.spi.MediaSource;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class MediaTest {

  @Mock
  private MediaSource mediaSource;
  private MediaRequest mediaRequest;

  private Media underTest;

  @Before
  public void setUp() {
    mediaRequest = new MediaRequest("/media/ref", new MediaArgs());
    underTest = new Media(mediaSource, mediaRequest);
  }


  @Test
  public void testMediaSourceRequest() {
    assertSame(mediaSource, underTest.getMediaSource());
    assertSame(mediaRequest, underTest.getMediaRequest());

    MediaRequest mediaRequest2 = new MediaRequest("/media/ref2", new MediaArgs());
    underTest.setMediaRequest(mediaRequest2);
    assertSame(mediaRequest2, underTest.getMediaRequest());
  }

  @Test
  public void testElement() {
    Div div = new Div();
    div.setText("test");

    underTest.setElement(div);
    assertSame(div, underTest.getElement());
    assertEquals("<div>test</div>", underTest.getMarkup());
  }

  @Test
  public void testUrlAndValid() {
    underTest.setUrl("/my/url");

    assertEquals("/my/url", underTest.getUrl());
  }

  @Test
  public void testAsset() {
    Asset asset = mock(Asset.class);
    underTest.setAsset(asset);
    assertSame(asset, underTest.getAsset());
  }


  @Test
  public void testRenditions() {
    assertNull(underTest.getRendition());
    assertTrue(underTest.getRenditions().isEmpty());

    Rendition rendition1 = mock(Rendition.class);
    Rendition rendition2 = mock(Rendition.class);
    Collection<Rendition> renditions = ImmutableList.of(rendition1, rendition2);
    underTest.setRenditions(renditions);

    assertSame(rendition1, underTest.getRendition());
    assertEquals(renditions, underTest.getRenditions());
  }

  @Test
  public void testCropDimension() {
    CropDimension dimension = new CropDimension(1, 2, 3, 4);
    underTest.setCropDimension(dimension);
    assertSame(dimension, underTest.getCropDimension());
  }

  @Test
  public void testMediaInvalidReason() {
    assertTrue(underTest.isValid());
    underTest.setMediaInvalidReason(MediaInvalidReason.MEDIA_REFERENCE_INVALID);
    assertEquals(MediaInvalidReason.MEDIA_REFERENCE_INVALID, underTest.getMediaInvalidReason());
    assertFalse(underTest.isValid());
  }

  @Test
  public void testToString() {
    assertEquals(
        "Media[mediaSource=mediaSource,mediaRequest=MediaRequest[mediaRef=/media/ref,"
            + "mediaArgs=MediaArgs[mediaFormatsMandatory=false,fixedWidth=0,fixedHeight=0,forceDownload=false,dummyImage=true,dragDropSupport=AUTO]]]",
            underTest.toString());
  }

}
