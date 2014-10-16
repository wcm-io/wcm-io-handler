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

import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_2COL;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.url.UrlModes;
import io.wcm.sling.commons.resource.ImmutableValueMap;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class MediaArgsTest {

  @Test
    public void testGetMediaFormats() {
      MediaArgs mediaArgs;
  
      mediaArgs = new MediaArgs(EDITORIAL_1COL);
      assertArrayEquals(new MediaFormat[] {
          EDITORIAL_1COL
      }, mediaArgs.getMediaFormats());
      assertFalse(mediaArgs.isMediaFormatsMandatory());
  
      mediaArgs = new MediaArgs("editorial_1col");
      assertArrayEquals(new String[] {
          "editorial_1col"
      }, mediaArgs.getMediaFormatNames());
      assertFalse(mediaArgs.isMediaFormatsMandatory());
  
      mediaArgs = new MediaArgs(EDITORIAL_1COL, EDITORIAL_2COL);
      assertArrayEquals(new MediaFormat[] {
          EDITORIAL_1COL, EDITORIAL_2COL
      }, mediaArgs.getMediaFormats());
      assertFalse(mediaArgs.isMediaFormatsMandatory());
  
      mediaArgs = new MediaArgs("editorial_1col", "editorial_2col");
      assertArrayEquals(new String[] {
          "editorial_1col", "editorial_2col"
      }, mediaArgs.getMediaFormatNames());
      assertFalse(mediaArgs.isMediaFormatsMandatory());
  
      assertNull(new MediaArgs().mediaFormat((MediaFormat)null).getMediaFormats());
      assertNull(new MediaArgs().mediaFormatName((String)null).getMediaFormatNames());
    }

  @Test
    public void testGetMediaFormatsMandatory() {
      MediaArgs mediaArgs;
  
      mediaArgs = new MediaArgs().mandatoryMediaFormats(EDITORIAL_1COL, EDITORIAL_2COL);
      assertArrayEquals(new MediaFormat[] {
          EDITORIAL_1COL, EDITORIAL_2COL
      }, mediaArgs.getMediaFormats());
      assertTrue(mediaArgs.isMediaFormatsMandatory());
  
      mediaArgs = new MediaArgs().mandatoryMediaFormatNames("editorial_1col", "editorial_2col");
      assertArrayEquals(new String[] {
          "editorial_1col", "editorial_2col"
      }, mediaArgs.getMediaFormatNames());
      assertTrue(mediaArgs.isMediaFormatsMandatory());
  
      assertNull(new MediaArgs().mandatoryMediaFormats((MediaFormat[])null).getMediaFormats());
      assertNull(new MediaArgs().mandatoryMediaFormatNames((String[])null).getMediaFormatNames());
    }

  @Test
    public void testGetUrlMode() {
      assertEquals(UrlModes.FULL_URL, new MediaArgs().urlMode(UrlModes.FULL_URL).getUrlMode());
    }

  @Test
  public void testFixedDimension() {
    MediaArgs mediaArgs = new MediaArgs().fixedDimension(100, 50);
    assertEquals(100, mediaArgs.getFixedWidth());
    assertEquals(50, mediaArgs.getFixedHeight());

    mediaArgs.fixedWidth(200);
    mediaArgs.fixedHeight(100);
    assertEquals(200, mediaArgs.getFixedWidth());
    assertEquals(100, mediaArgs.getFixedHeight());
  }

  @Test
    public void testGetFileExtensions() {
      assertArrayEquals(new String[] {
          "gif"
      }, new MediaArgs().fileExtension("gif").getFileExtensions());
      assertArrayEquals(new String[] {
          "gif", "jpg"
      }, new MediaArgs().fileExtensions("gif", "jpg").getFileExtensions());
  
      assertNull(new MediaArgs().fileExtension(null).getFileExtensions());
    }

  @Test
    public void testGetProperties() {
      Map<String, Object> props = ImmutableMap.<String, Object>of("prop1", "value1");
  
      MediaArgs mediaArgs = new MediaArgs()
      .property("prop3", "value3")
      .properties(props)
      .property("prop2", "value2");
  
      assertEquals(3, mediaArgs.getProperties().size());
      assertEquals("value1", mediaArgs.getProperties().get("prop1", String.class));
      assertEquals("value2", mediaArgs.getProperties().get("prop2", String.class));
      assertEquals("value3", mediaArgs.getProperties().get("prop3", String.class));
    }

  @Test
  public void testClone() {
    MediaFormat[] mediaFormats = new MediaFormat[] {
        EDITORIAL_1COL,
        EDITORIAL_2COL
    };
    String[] mediaFormatNames = new String[] {
        "mf1",
        "mf2"
    };
    String[] fileExtensions = new String[] {
        "ext1",
        "ext2"
    };
    Map<String,Object> props = ImmutableValueMap.of("prop1", "value1", "prop2", "value2");

    MediaArgs mediaArgs = new MediaArgs();
    mediaArgs.mediaFormats(mediaFormats);
    mediaArgs.mediaFormatNames(mediaFormatNames);
    mediaArgs.mediaFormatsMandatory(true);
    mediaArgs.fileExtensions(fileExtensions);
    mediaArgs.urlMode(UrlModes.FULL_URL_FORCENONSECURE);
    mediaArgs.fixedWidth(10);
    mediaArgs.fixedHeight(20);
    mediaArgs.forceDownload(true);
    mediaArgs.altText("altText");
    mediaArgs.dummyImageUrl("/dummy/url");
    mediaArgs.properties(props);

    MediaArgs clone = mediaArgs.clone();
    assertNotSame(mediaArgs, clone);
    assertNotSame(mediaArgs.getMediaFormats(), clone.getMediaFormats());
    assertNotSame(mediaArgs.getMediaFormatNames(), clone.getMediaFormatNames());
    assertNotSame(mediaArgs.getFileExtensions(), clone.getFileExtensions());
    assertNotSame(mediaArgs.getProperties(), clone.getProperties());

    assertArrayEquals(mediaArgs.getMediaFormats(), clone.getMediaFormats());
    assertArrayEquals(mediaArgs.getMediaFormatNames(), clone.getMediaFormatNames());
    assertEquals(mediaArgs.isMediaFormatsMandatory(), clone.isMediaFormatsMandatory());
    assertArrayEquals(mediaArgs.getFileExtensions(), clone.getFileExtensions());
    assertEquals(mediaArgs.getUrlMode(), clone.getUrlMode());
    assertEquals(mediaArgs.getFixedWidth(), clone.getFixedWidth());
    assertEquals(mediaArgs.getFixedHeight(), clone.getFixedHeight());
    assertEquals(mediaArgs.isForceDownload(), clone.isForceDownload());
    assertEquals(mediaArgs.getAltText(), clone.getAltText());
    assertEquals(mediaArgs.getDummyImageUrl(), clone.getDummyImageUrl());
    assertEquals(ImmutableValueMap.copyOf(mediaArgs.getProperties()), ImmutableValueMap.copyOf(clone.getProperties()));
  }

  @Test
  public void testEquals() {
    MediaArgs mediaArgs1 = new MediaArgs().mediaFormat(EDITORIAL_1COL).urlMode(UrlModes.FULL_URL).altText("abc");
    MediaArgs mediaArgs2 = new MediaArgs().mediaFormat(EDITORIAL_1COL).urlMode(UrlModes.FULL_URL).altText("abc");
    MediaArgs mediaArgs3 = new MediaArgs().mediaFormat(EDITORIAL_2COL).urlMode(UrlModes.FULL_URL).altText("abc");

    assertTrue(mediaArgs1.equals(mediaArgs2));
    assertTrue(mediaArgs2.equals(mediaArgs1));
    assertFalse(mediaArgs1.equals(mediaArgs3));
    assertFalse(mediaArgs2.equals(mediaArgs3));
  }

}
