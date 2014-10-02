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

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

/**
 * Synthetic resource for building links via {@link LinkHandler}.
 * Use properties with names provided by {@link LinkNameConstants}.
 */
public final class SyntheticLinkResource extends SyntheticResource {

  private static final String PATH = "/apps/wcm-io/handler/link/synthetic/resource";

  private final ValueMap properties;

  /**
   * Instantiate resource with static path/resource type
   * @param resourceResolver Resource resolver
   */
  public SyntheticLinkResource(ResourceResolver resourceResolver) {
    this(resourceResolver, new HashMap<String, Object>());
  }

  /**
   * Instantiate resource with static path/resource type
   * @param resourceResolver Resource resolver
   * @param properties Properties for resource
   */
  public SyntheticLinkResource(ResourceResolver resourceResolver, Map<String, Object> properties) {
    super(resourceResolver, PATH, PATH);
    this.properties = new ValueMapDecorator(properties);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <Type> Type adaptTo(Class<Type> type) {
    if (type == ValueMap.class) {
      return (Type)this.properties;
    }
    else {
      return super.adaptTo(type);
    }
  }

}
