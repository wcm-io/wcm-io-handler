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
package io.wcm.handler.mediasource.inline;

import io.wcm.handler.media.MediaItem;
import io.wcm.handler.media.MediaMetadata;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.args.MediaArgsType;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

/**
 * {@link MediaItem} implementation for inline media objects stored in a node in a content page.
 */
class InlineMediaItem extends SlingAdaptable implements MediaItem {

  private final Adaptable adaptable;
  private final Resource resource;
  private final MediaMetadata mediaMetadata;
  private final MediaArgsType defaultMediaArgs;
  private final String fileName;

  /**
   * @param resource Binary resource
   * @param mediaMetadata Media metadata
   * @param fileName File name
   */
  InlineMediaItem(Resource resource, MediaMetadata mediaMetadata, String fileName, Adaptable adaptable) {
    this.resource = resource;
    this.mediaMetadata = mediaMetadata;
    this.defaultMediaArgs = mediaMetadata.getMediaReference().getMediaArgs();
    this.fileName = fileName;
    this.adaptable = adaptable;
  }

  @Override
  public String getTitle() {
    return this.fileName;
  }

  @Override
  public String getAltText() {
    return this.defaultMediaArgs.getAltText();
  }

  @Override
  public String getDescription() {
    // not supported
    return null;
  }

  @Override
  public String getPath() {
    return this.resource.getPath();
  }

  @Override
  public ValueMap getProperties() {
    return this.resource.getValueMap();
  }

  @Override
  public Rendition getDefaultRendition() {
    return getRendition(this.defaultMediaArgs);
  }

  @Override
  public Rendition getRendition(MediaArgsType mediaArgs) {
    Rendition rendition = getInlineRendition(mediaArgs);

    // check if rendition is valid - otherwise return null
    if (StringUtils.isEmpty(rendition.getMediaUrl())) {
      rendition = null;
    }

    return rendition;
  }

  @Override
  public Rendition getImageRendition(MediaArgsType mediaArgs) {
    Rendition rendition = getRendition(mediaArgs);
    if (rendition != null && rendition.isImage()) {
      return rendition;
    }
    else {
      return null;
    }
  }

  @Override
  public Rendition getFlashRendition(MediaArgsType mediaArgs) {
    Rendition rendition = getRendition(mediaArgs);
    if (rendition != null && rendition.isFlash()) {
      return rendition;
    }
    else {
      return null;
    }
  }

  @Override
  public Rendition getDownloadRendition(MediaArgsType mediaArgs) {
    Rendition rendition = getRendition(mediaArgs);
    if (rendition != null && rendition.isDownload()) {
      return rendition;
    }
    else {
      return null;
    }
  }

  /**
   * Get inline rendition instance.
   * @param mediaArgs Media args
   * @return Inline rendition instance (may be invalid rendition)
   */
  private Rendition getInlineRendition(MediaArgsType mediaArgs) {
    return new InlineRendition(this.resource, this.mediaMetadata, mediaArgs, this.fileName, this.adaptable);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (type == Resource.class) {
      return (AdapterType)this.resource;
    }
    return super.adaptTo(type);
  }

}
