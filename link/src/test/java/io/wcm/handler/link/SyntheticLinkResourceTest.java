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
package io.wcm.handler.link;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import io.wcm.sling.commons.resource.ImmutableValueMap;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.day.cq.wcm.api.Page;

@RunWith(MockitoJUnitRunner.class)
public class SyntheticLinkResourceTest {

  @Mock
  private ResourceResolver resourceResolver;

  @Test
  public void testSimpleConstructor() {
    Resource underTest = new SyntheticLinkResource(resourceResolver);
    ValueMap props = underTest.getValueMap();
    assertTrue(props.isEmpty());
  }

  @Test
  public void testWithMap() {
    ValueMap givenProps = ImmutableValueMap.of("prop1", "value1");
    Resource underTest = new SyntheticLinkResource(resourceResolver, givenProps);
    ValueMap props = underTest.getValueMap();
    assertEquals(givenProps, ImmutableValueMap.copyOf(props));
  }

  @Test
  public void testAdaptTo() {
    Resource underTest = new SyntheticLinkResource(resourceResolver);
    Page page = underTest.adaptTo(Page.class);
    assertNull(page);
  }

}
