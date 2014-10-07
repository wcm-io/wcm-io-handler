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
import io.wcm.handler.media.MediaArgsType;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaHandlerConfig;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaMarkupBuilder;
import io.wcm.handler.media.MediaMetadata;
import io.wcm.handler.media.MediaMetadataProcessor;
import io.wcm.handler.media.MediaReference;
import io.wcm.handler.media.MediaSource;
import io.wcm.handler.media.args.MediaArgs;
import io.wcm.handler.url.UrlMode;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.models.annotations.AemObject;

import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.day.cq.wcm.api.Page;

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
  @AemObject
  private Page currentPage;

  @Override
  public HtmlElement<?> getMedia(String mediaRef) {
    return getMediaMetadata(new MediaReference(mediaRef, null)).getMedia();
  }

  @Override
  public HtmlElement<?> getMedia(String mediaRef, MediaArgsType mediaArgs) {
    return getMediaMetadata(new MediaReference(mediaRef, mediaArgs)).getMedia();
  }

  @Override
  public HtmlElement<?> getMedia(Resource pResource, MediaArgsType mediaArgs) {
    return getMediaMetadata(new MediaReference(pResource, null, mediaArgs)).getMedia();
  }

  @Override
  public HtmlElement<?> getMedia(Resource pResource, String refProperty, MediaArgsType mediaArgs) {
    return getMediaMetadata(new MediaReference(pResource, refProperty, mediaArgs)).getMedia();
  }

  @Override
  public String getMediaUrl(String mediaRef) {
    return getMediaMetadata(new MediaReference(mediaRef, null)).getMediaUrl();
  }

  @Override
  public String getMediaUrl(String mediaRef, UrlMode pUrlMode) {
    return getMediaMetadata(new MediaReference(mediaRef, MediaArgs.urlMode(pUrlMode))).getMediaUrl();
  }

  @Override
  public String getMediaUrl(String mediaRef, MediaArgsType mediaArgs) {
    return getMediaMetadata(new MediaReference(mediaRef, mediaArgs)).getMediaUrl();
  }

  @Override
  public String getMediaUrl(Resource pResource, MediaArgsType mediaArgs) {
    return getMediaMetadata(new MediaReference(pResource, null, mediaArgs)).getMediaUrl();
  }

  @Override
  public String getMediaUrl(Resource pResource, String refProperty, MediaArgsType mediaArgs) {
    return getMediaMetadata(new MediaReference(pResource, refProperty, mediaArgs)).getMediaUrl();
  }

  @Override
  public MediaMetadata getMediaMetadata(String mediaRef) {
    return getMediaMetadata(new MediaReference(mediaRef, null));
  }

  @Override
  public MediaMetadata getMediaMetadata(String mediaRef, MediaArgsType mediaArgs) {
    return getMediaMetadata(new MediaReference(mediaRef, mediaArgs));
  }

  @Override
  public MediaMetadata getMediaMetadata(Resource pResource) {
    return getMediaMetadata(new MediaReference(pResource, null, null));
  }

  @Override
  public MediaMetadata getMediaMetadata(Resource pResource, MediaArgsType mediaArgs) {
    return getMediaMetadata(new MediaReference(pResource, null, mediaArgs));
  }

  @Override
  public MediaMetadata getMediaMetadata(Resource pResource, String refProperty) {
    return getMediaMetadata(new MediaReference(pResource, refProperty, null));
  }

  @Override
  public MediaMetadata getMediaMetadata(Resource pResource, String refProperty, MediaArgsType mediaArgs) {
    return getMediaMetadata(new MediaReference(pResource, refProperty, mediaArgs));
  }

  @Override
  public MediaMetadata getMediaMetadata(MediaReference mediaReference) {
    return processMedia(mediaReference);
  }

  /**
   * Resolves the media reference
   * @param mediaReference Media reference
   * @return Media metadata (never null)
   */
  protected MediaMetadata processMedia(MediaReference mediaReference) {
    MediaReference originalMediaReference = mediaReference;

    // clone media reference to allow modifications on further processing without changing input parameters
    MediaReference mediaRef;
    try {
      mediaRef = (MediaReference)originalMediaReference.clone();
    }
    catch (CloneNotSupportedException ex) {
      throw new RuntimeException("Unable to clone MediaArgs instance.", ex);
    }

    // make sure mediaargs is not null
    if (mediaRef.getMediaArgs() == null) {
      mediaRef.setMediaArgs(new MediaArgs());
    }

    // detect media source
    MediaSource mediaSource = null;
    List<Class<? extends MediaSource>> mediaTypes = mediaHandlerConfig.getMediaSources();
    if (mediaTypes == null || mediaTypes.size() == 0) {
      throw new RuntimeException("No media sources defined.");
    }
    for (Class<? extends MediaSource> candidateMediaSourceClass : mediaTypes) {
      MediaSource candidateMediaSource = AdaptTo.notNull(adaptable, candidateMediaSourceClass);
      if (candidateMediaSource.accepts(mediaRef)) {
        mediaSource = candidateMediaSource;
        break;
      }
    }
    MediaMetadata mediaMetadata = new MediaMetadata(originalMediaReference, mediaRef, mediaSource);

    // preprocess media reference before resolving
    List<Class<? extends MediaMetadataProcessor>> mediaPreProcessors = mediaHandlerConfig.getMediaMetadataPreProcessors();
    if (mediaPreProcessors != null) {
      for (Class<? extends MediaMetadataProcessor> processorClass : mediaPreProcessors) {
        MediaMetadataProcessor processor = AdaptTo.notNull(adaptable, processorClass);
        mediaMetadata = processor.process(mediaMetadata);
        if (mediaMetadata == null) {
          throw new RuntimeException("MediaPreProcessor '" + processor + "' returned null, page '" + currentPage.getPath() + "'.");
        }
      }
    }

    // resolve media reference
    if (mediaSource != null) {
      mediaMetadata = mediaSource.resolveMedia(mediaMetadata);
      if (mediaMetadata == null) {
        throw new RuntimeException("MediaType '" + mediaSource + "' returned null, page '" + currentPage.getPath() + "'.");
      }
    }
    else {
      mediaMetadata.setMediaInvalidReason(MediaInvalidReason.NO_MEDIA_SOURCE);
    }

    // generate markup (if markup builder is available) - first accepting wins
    List<Class<? extends MediaMarkupBuilder>> mediaMarkupBuilders = mediaHandlerConfig.getMediaMarkupBuilders();
    if (mediaMarkupBuilders != null) {
      for (Class<? extends MediaMarkupBuilder> mediaMarkupBuilderClass : mediaMarkupBuilders) {
        MediaMarkupBuilder mediaMarkupBuilder = AdaptTo.notNull(adaptable, mediaMarkupBuilderClass);
        if (mediaMarkupBuilder.accepts(mediaMetadata)) {
          mediaMetadata.setMedia(mediaMarkupBuilder.build(mediaMetadata));
          break;
        }
      }
    }

    // postprocess media reference after resolving
    List<Class<? extends MediaMetadataProcessor>> mediaPostProcessors = mediaHandlerConfig.getMediaMetadataPostProcessors();
    if (mediaPostProcessors != null) {
      for (Class<? extends MediaMetadataProcessor> processorClass : mediaPostProcessors) {
        MediaMetadataProcessor processor = AdaptTo.notNull(adaptable, processorClass);
        mediaMetadata = processor.process(mediaMetadata);
        if (mediaMetadata == null) {
          throw new RuntimeException("MediaPostProcessor '" + processor + "' returned null, page '" + currentPage.getPath() + "'.");
        }
      }
    }

    return mediaMetadata;
  }

  @Override
  public boolean isValidMedia(HtmlElement<?> element) {

    // it it is null it is always invalid
    if (element == null) {
      return false;
    }

    // otherwise check if any media markup builder is available that rates this html element valid
    List<Class<? extends MediaMarkupBuilder>> mediaMarkupBuilders = mediaHandlerConfig.getMediaMarkupBuilders();
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

}
