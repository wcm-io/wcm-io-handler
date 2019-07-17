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
    RenditionMetadataGenerator generator = new RenditionMetadataGenerator(resourceResolver);
    for (String assetPath : assetPaths) {
      process(assetPath, resourceResolver, generator);
    }
  }

  /**
   * Process a single asset path.
   * @param assetPath Asset path
   * @param resourceResolver resource resolver
   * @param generator Rendition metadata generator
   */
  private void process(@NotNull String assetPath, @NotNull ResourceResolver resourceResolver,
      @NotNull RenditionMetadataGenerator generator) {
    Resource resource = resourceResolver.getResource(assetPath);
    if (resource != null) {
      Asset asset = DamUtil.resolveToAsset(resource);
      if (asset != null) {
        generator.processAllRenditions(asset);
      }
    }
  }

}
