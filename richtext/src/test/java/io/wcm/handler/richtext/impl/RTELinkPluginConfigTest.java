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
package io.wcm.handler.richtext.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;

import io.wcm.handler.richtext.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class RTELinkPluginConfigTest {

  private final AemContext context = AppAemContext.newAemContext();

  private RTELinkPluginConfig underTest;

  @BeforeEach
  void setUp() {
    underTest = context.registerInjectActivateService(new RTELinkPluginConfig());
  }

  @Test
  void testConfig() throws Exception {
    underTest.doGet(context.request(), context.response());

    String expectedJson = "{linkTypes: {"
        + "internal: {value:'internal',text:'Internal (same site)'},"
        + "external: {value:'external',text:'External'},"
        + "media:{value:'media',text:'Asset'}},"
        + "rootPaths:{"
        + "internal:'/content/unittest/de_test/brand/de',"
        + "media:'/content/dam'}}";

    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

}
