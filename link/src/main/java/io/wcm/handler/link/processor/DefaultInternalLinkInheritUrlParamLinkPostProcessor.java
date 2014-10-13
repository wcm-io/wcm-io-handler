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
package io.wcm.handler.link.processor;

import java.util.Set;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;

import aQute.bnd.annotation.ProviderType;

import com.google.common.collect.ImmutableSet;

/**
 * Linkhandler postprocessor to inherit URL parametres to internal links.
 * The list of URL parameters contains only "debugClientLibs".
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ProviderType
public final class DefaultInternalLinkInheritUrlParamLinkPostProcessor extends AbstractInternalLinkInheritUrlParamLinkPostProcessor {

  /**
   * Default list of inheritable URL parameter names
   */
  public static final Set<String> DEFAULT_INHERIT_URL_PARAMETER_NAMES = ImmutableSet.of(
      "debugClientLibs"
      );

  /**
   * Initialize inherited URL parameter names.
   */
  public DefaultInternalLinkInheritUrlParamLinkPostProcessor() {
    super(DEFAULT_INHERIT_URL_PARAMETER_NAMES);
  }

}
