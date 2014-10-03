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

import io.wcm.config.spi.ApplicationProvider;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;

/**
 * Register application in configuration API.
 */
@Component(immediate = true)
@Service(ApplicationProvider.class)
public class ApplicationProviderImpl implements ApplicationProvider {

  /**
   * Application Id
   */
  public static final String APPLICATION_ID = "/apps/wcm-io/handler/url";

  /**
   * Application label
   */
  public static final String APPLICATION_LABEL = "Url Handling";


  @Override
  public String getApplicationId() {
    return APPLICATION_ID;
  }

  @Override
  public String getLabel() {
    return APPLICATION_LABEL;
  }

  @Override
  public boolean matches(Resource resource) {
    return false;
  }

}
