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
package io.wcm.handler.url.impl.modes;

import io.wcm.handler.url.UrlMode;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;

import com.day.cq.wcm.api.Page;

abstract class AbstractUrlMode implements UrlMode {

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj.getClass() != getClass()) {
      return false;
    }
    return getId().equals(((UrlMode)obj).getId());
  }

  @Override
  public String toString() {
    return getId();
  }

  /**
   * Get URL configuration for target page. If this is invalid or not available, get it from adaptable.
   * @param adaptable Adaptable (request or resource)
   * @param targetPage Target page (may be null)
   * @return Url config (never null)
   */
  protected UrlConfig getUrlConfigForTarget(Adaptable adaptable, Page targetPage) {
    Resource targetResource = null;
    if (targetPage != null) {
      targetResource = targetPage.adaptTo(Resource.class);
    }
    return getUrlConfigForTarget(adaptable, targetResource);
  }

  /**
   * Get URL configuration for target resource. If this is invalid or not available, get it from adaptable.
   * @param adaptable Adaptable (request or resource)
   * @param targetResource Target resource (may be null)
   * @return Url config (never null)
   */
  protected UrlConfig getUrlConfigForTarget(Adaptable adaptable, Resource targetResource) {
    UrlConfig config = null;
    if (targetResource != null) {
      config = new UrlConfig(targetResource);
    }
    if (config == null || !config.isValid()) {
      config = new UrlConfig(adaptable);
    }
    return config;
  }

}
