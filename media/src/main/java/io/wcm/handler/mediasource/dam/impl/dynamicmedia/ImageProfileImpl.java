/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.handler.mediasource.dam.impl.dynamicmedia;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;

/**
 * Wraps access to dynamic media image profile.
 */
final class ImageProfileImpl implements ImageProfile {

  static final String PN_CROP_TYPE = "crop_type";
  static final String CROP_TYPE_SMART = "crop_smart";
  static final String PN_BANNER = "banner";

  private final List<NamedDimension> smartCropDefinitions;

  ImageProfileImpl(@NotNull Resource profileResource) {
    this.smartCropDefinitions = readSmartCropDefinitions(profileResource);
  }

  /**
   * Get defined smart cropping dimensions. Returns empty list of no definitions are found.
   * @return List of named smart cropping dimensions
   */
  @Override
  public @NotNull List<NamedDimension> getSmartCropDefinitions() {
    return smartCropDefinitions;
  }

  private static @NotNull List<NamedDimension> readSmartCropDefinitions(@NotNull Resource profileResource) {
    List<NamedDimension> result = new ArrayList<>();

    ValueMap props = profileResource.getValueMap();
    String cropType = props.get(PN_CROP_TYPE, String.class);
    String cropDefs = props.get(PN_BANNER, String.class);
    if (StringUtils.equals(cropType, CROP_TYPE_SMART) && cropDefs != null) {
      String[] defs = StringUtils.split(cropDefs, "|");
      for (String def : defs) {
        String[] parts = StringUtils.split(def, ",");
        if (parts.length == 3) {
          try {
            String name = parts[0];
            long width = Long.parseLong(parts[1]);
            long height = Long.parseLong(parts[2]);
            result.add(new NamedDimension(name, width, height));
          }
          catch (NumberFormatException ex) {
            // ignore
          }
        }
      }
    }

    return result;
  }

}
