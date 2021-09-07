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
package io.wcm.handler.url.testcontext;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;

import io.wcm.handler.url.integrator.IntegratorMode;
import io.wcm.handler.url.integrator.IntegratorModes;
import io.wcm.handler.url.spi.UrlHandlerConfig;

/**
 * Dummy link configuration
 */
@SuppressWarnings("null")
public class DummyUrlHandlerConfig extends UrlHandlerConfig {

  public static final int SITE_ROOT_LEVEL = 4;

  private static final List<IntegratorMode> INTEGRATOR_MODES = ImmutableList.<IntegratorMode>of(
      IntegratorModes.SIMPLE,
      IntegratorModes.EXTENDED
      );

  private boolean hostProvidedBySlingMapping = false;

  @Override
  public List<IntegratorMode> getIntegratorModes() {
    return INTEGRATOR_MODES;
  }

  @Override
  public boolean isSecure(Page page) {
    String templatePath = page.getProperties().get(NameConstants.PN_TEMPLATE, String.class);
    return StringUtils.equals(templatePath, DummyAppTemplate.CONTENT_SECURE.getTemplatePath());
  }

  @Override
  public boolean isIntegrator(Page page) {
    String templatePath = page.getProperties().get(NameConstants.PN_TEMPLATE, String.class);
    return StringUtils.equals(templatePath, DummyAppTemplate.INTEGRATOR.getTemplatePath());
  }

  @Override
  public int getSiteRootLevel(Resource contextResource) {
    return SITE_ROOT_LEVEL;
  }

  @Override
  public boolean isHostProvidedBySlingMapping() {
    return hostProvidedBySlingMapping;
  }

  public void setHostProvidedBySlingMapping(boolean hostProvidedBySlingMapping) {
    this.hostProvidedBySlingMapping = hostProvidedBySlingMapping;
  }

}
