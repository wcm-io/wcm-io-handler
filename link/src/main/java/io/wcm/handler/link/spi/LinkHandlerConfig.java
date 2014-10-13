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

import aQute.bnd.annotation.ConsumerType;

import com.day.cq.wcm.api.Page;

/**
 * Provides application-specific configuration information required for link handling.
 */
@ConsumerType
public interface LinkHandlerConfig {

  /**
   * @return Supported link types
   */
  List<Class<? extends LinkType>> getLinkTypes();

  /**
   * @return Available link markup builders
   */
  List<Class<? extends LinkMarkupBuilder>> getMarkupBuilders();

  /**
   * @return List of link metadata pre processors (optional). The processors are applied in list order.
   */
  List<Class<? extends LinkProcessor>> getPreProcessors();

  /**
   * @return List of link metadata post processors (optional). The processors are applied in list order.
   */
  List<Class<? extends LinkProcessor>> getPostProcessors();

  /**
   * Detected if page is acceptable as link target.
   * This is used by {@link io.wcm.handler.link.type.InternalLinkType}, other {@link LinkType} implementations
   * may implement other logic.
   * @param page Page
   * @return true if Page is acceptable as link target.
   */
  boolean isValidLinkTarget(Page page);

  /**
   * Detected if page contains redirect link information
   * @param page Page
   * @return true if Page is a redirect page
   */
  boolean isRedirect(Page page);

}
