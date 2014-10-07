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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AnchorTest {

  @Test
  public void testAnchor() {
    Anchor anchor1 = new Anchor("href1");
    assertEquals("href1", anchor1.getHRef());

    Anchor anchor2 = new Anchor("href2", "target1");
    assertEquals("href2", anchor2.getHRef());
    assertEquals("target1", anchor2.getTarget());
  }

  @Test
  public void testSimpleAttributes() throws Exception {
    Anchor anchor = new Anchor("href");
    assertEquals("a", anchor.getName());

    anchor.setRel("rel1");
    assertEquals("rel1", anchor.getRel());

    anchor.setHRef("ref1");
    assertEquals("ref1", anchor.getHRef());

    anchor.setTarget("target1");
    assertEquals("target1", anchor.getTarget());

    anchor.setTabIndex(5);
    assertEquals(5, anchor.getTabIndex());

    anchor.setAccessKey("key1");
    assertEquals("key1", anchor.getAccessKey());
  }

}
