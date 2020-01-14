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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.wcm.commons.component.ComponentPropertyResolution;
import io.wcm.wcm.commons.component.ComponentPropertyResolver;
import io.wcm.wcm.commons.component.ComponentPropertyResolverFactory;

/**
 * Resolves Link Handler component properties for the component associated
 * with the given resource from content policies and properties defined in the component resource.
 * Please make sure to {@link #close()} instances of this class after usage.
 */
@ProviderType
public final class LinkComponentPropertyResolver implements AutoCloseable {

  private final ComponentPropertyResolver resolver;

  /**
   * @param resource Resource containing link properties
   * @param componentPropertyResolverFactory Component property resolver factory
   */
  public LinkComponentPropertyResolver(@NotNull Resource resource,
      @NotNull ComponentPropertyResolverFactory componentPropertyResolverFactory) {
    // resolve media component properties 1. from policies and 2. from component definition
    resolver = componentPropertyResolverFactory.get(resource, true)
        .contentPolicyResolution(ComponentPropertyResolution.RESOLVE)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
  }

  /**
   * @param resource Resource containing link properties
   * @deprecated Please use {@link #LinkComponentPropertyResolver(Resource, ComponentPropertyResolverFactory)}
   */
  @Deprecated
  @SuppressWarnings("resource")
  public LinkComponentPropertyResolver(@NotNull Resource resource) {
    // resolve media component properties 1. from policies and 2. from component definition
    resolver = new ComponentPropertyResolver(resource, true)
        .contentPolicyResolution(ComponentPropertyResolution.RESOLVE)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
  }

  /**
   * @return Link target URL fallback property name
   */
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public @NotNull String @Nullable [] getLinkTargetUrlFallbackProperty() {
    return resolver.get(PN_COMPONENT_LINK_TARGET_URL_FALLBACK_PROPERTY, String[].class);
  }

  @Override
  public void close() throws Exception {
    resolver.close();
  }

}
