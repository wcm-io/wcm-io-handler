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
package io.wcm.handler.commons.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.day.cq.commons.jcr.JcrConstants;
import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class JcrBinaryTest {

  private static final String MIMETYPE_GIF = "image/gif";

  @Mock
  private Resource resource;
  @Mock
  private Resource subResource;

  @Test
  public void testIsNtFileResource_Resource() {
    when(resource.getResourceType()).thenReturn(JcrConstants.NT_RESOURCE);
    assertFalse(JcrBinary.isNtFile(resource));
    assertTrue(JcrBinary.isNtResource(resource));
    assertTrue(JcrBinary.isNtFileOrResource(resource));
  }

  @Test
  public void testIsNtFileResource_File() {
    when(resource.getResourceType()).thenReturn(JcrConstants.NT_FILE);
    assertTrue(JcrBinary.isNtFile(resource));
    assertFalse(JcrBinary.isNtResource(resource));
    assertTrue(JcrBinary.isNtFileOrResource(resource));
  }


  @Test
  public void testIsNtFileResource_Other() {
    when(resource.getResourceType()).thenReturn(null);
    assertFalse(JcrBinary.isNtFile(resource));
    assertFalse(JcrBinary.isNtResource(resource));
    assertFalse(JcrBinary.isNtFileOrResource(resource));
  }

  @Test
  public void testGetMimeType_Resource() {
    when(resource.getResourceType()).thenReturn(JcrConstants.NT_RESOURCE);
    when(resource.getValueMap()).thenReturn(new ValueMapDecorator(ImmutableMap.<String, Object>builder()
        .put(JcrConstants.JCR_MIMETYPE, MIMETYPE_GIF).build()));

    assertEquals(MIMETYPE_GIF, JcrBinary.getMimeType(resource));
  }

  @Test
  public void testGetMimeType_Resource_NoMimeType() {
    when(resource.getResourceType()).thenReturn(JcrConstants.NT_RESOURCE);
    when(resource.getValueMap()).thenReturn(ValueMap.EMPTY);

    assertNull(JcrBinary.getMimeType(resource));
  }

  @Test
  public void testGetMimeType_File() {
    when(resource.getResourceType()).thenReturn(JcrConstants.NT_FILE);
    when(resource.getChild(JcrConstants.JCR_CONTENT)).thenReturn(subResource);
    when(subResource.getResourceType()).thenReturn(JcrConstants.NT_RESOURCE);
    when(subResource.getValueMap()).thenReturn(new ValueMapDecorator(ImmutableMap.<String, Object>builder()
        .put(JcrConstants.JCR_MIMETYPE, MIMETYPE_GIF).build()));

    assertEquals(MIMETYPE_GIF, JcrBinary.getMimeType(resource));
  }

  @Test
  public void testGetMimeType_File_NoContent() {
    when(resource.getResourceType()).thenReturn(JcrConstants.NT_FILE);
    when(resource.getChild(JcrConstants.JCR_CONTENT)).thenReturn(null);

    assertNull(JcrBinary.getMimeType(resource));
  }

  @Test
  public void testGetMimeType_Other() {
    when(resource.getResourceType()).thenReturn("otherType");

    assertNull(JcrBinary.getMimeType(resource));
  }

}
