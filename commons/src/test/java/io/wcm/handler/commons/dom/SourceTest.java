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
package io.wcm.handler.commons.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SourceTest {

  @Test
  void testSimpleAttributes() throws Exception {
    Source source = new Source();
    assertEquals("source", source.getName());

    source.setMedia("media1");
    source.setSrc("ref1");
    source.setType("type1");
    source.setSrcSet("srcset1");
    source.setSizes("sizes1");

    assertEquals("media1", source.getMedia());
    assertEquals("ref1", source.getSrc());
    assertEquals("type1", source.getType());
    assertEquals("srcset1", source.getSrcSet());
    assertEquals("sizes1", source.getSizes());
  }

}
