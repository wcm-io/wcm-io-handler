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
package io.wcm.handler.media.spi;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ConsumerType;

import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;

import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.markup.DummyImageMediaMarkupBuilder;
import io.wcm.handler.media.markup.SimpleImageMediaMarkupBuilder;
import io.wcm.handler.mediasource.dam.DamMediaSource;
import io.wcm.sling.commons.caservice.ContextAwareService;

/**
 * {@link MediaHandlerConfig} OSGi services provide application-specific configuration for media handling.
 * Applications can set service properties or bundle headers as defined in {@link ContextAwareService} to apply this
 * configuration only for resources that match the relevant resource paths.
 */
@ConsumerType
public abstract class MediaHandlerConfig implements ContextAwareService {

  /**
   * Default value for JPEG quality.
   */
  public static final double DEFAULT_JPEG_QUALITY = 0.98d;

  private static final List<Class<? extends MediaSource>> DEFAULT_MEDIA_SOURCES = ImmutableList.<Class<? extends MediaSource>>of(
      DamMediaSource.class);

  private static final List<Class<? extends MediaMarkupBuilder>> DEFAULT_MEDIA_MARKUP_BUILDERS = ImmutableList.<Class<? extends MediaMarkupBuilder>>of(
      SimpleImageMediaMarkupBuilder.class,
      DummyImageMediaMarkupBuilder.class);

  /**
   * @return Supported media sources
   */
  public @NotNull List<Class<? extends MediaSource>> getSources() {
    return DEFAULT_MEDIA_SOURCES;
  }

  /**
   * @return Available media markup builders
   */
  public @NotNull List<Class<? extends MediaMarkupBuilder>> getMarkupBuilders() {
    return DEFAULT_MEDIA_MARKUP_BUILDERS;
  }

  /**
   * @return List of media metadata pre processors (optional). The processors are applied in list order.
   */
  public @NotNull List<Class<? extends MediaProcessor>> getPreProcessors() {
    // no processors
    return ImmutableList.of();
  }

  /**
   * @return List of media metadata post processors (optional). The processors are applied in list order.
   */
  public @NotNull List<Class<? extends MediaProcessor>> getPostProcessors() {
    // no processors
    return ImmutableList.of();
  }

  /**
   * Get the default quality for images in this app generated with the Layer API.
   * The meaning of the quality parameter for the different image formats is described in
   * {@link com.day.image.Layer#write(String, double, java.io.OutputStream)}.
   * @param mimeType MIME-type of the output format
   * @return Quality factor
   */
  public double getDefaultImageQuality(String mimeType) {
    if (StringUtils.isNotEmpty(mimeType)) {
      String format = StringUtils.substringAfter(mimeType.toLowerCase(), "image/");
      if (StringUtils.equals(format, "jpg") || StringUtils.equals(format, "jpeg")) {
        return DEFAULT_JPEG_QUALITY;
      }
      else if (StringUtils.equals(format, "gif")) {
        return 256d; // 256 colors
      }
    }
    // return quality "1" for all other mime types
    return 1d;
  }

  /**
   * With this switch it's possible to switch all used property and node names from (legacy) wcm.io
   * Handler standard to Adobe Standard (as used e.g. in Adobe Core WCM Components) - e.g.
   * using "fileReference" instead of property name "mediaRef" for the asset reference.
   * <p>
   * The benefit of the wcm.io Handler standard was that it supported storage multiple asset references
   * in one single node - but this it not well supported by the Touch UI anyway, so it's not of much
   * use nowadays.
   * </p>
   * <p>
   * For new projects it is recommended to always use the Adobe standard names. But for backward compatibility
   * the default values is false.
   * </p>
   * @return If true, Adobe standard property and node names are used.
   */
  public boolean useAdobeStandardNames() {
    return false;
  }

  /**
   * @return Default property name for reference to media library item
   */
  public @NotNull String getMediaRefProperty() {
    if (useAdobeStandardNames()) {
      return MediaNameConstants.PN_MEDIA_REF_STANDARD;
    }
    else {
      return MediaNameConstants.PN_MEDIA_REF;
    }
  }

  /**
   * @return Default property name for cropping parameters
   */
  public @NotNull String getMediaCropProperty() {
    if (useAdobeStandardNames()) {
      return MediaNameConstants.PN_MEDIA_CROP_STANDARD;
    }
    else {
      return MediaNameConstants.PN_MEDIA_CROP;
    }
  }

  /**
   * @return Default property name for rotate parameter
   */
  public @NotNull String getMediaRotationProperty() {
    if (useAdobeStandardNames()) {
      return MediaNameConstants.PN_MEDIA_ROTATION_STANDARD;
    }
    else {
      return MediaNameConstants.PN_MEDIA_ROTATION;
    }
  }

  /**
   * @return Default property name for map parameter
   */
  public @NotNull String getMediaMapProperty() {
    if (useAdobeStandardNames()) {
      return MediaNameConstants.PN_MEDIA_MAP_STANDARD;
    }
    else {
      return MediaNameConstants.PN_MEDIA_MAP;
    }
  }

  /**
   * @return Default property name for media alt. text
   */
  public @NotNull String getMediaAltTextProperty() {
    if (useAdobeStandardNames()) {
      return MediaNameConstants.PN_MEDIA_ALTTEXT_STANDARD;
    }
    else {
      return MediaNameConstants.PN_MEDIA_ALTTEXT;
    }
  }

  /**
   * @return Default node name for inline media item stored in node within the content page
   */
  public @NotNull String getMediaInlineNodeName() {
    if (useAdobeStandardNames()) {
      return MediaNameConstants.NN_MEDIA_INLINE_STANDARD;
    }
    else {
      return MediaNameConstants.NN_MEDIA_INLINE;
    }
  }

  /**
   * @return If set to true, web renditions generated by the DAM asset workflows (with cq5dam.web prefix) are
   *         taken into account by default when trying to resolve the media request.
   */
  public boolean includeAssetWebRenditionsByDefault() {
    return true;
  }

  /**
   * Get root path for picking assets using path field widgets.
   * @param page Context page
   * @return DAM root path
   */
  public @NotNull String getDamRootPath(@NotNull Page page) {
    return "/content/dam";
  }

}
