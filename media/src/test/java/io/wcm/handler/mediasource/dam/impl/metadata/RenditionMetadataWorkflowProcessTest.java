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

import static com.day.cq.commons.jcr.JcrConstants.JCR_LASTMODIFIED;
import static com.day.cq.commons.jcr.JcrConstants.JCR_LAST_MODIFIED_BY;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.NN_RENDITIONS_METADATA;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.PN_IMAGE_HEIGHT;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.PN_IMAGE_WIDTH;
import static io.wcm.handler.mediasource.dam.impl.metadata.WorkflowTestUtil.PKG_ROOT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.Calendar;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adobe.granite.workflow.PayloadMap;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
public class RenditionMetadataWorkflowProcessTest {

  private final AemContext context = AppAemContext.newAemContext(ResourceResolverType.JCR_MOCK);

  @Mock
  private WorkItem workItem;
  @Mock
  private WorkflowData workflowData;
  @Mock
  private WorkflowSession workflowSession;
  @Mock
  private MetaDataMap metaDataMap;

  private RenditionMetadataWorkflowProcess underTest;

  @BeforeEach
  @SuppressWarnings("null")
  void setUp() throws Exception {
    when(workItem.getWorkflowData()).thenReturn(workflowData);
    when(workflowData.getPayloadType()).thenReturn(PayloadMap.TYPE_JCR_PATH);
    when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(context.resourceResolver());

    context.registerInjectActivateService(new AssetSynchonizationService());
    underTest = context.registerInjectActivateService(new RenditionMetadataWorkflowProcess());
  }

  @Test
  void testWithInvalidPayload() {
    when(workflowData.getPayload()).thenReturn("/invalid/path");

    // execute, ensure no exceptions are thrown
    underTest.execute(workItem, workflowSession, metaDataMap);
  }

  @Test
  void testWithAssetPayload() {
    Asset asset = context.create().asset("/content/dam/asset1.jpg", 10, 10, "image/jpeg");
    context.create().assetRendition(asset, "rendition1.jpg", 12, 12, "image/jpg");
    context.create().assetRendition(asset, "rendition2.png", 10, 5, "image/png");
    context.create().assetRendition(asset, "rendition3.txt", "/sample.txt", "text/plain");
    when(workflowData.getPayload()).thenReturn(asset.getPath());

    assertNoRenditionMetadata(asset, "rendition1.jpg");
    assertNoRenditionMetadata(asset, "rendition2.png");
    assertNoRenditionMetadata(asset, "rendition3.txt");

    underTest.execute(workItem, workflowSession, metaDataMap);

    assertRenditionMetadata(asset, "rendition1.jpg", 12, 12);
    assertRenditionMetadata(asset, "rendition2.png", 10, 5);
    // no metadata expected for non-image rendition
    assertNoRenditionMetadata(asset, "rendition3.txt");
  }

  @Test
  void testWithAssetOriginalRenditionPayload() {
    Asset asset = context.create().asset("/content/dam/asset1.jpg", 10, 10, "image/jpeg");
    context.create().assetRendition(asset, "rendition1.jpg", 12, 12, "image/jpg");
    when(workflowData.getPayload()).thenReturn(asset.getOriginal().getPath());

    assertNoRenditionMetadata(asset, "rendition1.jpg");

    underTest.execute(workItem, workflowSession, metaDataMap);

    assertRenditionMetadata(asset, "rendition1.jpg", 12, 12);
  }

  @Test
  void testWithWorkflowPackagePayload() {
    Asset asset1 = context.create().asset("/content/dam/asset1.jpg", 10, 10, "image/jpeg");
    context.create().assetRendition(asset1, "rendition1.jpg", 12, 12, "image/jpg");

    Asset asset2 = context.create().asset("/content/dam/asset2.jpg", 10, 10, "image/jpeg");
    context.create().assetRendition(asset2, "rendition1.jpg", 5, 5, "image/jpg");

    Resource pkg1 = WorkflowTestUtil.createPackage(context, PKG_ROOT + "/pkg1",
        asset1.getPath(),
        asset2.getPath());

    when(workflowData.getPayload()).thenReturn(pkg1.getPath());

    assertNoRenditionMetadata(asset1, "rendition1.jpg");
    assertNoRenditionMetadata(asset2, "rendition1.jpg");

    underTest.execute(workItem, workflowSession, metaDataMap);

    assertRenditionMetadata(asset1, "rendition1.jpg", 12, 12);
    assertRenditionMetadata(asset2, "rendition1.jpg", 5, 5);
  }

  @Test
  void testRemovalOfObsoleteRenditionMetadata() {
    Asset asset = context.create().asset("/content/dam/asset1.jpg", 10, 10, "image/jpeg");
    context.create().assetRendition(asset, "rendition1.jpg", 12, 12, "image/jpg");
    when(workflowData.getPayload()).thenReturn(asset.getPath());

    // create some obsolete rendition metadata with no rendition attached
    String obsoletePath = asset.getPath() + "/jcr:content/" + NN_RENDITIONS_METADATA + "/obsolete.jpg";
    context.create().resource(obsoletePath,
        PN_IMAGE_WIDTH, 20,
        PN_IMAGE_HEIGHT, 10,
        JCR_LASTMODIFIED, Calendar.getInstance(),
        JCR_LAST_MODIFIED_BY, "dummy");

    assertNoRenditionMetadata(asset, "rendition1.jpg");
    assertRenditionMetadata(asset, "obsolete.jpg", 20, 10);

    underTest.execute(workItem, workflowSession, metaDataMap);

    assertRenditionMetadata(asset, "rendition1.jpg", 12, 12);
    assertNoRenditionMetadata(asset, "obsolete.jpg");
  }

  @SuppressWarnings("null")
  private void assertRenditionMetadata(Asset asset, String renditionName, int width, int height) {
    String renditionPath = asset.getPath() + "/jcr:content/" + NN_RENDITIONS_METADATA + "/" + renditionName;
    Resource metadata = context.resourceResolver().getResource(renditionPath);
    assertNotNull(metadata);

    ValueMap props = metadata.getValueMap();
    assertEquals((Integer)width, props.get(PN_IMAGE_WIDTH, 0));
    assertEquals((Integer)height, props.get(PN_IMAGE_HEIGHT, 0));
    assertNotNull(props.get(JCR_LASTMODIFIED, Calendar.class));
    assertNotNull(props.get(JCR_LAST_MODIFIED_BY, String.class));
  }

  private void assertNoRenditionMetadata(Asset asset, String renditionName) {
    String renditionPath = asset.getPath() + "/jcr:content/" + NN_RENDITIONS_METADATA + "/" + renditionName;
    Resource metadata = context.resourceResolver().getResource(renditionPath);
    assertNull(metadata);
  }

}
