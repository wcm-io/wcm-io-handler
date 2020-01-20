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

import static com.day.cq.commons.jcr.JcrConstants.JCR_LASTMODIFIED;
import static com.day.cq.commons.jcr.JcrConstants.JCR_LAST_MODIFIED_BY;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.NN_RENDITIONS_METADATA;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.PN_IMAGE_HEIGHT;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.PN_IMAGE_WIDTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.DamEvent;

import io.wcm.handler.media.testcontext.MediaSourceDamAppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.util.RunMode;

@ExtendWith(AemContextExtension.class)
class RenditionMetadataListenerServiceTest {

  private static final String ASSET_PATH = MediaSourceDamAppAemContext.DAM_PATH + "/standard.jpg";
  private static final String RENDITIONS_PATH = ASSET_PATH + "/jcr:content/renditions";
  private static final String RENDITIONS_METADATA_PATH = ASSET_PATH + "/jcr:content/" + NN_RENDITIONS_METADATA;

  private final AemContext context = MediaSourceDamAppAemContext.newAemContext(ResourceResolverType.JCR_MOCK);

  private RenditionMetadataListenerService underTest;
  private Resource assetResource;

  @BeforeEach
  void setUp() {
    context.registerInjectActivateService(new AssetSynchonizationService());

    context.load().json("/mediasource/dam/damcontent-sample.json", MediaSourceDamAppAemContext.DAM_PATH);
    assetResource = context.resourceResolver().getResource(ASSET_PATH);
    assertNotNull(assetResource);

    context.runMode(RunMode.AUTHOR);
  }

  @Test
  void testAddRendition_Metadata() {
    underTest = context.registerInjectActivateService(new RenditionMetadataListenerService(),
        "threadPoolSize", 0);
    addRendition("test.jpg");
    assertRenditionMetadata("test.jpg", 215, 102, true);
  }

  @Test
  @SuppressWarnings("null")
  void testAddRendition_Metadata_createMetadataNode() throws PersistenceException {
    underTest = context.registerInjectActivateService(new RenditionMetadataListenerService(),
        "threadPoolSize", 0);

    // remove all existing renditions metadata incl. renditionsMetadata node
    Resource metadata = context.resourceResolver().getResource(RENDITIONS_METADATA_PATH);
    context.resourceResolver().delete(metadata);

    assertNoRenditionMetadata("test.jpg");
    addRendition("test.jpg");
    assertRenditionMetadata("test.jpg", 215, 102, true);
  }

  @Test
  void testAddRendition_PublishInstance_NoMetadata() {
    context.runMode(RunMode.PUBLISH);
    underTest = context.registerInjectActivateService(new RenditionMetadataListenerService(),
        "threadPoolSize", 0);
    addRendition("test.jpg");
    assertNoRenditionMetadata("test.jpg");
  }

  @Test
  void testAddRendition_Disabled_NoMetadata() {
    underTest = context.registerInjectActivateService(new RenditionMetadataListenerService(),
        "enabled", false);
    addRendition("test.jpg");
    assertNoRenditionMetadata("test.jpg");
  }

  @Test
  void testUpdateRendition() throws PersistenceException {
    underTest = context.registerInjectActivateService(new RenditionMetadataListenerService(),
        "threadPoolSize", 0);

    // check existing metadata
    assertRenditionMetadata("cq5dam.web.450.213.jpg", 450, 213, false);

    // replace with new rendition with different dimensions
    updateRendition("cq5dam.web.450.213.jpg");

    // ensure metadata was generated
    assertRenditionMetadata("cq5dam.web.450.213.jpg", 215, 102, true);
  }

  @Test
  void testUpdateRendition_LastModified() throws PersistenceException, InterruptedException {
    underTest = context.registerInjectActivateService(new RenditionMetadataListenerService(),
        "threadPoolSize", 0);

    // check existing metadata
    assertNull(getRenditionMetadataLastModified("cq5dam.web.450.213.jpg"));

    // replace with new rendition with different dimensions
    updateRendition("cq5dam.web.450.213.jpg");

    // ensure metadata was generated
    Calendar lastModifiedAfterUpdate = getRenditionMetadataLastModified("cq5dam.web.450.213.jpg");
    assertNotNull(lastModifiedAfterUpdate);

    // manually send an updated DAM event
    sendRenditionUpdatedEvent("cq5dam.web.450.213.jpg");

    // ensure rendition metadata was not newly generated
    Calendar lastModifiedAfterEvent = getRenditionMetadataLastModified("cq5dam.web.450.213.jpg");
    assertEquals(lastModifiedAfterUpdate, lastModifiedAfterEvent);

    // wait a little bit and replace rendition again
    Thread.sleep(10);
    updateRendition("cq5dam.web.450.213.jpg");

    // ensure rendition metadata was re-generated and new last modified date is set
    Calendar lastModifiedAfter2ndUpdate = getRenditionMetadataLastModified("cq5dam.web.450.213.jpg");
    assertTrue(lastModifiedAfter2ndUpdate.after(lastModifiedAfterUpdate));
  }

