/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.Adaptable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.scene7.api.constants.Scene7Constants;

import io.wcm.handler.mediasource.dam.impl.dynamicmedia.DynamicMediaSupportService;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.ImageProfile;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.NamedDimension;

/**
 * Context objects require in DAM support implementation.
 */
public final class DamContext implements Adaptable {

  private final Asset asset;
  private final DynamicMediaSupportService dynamicMediaSupportService;
  private final Adaptable adaptable;

  private Boolean dynamicMediaEnabled;
  private String dynamicMediaObject;
  private String dynamicMediaProductionAssetUrl;
  private ImageProfile imageProfile;

  private static final ImageProfile NO_IMAGE_PROFILE = new ImageProfile() {
    @Override
    public @NotNull List<NamedDimension> getSmartCropDefinitions() {
      return Collections.emptyList();
    }
  };

  /**
   * @param asset DAM asset
   * @param dynamicMediaSupportService Dynamic media support service
   * @param adaptable Adaptable from current context
   */
  public DamContext(Asset asset, DynamicMediaSupportService dynamicMediaSupportService,
      Adaptable adaptable) {
    this.asset = asset;
    this.adaptable = adaptable;
    this.dynamicMediaSupportService = dynamicMediaSupportService;
  }

  /**
   * @return DAM asset
   */
  public Asset getAsset() {
    return asset;
  }

  /**
   * @return Whether dynamic media is enabled on this AEM instance
   */
  public boolean isDynamicMediaEnabled() {
    if (dynamicMediaEnabled == null) {
      dynamicMediaEnabled = dynamicMediaSupportService.isDynamicMediaEnabled();
    }
    return dynamicMediaEnabled;
  }

  /**
   * @return Dynamic media object identifier (value of dam:scene7File property).
   */
  public @Nullable String getDynamicMediaObject() {
    if (dynamicMediaObject == null) {
      dynamicMediaObject = asset.getMetadataValueFromJcr(Scene7Constants.PN_S7_FILE);
    }
    return dynamicMediaObject;
  }

  /**
   * @return true if the DAM asset from this context has dynamic media metadata applied.
   */
  public boolean isDynamicMediaAsset() {
    return StringUtils.isNotBlank(getDynamicMediaObject());
  }

  /**
   * @return Get scene7 host for publish environment.
   */
  public @Nullable String getDynamicMediaProductionAssetUrl() {
    if (dynamicMediaProductionAssetUrl == null) {
      dynamicMediaProductionAssetUrl = dynamicMediaSupportService.getDynamicMediaProductionAssetUrl(asset);
    }
    return dynamicMediaProductionAssetUrl;
  }

  /**
   * Get image profile for current DAM asset.
   * @return Image profile or null if none associated/found
   */
  public @Nullable ImageProfile getImageProfile() {
    if (imageProfile == null) {
      imageProfile = dynamicMediaSupportService.getImageProfileForAsset(asset);
      if (imageProfile == null) {
        imageProfile = NO_IMAGE_PROFILE;
      }
    }
    if (imageProfile == NO_IMAGE_PROFILE) {
      return null;
    }
    else {
      return imageProfile;
    }
  }

  @Override
  public <AdapterType> @Nullable AdapterType adaptTo(@NotNull Class<AdapterType> type) {
    return adaptable.adaptTo(type);
  }

}
