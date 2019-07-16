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

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.commons.jcr.JcrConstants.NT_UNSTRUCTURED;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.NN_RENDITIONS_METADATA;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.PN_IMAGE_HEIGHT;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.PN_IMAGE_WIDTH;

import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.image.Layer;
import com.google.common.collect.ImmutableMap;

import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Generates metadata (widht/height) for renditions in DAM assets.
 */
public final class RenditionMetadataGenerator {

  private final ResourceResolver resolver;
  @SuppressWarnings("unused")
  private final String userId;

  private static final Logger log = LoggerFactory.getLogger(RenditionMetadataGenerator.class);

  /**
   * @param resolver Resource resolver
   * @param userId User ID
   */
  public RenditionMetadataGenerator(ResourceResolver resolver, String userId) {
    this.resolver = resolver;
    this.userId = userId;
  }

  /**
   * Create or update rendition metadata if rendition is created or updated.
   * @param asset Asset
   * @param renditionPath Rendition path
   */
  public void renditionAddedOrUpdated(Asset asset, String renditionPath) {
    String renditionNodeName = Text.getName(renditionPath);

    // check for resource existence and try to get layer from image
    // (record duration of converting resource to layer for debugging)
    Resource renditionResource = resolver.getResource(renditionPath);
    if (renditionResource == null) {
      return;
    }
    long startTime = System.currentTimeMillis();
    Layer renditionLayer = renditionResource.adaptTo(Layer.class);
    long conversionDuration = System.currentTimeMillis() - startTime;
    if (renditionLayer == null) {
      return;
    }

    // update metadata
    Resource renditionsMetadata = getRenditionsMetadataResource(asset, true);
    if (renditionsMetadata != null) {
      try {
        Resource metadataResource = ResourceUtil.getOrCreateResource(renditionsMetadata.getResourceResolver(),
            renditionsMetadata.getPath() + "/" + renditionNodeName,
            ImmutableMap.<String, Object>of(JCR_PRIMARYTYPE, NT_UNSTRUCTURED), NT_UNSTRUCTURED, false);
        ModifiableValueMap props = AdaptTo.notNull(metadataResource, ModifiableValueMap.class);
        props.put(PN_IMAGE_WIDTH, renditionLayer.getWidth());
        props.put(PN_IMAGE_HEIGHT, renditionLayer.getHeight());
        updateLastModifiedAndSave(asset);
        log.debug("Updated rendition metadata at {} (width={}, height={}); duration={}ms.",
            metadataResource.getPath(),
            renditionLayer.getWidth(),
            renditionLayer.getHeight(),
            conversionDuration);
      }
      catch (PersistenceException ex) {
        log.error("Unable to create or update rendition metadata node for " + renditionPath, ex);
      }
    }
  }

  /**
   * Remove rendition metadata node if rendition is removed.
   * @param asset Asset
   * @param renditionPath Rendition path
   */
  public void renditionRemoved(Asset asset, String renditionPath) {
    Resource renditionsResource = getRenditionsMetadataResource(asset, false);
    if (renditionsResource == null) {
      return;
    }
    try {
      String renditionNodeName = Text.getName(renditionPath);
      Resource metadataResource = renditionsResource.getChild(renditionNodeName);
      if (metadataResource != null) {
        String pathToRemove = metadataResource.getPath();
        renditionsResource.getResourceResolver().delete(metadataResource);
        updateLastModifiedAndSave(asset);
        log.debug("Removed rendition metadata at {}.", pathToRemove);
      }
    }
    catch (PersistenceException ex) {
      log.error("Unable to delete rendition metadata node for " + renditionPath, ex);
    }
  }

  /**
   * Updates last modified information and saves the session.
   * @param asset Asset
   * @throws PersistenceException
   */
  private void updateLastModifiedAndSave(Asset asset) throws PersistenceException {
    // -- this is currently DISABLED due to WCMIO-28, concurrency issues with DAM workflows
    /*
    Node node = asset.adaptTo(Node.class);
    Node contentNode = node.getNode(JCR_CONTENT);
    // this is a workaround to make sure asset is marked as modified
    contentNode.setProperty(JcrConstants.JCR_LASTMODIFIED, Calendar.getInstance());
    contentNode.setProperty(JcrConstants.JCR_LAST_MODIFIED_BY, userId);
     */
    resolver.commit();
  }

  /**
   * Get node for storing the renditions additional metadata.
   * @param asset Asset
   * @param createIfNotExists if true the node is (tried to be) created automatically if it does not exist
   * @return Node or null if it does not exist or could not be created
   */
  @SuppressWarnings("null")
  private Resource getRenditionsMetadataResource(Asset asset, boolean createIfNotExists) {
    Resource assetResource = asset.adaptTo(Resource.class);
    String renditionsMetadataPath = assetResource.getPath() + "/" + JCR_CONTENT + "/" + NN_RENDITIONS_METADATA;
    try {
      if (createIfNotExists) {
        return ResourceUtil.getOrCreateResource(assetResource.getResourceResolver(), renditionsMetadataPath,
            ImmutableMap.<String, Object>of(JCR_PRIMARYTYPE, NT_UNSTRUCTURED), NT_UNSTRUCTURED, false);
      }
      else {
        return assetResource.getChild(renditionsMetadataPath);
      }
    }
    catch (PersistenceException ex) {
      log.error("Unable to get/create renditions metadata node at " + asset.getPath(), ex);
    }
    return null;
  }

}
