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

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaArgs.ImageSizes;
import io.wcm.handler.media.MediaArgs.MediaFormatOption;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaArgs.WidthOption;
import io.wcm.handler.media.MediaBuilder;
import io.wcm.handler.media.MediaComponentPropertyResolver;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.MediaRequest.MediaPropertyNames;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.markup.DragDropSupport;
import io.wcm.handler.url.UrlMode;
import io.wcm.wcm.commons.component.ComponentPropertyResolverFactory;

/**
 * Default implementation or {@link MediaBuilder}.
 */
final class MediaBuilderImpl implements MediaBuilder {

  private final MediaHandlerImpl mediaHandler;

  private final Resource resource;
  private final String mediaRef;

  private MediaArgs mediaArgs = new MediaArgs();
  private MediaPropertyNames mediaPropertyNames = new MediaPropertyNames();
  private List<PictureSource> pictureSourceSets = new ArrayList<>();

  private static final Logger log = LoggerFactory.getLogger(MediaBuilderImpl.class);

  MediaBuilderImpl(@Nullable Resource resource, @NotNull MediaHandlerImpl mediaHandler,
      @Nullable ComponentPropertyResolverFactory componentPropertyResolverFactory) {
    this.resource = resource;
    this.mediaRef = null;
    this.mediaHandler = mediaHandler;

    // resolve default settings from content policies and component properties
    if (resource != null) {
      try (MediaComponentPropertyResolver resolver = getMediaComponentPropertyResolver(resource, componentPropertyResolverFactory)) {
        mediaArgs.mediaFormatOptions(resolver.getMediaFormatOptions());
        mediaArgs.autoCrop(resolver.isAutoCrop());
        mediaArgs.imageSizes(resolver.getImageSizes());
        mediaArgs.pictureSources(resolver.getPictureSources());
      }
      catch (Exception ex) {
        log.warn("Error closing component property resolver.", ex);
      }
    }
  }

  @SuppressWarnings("deprecation")
  private static MediaComponentPropertyResolver getMediaComponentPropertyResolver(@NotNull Resource resource,
      @Nullable ComponentPropertyResolverFactory componentPropertyResolverFactory) {
    if (componentPropertyResolverFactory != null) {
      return new MediaComponentPropertyResolver(resource, componentPropertyResolverFactory);
    }
    else {
      // fallback mode if ComponentPropertyResolverFactory is not available
      return new MediaComponentPropertyResolver(resource);
    }
  }

  MediaBuilderImpl(String mediaRef, MediaHandlerImpl mediaHandler) {
    this.resource = null;
    this.mediaRef = mediaRef;
    this.mediaHandler = mediaHandler;
  }

  MediaBuilderImpl(MediaRequest mediaRequest, MediaHandlerImpl mediaHandler) {
    if (mediaRequest == null) {
      throw new IllegalArgumentException("Media request is null.");
    }
    this.resource = mediaRequest.getResource();
    this.mediaRef = mediaRequest.getMediaRef();
    // clone media args to make sure the original object is not modified
    this.mediaArgs = mediaRequest.getMediaArgs().clone();
    this.mediaPropertyNames = mediaRequest.getMediaPropertyNames();
    this.mediaHandler = mediaHandler;
  }

  @Override
  @SuppressWarnings({ "null", "unused" })
  public @NotNull MediaBuilder args(@NotNull MediaArgs value) {
    if (value == null) {
      throw new IllegalArgumentException("MediaArgs is null.");
    }
    // clone media args to make sure the original object is not modified
    this.mediaArgs = value.clone();
    return this;
  }

  @Override
  @SuppressWarnings("null")
  public @NotNull MediaBuilder mediaFormats(@NotNull MediaFormat @NotNull... values) {
    this.mediaArgs.mediaFormats(values);
    return this;
  }

  @Override
  public @NotNull MediaBuilder mandatoryMediaFormats(@NotNull MediaFormat @NotNull... values) {
    this.mediaArgs.mandatoryMediaFormats(values);
    return this;
  }

