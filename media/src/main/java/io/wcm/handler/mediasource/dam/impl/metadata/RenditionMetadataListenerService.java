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
package io.wcm.handler.mediasource.dam.impl.metadata;

import static com.day.cq.dam.api.DamConstants.ORIGINAL_FILE;

import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.DamEvent;
import com.day.cq.dam.api.DamEvent.Type;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.wcm.handler.media.MediaFileType;
import io.wcm.wcm.commons.util.RunMode;

/**
 * Background service that extracts additional metadata like width and height for DAM renditions.
 */
@Component(service = EventHandler.class, immediate = true,
    // define explicit PID which was the old location of the implementation class
    configurationPid = "io.wcm.handler.mediasource.dam.impl.DamRenditionMetadataService",
    property = {
        EventConstants.EVENT_TOPIC + "=" + DamEvent.EVENT_TOPIC
    })
@Designate(ocd = RenditionMetadataListenerService.Config.class)
public final class RenditionMetadataListenerService implements EventHandler {

  @ObjectClassDefinition(
      name = "wcm.io Media Handler Rendition Metadata Service",
      description = "Extracts additional metadata like width and height for AEM asset renditions.")
  static @interface Config {

    @AttributeDefinition(name = "Enabled", description = "Switch to enable or disable this service.")
    boolean enabled() default true;

    @AttributeDefinition(name = "Allowed run mode(s)", description = "Run modes this service is allowed on. "
        + "The service is disabled if the run mode does not match.")
    String[] allowedRunMode() default RunMode.AUTHOR;

    @AttributeDefinition(name = "Thread pool size", description = "Size of threads in pool that is used to process "
        + "asset rendition events asynchronously. "
        + "Setting it to 0 disables asynchronous processing completely (not recommended for production environments).")
    int threadPoolSize() default 10;

  }

  private static final int REMOVE_EVENT_EXECUTION_DELAY_SECONDS = 10;
  private static final int MAX_RETRY_COUNT = 3;
  private static final int RETRY_DELAY_SECONDS = 5;

  private static final EnumSet<DamEvent.Type> SUPPORTED_EVENT_TYPES = EnumSet.of(DamEvent.Type.RENDITION_UPDATED, DamEvent.Type.RENDITION_REMOVED);
  private static final Logger log = LoggerFactory.getLogger(RenditionMetadataListenerService.class);

  private boolean enabled;
  private boolean synchronousProcessing;

  @Reference
  private ResourceResolverFactory resourceResolverFactory;
  @Reference
  private SlingSettingsService slingSettings;
  @Reference
  private AssetSynchonizationService assetSynchronizationService;

  private ScheduledExecutorService executorService;

  @Activate
  @SuppressWarnings("deprecation")
  private void activate(ComponentContext componentContext, Config config) {
    if (config.enabled()) {
      if (config.allowedRunMode() != null && config.allowedRunMode().length > 0) {
        // Activate only if configured run modes are met
        this.enabled = !RunMode.disableIfNoRunModeActive(slingSettings.getRunModes(), config.allowedRunMode(), componentContext, log);
      }
      else {
        this.enabled = true;
      }
    }
    else {
      this.enabled = false;
    }
    this.synchronousProcessing = config.threadPoolSize() <= 0;
    if (this.enabled && !this.synchronousProcessing) {
      this.executorService = Executors.newScheduledThreadPool(config.threadPoolSize(),
          new ThreadFactoryBuilder().setNameFormat(getClass().getSimpleName() + "-%d").build());
    }
  }

  @Deactivate
  private void deactivate() {
    this.enabled = false;
    if (executorService != null) {
      executorService.shutdown();
      try {
        executorService.awaitTermination(10, TimeUnit.SECONDS);
      }
      catch (InterruptedException ex) {
        // ignore
      }
      executorService = null;
    }
  }

