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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.FilenameUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.PayloadMap;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.collection.ResourceCollection;
import com.adobe.granite.workflow.collection.ResourceCollectionManager;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;

import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.contenttype.FileExtension;

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
    WorkflowData data = item.getWorkflowData();
    if (!data.getPayloadType().equals(PayloadMap.TYPE_JCR_PATH)) {
      return;
    }

    ResourceResolver resourceResolver = AdaptTo.notNull(workflowSession, ResourceResolver.class);
    String payloadPath = item.getWorkflowData().getPayload().toString();

    List<String> assetPaths = getAssetPaths(payloadPath, resourceResolver);
    RenditionMetadataGenerator generator = new RenditionMetadataGenerator(resourceResolver);
    for (String assetPath : assetPaths) {
      process(assetPath, resourceResolver, generator);
    }
  }

  /**
   * Get all asset paths form workflow payload - either the asset path itself of all assets references in
   * a workflow package.
   * @param payloadPath Payload path
   * @param resourceResolver Resource resolver
   * @return List of asset paths
   */
  private List<String> getAssetPaths(@NotNull String payloadPath, @NotNull ResourceResolver resourceResolver) {
    Session session = AdaptTo.notNull(resourceResolver, Session.class);
    List<String> assetPaths = new ArrayList<>();
    try {
      if (session.nodeExists(payloadPath)) {
        Node node = session.getNode(payloadPath);

        // check for direct reference to asset
        if (node.isNodeType(NT_DAM_ASSET)) {
          assetPaths.add(payloadPath);
        }

        // otherwise check if payload node is a workflow package - collect all asset paths from it
        else {
          List<ResourceCollection> resourceCollections = resourceCollectionManager.getCollectionsForNode(node);
          for (ResourceCollection resourceCollection : resourceCollections) {
            for (Node memberNode : resourceCollection.list(new String[] { NT_DAM_ASSET })) {
              assetPaths.add(memberNode.getPath());
            }
          }
        }

      }
    }
    catch (RepositoryException ex) {
      log.warn("Unable to resolve asset paths from workflow payload: " + payloadPath, ex);
    }
    if (assetPaths.isEmpty()) {
      log.debug("Did not found any asset reference in workflow payload: {}", payloadPath);
    }
    else {
      log.debug("Found asset references: {}", assetPaths);
    }
    return assetPaths;
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
      Asset asset = resource.adaptTo(Asset.class);
      if (asset != null) {
        String fileExtension = FilenameUtils.getExtension(asset.getName());
        if (FileExtension.isImage(fileExtension)) {
          generator.processAllRenditions(asset);
        }
        else {
          log.debug("Skip non-image asset: {}", assetPath);
        }
      }
    }
  }

}
