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
package io.wcm.handler.mediasource.dam.markup;

import static io.wcm.handler.media.testcontext.DummyMediaFormats.VIDEO_2COL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.wcm.handler.commons.dom.Div;
import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Source;
import io.wcm.handler.commons.dom.Video;
import io.wcm.handler.media.Media;
import io.wcm.handler.mediasource.dam.AbstractDamTest;
import io.wcm.wcm.commons.contenttype.ContentType;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

public class DamVideoMediaMarkupBuilderTest extends AbstractDamTest {

  private Media video;

  private DamVideoMediaMarkupBuilder underTest;

  @Before
  public void setUp() throws Exception {
    // prepare video profiles
    context.load().json("/mediasource/dam/dam-video-profiles.json", "/etc/dam/video");

    video = mediaHandler().get(MEDIAITEM_VIDEO, VIDEO_2COL).build();
    assertTrue(video.isValid());
    underTest = context.request().adaptTo(DamVideoMediaMarkupBuilder.class);
  }

  @Test
  public void testAccepts() throws Exception {
    // accepts video
    assertTrue(underTest.accepts(video));
    // does not accept image
    assertFalse(underTest.accepts(mediaHandler().get(MEDIAITEM_PATH_STANDARD).build()));
  }

  @Test
  public void testBuild() throws Exception {
    HtmlElement<?> element = underTest.build(video);
    assertTrue(element instanceof Video);

    Video videoElement = (Video)element;
    assertEquals(VIDEO_2COL.getWidth(), videoElement.getWidth());
    assertEquals(VIDEO_2COL.getHeight(), videoElement.getHeight());

    List<Element> sources = videoElement.getChildren("source");
    assertEquals(2, sources.size());

    Source source1 = (Source)sources.get(0);
    assertTrue(StringUtils.startsWith(source1.getType(), "video/mp4"));
    assertEquals(MEDIAITEM_VIDEO + "/_jcr_content/renditions/cq5dam.video.hq.m4v", source1.getSrc());

    Source source2 = (Source)sources.get(1);
    assertTrue(StringUtils.startsWith(source2.getType(), "video/ogg"));
    assertEquals(MEDIAITEM_VIDEO + "/_jcr_content/renditions/cq5dam.video.firefoxhq.ogg", source2.getSrc());

    Element flashObject = videoElement.getChild("object");
    assertNotNull(flashObject);
    assertEquals(ContentType.SWF, flashObject.getAttributeValue("type"));
  }

  @Test
  public void testIsValidMedia() {
    assertTrue(underTest.isValidMedia(new Video()));
    assertFalse(underTest.isValidMedia(new Div()));
  }

}
