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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ScriptTest {

  @Test
  public void testSimpleAttributes() throws Exception {
    Script script = new Script();
    assertEquals("script", script.getName());

    script.setType("type1");
    assertEquals("type1", script.getType());

    script.setSrc("src1");
    assertEquals("src1", script.getSrc());

    Script script2 = new Script("text1");
    assertTrue(script2.getText().contains("text1"));

  }

}
