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

import static io.wcm.handler.media.MediaNameConstants.PROP_CSS_CLASS;
import static io.wcm.handler.media.impl.WidthUtils.parseWidths;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs.WidthOption;
import io.wcm.handler.media.MediaBuilder;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.format.MediaFormatHandler;

/**
 * Generic resource-based media model.
 * <p>
 * Optional use parameters when referencing model from Sightly template:
 * </p>
 * <ul>
 * <li><code>mediaFormat</code>: Media format name to restrict the allowed media items</li>
 * <li><code>refProperty</code>: Name of the property from which the media reference path is read, or node name for
 * inline media.</li>
 * <li><code>cropProperty</code>: Name of the property which contains the cropping parameters</li>
 * <li><code>rotationProperty</code>: Name of the property which contains the rotation parameter</li>
 * <li><code>cssClass</code>: CSS classes to be applied on the generated media element (most cases img element)</li>
 * </ul>
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class ResourceMedia {

  /**
   * Optional: Media format to be used.
   * By default the media formats are read from the component properties of the component and this
   * parameter should not be set. But for components that allow to choose one from the allowed media
   * formats via their edit dialog the format can be set here.
   * To be used together with 'imageSizes' and 'widths'.<br>
   * Cannot be used together with the picture source parameters.
   */
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String mediaFormat;

  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String refProperty;

  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String cropProperty;

  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String rotationProperty;

  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String cssClass;

  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private Boolean autoCrop;

  /**
   * Defines responsive rendition widths for image.
   * To be used together with 'imageSizes' property.
   * Example: "{@literal 2560?,1920,?1280,640,320}" <br>
   * Widths are by default required. To declare an optional width append the "{@literal ?}" suffix, eg. "1440?"<br>
   * Cannot be used together with the picture source parameters.
   */
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String imageWidths;

  /**
   * "Sizes" string for img element.
   * Example: "{@literal (min-width: 400px) 400px, 100vw}"<br>
   * Cannot be used together with the picture source parameters.
   */
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String imageSizes;

  /**
   * List of media formats for the picture source elements.
   * Example: "{@literal ['mf_16_9']}"<br>
   * You have to define the same number of array items in all pictureSource* properties.<br>
   * Cannot be used together with image sizes.
   */
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private Object[] pictureSourceMediaFormat;

  /**
   * List of media expressions for the picture source elements.
   * Example: "{@literal ['(max-width: 799px)', '(min-width: 800px)']}"<br>
   * You have to define the same number of array items in all pictureSource* properties.<br>
   * Cannot be used together with image sizes.
   */
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private Object[] pictureSourceMedia;

  /**
   * List of widths for the picture source elements.
   * Example: "{@literal 479,719,959,1279,1439?,1440?}"<br>
   * You have to define the same number of array items in all pictureSource* properties.
   * Widths are by default required. To declare an optional width append the "{@literal ?}" suffix, eg. "1440?"<br>
   * Cannot be used together with image sizes.
   */
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private Object[] pictureSourceWidths;

  @Self
  private MediaHandler mediaHandler;
  @Self
  private MediaFormatHandler mediaFormatHandler;
  @SlingObject
  private Resource resource;

  private Media media;

  @PostConstruct
  @SuppressWarnings("null")
  private void activate() {
    MediaBuilder builder = mediaHandler.get(resource);

    if (StringUtils.isNotEmpty(mediaFormat)) {
      builder.mediaFormatName(mediaFormat);
    }
    if (StringUtils.isNotEmpty(refProperty)) {
      builder.refProperty(refProperty);
    }
    if (StringUtils.isNotEmpty(cropProperty)) {
      builder.cropProperty(cropProperty);
    }
    if (StringUtils.isNotEmpty(rotationProperty)) {
      builder.rotationProperty(rotationProperty);
    }
    if (autoCrop != null) {
      builder.autoCrop(autoCrop);
    }
    if (StringUtils.isNotEmpty(cssClass)) {
      builder.property(PROP_CSS_CLASS, cssClass);
    }

    // apply responsive image handling - either via image sizes or picture sources
    if (StringUtils.isNotEmpty(imageSizes)) {
      WidthOption[] widthOptionsArray = parseWidths(imageWidths);
      if (widthOptionsArray != null) {
        builder.imageSizes(imageSizes, widthOptionsArray);
      }
    }
    else if (pictureSourceMediaFormat != null && pictureSourceMedia != null && pictureSourceWidths != null) {
      ImageUtils.applyPictureSources(mediaFormatHandler, builder,
          toStringArray(pictureSourceMediaFormat),
          toStringArray(pictureSourceMedia),
          toStringArray(pictureSourceWidths));
    }

    media = builder.build();
  }

  /**
   * For some reason passing in arrays from HTL works only with Object[], not with String[].
   * Thus, convert it here to String[].
   *
   * @param objectArray Array of objects
   * @return Array with objects converted to strings
   */
  private static String[] toStringArray(Object... objectArray) {
    return Arrays.stream(objectArray)
        .map(obj -> obj == null ? "" : obj.toString())
        .toArray(String[]::new);
  }

  /**
   * Returns a {@link Media} object with the metadata of the resolved media.
   * Result is never null, check for validness with the {@link Media#isValid()} method.
   * @return Media
   */
  public @NotNull Media getMetadata() {
    return media;
  }

  /**
   * Returns true if the media was resolved successful.
   * @return Media is valid
   */
  public boolean isValid() {
    return media.isValid();
  }

  /**
   * Returns the XHTML markup for the resolved media object (if valid).
   * This is in most cases an img element, but may also contain other arbitrary markup.
   * @return Media markup
   */
  public @Nullable String getMarkup() {
    return media.getMarkup();
  }

}
