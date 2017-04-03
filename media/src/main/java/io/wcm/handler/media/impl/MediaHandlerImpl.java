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

import java.util.ArrayList;
import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

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

  private static final Logger log = LoggerFactory.getLogger(MediaHandlerImpl.class);

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

    // resolve media format names to media formats
    if (!resolveMediaFormats(mediaRequest.getMediaArgs())) {
      media.setMediaInvalidReason(MediaInvalidReason.INVALID_MEDIA_FORMAT);
      return media;
    }

    // if only downloads are accepted prepare media format filter set which only contains download media formats
    if (!resolveDownloadMediaFormats(mediaRequest.getMediaArgs())) {
      media.setMediaInvalidReason(MediaInvalidReason.INVALID_MEDIA_FORMAT);
      return media;
    }

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
   * @return true if resolving was successful.
   */
  private boolean resolveMediaFormats(MediaArgs mediaArgs) {
    // resolved media formats already set? done.
    if (mediaArgs.getMediaFormats() != null) {
      return true;
    }
    // no media format names present? done.
    if (mediaArgs.getMediaFormatNames() == null) {
      return true;
    }
    String[] mediaFormatNames = mediaArgs.getMediaFormatNames();
    MediaFormat[] mediaFormats = new MediaFormat[mediaFormatNames.length];
    boolean resolutionSuccessful = true;
    for (int i = 0; i < mediaFormatNames.length; i++) {
      mediaFormats[i] = mediaFormatHandler.getMediaFormat(mediaFormatNames[i]);
      if (mediaFormats[i] == null) {
        log.warn("Media format name '" + mediaFormatNames[i] + "' is invalid.");
        resolutionSuccessful = false;
      }
    }
    mediaArgs.mediaFormats(mediaFormats);
    mediaArgs.mediaFormatNames((String[])null);
    return resolutionSuccessful;
  }

  /**
   * If a set of media formats is given it is filtered to contain only download media formats.
   * If no is given a new set of allowed media formats is created by getting from all media formats those marked as "download".
   * If the result is an empty set of media formats (but downloads are requests) resolution is not successful.
   * @param mediaArgs Media args
   * @return true if resolving was successful
   */
  private boolean resolveDownloadMediaFormats(MediaArgs mediaArgs) {
    if (!mediaArgs.isDownload()) {
      // not filtering for downloads
      return true;
    }
    List<MediaFormat> candidates = new ArrayList<>();
    if (mediaArgs.getMediaFormats() != null) {
      candidates.addAll(ImmutableList.copyOf(mediaArgs.getMediaFormats()));
    }
    else {
      candidates.addAll(mediaFormatHandler.getMediaFormats());
    }
    MediaFormat[] result = candidates.stream()
        .filter(MediaFormat::isDownload)
        .toArray(size -> new MediaFormat[size]);
    if (result.length > 0) {
      mediaArgs.mediaFormats(result);
      return true;
    }
    else {
      return false;
    }
  }

}
