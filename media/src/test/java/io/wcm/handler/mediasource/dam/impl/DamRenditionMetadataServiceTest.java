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

import static io.wcm.handler.mediasource.dam.impl.DamRenditionMetadataService.NN_RENDITIONS_METADATA;
import static io.wcm.handler.mediasource.dam.impl.DamRenditionMetadataService.PN_IMAGE_HEIGHT;
import static io.wcm.handler.mediasource.dam.impl.DamRenditionMetadataService.PN_IMAGE_WIDTH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import io.wcm.handler.media.testcontext.MediaSourceDamAppAemContext;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.commons.util.RunMode;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.dam.api.DamEvent;

public class DamRenditionMetadataServiceTest {

  private static final String ASSET_PATH = MediaSourceDamAppAemContext.DAM_PATH + "/standard.jpg";
  private static final String RENDITIONS_PATH = ASSET_PATH + "/jcr:content/renditions";
  private static final String RENDITIONS_METADATA_PATH = ASSET_PATH + "/jcr:content/" + NN_RENDITIONS_METADATA;

  @Rule
  public AemContext context = MediaSourceDamAppAemContext.newAemContext(ResourceResolverType.JCR_MOCK);

  private DamRenditionMetadataService underTest;
  private Resource assetResource;

  @Before
  public void setUp() {
    context.load().json("/mediasource/dam/damcontent-sample.json", MediaSourceDamAppAemContext.DAM_PATH);
    assetResource = context.resourceResolver().getResource(ASSET_PATH);
    assertNotNull(assetResource);

    context.runMode(RunMode.AUTHOR);
  }

  @Test
  public void testAddRendition_Metadata() {
    underTest = context.registerInjectActivateService(new DamRenditionMetadataService());
    addRendition("test.jpg");
    assertRenditionMetadata("test.jpg", 215, 102);
  }

  @Test
  public void testAddRendition_Metadata_createMetadataNode() throws PersistenceException {
    underTest = context.registerInjectActivateService(new DamRenditionMetadataService());

    // remove all existing renditions metadata incl. renditionsMetadata node
    Resource metadata = context.resourceResolver().getResource(RENDITIONS_METADATA_PATH);
    context.resourceResolver().delete(metadata);

    assertNoRenditionMetadata("test.jpg");
    addRendition("test.jpg");
    assertRenditionMetadata("test.jpg", 215, 102);
  }

  @Test
  public void testAddRendition_PublishInstance_NoMetadata() {
    context.runMode(RunMode.PUBLISH);
    underTest = context.registerInjectActivateService(new DamRenditionMetadataService());
    addRendition("test.jpg");
    assertNoRenditionMetadata("test.jpg");
  }

  @Test
  public void testAddRendition_Disabled_NoMetadata() {
    underTest = context.registerInjectActivateService(new DamRenditionMetadataService(),
        ImmutableValueMap.of(DamRenditionMetadataService.PROPERTY_ENABLED, false));
    addRendition("test.jpg");
    assertNoRenditionMetadata("test.jpg");
  }

  @Test
  public void testUpdateRendition() throws PersistenceException {
    underTest = context.registerInjectActivateService(new DamRenditionMetadataService());

    // check existing metadata
    assertRenditionMetadata("cq5dam.web.450.213.jpg", 450, 213);

    // replace with new rendition with different dimensions
    updateRendition("cq5dam.web.450.213.jpg");

    // ensure metadata was generated
    assertRenditionMetadata("cq5dam.web.450.213.jpg", 215, 102);
  }

  @Test
  public void testUpdateRendition_Video_NoMetadata() {
    underTest = context.registerInjectActivateService(new DamRenditionMetadataService());

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
  public void testRemoveRendition() throws PersistenceException {
    underTest = context.registerInjectActivateService(new DamRenditionMetadataService());

    // check existing metadata
    assertRenditionMetadata("cq5dam.web.450.213.jpg", 450, 213);

    // simulate rendition removal
    removeRendition("cq5dam.web.450.213.jpg");

    // ensure metadata was generated
    assertNoRenditionMetadata("cq5dam.web.450.213.jpg");
  }

  private void addRendition(String renditionName) {
    Resource rendition = context.load().binaryFile("/sample_image_215x102.jpg", RENDITIONS_PATH + "/" + renditionName);
    underTest.handleEvent(DamEvent.renditionUpdated(assetResource.getPath(), null, rendition.getPath()).toEvent());
  }

  private void updateRendition(String renditionName) throws PersistenceException {
    String existingPath = RENDITIONS_PATH + "/" + renditionName;
    context.resourceResolver().delete(context.resourceResolver().getResource(existingPath));
    context.load().binaryFile("/sample_image_215x102.jpg", existingPath);
    underTest.handleEvent(DamEvent.renditionUpdated(assetResource.getPath(), null, existingPath).toEvent());
  }

  private void removeRendition(String renditionName) throws PersistenceException {
    String existingPath = RENDITIONS_PATH + "/" + renditionName;
    context.resourceResolver().delete(context.resourceResolver().getResource(existingPath));
    underTest.handleEvent(DamEvent.renditionRemoved(assetResource.getPath(), null, existingPath).toEvent());
  }

  private void assertRenditionMetadata(String renditionName, int width, int height) {
    underTest = context.registerInjectActivateService(new DamRenditionMetadataService());

    String path = RENDITIONS_METADATA_PATH + "/" + renditionName;
    Resource metadata = context.resourceResolver().getResource(path);
    assertNotNull(metadata);

    ValueMap props = metadata.getValueMap();
    assertEquals((Integer)width, props.get(PN_IMAGE_WIDTH, 0));
    assertEquals((Integer)height, props.get(PN_IMAGE_HEIGHT, 0));
  }

  private void assertNoRenditionMetadata(String renditionName) {
    String path = RENDITIONS_METADATA_PATH + renditionName;
    Resource metadata = context.resourceResolver().getResource(path);
    assertNull(metadata);
  }

}
