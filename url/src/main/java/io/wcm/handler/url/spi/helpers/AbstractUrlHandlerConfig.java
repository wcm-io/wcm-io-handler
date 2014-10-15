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
package io.wcm.handler.url.spi.helpers;

import io.wcm.handler.url.UrlMode;
import io.wcm.handler.url.UrlModes;
import io.wcm.handler.url.integrator.IntegratorMode;
import io.wcm.handler.url.spi.UrlHandlerConfig;

import java.util.List;

import org.osgi.annotation.versioning.ConsumerType;

import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;

/**
 * Default implementation of configuration options of {@link UrlHandlerConfig} interface.
 * Subclasses may decide to override only some of the methods.
 */
@ConsumerType
public abstract class AbstractUrlHandlerConfig implements UrlHandlerConfig {

  @Override
  public int getSiteRootLevel(String contextPath) {
    // not supported by default
    return 0;
  }

  @Override
  public boolean isSecure(Page page) {
    // not supported by default
    return false;
  }

  @Override
  public boolean isIntegrator(Page page) {
    // not supported by default
    return false;
  }

  @Override
  public UrlMode getDefaultUrlMode() {
    return UrlModes.DEFAULT;
  }

  @Override
  public List<IntegratorMode> getIntegratorModes() {
    // not supported by default
    return ImmutableList.of();
  }

}
