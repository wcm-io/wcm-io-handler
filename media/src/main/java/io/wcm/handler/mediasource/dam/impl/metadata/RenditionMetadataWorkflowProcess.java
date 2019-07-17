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
package io.wcm.handler.mediasource.dam.impl.metadata;

import static com.day.cq.dam.api.DamConstants.NT_DAM_ASSET;

import java.util.List;
import java.util.concurrent.locks.Lock;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.collection.ResourceCollectionManager;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;

import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Workflow process that generated rendition metadata required for the media handler processing.
 * Can also process workflow packages.
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=wcm.io Media Handler: Rendition metadata"
    })
public final class RenditionMetadataWorkflowProcess implements WorkflowProcess {

  private static final Logger log = LoggerFactory.getLogger(RenditionMetadataWorkflowProcess.class);

  @Reference
  private ResourceCollectionManager resourceCollectionManager;
  @Reference
  private AssetSynchonizationService assetSynchronizationService;

  @Override
  public void execute(WorkItem item, WorkflowSession workflowSession, MetaDataMap args) {
    String payloadPath = WorkflowProcessUtil.getPayloadResourcePath(item);
    if (payloadPath == null) {
      log.warn("Invalid payload: " + item.getWorkflowData().getPayloadType());
      return;
    }

    // collect asset(s) from payload
    ResourceResolver resourceResolver = AdaptTo.notNull(workflowSession, ResourceResolver.class);
    List<String> assetPaths = WorkflowProcessUtil.getPayloadResourcePaths(payloadPath,
        NT_DAM_ASSET, resourceResolver, resourceCollectionManager);
    if (assetPaths.isEmpty()) {
      log.debug("Did not found any asset reference in workflow payload: {}", payloadPath);
    }
    else {
      log.debug("Process asset references: {}", assetPaths);
    }

    // process all assets
    for (String assetPath : assetPaths) {
      process(assetPath, resourceResolver);
    }
  }

  /**
   * Process a single asset path.
   * @param assetOrRenditionPath Path to asset or a rendition of it
   * @param resourceResolver Resource resolver from workflow
   */
  private void process(@NotNull String assetOrRenditionPath, @NotNull ResourceResolver resourceResolver) {
    // make sure asset exists
    Asset asset = getAsset(assetOrRenditionPath, resourceResolver);
    if (asset == null) {
      log.debug("Unable to read asset at {} with user {}", assetOrRenditionPath, resourceResolver.getUserID());
      return;
    }

    // process event synchronized per asset path
    Lock lock = assetSynchronizationService.getLock(asset.getPath());
    lock.lock();

    try {
      // refresh resource resolver to reflect changes on metadata probably made by listener service
      resourceResolver.refresh();

      // process asset renditions
      RenditionMetadataGenerator generator = new RenditionMetadataGenerator(resourceResolver);
      generator.processAllRenditions(asset);
    }
    finally {
      lock.unlock();
    }
  }

  /**
   * Get asset instance for given asset path.
   * @param assetOrRenditionPath Path to asset or a rendition of it
   * @return Asset or null if path is invalid
   */
  private Asset getAsset(String assetOrRenditionPath, ResourceResolver resolver) {
    Resource resource = resolver.getResource(assetOrRenditionPath);
    if (resource != null) {
      return DamUtil.resolveToAsset(resource);
    }
    else {
      return null;
    }
  }

}
