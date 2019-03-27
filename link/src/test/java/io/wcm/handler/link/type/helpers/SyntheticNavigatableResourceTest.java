/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.handler.link.type.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class SyntheticNavigatableResourceTest {

  private final AemContext context = new AemContext();

  @Test
  @SuppressWarnings("null")
  void testGet() {
    context.create().resource("/content");

    Resource test2 = SyntheticNavigatableResource.get("/content/test1/test2", context.resourceResolver());
    assertNotNull(test2);
    assertEquals("/content/test1/test2", test2.getPath());
    assertTrue(test2 instanceof SyntheticNavigatableResource);

    Resource test1 = test2.getParent();
    assertNotNull(test1);
    assertEquals("/content/test1", test1.getPath());
    assertTrue(test1 instanceof SyntheticNavigatableResource);

    Resource content = test1.getParent();
    assertNotNull(content);
    assertEquals("/content", content.getPath());
    assertFalse(content instanceof SyntheticNavigatableResource);
  }

  @Test
  void testNullPath() {
    Resource test2 = SyntheticNavigatableResource.get(null, context.resourceResolver());
    assertNotNull(test2);
    assertNull(test2.getPath());
    assertNull(test2.getParent());
  }

}
