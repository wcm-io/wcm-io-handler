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
package io.wcm.handler.media.impl.ipeconfig;

import org.apache.sling.api.resource.AbstractResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class OverlayResourceWrapper extends AbstractResource {

  private final ResourceResolver resolver;
  private final Resource overlayedResource;
  private final String path;
  private final ResourceMetadata resourceMetadata;

  OverlayResourceWrapper(@NotNull Resource resource, String path) {
    this.resolver = resource.getResourceResolver();
    this.overlayedResource = resource;
    this.path = path;
    this.resourceMetadata = buildMetadata(path);
  }

  private static ResourceMetadata buildMetadata(String path) {
    ResourceMetadata metadata = new ResourceMetadata();
    metadata.setResolutionPath(path);
    return metadata;
  }

  @Override
  public @NotNull String getPath() {
    return path;
  }

  @Override
  public @NotNull ResourceMetadata getResourceMetadata() {
    return resourceMetadata;
  }

  @Override
  public @NotNull ResourceResolver getResourceResolver() {
    return this.resolver;
  }

  @Override
  public @NotNull String getResourceType() {
    return overlayedResource.getResourceType();
  }

  @Override
  public @Nullable String getResourceSuperType() {
    return overlayedResource.getResourceSuperType();
  }

  @Override
  @SuppressWarnings({ "unchecked", "null" })
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (type == ValueMap.class) {
      return (AdapterType)overlayedResource.getValueMap();
    }
    return super.adaptTo(type);
  }

  public Resource getOverlayedResource() {
    return this.overlayedResource;
  }

}
