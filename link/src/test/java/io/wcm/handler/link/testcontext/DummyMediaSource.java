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
package io.wcm.handler.link.testcontext;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.spi.helpers.AbstractMediaSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;

/**
 * Dummy media source
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
public class DummyMediaSource extends AbstractMediaSource {

  @Override
  public String getId() {
    return "dummy";
  }

  @Override
  public boolean accepts(String mediaRef) {
    return StringUtils.startsWith(mediaRef, "/content/dummymedia/");
  }

  @Override
  public String getPrimaryMediaRefProperty() {
    return MediaNameConstants.PN_MEDIA_REF;
  }

  @Override
  public Media resolveMedia(Media media) {
    String mediaUrl = media.getMediaRequest().getMediaRef();
    if (StringUtils.contains(mediaUrl, "image")) {
      mediaUrl += ".gif";
    }
    else if (StringUtils.contains(mediaUrl, "pdf")) {
      mediaUrl += ".pdf";
    }
    else {
      mediaUrl = null;
    }
    media.setUrl(mediaUrl);
    return media;
  }

  @Override
  public void enableMediaDrop(HtmlElement element, MediaRequest mediaRequest) {
    // not supported
  }

}
