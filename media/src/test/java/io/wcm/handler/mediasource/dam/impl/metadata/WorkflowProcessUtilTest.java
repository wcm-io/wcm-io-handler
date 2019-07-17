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

import static com.day.cq.dam.api.DamConstants.NT_DAM_ASSET;
import static com.day.cq.wcm.api.NameConstants.NT_PAGE;
import static io.wcm.handler.mediasource.dam.impl.metadata.WorkflowTestUtil.PKG_ROOT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.adobe.granite.workflow.PayloadMap;
import com.adobe.granite.workflow.collection.ResourceCollectionManager;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;

import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowProcessUtilTest {

  private final AemContext context = AppAemContext.newAemContext(ResourceResolverType.JCR_MOCK);

  private Page samplePage1;
  private Page samplePage2;
  private Asset sampleAsset;

  @Mock
  private WorkItem workItem;
  @Mock
  private WorkflowData workflowData;

  @BeforeEach
  void setUp() throws Exception {
    samplePage1 = context.create().page("/content/mysite/page1");
    samplePage2 = context.create().page("/content/mysite/page2");
    sampleAsset = context.create().asset("/content/dam/asset1.jpg", 10, 10, "image/jpeg");

    when(workItem.getWorkflowData()).thenReturn(workflowData);
  }

  @Test
  void testGetPayloadResourcePath_JcrPath() {
    when(workflowData.getPayloadType()).thenReturn(PayloadMap.TYPE_JCR_PATH);
    when(workflowData.getPayload()).thenReturn("/my/path");
    assertEquals("/my/path", WorkflowProcessUtil.getPayloadResourcePath(workItem));
  }

  @Test
  void testGetPayloadResourcePath_OtherType() {
    when(workflowData.getPayloadType()).thenReturn("other_type");
    assertNull(WorkflowProcessUtil.getPayloadResourcePath(workItem));
  }

  @Test
  void testGetPayloadResourcePaths_InvalidPayloadPath() {
    assertPaths("/invalid/path", NT_PAGE);
  }

  @Test
  void testGetPayloadResourcePaths_DirectPayloadPath_Page() {
    assertPaths(samplePage1.getPath(), NT_PAGE,
        samplePage1.getPath());
  }

  @Test
  void testGetPayloadResourcePaths_DirectPayloadPath_Asset() {
    assertPaths(sampleAsset.getPath(), NT_DAM_ASSET,
        sampleAsset.getPath());
  }

  @Test
  void testGetPayloadResourcePaths_WorkflowPackage() {
    Resource pkg1 = WorkflowTestUtil.createPackage(context, PKG_ROOT + "/pkg1",
        samplePage1.getPath(),
        sampleAsset.getPath(),
        samplePage2.getPath());

    assertPaths(pkg1.getPath(), NT_PAGE,
        samplePage1.getPath(),
        samplePage2.getPath());
    assertPaths(pkg1.getPath(), NT_DAM_ASSET,
        sampleAsset.getPath());
  }

  @Test
  void testGetPayloadResourcePaths_AnyOtherResource() {
    Resource anyResource = context.create().resource(PKG_ROOT + "/pkg1");
    context.create().resource(anyResource, "child1");
    context.create().resource(anyResource, "child2");

    assertPaths(PKG_ROOT + "/pkg1", NT_PAGE,
        anyResource.getPath());
    assertPaths(PKG_ROOT + "/pkg1", NT_DAM_ASSET,
        anyResource.getPath());
  }

  @SuppressWarnings("null")
  private void assertPaths(String payloadPaths, String primaryTypeResourceType, String... expectedPaths) {
    List<String> expected = ImmutableList.copyOf(expectedPaths);
    List<String> result = WorkflowProcessUtil.getPayloadResourcePaths(payloadPaths, primaryTypeResourceType,
        context.resourceResolver(), context.getService(ResourceCollectionManager.class));
    assertEquals(expected, result);
  }

}
