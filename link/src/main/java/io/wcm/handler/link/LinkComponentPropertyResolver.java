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
package io.wcm.handler.link;

import static io.wcm.handler.link.LinkNameConstants.PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.wcm.commons.component.ComponentPropertyResolution;
import io.wcm.wcm.commons.component.ComponentPropertyResolver;

/**
 * Resolves component properties for Link Handler for the component associated
 * with the given resources from content policies and properties defined in the component resource.
 */
@ProviderType
public final class LinkComponentPropertyResolver {

  private final ComponentPropertyResolver resolver;

  /**
   * @param resource Resource containing link properties
   */
  public LinkComponentPropertyResolver(Resource resource) {
    // resolve media component properties 1. from policies and 2. from component definition
    resolver = new ComponentPropertyResolver(resource)
        .contentPolicyResolution(ComponentPropertyResolution.RESOLVE)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
  }

  /**
   * @return Link target URL fallback property name
   */
  @Nullable
  public String getLinkTargetUrlFallbackProperty() {
    return resolver.get(PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY, String.class);
  }

}
