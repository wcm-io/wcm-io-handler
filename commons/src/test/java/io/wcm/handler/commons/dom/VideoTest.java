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
package io.wcm.handler.commons.dom;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VideoTest {

  @Test
  public void testSimpleAttributes() throws Exception {
    Video video = new Video();
    assertEquals("video", video.getName());

    video.setAutoPlay(true);
    assertEquals(true, video.isAutoPlay());

    video.setControls(true);
    assertEquals(true, video.isControls());

    video.setLoop(true);
    assertEquals(true, video.isLoop());

    video.setMuted(true);
    assertEquals(true, video.isMuted());

    video.setPoster("poster1");
    assertEquals("poster1", video.getPoster());

    video.setPreload("preload1");
    assertEquals("preload1", video.getPreload());

    video.setSrc("ref1");
    assertEquals("ref1", video.getSrc());

    video.setHeight(25);
    assertEquals(25, video.getHeight());

    video.setWidth(30);
    assertEquals(30, video.getWidth());
  }

}
