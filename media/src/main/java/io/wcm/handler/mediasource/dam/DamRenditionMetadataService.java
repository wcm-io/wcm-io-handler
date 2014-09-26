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
package io.wcm.handler.mediasource.dam;

import io.wcm.wcm.commons.contenttype.FileExtension;
import io.wcm.wcm.commons.util.RunMode;

import java.util.Calendar;
import java.util.EnumSet;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamEvent;
import com.day.image.Layer;
import com.day.text.Text;

/**
 * Background service that extracts additional metadata like width and height for DAM renditions.
 * TODO: migrate JCR logic to Sling CRUD - remove JCR dependency in POM then as well
 */
@Component(immediate = true, metatype = true,
label = "wcm.io DAM Rendition Metadata Service",
description = "Extracts additional metadata like with and hight for DAM renditions.")
@Property(name = EventConstants.EVENT_TOPIC, value = DamEvent.EVENT_TOPIC, propertyPrivate = true)
@Service(EventHandler.class)
public final class DamRenditionMetadataService implements EventHandler {

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

  private static final boolean DEFAULT_ENABLED = true;

  @Property(boolValue = DEFAULT_ENABLED, label = "Enabled", description = "Switch to enable or disable this service.")
  private static final String PROPERTY_ENABLED = "enabled";

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private boolean enabled;

  @Reference
  private ResourceResolverFactory resourceResolverFactory;

  @Reference
  private SlingSettingsService slingSettings;

  @Activate
  protected void activate(ComponentContext componentContext) {
    // Activate only in author mode, and check enabled status in service configuration as well
    enabled = !RunMode.disableIfNotAuthor(slingSettings.getRunModes(), componentContext, log)
        && PropertiesUtil.toBoolean(componentContext.getProperties().get(PROPERTY_ENABLED), DEFAULT_ENABLED);
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
      log.warn("Getting service resource resolver failed. "
          + "Please make sure a service user is defined for bundle 'io.wcm.handler.mediasource.dam'.", ex);
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
    Node renditionsMetadata = getRenditionsMetadataNode(asset, true);
    if (renditionsMetadata != null) {
      try {
        Node metadataNode;
        if (renditionsMetadata.hasNode(renditionNodeName)) {
          metadataNode = renditionsMetadata.getNode(renditionNodeName);
        }
        else {
          metadataNode = renditionsMetadata.addNode(renditionNodeName, JcrConstants.NT_UNSTRUCTURED);
        }
        metadataNode.setProperty(PN_IMAGE_WIDTH, renditionLayer.getWidth());
        metadataNode.setProperty(PN_IMAGE_HEIGHT, renditionLayer.getHeight());
        updateLastModifiedAndSave(asset, userId, resolver);
        log.debug("Updated rendition metadata at " + metadataNode.getPath() + " "
            + "(width=" + renditionLayer.getWidth() + ", height=" + renditionLayer.getHeight() + ").");
      }
      catch (RepositoryException ex) {
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
    Node renditionsMetadata = getRenditionsMetadataNode(asset, false);
    if (renditionsMetadata == null) {
      return;
    }
    try {
      String renditionNodeName = Text.getName(renditionPath);
      if (renditionsMetadata.hasNode(renditionNodeName)) {
        Node metadataNode = renditionsMetadata.getNode(renditionNodeName);
        metadataNode.remove();
        updateLastModifiedAndSave(asset, userId, resolver);
        log.debug("Removed rendition metadata at " + metadataNode.getPath() + ".");
      }
    }
    catch (RepositoryException ex) {
      log.error("Unable to delete rendition metadata node for " + renditionPath, ex);
    }
  }

  /**
   * Updates last modified information and saves the session.
   * @param asset Asset
   * @param userId User id
   */
  private void updateLastModifiedAndSave(Asset asset, String userId, ResourceResolver resolver) throws RepositoryException {
    Node node = asset.adaptTo(Node.class);
    Node contentNode = node.getNode(JcrConstants.JCR_CONTENT);
    // this is a workaround to make sure asset is marked as modified
    contentNode.setProperty(JcrConstants.JCR_LASTMODIFIED, Calendar.getInstance());
    contentNode.setProperty(JcrConstants.JCR_LAST_MODIFIED_BY, userId);
    resolver.adaptTo(Session.class).save();
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
  private Node getRenditionsMetadataNode(Asset asset, boolean createIfNotExists) {
    try {
      Node assetNode = asset.adaptTo(Node.class);
      if (assetNode != null) {
        Node assetContentNode = assetNode.getNode(JcrConstants.JCR_CONTENT);
        if (assetContentNode.hasNode(NN_RENDITIONS_METADATA)) {
          return assetContentNode.getNode(NN_RENDITIONS_METADATA);
        }
        else if (createIfNotExists) {
          return assetContentNode.addNode(NN_RENDITIONS_METADATA, JcrConstants.NT_UNSTRUCTURED);
        }
      }
    }
    catch (RepositoryException ex) {
      log.error("Unable to get/create renditions metadata node at " + asset.getPath(), ex);
    }
    return null;
  }

}
