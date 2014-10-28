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
package io.wcm.handler.link;

import io.wcm.wcm.commons.util.ToStringStyle;

import java.util.HashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;

/**
 * Holds all properties that are part of a link handling request.
 */
@ProviderType
public final class LinkRequest {

  private final Resource resource;
  private final Page page;
  private final LinkArgs linkArgs;

  private ValueMap resourceProperties;

  /**
   * @param resource Resource containing properties that define the link target
   * @param page Target content page
   * @param linkArgs Link arguments
   */
  public LinkRequest(Resource resource, Page page, LinkArgs linkArgs) {
    this.resource = resource;
    this.page = page;
    this.linkArgs = linkArgs != null ? linkArgs : new LinkArgs();

    // validate parameters
    if (this.resource != null && this.page != null) {
      throw new IllegalArgumentException("Set resource or page, not both.");
    }
  }

  /**
   * @return Resource containing properties that define the link target
   */
  public Resource getResource() {
    return this.resource;
  }

  /**
   * @return Target content page
   */
  public Page getPage() {
    return this.page;
  }

  /**
   * @return Link arguments
   */
  public LinkArgs getLinkArgs() {
    return this.linkArgs;
  }

  /**
   * @return Properties from resource containing target link. The value map is a copy
   *         of the original map so it is safe to change the property values contained in the map.
   */
  public ValueMap getResourceProperties() {
    if (this.resourceProperties == null) {
      // create a copy of the original map
      this.resourceProperties = new ValueMapDecorator(new HashMap<String, Object>());
      if (this.resource != null) {
        this.resourceProperties.putAll(resource.getValueMap());
      }
    }
    return this.resourceProperties;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_OMIT_NULL_STYLE);
  }

}
