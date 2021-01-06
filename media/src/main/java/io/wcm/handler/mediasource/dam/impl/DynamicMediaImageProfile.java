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
package io.wcm.handler.mediasource.dam.impl;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;

import io.wcm.handler.media.Dimension;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.util.ToStringStyle;

/**
 * Wraps access to dynamic media image profile.
 */
final class DynamicMediaImageProfile {

  static final String PN_CROP_TYPE = "crop_type";
  static final String CROP_TYPE_SMART = "crop_smart";
  static final String PN_BANNER = "banner";

  private final Resource profileResource;

  private DynamicMediaImageProfile(@NotNull Resource profileResource) {
    this.profileResource = profileResource;
  }

  /**
   * Get defined smart cropping dimensions. Returns empty list of no definitions are found.
   * @return List of named smart cropping dimensions
   */
  public @NotNull List<NamedDimension> getSmartCropDefinitions() {
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

  /**
   * Get dynamic media image profile for given asset (as configured in folder or parent folder of asset).
   * @param asset DAM asset
   * @return Profile instance or null if none found
   */
  @SuppressWarnings("null")
  static @Nullable DynamicMediaImageProfile get(@NotNull Asset asset) {
    Resource assetResource = AdaptTo.notNull(asset, Resource.class);
    Resource folderResource = assetResource.getParent();
    if (folderResource != null) {
      Resource folderContentResource = folderResource.getChild(JCR_CONTENT);
      if (folderContentResource != null) {
        InheritanceValueMap inheritanceValueMap = new HierarchyNodeInheritanceValueMap(folderContentResource);
        String imageProfilePath = inheritanceValueMap.getInherited(DamConstants.IMAGE_PROFILE, String.class);
        if (imageProfilePath != null) {
          ResourceResolver resourceResolver = assetResource.getResourceResolver();
          Resource profileResource = resourceResolver.getResource(imageProfilePath);
          if (profileResource != null) {
            return new DynamicMediaImageProfile(profileResource);
          }
        }
      }
    }
    return null;
  }

  /**
   * Dimension with a name
   */
  static final class NamedDimension extends Dimension {

    private final String name;

    NamedDimension(String name, long width, long height) {
      super(width, height);
      this.name = name;
    }

    public String getName() {
      return this.name;
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_OMIT_NULL_STYLE)
          .append("name", getName())
          .append("width", getWidth())
          .append("height", getHeight())
          .build();
    }

  }

}
