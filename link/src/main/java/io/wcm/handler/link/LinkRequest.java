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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;

import io.wcm.wcm.commons.util.ToStringStyle;

/**
 * Holds all properties that are part of a link handling request.
 */
@ProviderType
public final class LinkRequest {

  private final Resource resource;
  private final Page page;
  private final String reference;
  private final LinkArgs linkArgs;

  private ValueMap resourceProperties;

  /**
   * @param resource Resource containing properties that define the link target
   * @param page Target content page
   * @param linkArgs Link arguments
   */
  public LinkRequest(@Nullable Resource resource, @Nullable Page page, @Nullable LinkArgs linkArgs) {
    this(resource, page, null, linkArgs);
  }

  /**
   * @param resource Resource containing properties that define the link target
   * @param page Target content page
   * @param reference Link reference (internal or external).
   * @param linkArgs Link arguments
   */
  public LinkRequest(@Nullable Resource resource, @Nullable Page page, @Nullable String reference, @Nullable LinkArgs linkArgs) {
    this.resource = resource;
    this.page = page;
    this.reference = reference;
    this.linkArgs = linkArgs != null ? linkArgs : new LinkArgs();

    // validate parameters
    int linkParamCount = (resource != null ? 1 : 0)
        + (page != null ? 1 : 0)
        + (reference != null ? 1 : 0);
    if (linkParamCount > 1) {
      throw new IllegalArgumentException("Set only one of resource, page, or reference.");
    }
  }

  /**
   * @return Resource containing properties that define the link target
   */
  public @Nullable Resource getResource() {
    return this.resource;
  }

  /**
   * @return Target content page
   */
  public @Nullable Page getPage() {
    return this.page;
  }

  /**
   * @return Link reference (internal or external).
   */
  public String getReference() {
    return this.reference;
  }

  /**
   * @return Link arguments
   */
  public @NotNull LinkArgs getLinkArgs() {
    return this.linkArgs;
  }

  /**
   * @return Properties from resource containing target link. The value map is a copy
   *         of the original map so it is safe to change the property values contained in the map.
   */
  public @NotNull ValueMap getResourceProperties() {
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
