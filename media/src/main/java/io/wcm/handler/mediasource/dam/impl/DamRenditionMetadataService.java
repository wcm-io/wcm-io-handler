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

import java.util.EnumSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
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

import io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataGenerator;
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

  @ObjectClassDefinition(name = "wcm.io Media Handler Rendition Metadata Service",
      description = "Extracts additional metadata like width and height for AEM asset renditions.")
  static @interface Config {

    @AttributeDefinition(name = "Enabled", description = "Switch to enable or disable this service.")
    boolean enabled() default true;

  }

  private static final EnumSet<DamEvent.Type> SUPPORTED_EVENT_TYPES = EnumSet.of(DamEvent.Type.RENDITION_UPDATED, DamEvent.Type.RENDITION_REMOVED);

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private boolean enabled;

  @Reference
  private ResourceResolverFactory resourceResolverFactory;

  @Reference
  private SlingSettingsService slingSettings;

  @Activate
  @SuppressWarnings("deprecation")
  private void activate(ComponentContext componentContext, Config config) {
    if (config.enabled()) {
      // Activate only in author mode, and check enabled status in service configuration as well
      this.enabled = !RunMode.disableIfNotAuthor(slingSettings.getRunModes(), componentContext, log);
    }
    else {
      this.enabled = false;
    }
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
    String fileExtension = FilenameUtils.getExtension(renditionNodeName);
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
        log.debug("Unable to read DAM asset at {} with user {}", event.getAssetPath(), adminResourceResolver.getUserID());
        return;
      }

      if (event.getType() == DamEvent.Type.RENDITION_UPDATED) {
        renditionAddedOrUpdated(asset, renditionPath, adminResourceResolver);
      }
      else if (event.getType() == DamEvent.Type.RENDITION_REMOVED) {
        renditionRemoved(asset, renditionPath, adminResourceResolver);
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
  private void renditionAddedOrUpdated(Asset asset, String renditionPath, ResourceResolver resolver) {
    log.trace("Process rendition added/updated event: {}", renditionPath);
    RenditionMetadataGenerator generator = new RenditionMetadataGenerator(resolver);
    generator.renditionAddedOrUpdated(asset, renditionPath);
  }

  /**
   * Remove rendition metadata node if rendition is removed.
   * @param asset Asset
   * @param renditionPath Rendition path
   */
  private void renditionRemoved(Asset asset, String renditionPath, ResourceResolver resolver) {
    log.trace("Process rendition removed event: {}", renditionPath);
    RenditionMetadataGenerator generator = new RenditionMetadataGenerator(resolver);
    generator.renditionRemoved(asset, renditionPath);
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

}