  @Test
  void testUpdateRendition_Video_NoMetadata() {
    underTest = context.registerInjectActivateService(new RenditionMetadataListenerService(),
        "threadPoolSize", 0);

    // simulate rendition update on video rendition
    String assetPath = MediaSourceDamAppAemContext.DAM_PATH + "/movie.wmf";
    String renditionPath = assetPath + "/jcr:content/renditions/cq5dam.video.hq.m4v";
    assertNotNull(context.resourceResolver().getResource(renditionPath));
    underTest.handleEvent(DamEvent.renditionUpdated(assetPath, null, renditionPath).toEvent());

    // ensure metadata was not generated
    String metadataPath = assetPath + "/jcr:content/" + NN_RENDITIONS_METADATA + "/cq5dam.video.hq.m4v";
    assertNull(context.resourceResolver().getResource(metadataPath));
  }

  @Test
  void testRemoveRendition() throws PersistenceException {
    underTest = context.registerInjectActivateService(new RenditionMetadataListenerService(),
        "threadPoolSize", 0);

    // check existing metadata
    assertRenditionMetadata("cq5dam.web.450.213.jpg", 450, 213, false);

    // remove rendition
    removeRendition("cq5dam.web.450.213.jpg");

    // ensure metadata is no longer present
    assertNoRenditionMetadata("cq5dam.web.450.213.jpg");
  }

  @Test
  void testRemoveRendition_RenditionNotRemoved() {
    underTest = context.registerInjectActivateService(new RenditionMetadataListenerService(),
        "threadPoolSize", 0);

    // check existing metadata
    assertRenditionMetadata("cq5dam.web.450.213.jpg", 450, 213, false);

    // send remove event for rendition without actually removing the rendition
    sendRenditionRemovedEvent("cq5dam.web.450.213.jpg");

    // ensure metadata is still in place
    assertRenditionMetadata("cq5dam.web.450.213.jpg", 450, 213, false);
  }

  private void addRendition(String renditionName) {
    Resource rendition = context.load().binaryFile("/sample_image_215x102.jpg", RENDITIONS_PATH + "/" + renditionName);
    underTest.handleEvent(DamEvent.renditionUpdated(assetResource.getPath(), null, rendition.getPath()).toEvent());
  }

  @SuppressWarnings("null")
  private void updateRendition(String renditionName) throws PersistenceException {
    String renditionPath = RENDITIONS_PATH + "/" + renditionName;
    context.resourceResolver().delete(context.resourceResolver().getResource(renditionPath));
    context.load().binaryFile("/sample_image_215x102.jpg", renditionPath);
    sendRenditionUpdatedEvent(renditionName);
  }

  private void sendRenditionUpdatedEvent(String renditionName) {
    String renditionPath = RENDITIONS_PATH + "/" + renditionName;
    underTest.handleEvent(DamEvent.renditionUpdated(assetResource.getPath(), null, renditionPath).toEvent());
  }

  @SuppressWarnings("null")
  private void removeRendition(String renditionName) throws PersistenceException {
    String renditionPath = RENDITIONS_PATH + "/" + renditionName;
    context.resourceResolver().delete(context.resourceResolver().getResource(renditionPath));
    sendRenditionRemovedEvent(renditionName);
  }

  private void sendRenditionRemovedEvent(String renditionName) {
    String renditionPath = RENDITIONS_PATH + "/" + renditionName;
    underTest.handleEvent(DamEvent.renditionRemoved(assetResource.getPath(), null, renditionPath).toEvent());
  }

  @SuppressWarnings("null")
  private void assertRenditionMetadata(String renditionName, int width, int height, boolean withLastModified) {
    String path = RENDITIONS_METADATA_PATH + "/" + renditionName;
    Resource metadata = context.resourceResolver().getResource(path);
    assertNotNull(metadata);

    ValueMap props = metadata.getValueMap();
    assertEquals((Integer)width, props.get(PN_IMAGE_WIDTH, 0));
    assertEquals((Integer)height, props.get(PN_IMAGE_HEIGHT, 0));
    if (withLastModified) {
      assertNotNull(props.get(JCR_LASTMODIFIED, Calendar.class));
      assertNotNull(props.get(JCR_LAST_MODIFIED_BY, String.class));
    }
  }

  private void assertNoRenditionMetadata(String renditionName) {
    String path = RENDITIONS_METADATA_PATH + "/" + renditionName;
    Resource metadata = context.resourceResolver().getResource(path);
    assertNull(metadata);
  }

  private Calendar getRenditionMetadataLastModified(String renditionName) {
    String path = RENDITIONS_METADATA_PATH + "/" + renditionName;
    Resource metadata = context.resourceResolver().getResource(path);
    return ResourceUtil.getValueMap(metadata).get(JCR_LASTMODIFIED, Calendar.class);
  }

}
