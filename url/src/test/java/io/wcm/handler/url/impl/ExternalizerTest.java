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
package io.wcm.handler.url.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import io.wcm.handler.url.integrator.IntegratorPlaceholder;

import org.junit.Test;

public class ExternalizerTest {

  @Test
  public void testIsExternalized() throws Exception {
    assertFalse(Externalizer.isExternalized("/absolute/path"));
    assertFalse(Externalizer.isExternalized("relative/path"));
    assertTrue(Externalizer.isExternalized("http://www.heise.de/path1"));
    assertTrue(Externalizer.isExternalized("https://www.heise.de/path1"));
    assertTrue(Externalizer.isExternalized("mailto:info@jodelkaiser.de"));
    assertTrue(Externalizer.isExternalized("//www.heise.de"));
    assertTrue(Externalizer.isExternalized("ftp://ftp.heise.de"));
    assertTrue(Externalizer.isExternalized(IntegratorPlaceholder.URL_CONTENT + "/path1"));
  }

  @Test
  public void testMangleNamespaces() throws Exception {
    assertEquals("/content/aa/bb/content.png", Externalizer.mangleNamespaces("/content/aa/bb/content.png"));
    assertEquals("/content/aa/bb/_jcr_content.png", Externalizer.mangleNamespaces("/content/aa/bb/jcr:content.png"));
  }

}
