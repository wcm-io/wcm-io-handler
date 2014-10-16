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
package io.wcm.handler.media.impl;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaBuilder;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.url.UrlMode;

import org.apache.sling.api.resource.Resource;

/**
 * Default implementation or {@link MediaBuilder}.
 */
final class MediaBuilderImpl implements MediaBuilder {

  private final MediaHandlerImpl mediaHandler;

  private final Resource resource;
  private final String mediaRef;

  private MediaArgs mediaArgs;
  private MediaFormat[] mediaFormats;
  private String refProperty;
  private String cropProperty;
  private UrlMode urlMode;

  public MediaBuilderImpl(Resource resource, MediaHandlerImpl mediaHandler) {
    this.resource = resource;
    this.mediaRef = null;
    this.mediaHandler = mediaHandler;
  }

  public MediaBuilderImpl(String mediaRef, MediaHandlerImpl mediaHandler) {
    this.resource = null;
    this.mediaRef = mediaRef;
    this.mediaHandler = mediaHandler;
  }

  public MediaBuilderImpl(MediaRequest mediaRequest, MediaHandlerImpl mediaHandler) {
    if (mediaRequest == null) {
      throw new IllegalArgumentException("Media request is null.");
    }
    this.resource = mediaRequest.getResource();
    this.mediaRef = mediaRequest.getMediaRef();
    this.mediaArgs = mediaRequest.getMediaArgs();
    this.refProperty = mediaRequest.getRefProperty();
    this.cropProperty = mediaRequest.getCropProperty();
    this.mediaHandler = mediaHandler;
  }

  @Override
  public MediaBuilder args(MediaArgs value) {
    this.mediaArgs = value;
    return this;
  }

  @Override
  public MediaBuilder mediaFormats(MediaFormat... value) {
    this.mediaFormats = value;
    return this;
  }

  @Override
  public MediaBuilder refProperty(String value) {
    this.refProperty = value;
    return this;
  }

  @Override
  public MediaBuilder cropProperty(String value) {
    this.cropProperty = value;
    return this;
  }

  @Override
  public MediaBuilder urlMode(UrlMode value) {
    this.urlMode = value;
    return this;
  }

  @Override
  public Media build() {
    if (this.mediaArgs != null && (this.urlMode != null || this.mediaFormats != null)) {
      throw new IllegalArgumentException("Please set media arguments or URL/media formats mode, not both.");
    }
    if (this.mediaArgs == null) {
      this.mediaArgs = new MediaArgs();
      this.mediaArgs.mediaFormats(this.mediaFormats);
      this.mediaArgs.urlMode(this.urlMode);
    }
    else {
      // clone media args to make sure the original object is not modified
      this.mediaArgs = this.mediaArgs.clone();
    }
    MediaRequest request = new MediaRequest(this.resource, this.mediaRef, this.mediaArgs,
        this.refProperty, this.cropProperty);
    return mediaHandler.processRequest(request);
  }

  @Override
  public String buildMarkup() {
    return build().getMarkup();
  }

  @Override
  public HtmlElement<?> buildElement() {
    return build().getElement();
  }

  @Override
  public String buildUrl() {
    return build().getUrl();
  }

}
