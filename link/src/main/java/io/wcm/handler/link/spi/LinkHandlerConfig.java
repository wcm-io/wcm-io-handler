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
package io.wcm.handler.link.spi;

import java.util.List;

import org.osgi.annotation.versioning.ConsumerType;

import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;

import io.wcm.handler.commons.spisupport.SpiMatcher;
import io.wcm.handler.link.markup.DummyLinkMarkupBuilder;
import io.wcm.handler.link.markup.SimpleLinkMarkupBuilder;
import io.wcm.handler.link.processor.DefaultInternalLinkInheritUrlParamLinkPostProcessor;
import io.wcm.handler.link.type.ExternalLinkType;
import io.wcm.handler.link.type.InternalLinkType;
import io.wcm.handler.link.type.MediaLinkType;

/**
 * Provides application-specific configuration information required for link handling.
 * <p>
 * This interface has to be implemented by a Sling Model class, optional with @Application annotation. The adaptables
 * should be {@link org.apache.sling.api.SlingHttpServletRequest} and {@link org.apache.sling.api.resource.Resource}.
 * </p>
 */
@ConsumerType
public abstract class LinkHandlerConfig implements SpiMatcher {

  private static final List<Class<? extends LinkType>> DEFAULT_LINK_TYPES = ImmutableList.<Class<? extends LinkType>>of(
      InternalLinkType.class,
      ExternalLinkType.class,
      MediaLinkType.class);

  private static final List<Class<? extends LinkMarkupBuilder>> DEFAULT_LINK_MARKUP_BUILDERS = ImmutableList.<Class<? extends LinkMarkupBuilder>>of(
      SimpleLinkMarkupBuilder.class,
      DummyLinkMarkupBuilder.class);

  private static final List<Class<? extends LinkProcessor>> DEFAULT_POST_PROCESSORS = ImmutableList.<Class<? extends LinkProcessor>>of(
      DefaultInternalLinkInheritUrlParamLinkPostProcessor.class);

  /**
   * @return Supported link types
   */
  public List<Class<? extends LinkType>> getLinkTypes() {
    return DEFAULT_LINK_TYPES;
  }

  /**
   * @return Available link markup builders
   */
  public List<Class<? extends LinkMarkupBuilder>> getMarkupBuilders() {
    return DEFAULT_LINK_MARKUP_BUILDERS;
  }

  /**
   * @return List of link metadata pre processors (optional). The processors are applied in list order.
   */
  public List<Class<? extends LinkProcessor>> getPreProcessors() {
    // no processors
    return ImmutableList.of();
  }

  /**
   * @return List of link metadata post processors (optional). The processors are applied in list order.
   */
  public List<Class<? extends LinkProcessor>> getPostProcessors() {
    return DEFAULT_POST_PROCESSORS;
  }

  /**
   * Detected if page is acceptable as link target.
   * This is used by {@link io.wcm.handler.link.type.InternalLinkType}, other {@link LinkType} implementations
   * may implement other logic.
   * @param page Page
   * @return true if Page is acceptable as link target.
   */
  public boolean isValidLinkTarget(Page page) {
    // by default accept all pages
    return true;
  }

  /**
   * Detected if page contains redirect link information
   * @param page Page
   * @return true if Page is a redirect page
   */
  public boolean isRedirect(Page page) {
    // not supported by default
    return false;
  }

}
