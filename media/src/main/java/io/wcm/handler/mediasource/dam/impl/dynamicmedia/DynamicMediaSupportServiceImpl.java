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

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;

import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.featureflags.Features;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.s7dam.utils.PublishUtils;
import com.day.cq.dam.entitlement.api.EntitlementConstants;
import com.google.common.collect.ImmutableMap;

import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Implements {@link DynamicMediaSupportService}.
 */
@Component(service = DynamicMediaSupportService.class, immediate = true)
public class DynamicMediaSupportServiceImpl implements DynamicMediaSupportService {

  @Reference
  private Features featureFlagService;
  @Reference
  private PublishUtils dynamicMediaPublishUtils;
  @Reference
  private ResourceResolverFactory resourceResolverFactory;

  private static final String SERVICEUSER_SUBSERVICE = "dynamic-media-support";

  private static final Logger log = LoggerFactory.getLogger(DynamicMediaSupportServiceImpl.class);

  @Override
  public boolean isDynamicMediaEnabled() {
    return featureFlagService.isEnabled(EntitlementConstants.ASSETS_SCENE7_FEATURE_FLAG_PID);
  }

  @Override
  public @Nullable ImageProfile getImageProfile(@NotNull String profilePath) {
    try (ResourceResolver resourceResolver = resourceResolverFactory
        .getServiceResourceResolver(ImmutableMap.of(ResourceResolverFactory.SUBSERVICE, SERVICEUSER_SUBSERVICE))) {
      Resource profileResource = resourceResolver.getResource(profilePath);
      if (profileResource != null) {
        return new ImageProfileImpl(profileResource);
      }
    }
    catch (LoginException ex) {
      log.error("Missing service user mapping for 'io.wcm.handler.media:dynamic-media-support' - see https://wcm.io/handler/media/configuration.html", ex);
    }
    return null;
  }

  @Override
  @SuppressWarnings("null")
  public @Nullable ImageProfile getImageProfileForAsset(@NotNull Asset asset) {
    Resource assetResource = AdaptTo.notNull(asset, Resource.class);
    Resource folderResource = assetResource.getParent();
    if (folderResource != null) {
      Resource folderContentResource = folderResource.getChild(JCR_CONTENT);
      if (folderContentResource != null) {
        InheritanceValueMap inheritanceValueMap = new HierarchyNodeInheritanceValueMap(folderContentResource);
        String imageProfilePath = inheritanceValueMap.getInherited(DamConstants.IMAGE_PROFILE, String.class);
        if (imageProfilePath != null) {
          return getImageProfile(imageProfilePath);
        }
      }
    }
    return null;
  }

  @Override
  public @Nullable String getDynamicMediaProductionAssetUrl(@NotNull Asset asset) {
    Resource assetResource = AdaptTo.notNull(asset, Resource.class);
    try {
      String[] productionAssetUrls = dynamicMediaPublishUtils.externalizeImageDeliveryAsset(assetResource);
      if (productionAssetUrls != null && productionAssetUrls.length > 0) {
        return productionAssetUrls[0];
      }
    }
    catch (RepositoryException ex) {
      log.warn("Unable to get dynamic media production asset URLs for {}", assetResource.getPath(), ex);
    }
    return null;
  }

}
