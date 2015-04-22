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
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatHandler;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.spi.MediaMarkupBuilder;
import io.wcm.handler.media.spi.MediaProcessor;
import io.wcm.handler.media.spi.MediaSource;
import io.wcm.sling.commons.adapter.AdaptTo;

import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

/**
 * Default Implementation of a {@link MediaHandler}.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
}, adapters = MediaHandler.class)
public final class MediaHandlerImpl implements MediaHandler {

  @Self
  private Adaptable adaptable;
  @Self
  private MediaHandlerConfig mediaHandlerConfig;
  @Self
  private MediaFormatHandler mediaFormatHandler;

  @Override
  public MediaBuilder get(Resource resource) {
    return new MediaBuilderImpl(resource, this);
  }

  @Override
  public MediaBuilder get(Resource resource, MediaArgs mediaArgs) {
    return get(resource).args(mediaArgs);
  }

  @Override
  public MediaBuilder get(Resource resource, MediaFormat... mediaFormats) {
    return get(resource).mediaFormats(mediaFormats);
  }

  @Override
  public MediaBuilder get(String mediaRef) {
    return new MediaBuilderImpl(mediaRef, this);
  }

  @Override
  public MediaBuilder get(String mediaRef, MediaArgs mediaArgs) {
    return get(mediaRef).args(mediaArgs);
  }

  @Override
  public MediaBuilder get(String mediaRef, MediaFormat... mediaFormats) {
    return get(mediaRef).mediaFormats(mediaFormats);
  }

  @Override
  public MediaBuilder get(MediaRequest mediaRequest) {
    return new MediaBuilderImpl(mediaRequest, this);
  }

  /**
   * Resolves the media request
   * @param mediaRequest Media request
   * @return Media metadata (never null)
   */
  Media processRequest(final MediaRequest mediaRequest) {

    // resolve media format names to media formats
    resolveMediaFormats(mediaRequest.getMediaArgs());

    // detect media source
    MediaSource mediaSource = null;
    List<Class<? extends MediaSource>> mediaTypes = mediaHandlerConfig.getSources();
    if (mediaTypes == null || mediaTypes.isEmpty()) {
      throw new RuntimeException("No media sources defined.");
    }
    for (Class<? extends MediaSource> candidateMediaSourceClass : mediaTypes) {
      MediaSource candidateMediaSource = AdaptTo.notNull(adaptable, candidateMediaSourceClass);
      if (candidateMediaSource.accepts(mediaRequest)) {
        mediaSource = candidateMediaSource;
        break;
      }
    }
    Media media = new Media(mediaSource, mediaRequest);

    // preprocess media request before resolving
    List<Class<? extends MediaProcessor>> mediaPreProcessors = mediaHandlerConfig.getPreProcessors();
    if (mediaPreProcessors != null) {
      for (Class<? extends MediaProcessor> processorClass : mediaPreProcessors) {
        MediaProcessor processor = AdaptTo.notNull(adaptable, processorClass);
        media = processor.process(media);
        if (media == null) {
          throw new RuntimeException("MediaPreProcessor '" + processor + "' returned null, request: " + mediaRequest);
        }
      }
    }

    // resolve media request
    if (mediaSource != null) {
      media = mediaSource.resolveMedia(media);
      if (media == null) {
        throw new RuntimeException("MediaType '" + mediaSource + "' returned null, request: " + mediaRequest);
      }
    }
    else {
      media.setMediaInvalidReason(MediaInvalidReason.NO_MEDIA_SOURCE);
    }

    // generate markup (if markup builder is available) - first accepting wins
    List<Class<? extends MediaMarkupBuilder>> mediaMarkupBuilders = mediaHandlerConfig.getMarkupBuilders();
    if (mediaMarkupBuilders != null) {
      for (Class<? extends MediaMarkupBuilder> mediaMarkupBuilderClass : mediaMarkupBuilders) {
        MediaMarkupBuilder mediaMarkupBuilder = AdaptTo.notNull(adaptable, mediaMarkupBuilderClass);
        if (mediaMarkupBuilder.accepts(media)) {
          media.setElement(mediaMarkupBuilder.build(media));
          break;
        }
      }
    }

    // postprocess media request after resolving
    List<Class<? extends MediaProcessor>> mediaPostProcessors = mediaHandlerConfig.getPostProcessors();
    if (mediaPostProcessors != null) {
      for (Class<? extends MediaProcessor> processorClass : mediaPostProcessors) {
        MediaProcessor processor = AdaptTo.notNull(adaptable, processorClass);
        media = processor.process(media);
        if (media == null) {
          throw new RuntimeException("MediaPostProcessor '" + processor + "' returned null, request: " + mediaRequest);
        }
      }
    }

    return media;
  }

  @Override
  public boolean isValidElement(HtmlElement<?> element) {

    // it it is null it is always invalid
    if (element == null) {
      return false;
    }

    // otherwise check if any media markup builder is available that rates this html element valid
    List<Class<? extends MediaMarkupBuilder>> mediaMarkupBuilders = mediaHandlerConfig.getMarkupBuilders();
    if (mediaMarkupBuilders != null) {
      for (Class<? extends MediaMarkupBuilder> mediaMarkupBuilderClass : mediaMarkupBuilders) {
        MediaMarkupBuilder mediaMarkupBuilder = AdaptTo.notNull(adaptable, mediaMarkupBuilderClass);
        if (mediaMarkupBuilder.isValidMedia(element)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Resolve media format names to media formats so all downstream logic has only to handle the resolved media formats.
   * If resolving fails an exception is thrown.
   * @param mediaArgs Media args
   */
  private void resolveMediaFormats(MediaArgs mediaArgs) {
    // resolved media formats already set? done.
    if (mediaArgs.getMediaFormats() != null) {
      return;
    }
    // no media format names present? done.
    if (mediaArgs.getMediaFormatNames() == null) {
      return;
    }
    String[] mediaFormatNames = mediaArgs.getMediaFormatNames();
    MediaFormat[] mediaFormats = new MediaFormat[mediaFormatNames.length];
    for (int i = 0; i < mediaFormatNames.length; i++) {
      mediaFormats[i] = mediaFormatHandler.getMediaFormat(mediaFormatNames[i]);
      if (mediaFormats[i] == null) {
        throw new RuntimeException("Media format name '" + mediaFormatNames[i] + "' is invalid.");
      }
    }
    mediaArgs.mediaFormats(mediaFormats);
    mediaArgs.mediaFormatNames((String[])null);
  }

}
