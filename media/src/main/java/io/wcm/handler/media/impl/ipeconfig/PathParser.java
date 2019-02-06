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
package io.wcm.handler.media.impl.ipeconfig;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

class PathParser {

  public static final String NN_ASPECT_RATIOS = "aspectRatios";
  static final String NN_MEDIA_FORMAT = "wcmio:mediaFormat";
  static final String NN_CONFIG = "wcmio:config";

  private static final Pattern PATH_PATTERN = Pattern.compile(
      "^" + IPEConfigResourceProvider.IPECONFIG_OVERLAY_ROOTPATH + "((/[^/]+)+)"
          + "/" + NN_MEDIA_FORMAT + "((/[^/]+)+)"
          + "/" + NN_CONFIG + "(/.*)?$");

  private static final Pattern PLUGINS_CROP_PATH_PATTERN = Pattern.compile(
      "^.*/plugins/crop(/" + NN_ASPECT_RATIOS + "(/([^/]+))?)?$");

  private String componentContentPath;
  private String relativeConfigPath;
  private SortedSet<String> mediaFormatNames;

  private boolean pluginsCropNode;
  private boolean aspectRatiosNode;
  private String aspectRatioItemName;

  PathParser(String path) {
    Matcher matcher = PATH_PATTERN.matcher(path);
    if (matcher.matches()) {
      this.componentContentPath = matcher.group(1);
      String[] names = StringUtils.split(matcher.group(3), "/");
      this.mediaFormatNames = new TreeSet<>(Arrays.asList(names));
      this.relativeConfigPath = matcher.group(5);

      // check if related config path is around the "aspectRatios" node of crop plugin
      if (StringUtils.isNotEmpty(this.relativeConfigPath)) {
        Matcher pluginsCropPathMatcher = PLUGINS_CROP_PATH_PATTERN.matcher(this.relativeConfigPath);
        if (pluginsCropPathMatcher.matches()) {
          if (StringUtils.isEmpty(pluginsCropPathMatcher.group(1))) {
            pluginsCropNode = true;
          }
          else if (StringUtils.isEmpty(pluginsCropPathMatcher.group(2))) {
            aspectRatiosNode = true;
          }
          else {
            aspectRatioItemName = pluginsCropPathMatcher.group(3);
          }
        }
      }
    }
  }

  public String getComponentContentPath() {
    return this.componentContentPath;
  }

  public String getRelativeConfigPath() {
    return this.relativeConfigPath;
  }

  public SortedSet<String> getMediaFormatNames() {
    return this.mediaFormatNames;
  }

  public boolean isValid() {
    return StringUtils.isNotEmpty(this.componentContentPath);
  }

  public boolean isPluginsCropNode() {
    return pluginsCropNode;
  }

  public boolean isAspectRatiosNode() {
    return aspectRatiosNode;
  }

  public boolean isAspectRatioItem() {
    return StringUtils.isNotEmpty(this.aspectRatioItemName);
  }

  public String getAspectRatioItemName() {
    return this.aspectRatioItemName;
  }

}
