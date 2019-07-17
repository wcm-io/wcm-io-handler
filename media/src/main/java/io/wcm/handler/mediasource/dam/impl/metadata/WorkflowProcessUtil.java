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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.PayloadMap;
import com.adobe.granite.workflow.collection.ResourceCollection;
import com.adobe.granite.workflow.collection.ResourceCollectionManager;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.day.cq.wcm.api.NameConstants;

import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Helper methods for processing workflow payload.
 */
final class WorkflowProcessUtil {

  private static final Logger log = LoggerFactory.getLogger(WorkflowProcessUtil.class);

  static final String RT_WORKFLOW_PACKAGE = "cq/workflow/components/collection/page";

  private WorkflowProcessUtil() {
    // static methods only
  }

  /**
   * Checks if the payload points to a resource path and returns it.
   * @param workItem Work item
   * @return Payload resource path or null
   */
  public static @Nullable String getPayloadResourcePath(@NotNull WorkItem workItem) {
    WorkflowData data = workItem.getWorkflowData();
    if (StringUtils.equals(data.getPayloadType(), PayloadMap.TYPE_JCR_PATH)) {
      return data.getPayload().toString();
    }
    else {
      return null;
    }
  }

  /**
   * Get all resource paths paths form workflow payload - either directly referenced in the payload,
   * or a collection of resources referenced via a workflow package.
   * @param payloadPath Payload path
   * @param primaryTypeResourceType JCR primary type or node type, in case of workflow package the result is filtered
   *          to return only matching resources
   * @param resourceResolver Resource resolver
   * @param resourceCollectionManager Resource collection manager
   * @return List of asset paths
   */
  public static @NotNull List<String> getPayloadResourcePaths(@NotNull String payloadPath,
      @NotNull String primaryTypeResourceType,
      @NotNull ResourceResolver resourceResolver,
      @NotNull ResourceCollectionManager resourceCollectionManager) {
    Session session = AdaptTo.notNull(resourceResolver, Session.class);
    List<String> assetPaths = new ArrayList<>();
    try {
      if (session.nodeExists(payloadPath)) {
        Node node = session.getNode(payloadPath);

        // check if payload node is a workflow package - collect all matching resources from it
        if (isWorkflowPackagePage(node, resourceResolver)) {
          List<ResourceCollection> resourceCollections = resourceCollectionManager.getCollectionsForNode(node);
          for (ResourceCollection resourceCollection : resourceCollections) {
            for (Node memberNode : resourceCollection.list(new String[] { primaryTypeResourceType })) {
              assetPaths.add(memberNode.getPath());
            }
          }
        }

        // otherwise directly return the payload path
        else {
          assetPaths.add(payloadPath);
        }

      }
    }
    catch (RepositoryException ex) {
      log.warn("Unable to resolve resource paths from workflow payload: " + payloadPath, ex);
    }
    return assetPaths;
  }

  private static boolean isWorkflowPackagePage(Node node, ResourceResolver resourceResolver) throws RepositoryException {
    if (node.isNodeType(NameConstants.NT_PAGE)) {
      Resource resource = resourceResolver.getResource(node.getPath());
      if (resource != null) {
        return resource.isResourceType(RT_WORKFLOW_PACKAGE);
      }
    }
    return false;
  }

}
