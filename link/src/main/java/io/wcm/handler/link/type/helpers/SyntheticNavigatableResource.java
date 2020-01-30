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
package io.wcm.handler.link.type.helpers;

import org.apache.sling.api.resource.AbstractResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Synthetic resource implementation which support navigating it's parents until it reaches an existing resource.
 */
final class SyntheticNavigatableResource extends AbstractResource {

  private final String path;
  private final ResourceResolver resolver;
  private ResourceMetadata metadata;

  private static final String RESOURCE_TYPE = "wcm-io/handler/link/synthetic/resource/navigatable";

  private SyntheticNavigatableResource(String path, ResourceResolver resolver) {
    this.path = path;
    this.resolver = resolver;
  }

  @Override
  public @NotNull String getPath() {
    return path;
  }

  @Override
  public @NotNull String getResourceType() {
    return RESOURCE_TYPE;
  }

  @Override
  public String getResourceSuperType() {
    return null;
  }

  @Override
  public @NotNull ResourceMetadata getResourceMetadata() {
    if (metadata == null) {
      metadata = new ResourceMetadata();
      metadata.setResolutionPath(path);
    }
    return metadata;
  }

  @Override
  public @NotNull ResourceResolver getResourceResolver() {
    return resolver;
  }

  @Override
  public Resource getParent() {
    if (path == null) {
      return null;
    }
    String parentPath = ResourceUtil.getParent(path);
    if (parentPath == null) {
      return null;
    }
    return SyntheticNavigatableResource.get(parentPath, resolver);
  }

  /**
   * Get resource for path. If the path does not exist a synthetic resource is created which supports
   * navigation over it's parents until it reaches a resource that exists.
   * @param path Path
   * @return Resource (never null)
   */
  static @NotNull Resource get(String path, ResourceResolver resolver) {
    Resource resource = resolver.getResource(path);
    if (resource != null) {
      return resource;
    }
    return new SyntheticNavigatableResource(path, resolver);
  }

}
