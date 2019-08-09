/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import static io.wcm.handler.media.MediaNameConstants.NN_COMPONENT_MEDIA_RESPONSIVE_IMAGE_SIZES;
import static io.wcm.handler.media.MediaNameConstants.NN_COMPONENT_MEDIA_RESPONSIVE_PICTURE_SOURCES;
import static io.wcm.handler.media.MediaNameConstants.PN_COMPONENT_MEDIA_AUTOCROP;
import static io.wcm.handler.media.MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS;
import static io.wcm.handler.media.MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS_MANDATORY;
import static io.wcm.handler.media.MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS_MANDATORY_NAMES;
import static io.wcm.handler.media.MediaNameConstants.PN_COMPONENT_MEDIA_RESPONSIVE_TYPE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.MediaArgs.ImageSizes;
import io.wcm.handler.media.MediaArgs.MediaFormatOption;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaArgs.WidthOption;
import io.wcm.handler.media.MediaBuilder;
import io.wcm.wcm.commons.component.ComponentPropertyResolution;
import io.wcm.wcm.commons.component.ComponentPropertyResolver;

/**
 * Implements resolving initial settings for {@link MediaBuilder} from
 * content policies and properties defined in component resources.
 */
class MediaComponentPropertyResolver {

  static final String RESPONSIVE_TYPE_IMAGE_SIZES = "imageSizes";
  static final String RESPONSIVE_TYPE_PICTURE_SOURCES = "pictureSources";

  static final String PN_IMAGES_SIZES_SIZES = "sizes";
  static final String PN_IMAGES_SIZES_WIDTHS = "widths";

  static final String PN_PICTURE_SOURCES_MEDIAFORMAT = "mediaFormat";
  static final String PN_PICTURE_SOURCES_MEDIA = "media";
  static final String PN_PICTURE_SOURCES_WIDTHS = "widths";

  private static final Pattern WIDTHS_PATTERN = Pattern.compile("^\\s*\\d+\\??\\s*(,\\s*\\d+\\??\\s*)*$");

  private final ComponentPropertyResolver resolver;

  MediaComponentPropertyResolver(Resource resource) {
    // resolve media component properties 1. from policies and 2. from component definition
    resolver = new ComponentPropertyResolver(resource)
        .contentPolicyResolution(ComponentPropertyResolution.RESOLVE)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
  }

  /**
   * @return AutoCrop state
   */
  boolean isAutoCrop() {
    return resolver.get(PN_COMPONENT_MEDIA_AUTOCROP, false);
  }

  /**
   * @return List of media formats with and without mandatory setting.
   */
  MediaFormatOption[] getMediaFormatOptions() {
    Map<String, MediaFormatOption> mediaFormatOptions = new LinkedHashMap<>();

    // media formats with optional mandatory boolean flag(s)
    String[] mediaFormatNames = resolver.get(PN_COMPONENT_MEDIA_FORMATS, String[].class);
    Boolean[] mediaFormatsMandatory = resolver.get(PN_COMPONENT_MEDIA_FORMATS_MANDATORY, Boolean[].class);
    if (mediaFormatNames != null) {
      for (int i = 0; i < mediaFormatNames.length; i++) {
        boolean mandatory = false;
        if (mediaFormatsMandatory != null) {
          if (mediaFormatsMandatory.length == 1) {
            // backward compatibility: support a single flag for all media formats
            mandatory = mediaFormatsMandatory[0];
          }
          else if (mediaFormatsMandatory.length > i) {
            mandatory = mediaFormatsMandatory[i];
          }
        }
        if (StringUtils.isNotBlank(mediaFormatNames[i])) {
          mediaFormatOptions.put(mediaFormatNames[i], new MediaFormatOption(mediaFormatNames[i], mandatory));
        }
      }
    }

    // support additional property with list of media format names that are all rated as mandatory
    String[] mediaFormatsMandatoryNames = resolver.get(PN_COMPONENT_MEDIA_FORMATS_MANDATORY_NAMES, String[].class);
    if (mediaFormatsMandatoryNames != null) {
      for (String mediaFormatName : mediaFormatsMandatoryNames) {
        if (StringUtils.isNotBlank(mediaFormatName)) {
          mediaFormatOptions.put(mediaFormatName, new MediaFormatOption(mediaFormatName, true));
        }
      }
    }

    if (mediaFormatOptions.isEmpty()) {
      return null;
    }
    else {
      return mediaFormatOptions.values().stream().toArray(size -> new MediaFormatOption[size]);
    }
  }

  ImageSizes getImageSizes() {
    String responsiveType = getResponsiveType();
    if (responsiveType != null && !StringUtils.equals(responsiveType, RESPONSIVE_TYPE_IMAGE_SIZES)) {
      return null;
    }

    String sizes = StringUtils.trimToNull(resolver.get(NN_COMPONENT_MEDIA_RESPONSIVE_IMAGE_SIZES + "/" + PN_IMAGES_SIZES_SIZES, String.class));
    WidthOption[] widths = parseWidths(resolver.get(NN_COMPONENT_MEDIA_RESPONSIVE_IMAGE_SIZES + "/" + PN_IMAGES_SIZES_WIDTHS, String.class));
    if (sizes != null && widths != null) {
      return new ImageSizes(sizes, widths);
    }
    return null;
  }

  PictureSource[] getPictureSources() {
    String responsiveType = getResponsiveType();
    if (responsiveType != null && !StringUtils.equals(responsiveType, RESPONSIVE_TYPE_PICTURE_SOURCES)) {
      return null;
    }

    Collection<Resource> sourceResources = resolver.getResources(NN_COMPONENT_MEDIA_RESPONSIVE_PICTURE_SOURCES);
    if (sourceResources == null) {
      return null;
    }

    List<PictureSource> sources = new ArrayList<>();
    for (Resource sourceResource : sourceResources) {
      ValueMap props = sourceResource.getValueMap();
      String mediaFormatName = StringUtils.trimToNull(props.get(PN_PICTURE_SOURCES_MEDIAFORMAT, String.class));
      String media = StringUtils.trimToNull(props.get(PN_PICTURE_SOURCES_MEDIA, String.class));
      WidthOption[] widths = parseWidths(props.get(PN_PICTURE_SOURCES_WIDTHS, String.class));
      if (mediaFormatName != null && widths != null) {
        sources.add(new PictureSource(mediaFormatName, media, widths));
      }
    }

    if (sources.isEmpty()) {
      return null;
    }
    else {
      return sources.stream().toArray(size -> new PictureSource[size]);
    }
  }

  private String getResponsiveType() {
    return resolver.get(PN_COMPONENT_MEDIA_RESPONSIVE_TYPE, String.class);
  }

  static @NotNull WidthOption @Nullable [] parseWidths(@Nullable String widths) {
    if (StringUtils.isBlank(widths)) {
      return null;
    }
    if (!WIDTHS_PATTERN.matcher(widths).matches()) {
      return null;
    }
    String[] widthItems = StringUtils.split(widths, ",");
    return Arrays.stream(widthItems)
        .map(StringUtils::trim)
        .map(MediaComponentPropertyResolver::toWidthOption)
        .toArray(size -> new WidthOption[size]);
  }

  private static @NotNull WidthOption toWidthOption(String width) {
    boolean optional = StringUtils.endsWith(width, "?");
    String widthValue;
    if (optional) {
      widthValue = StringUtils.substringBefore(width, "?");
    }
    else {
      widthValue = width;
    }
    return new WidthOption(NumberUtils.toLong(widthValue), !optional);
  }

}
