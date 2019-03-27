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
package io.wcm.handler.media.format.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.media.testcontext.DummyMediaFormats;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class DefaultMediaFormatListProviderTest {

  private final AemContext context = AppAemContext.newAemContext();

  private DefaultMediaFormatListProvider underTest;

  @BeforeEach
  void setUp() {
    underTest = new DefaultMediaFormatListProvider();
  }

  @Test
  void testGet() throws Exception {
    underTest.service(context.request(), context.response());

    String response = context.response().getOutputAsString();
    assertTrue(StringUtils.contains(response, "\"" + DummyMediaFormats.EDITORIAL_1COL.getName() + "\""));
    assertTrue(StringUtils.contains(response, "\"" + DummyMediaFormats.EDITORIAL_2COL.getName() + "\""));
    assertTrue(StringUtils.contains(response, "\"" + DummyMediaFormats.EDITORIAL_3COL.getName() + "\""));
  }

}
