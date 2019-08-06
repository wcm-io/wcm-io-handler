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
import static com.day.cq.commons.jcr.JcrConstants.JCR_CREATED;
import static com.day.cq.commons.jcr.JcrConstants.JCR_LASTMODIFIED;
import static com.day.cq.commons.jcr.JcrConstants.JCR_LAST_MODIFIED_BY;
import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.commons.jcr.JcrConstants.NT_UNSTRUCTURED;
import static com.day.cq.dam.api.DamConstants.ORIGINAL_FILE;
import static com.day.cq.dam.api.DamConstants.RENDITIONS_FOLDER;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.NN_RENDITIONS_METADATA;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.PN_IMAGE_HEIGHT;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.PN_IMAGE_WIDTH;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.image.Layer;
import com.google.common.collect.ImmutableMap;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaFileType;
import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Generates metadata (widht/height) for renditions in DAM assets.
 */
public final class RenditionMetadataGenerator {

  private final ResourceResolver resourceResolver;

  private static final Logger log = LoggerFactory.getLogger(RenditionMetadataGenerator.class);

  /**
   * @param resourceResolver Resource resolver
   */
  public RenditionMetadataGenerator(ResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
  }

  /**
   * Generate/validate rendition metadata of all renditions of this asset.
   * @param asset Asset
   */
  public void processAllRenditions(Asset asset) {
    Set<String> existingRenditionNames = new HashSet<>();
    List<String> renditionPaths = new ArrayList<>();

    int addUpdateCount = 0;
    int removeCount = 0;
    int errorCount = 0;

    // get existing rendition names and paths
    for (Rendition rendition : asset.getRenditions()) {
      // skip original rendition
      if (StringUtils.equals(rendition.getName(), ORIGINAL_FILE)) {
        continue;
      }
      existingRenditionNames.add(rendition.getName());
      renditionPaths.add(rendition.getPath());
    }

    // get existing rendition names for which metadata exists (some may be obsolete)
    Set<String> existingMetadataRenditionNames = new HashSet<>();
    Resource metadataResource = resourceResolver.getResource(asset.getPath() + "/" + JCR_CONTENT + "/" + NN_RENDITIONS_METADATA);
    if (metadataResource != null) {
      for (Resource metadataItem : metadataResource.getChildren()) {
        existingMetadataRenditionNames.add(metadataItem.getName());
      }
    }

    // generate metadata for all existing renditions
    for (String renditionPath : renditionPaths) {
      try {
        if (renditionAddedOrUpdated(asset, renditionPath)) {
          addUpdateCount++;
        }
      }
      catch (PersistenceException ex) {
        log.error(ex.getMessage(), ex);
        errorCount++;
      }
    }

    // remove obsolete metadata
    existingMetadataRenditionNames.removeAll(existingRenditionNames);
    for (String obsoleteRenditionName : existingMetadataRenditionNames) {
      String nonexistingRenditionPath = asset.getPath() + "/" + JCR_CONTENT + "/" + RENDITIONS_FOLDER
          + "/" + obsoleteRenditionName;
      try {
        if (renditionRemoved(asset, nonexistingRenditionPath)) {
          removeCount++;
        }
      }
      catch (PersistenceException ex) {
        log.error(ex.getMessage(), ex);
        errorCount++;
      }
    }

    log.info("Processed rendition metadata for {}, {} added/updated, {} removed, {} error(s)",
        asset.getPath(), addUpdateCount, removeCount, errorCount);
  }

