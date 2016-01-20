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
package io.wcm.handler.media.ui;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaBuilder;
import io.wcm.handler.media.MediaHandler;

/**
 * Generic resource-based media model.
 * Optional parameters: mediaFormat, cssClass.
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class ResourceMedia {

  @RequestAttribute(optional = true)
  private String mediaFormat;

  @RequestAttribute(optional = true)
  private String cssClass;

  @Self
  private MediaHandler mediaHandler;
  @SlingObject
  private Resource resource;

  private Media media;

  @PostConstruct
  protected void activate() {
    MediaBuilder builder = mediaHandler.get(resource);

    if (StringUtils.isNotEmpty(mediaFormat)) {
      builder.mediaFormatName(mediaFormat);
    }

    media = builder.build();

    if (media.isValid() && StringUtils.isNotEmpty(cssClass)) {
      media.getElement().addCssClass(cssClass);
    }
  }

  /**
   * @return Media metadata
   */
  public Media getMetadata() {
    return media;
  }

  /**
   * @return Media is valid
   */
  public boolean isValid() {
    return media.isValid();
  }

  /**
   * @return Media markup
   */
  public String getMarkup() {
    return media.getMarkup();
  }

}
