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
package io.wcm.handler.media.format;

import io.wcm.handler.media.MediaNameConstants;

import java.util.ArrayList;
import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Special builder that supports generating a on-the-fly list of media formats derived from a main
 * media format with same ratio but different sizes for different breakpoints.
 * <p>
 * The main media format should not have a fixed dimension defined, but only a ratio and probably min. width and height.
 * The resulting media formats are only generated on-the-fly for the media resolution process. On each format a
 * {@link MediaNameConstants#PROP_BREAKPOINT} breakpoint is set that is used by the
 * {@link io.wcm.handler.media.markup.ResponsiveImageMediaMarkupBuilder}.
 * </p>
 */
@ProviderType
public final class ResponsiveMediaFormatsBuilder {

  private final MediaFormat mainMediaFormat;
  private final List<MediaFormat> mediaFormats = new ArrayList<>();

  /**
   * @param mainMediaFormat Main media format from which the reponsive "on-the-fly" formats are derived from.
   */
  public ResponsiveMediaFormatsBuilder(MediaFormat mainMediaFormat) {
    this.mainMediaFormat = mainMediaFormat;
  }

  /**
   * Defines one breakpoint for each "on-the-fly" format required.
   * @param breakpoint Breakpoint name which is set in the {@link MediaNameConstants#PROP_BREAKPOINT} property.
   * @param width Width for the breakpoint
   * @param height Height for the breakpoint
   * @return this
   */
  public ResponsiveMediaFormatsBuilder breakpoint(String breakpoint, int width, int height) {
    mediaFormats.add(MediaFormatBuilder.create()
        .name(buildCombinedName(mainMediaFormat, breakpoint, width, height))
        .applicationId(mainMediaFormat.getApplicationId())
        .label(mainMediaFormat.getLabel())
        .extensions(mainMediaFormat.getExtensions())
        .ratio(mainMediaFormat.getRatio())
        .fixedDimension(width, height)
        .property(MediaNameConstants.PROP_BREAKPOINT, breakpoint)
        .build());
    return this;
  }

  /**
   * Builds an array of media formats that can be used as for
   * {@link io.wcm.handler.media.MediaBuilder#mandatoryMediaFormats(MediaFormat...)}.
   * @return Array of on-the-fly media formats
   */
  public MediaFormat[] build() {
    return mediaFormats.toArray(new MediaFormat[mediaFormats.size()]);
  }

  static String buildCombinedName(MediaFormat mediaFormat, String breakpoint, int width, int height) {
    return mediaFormat.getName() + "_" + breakpoint + "_" + width + "_" + height;
  }

}
