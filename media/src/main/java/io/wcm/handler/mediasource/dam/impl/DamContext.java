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

import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.featureflags.Features;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.s7dam.utils.PublishUtils;
import com.day.cq.dam.entitlement.api.EntitlementConstants;
import com.day.cq.dam.scene7.api.constants.Scene7Constants;

import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Context objects require in DAM support implementation.
 */
class DamContext implements Adaptable {

  private final Asset asset;
  private final Features featureFlagService;
  private final PublishUtils dynamicMediaPublishUtils;
  private final Adaptable adaptable;

  private Boolean dynamicMediaEnabled;
  private String dynamicMediaObject;
  private String dynamicMediaProductionAssetUrl;

  private static final Logger log = LoggerFactory.getLogger(DamContext.class);

  /**
   * @param asset DAM asset
   * @param featureFlagService Feature flag service
   * @param dynamicMediaPublishUtils Dynamic media publish utils service
   * @param adaptable Adaptable from current context
   */
  DamContext(Asset asset, Features featureFlagService, PublishUtils dynamicMediaPublishUtils, Adaptable adaptable) {
    this.asset = asset;
    this.adaptable = adaptable;
    this.featureFlagService = featureFlagService;
    this.dynamicMediaPublishUtils = dynamicMediaPublishUtils;
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
      dynamicMediaEnabled = featureFlagService.isEnabled(EntitlementConstants.ASSETS_SCENE7_FEATURE_FLAG_PID);
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
      Resource assetResource = AdaptTo.notNull(asset, Resource.class);
      try {
        String[] productionAssetUrls = dynamicMediaPublishUtils.externalizeImageDeliveryAsset(assetResource);
        if (productionAssetUrls != null && productionAssetUrls.length > 0) {
          dynamicMediaProductionAssetUrl = productionAssetUrls[0];
        }
      }
      catch (RepositoryException ex) {
        log.warn("Unable to get dynamic media production asset URLs for {}", assetResource.getPath(), ex);
      }
    }
    return dynamicMediaProductionAssetUrl;
  }

  @Override
  public <AdapterType> @Nullable AdapterType adaptTo(@NotNull Class<AdapterType> type) {
    return adaptable.adaptTo(type);
  }

}
