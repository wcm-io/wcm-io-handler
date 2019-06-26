/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.commons.jcr.JcrConstants.NT_UNSTRUCTURED;

import java.util.EnumSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamEvent;
import com.day.image.Layer;
import com.google.common.collect.ImmutableMap;

import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.contenttype.FileExtension;
import io.wcm.wcm.commons.util.RunMode;

/**
 * Background service that extracts additional metadata like width and height for DAM renditions.
 */
@Component(service = EventHandler.class, immediate = true, property = {
    EventConstants.EVENT_TOPIC + "=" + DamEvent.EVENT_TOPIC
})
@Designate(ocd = DamRenditionMetadataService.Config.class)
public final class DamRenditionMetadataService implements EventHandler {

  @ObjectClassDefinition(name = "wcm.io DAM Rendition Metadata Service",
      description = "Extracts additional metadata like width and height for DAM renditions")
  static @interface Config {

    @AttributeDefinition(name = "Enabled", description = "Switch to enable or disable this service.")
    boolean enabled() default true;

  }

  /**
   * Name for Renditions Metadata node
   */
  public static final String NN_RENDITIONS_METADATA = "renditionsMetadata";

  /**
   * Property for image with in pixels
   */
  public static final String PN_IMAGE_WIDTH = "imageWidth";

  /**
   * Property for image height in pixels
   */
  public static final String PN_IMAGE_HEIGHT = "imageHeight";

  private static final EnumSet<DamEvent.Type> SUPPORTED_EVENT_TYPES = EnumSet.of(DamEvent.Type.RENDITION_UPDATED, DamEvent.Type.RENDITION_REMOVED);

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private boolean enabled;

  @Reference
  private ResourceResolverFactory resourceResolverFactory;

  @Reference
  private SlingSettingsService slingSettings;

  @SuppressWarnings("deprecation")
  @Activate
  private void activate(ComponentContext componentContext, Config config) {
    // Activate only in author mode, and check enabled status in service configuration as well
    enabled = !RunMode.disableIfNotAuthor(slingSettings.getRunModes(), componentContext, log)
        && config.enabled();
  }

  @Override
  public void handleEvent(Event event) {
    if (!enabled || !StringUtils.equals(event.getTopic(), DamEvent.EVENT_TOPIC)) {
      return;
    }
    DamEvent damEvent = DamEvent.fromEvent(event);
    if (SUPPORTED_EVENT_TYPES.contains(damEvent.getType())) {
      handleDamEvent(damEvent);
    }
  }

  /**
   * Handle dam event if certain conditions are fulfilled.
   * @param event DAM event
   */
  private void handleDamEvent(DamEvent event) {

    // make sure rendition file extension is an image extensions
    String renditionPath = event.getAdditionalInfo();
    String renditionNodeName = Text.getName(renditionPath);
    String fileExtension = StringUtils.substringAfterLast(renditionNodeName, ".");
    if (!FileExtension.isImage(fileExtension)) {
      return;
    }

    // open admin session for reading/writing rendition metadata
    ResourceResolver adminResourceResolver = null;
    try {
      adminResourceResolver = resourceResolverFactory.getServiceResourceResolver(null);

      // make sure asset exists
      Asset asset = getAsset(event.getAssetPath(), adminResourceResolver);
      if (asset == null) {
        return;
      }

      if (event.getType() == DamEvent.Type.RENDITION_UPDATED) {
        renditionAddedOrUpdated(asset, renditionPath, event.getUserId(), adminResourceResolver);
      }
      else if (event.getType() == DamEvent.Type.RENDITION_REMOVED) {
        renditionRemoved(asset, renditionPath, event.getUserId(), adminResourceResolver);
      }

    }
    catch (LoginException ex) {
      log.warn("Missing service user mapping for 'io.wcm.handler.media' - "
          + "see https://wcm.io/handler/media/configuration.html", ex);
    }
    finally {
      if (adminResourceResolver != null) {
        adminResourceResolver.close();
      }
    }
  }

  /**
   * Create or update rendition metadata if rendition is created or updated.
   * @param asset Asset
   * @param renditionPath Rendition path
   */
  private void renditionAddedOrUpdated(Asset asset, String renditionPath, String userId, ResourceResolver resolver) {
    String renditionNodeName = Text.getName(renditionPath);

    // check for resource existence and try to get layer from image
    Resource renditionResource = resolver.getResource(renditionPath);
    if (renditionResource == null) {
      return;
    }
    Layer renditionLayer = renditionResource.adaptTo(Layer.class);
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
        updateLastModifiedAndSave(asset, userId, resolver);
        log.debug("Updated rendition metadata at " + metadataResource.getPath() + " "
            + "(width=" + renditionLayer.getWidth() + ", height=" + renditionLayer.getHeight() + ").");
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
  private void renditionRemoved(Asset asset, String renditionPath, String userId, ResourceResolver resolver) {
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
        updateLastModifiedAndSave(asset, userId, resolver);
        log.debug("Removed rendition metadata at " + pathToRemove + ".");
      }
    }
    catch (PersistenceException ex) {
      log.error("Unable to delete rendition metadata node for " + renditionPath, ex);
    }
  }

  /**
   * Updates last modified information and saves the session.
   * @param asset Asset
   * @param userId User id
   * @throws PersistenceException
   */
  private void updateLastModifiedAndSave(Asset asset, String userId, ResourceResolver resolver) throws PersistenceException {
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
   * Get asset instance for given asset path.
   * @param assetPath Asset path
   * @return Asset or null if path is invalid
   */
  private Asset getAsset(String assetPath, ResourceResolver resolver) {
    Resource assetResource = resolver.getResource(assetPath);
    if (assetResource != null) {
      return assetResource.adaptTo(Asset.class);
    }
    else {
      return null;
    }
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