  /**
   * Create or update rendition metadata if rendition is created or updated.
   * @param asset Asset
   * @param renditionPath Rendition path
   * @throws PersistenceException Persistence exception
   * @return true if rendition data was added or updated
   */
  public boolean renditionAddedOrUpdated(Asset asset, String renditionPath) throws PersistenceException {

    // ensure rendition is an image rendition for which metadata can be generated
    String fileExtension = FilenameUtils.getExtension(renditionPath);
    if (!MediaFileType.isImage(fileExtension)) {
      log.debug("Skip non-image rendition {}", renditionPath);
      return false;
    }

    // check for resource existence and try to get layer from image
    // (record duration of converting resource to layer for debugging)
    Resource renditionResource = resourceResolver.getResource(renditionPath);
    if (renditionResource == null) {
      log.debug("Skip generation of metadata for non-existing rendition {}", renditionPath);
      return false;
    }

    // Compare timestamps of rendition and rendition metadata
    Calendar renditionTimestamp = getLastModified(renditionResource);
    String metdataResourcePath = getRenditionMetadataResourcePath(asset, renditionPath);
    Resource metadataResource = resourceResolver.getResource(metdataResourcePath);
    Calendar renditionsMetadataTimestamp = getLastModified(metadataResource);
    boolean metadataOutdated = (renditionTimestamp == null)
        || (renditionsMetadataTimestamp == null)
        || renditionsMetadataTimestamp.before(renditionTimestamp);
    if (!metadataOutdated) {
      log.debug("Skip re-generation of metadata for unchanged rendition {}", renditionPath);
      return false;
    }

    // calculate rendition dimension
    long startTime = System.currentTimeMillis();
    Dimension dimension = getRenditionDimension(renditionResource);
    long conversionDuration = System.currentTimeMillis() - startTime;
    if (dimension == null) {
      log.debug("Unable to calculate dimension of rendition {}", renditionPath);
      return false;
    }

    // write metadata
    try {
      log.debug("Update rendition metadata at {} (width={}, height={}); duration={}ms.",
          metdataResourcePath, dimension.getWidth(), dimension.getHeight(), conversionDuration);

      if (metadataResource == null) {
        metadataResource = ResourceUtil.getOrCreateResource(resourceResolver,
            metdataResourcePath,
            ImmutableMap.<String, Object>of(JCR_PRIMARYTYPE, NT_UNSTRUCTURED),
            null, false);
      }

      ModifiableValueMap props = AdaptTo.notNull(metadataResource, ModifiableValueMap.class);
      props.put(PN_IMAGE_WIDTH, dimension.getWidth());
      props.put(PN_IMAGE_HEIGHT, dimension.getHeight());
      props.put(JCR_LASTMODIFIED, Calendar.getInstance());
      props.put(JCR_LAST_MODIFIED_BY, resourceResolver.getUserID());
      resourceResolver.commit();
      return true;
    }
    catch (PersistenceException ex) {
      throw new PersistenceException("Unable to create or update rendition metadata node for " + renditionPath, ex);
    }
  }

  private Calendar getLastModified(@Nullable Resource resource) {
    Calendar lastModified = null;
    if (resource != null) {
      // if a rendition is updated it's last modified date is stored in the jcr:content child node
      Resource contentResource = resource.getChild(JCR_CONTENT);
      if (contentResource != null) {
        lastModified = contentResource.getValueMap().get(JCR_LASTMODIFIED, Calendar.class);
      }
      if (lastModified == null) {
        lastModified = resource.getValueMap().get(JCR_LASTMODIFIED, Calendar.class);
      }
      if (lastModified == null) {
        lastModified = resource.getValueMap().get(JCR_CREATED, Calendar.class);
      }
    }
    return lastModified;
  }

  /**
   * Remove rendition metadata node if rendition is removed.
   * @param asset Asset
   * @param renditionPath Rendition path
   * @throws PersistenceException Persistence exception
   * @return true if rendition data was removed
   */
  public boolean renditionRemoved(Asset asset, String renditionPath) throws PersistenceException {

    // check if rendition still exist (or exists again) - in this case skip removing of renditions metadata
    Resource renditionResource = resourceResolver.getResource(renditionPath);
    if (renditionResource != null) {
      log.debug("Skip removing of metadata for existing rendition {}", renditionPath);
      return false;
    }

    // remove rendition metadata for non-existing rendition
    String metdataResourcePath = getRenditionMetadataResourcePath(asset, renditionPath);
    Resource metadataResource = resourceResolver.getResource(metdataResourcePath);
    if (metadataResource == null) {
      return false;
    }
    try {
      log.debug("Remove rendition metadata at {}.", metadataResource.getPath());
      resourceResolver.delete(metadataResource);
      resourceResolver.commit();
      return true;
    }
    catch (PersistenceException ex) {
      throw new PersistenceException("Unable to delete rendition metadata node for " + renditionPath, ex);
    }
  }

  /**
   * Get dimension (with/height) of rendition.
   * @param renditionResource Rendition
   * @return Dimension or null if it could not be detected
   */
  private Dimension getRenditionDimension(Resource renditionResource) {
    Layer layer = renditionResource.adaptTo(Layer.class);
    if (layer == null) {
      return null;
    }
    return new Dimension(layer.getWidth(), layer.getHeight());
  }

  /**
   * Get resource path for metadata for given rendition.
   * @param asset Asset
   * @param renditionPath Rendition path
   * @return Metadata resource or null if none exist
   */
  private String getRenditionMetadataResourcePath(Asset asset, String renditionPath) {
    String renditionName = Text.getName(renditionPath);
    return asset.getPath() + "/" + JCR_CONTENT + "/" + NN_RENDITIONS_METADATA + "/" + renditionName;
  }

}