  @Override
  public void handleEvent(Event event) {
    if (!enabled || !StringUtils.equals(event.getTopic(), DamEvent.EVENT_TOPIC)) {
      return;
    }
    DamEvent damEvent = DamEvent.fromEvent(event);
    // process only rendition-related events
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
    boolean isOriginal = StringUtils.equals(renditionNodeName, ORIGINAL_FILE);
    String fileExtension = FilenameUtils.getExtension(renditionNodeName);
    if (!(isOriginal || MediaFileType.isImage(fileExtension))) {
      return;
    }

    RenditionMetadataEvent runnable = new RenditionMetadataEvent(event.getAssetPath(),
        renditionPath, event.getType());
    if (synchronousProcessing) {
      // execute directly in synchronous mode (e.g. for unit tests)
      runnable.run();
    }
    else {
      // decouple event processing from listener to avoid timeouts
      executorService.schedule(runnable, runnable.getDelaySeconds(), TimeUnit.SECONDS);
    }
  }

  private final class RenditionMetadataEvent implements Runnable {

    private final String assetPath;
    private final String renditionPath;
    private final DamEvent.Type eventType;

    private int retryCount;

    RenditionMetadataEvent(String assetPath, String renditionPath, Type eventType) {
      this.assetPath = assetPath;
      this.renditionPath = renditionPath;
      this.eventType = eventType;
    }

    private int getDelaySeconds() {
      if (eventType == DamEvent.Type.RENDITION_REMOVED) {
        // delay event handling in case of removed event for some time to avoid repository conflicts
        // e.g. when new packages with sample content are installed remove and udpate events
        // are quickly fired after another
        return REMOVE_EVENT_EXECUTION_DELAY_SECONDS;
      }
      else {
        return 0;
      }
    }

    @Override
    public void run() {
      // process event synchronized per asset path
      Lock lock = assetSynchronizationService.getLock(assetPath);
      lock.lock();

      ResourceResolver serviceResourceResolver = null;
      try {
        // open service user session for reading/writing rendition metadata
        serviceResourceResolver = resourceResolverFactory.getServiceResourceResolver(null);

        // make sure asset resource exists
        Resource assetResource = serviceResourceResolver.getResource(assetPath);
        if (assetResource == null) {
          log.debug("Unable to read asset at {} with user {}", assetPath, serviceResourceResolver.getUserID());
          return;
        }

        if (eventType == DamEvent.Type.RENDITION_UPDATED) {
          renditionAddedOrUpdated(serviceResourceResolver);
        }
        else if (eventType == DamEvent.Type.RENDITION_REMOVED) {
          renditionRemoved(serviceResourceResolver);
        }
      }
      catch (PersistenceException ex) {
        // in case of persistence exception retry execution some times later
        this.retryCount++;
        if (this.retryCount >= MAX_RETRY_COUNT) {
          // retried too often - log as error
          log.error("Failed after {} attempts: {}", this.retryCount, ex.getMessage(), ex);
        }
        else {
          log.debug("Failed {} attempt(s), retry: {}", this.retryCount, ex.getMessage(), ex);
          executorService.schedule(this, RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
        }
      }
      catch (LoginException ex) {
        log.error("Missing service user mapping for 'io.wcm.handler.media' - "
            + "see https://wcm.io/handler/media/configuration.html", ex);
      }
      finally {
        lock.unlock();
        if (serviceResourceResolver != null) {
          serviceResourceResolver.close();
        }
      }
    }

    /**
     * Create or update rendition metadata if rendition is created or updated.
     * @throws PersistenceException Persistence exception
     */
    private void renditionAddedOrUpdated(ResourceResolver resolver) throws PersistenceException {
      log.trace("Process rendition added/updated event: {}", renditionPath);
      RenditionMetadataGenerator generator = new RenditionMetadataGenerator(resolver);
      generator.renditionAddedOrUpdated(renditionPath);
    }

    /**
     * Remove rendition metadata node if rendition is removed.
     * @throws PersistenceException Persistence exception
     */
    private void renditionRemoved(ResourceResolver resolver) throws PersistenceException {
      log.trace("Process rendition removed event: {}", renditionPath);
      RenditionMetadataGenerator generator = new RenditionMetadataGenerator(resolver);
      generator.renditionRemoved(renditionPath);
    }

  }

}
