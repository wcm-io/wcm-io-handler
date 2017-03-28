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
package io.wcm.handler.commons.spisupport.impl;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.Constants;

import io.wcm.handler.commons.spisupport.SpiResolver;
import io.wcm.testing.mock.aem.junit.AemContext;

public class SpiResolverImplTest {

  @Rule
  public AemContext context = new AemContext();

  private DummySpi defaultImpl;
  private DummySpi contentImpl;
  private DummySpi contentDamImpl;
  private DummySpi contentSampleImpl;

  private SpiResolver underTest;

  @Before
  public void setUp() {
    contentImpl = context.registerService(DummySpi.class, new DummySpiImpl("^/content(/.*)?$"),
        Constants.SERVICE_RANKING, 100);
    contentDamImpl = context.registerService(DummySpi.class, new DummySpiImpl("^/content/dam(/.*)?$"),
        Constants.SERVICE_RANKING, 200);
    contentSampleImpl = context.registerService(DummySpi.class, new DummySpiImpl("^/content/sample(/.*)?$"),
        Constants.SERVICE_RANKING, 300);

    underTest = context.registerInjectActivateService(new SpiResolverImpl());
  }

  @Test
  public void testWithDefaultImpl() {
    defaultImpl = context.registerService(DummySpi.class, new DummyDefaultSpiImpl(),
        Constants.SERVICE_RANKING, Integer.MIN_VALUE);

    assertSame(contentImpl, underTest.resolve(DummySpi.class, context.create().resource("/content/test1")));
    assertSame(contentSampleImpl, underTest.resolve(DummySpi.class, context.create().resource("/content/sample/test1")));
    assertSame(contentDamImpl, underTest.resolve(DummySpi.class, context.create().resource("/content/dam/test1")));
    assertSame(defaultImpl, underTest.resolve(DummySpi.class, context.create().resource("/etc/test1")));
  }

  @Test
  public void testWithoutDefaultImpl() {
    assertSame(contentImpl, underTest.resolve(DummySpi.class, context.create().resource("/content/test1")));
    assertSame(contentSampleImpl, underTest.resolve(DummySpi.class, context.create().resource("/content/sample/test1")));
    assertSame(contentDamImpl, underTest.resolve(DummySpi.class, context.create().resource("/content/dam/test1")));
    assertNull(underTest.resolve(DummySpi.class, context.create().resource("/etc/test1")));
  }

  @Test
  public void testWithSlingHttpServletRequest() {
    context.currentResource(context.create().resource("/content/test1"));
    assertSame(contentImpl, underTest.resolve(DummySpi.class, context.request()));
  }

  @Test
  public void testWithNull() {
    assertSame(defaultImpl, underTest.resolve(DummySpi.class, null));
  }

}