  @Override
  public @NotNull MediaBuilder mediaFormat(@NotNull MediaFormat value) {
    this.mediaArgs.mediaFormat(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder mediaFormatsMandatory(boolean value) {
    this.mediaArgs.mediaFormatsMandatory(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder mediaFormatNames(@NotNull String @NotNull... values) {
    this.mediaArgs.mediaFormatNames(values);
    return this;
  }

  @Override
  public @NotNull MediaBuilder mandatoryMediaFormatNames(@NotNull String @NotNull... values) {
    this.mediaArgs.mandatoryMediaFormatNames(values);
    return this;
  }

  @Override
  public @NotNull MediaBuilder mediaFormatName(@NotNull String value) {
    this.mediaArgs.mediaFormatName(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder mediaFormatOptions(@NotNull MediaFormatOption @NotNull... values) {
    this.mediaArgs.mediaFormatOptions(values);
    return this;
  }

  @Override
  public @NotNull MediaBuilder autoCrop(boolean value) {
    this.mediaArgs.autoCrop(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder fileExtensions(@NotNull String @NotNull... values) {
    this.mediaArgs.fileExtensions(values);
    return this;
  }

  @Override
  public @NotNull MediaBuilder fileExtension(@NotNull String value) {
    this.mediaArgs.fileExtension(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder urlMode(@NotNull UrlMode value) {
    this.mediaArgs.urlMode(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder fixedWidth(long value) {
    this.mediaArgs.fixedWidth(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder fixedHeight(long value) {
    this.mediaArgs.fixedHeight(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder fixedDimension(long widthValue, long heightValue) {
    this.mediaArgs.fixedDimension(widthValue, heightValue);
    return this;
  }

  @Override
  public @NotNull MediaBuilder contentDispositionAttachment(boolean value) {
    this.mediaArgs.contentDispositionAttachment(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder altText(@NotNull String value) {
    this.mediaArgs.altText(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder dummyImage(boolean value) {
    this.mediaArgs.dummyImage(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder dummyImageUrl(@NotNull String value) {
    this.mediaArgs.dummyImageUrl(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder includeAssetThumbnails(boolean value) {
    this.mediaArgs.includeAssetThumbnails(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder includeAssetWebRenditions(boolean value) {
    this.mediaArgs.includeAssetWebRenditions(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder dragDropSupport(@NotNull DragDropSupport value) {
    this.mediaArgs.dragDropSupport(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder property(@NotNull String key, @Nullable Object value) {
    this.mediaArgs.property(key, value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder imageSizes(@NotNull String sizes, long @NotNull... widths) {
    this.mediaArgs.imageSizes(new ImageSizes(sizes, widths));
    return this;
  }

  @Override
  public @NotNull MediaBuilder imageSizes(@NotNull String sizes, @NotNull WidthOption @NotNull... widthOptions) {
    this.mediaArgs.imageSizes(new ImageSizes(sizes, widthOptions));
    return this;
  }

  @Override
  public @NotNull MediaBuilder pictureSource(@NotNull PictureSource pictureSource) {
    this.pictureSourceSets.add(pictureSource);
    return this;
  }

  @Override
  public @NotNull MediaBuilder pictureSource(@NotNull MediaFormat mediaFormat, @NotNull String media, long @NotNull... widths) {
    this.pictureSourceSets.add(new PictureSource(mediaFormat).media(media).widths(widths));
    return this;
  }

  @Override
  public @NotNull MediaBuilder pictureSource(@NotNull MediaFormat mediaFormat, long @NotNull... widths) {
    this.pictureSourceSets.add(new PictureSource(mediaFormat).widths(widths));
    return this;
  }

  @Override
  public @NotNull MediaBuilder refProperty(@NotNull String value) {
    this.mediaPropertyNames.refProperty(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder cropProperty(@NotNull String value) {
    this.mediaPropertyNames.cropProperty(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder rotationProperty(@NotNull String value) {
    this.mediaPropertyNames.rotationProperty(value);
    return this;
  }

  @Override
  public @NotNull MediaBuilder mapProperty(@NotNull String value) {
    this.mediaPropertyNames.mapProperty(value);
    return this;
  }

  @Override
  public @NotNull Media build() {
    if (!pictureSourceSets.isEmpty()) {
      this.mediaArgs.pictureSources(pictureSourceSets.toArray(new PictureSource[pictureSourceSets.size()]));
    }
    if (this.mediaArgs.getImageSizes() != null && this.mediaArgs.getPictureSources() != null) {
      throw new IllegalArgumentException("Image sizes must not be used together with pictures source sets.");
    }
    MediaRequest request = new MediaRequest(this.resource, this.mediaRef, this.mediaArgs, this.mediaPropertyNames);
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
