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
package io.wcm.handler.commons.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AreaTest {

  @Test
  void testSimpleAttributes() throws Exception {
    Area area = new Area();
    assertEquals("area", area.getName());

    area.setRel("rel1");
    assertEquals("rel1", area.getRel());

    area.setHRef("ref1");
    assertEquals("ref1", area.getHRef());

    area.setTarget("target1");
    assertEquals("target1", area.getTarget());

    area.setTabIndex(5);
    assertEquals(5, area.getTabIndex());

    area.setAccessKey("key1");
    assertEquals("key1", area.getAccessKey());

    area.setAlt("alt1");
    assertEquals("alt1", area.getAlt());

    area.setShape("shape1");
    assertEquals("shape1", area.getShape());

    area.setCoords("coords1");
    assertEquals("coords1", area.getCoords());
  }

}
