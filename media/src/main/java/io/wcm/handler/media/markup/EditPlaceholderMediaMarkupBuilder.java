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
package io.wcm.handler.media.markup;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Image;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaMetadata;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.args.MediaArgsType;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.spi.MediaMarkupBuilder;
import io.wcm.handler.media.spi.MediaSource;
import io.wcm.handler.url.UrlHandler;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.models.annotations.AemObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.day.cq.wcm.api.WCMMode;

/**
 * Generates a dummy image as edit placeholder in WCM edit mode, if no media item is set.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
public final class EditPlaceholderMediaMarkupBuilder implements MediaMarkupBuilder {

  @Self
  private Adaptable adaptable;
  @Self
  private UrlHandler urlHandler;
  @Self
  private MediaHandlerConfig mediaHandlerConfig;
  @AemObject
  private WCMMode wcmMode;

  @Override
  public boolean accepts(MediaMetadata mediaMetadata) {
    // accept if not rendition was found and in edit mode
    // and at least one media format is given, and dummy image is not suppressed
    MediaArgsType mediaArgs = mediaMetadata.getMediaReference().getMediaArgs();
    MediaFormat[] mediaFormats = mediaArgs.getMediaFormats();
    return mediaMetadata.getRendition() == null
        && wcmMode == WCMMode.EDIT
        && (mediaFormats != null && mediaFormats.length > 0)
        && !mediaArgs.isNoDummyImage();
  }

  @Override
  public HtmlElement<?> build(MediaMetadata mediaMetadata) {

    // Create dummy image element to be displayed in Edit mode as placeholder.
    Dimension dimension = MediaMarkupBuilderUtil.getMediaformatDimension(mediaMetadata);
    MediaArgsType mediaArgs = mediaMetadata.getMediaReference().getMediaArgs();

    // create dummy image
    String dummyImageUrl = StringUtils.defaultString(mediaArgs.getDummyImageUrl(), DUMMY_IMAGE);
    dummyImageUrl = urlHandler.url(dummyImageUrl)
        .urlMode(mediaMetadata.getMediaReference().getMediaArgs().getUrlMode())
        .externalizeResource().build();
    Image image = new Image(dummyImageUrl, dimension.getWidth(), dimension.getHeight())
    .addCssClass(MediaNameConstants.CSS_DUMMYIMAGE);

    // enable drag&drop for media source - if none is specified use first one defined in config
    MediaSource mediaSource = mediaMetadata.getMediaSource();
    if (mediaSource == null && !mediaHandlerConfig.getMediaSources().isEmpty()) {
      Class<? extends MediaSource> mediaSourceClass = mediaHandlerConfig.getMediaSources().iterator().next();
      mediaSource = AdaptTo.notNull(adaptable, mediaSourceClass);
    }
    if (mediaSource != null) {
      mediaSource.enableMediaDrop(image, mediaMetadata.getMediaReference());
    }

    return image;
  }

  @Override
  public boolean isValidMedia(HtmlElement<?> element) {
    return false;
  }

}
