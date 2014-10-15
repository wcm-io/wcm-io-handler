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

import io.wcm.handler.commons.dom.Anchor;
import io.wcm.handler.link.Link;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Builds XHTML markup for links
 */
@ConsumerType
public interface LinkMarkupBuilder {

  /**
   * Checks whether this builder can generate markup for the given link.
   * @param link Link metadata
   * @return true if this markup builder can handle the given link
   */
  boolean accepts(Link link);

  /**
   * Build link anchor markup
   * @param link Link metadata with resolved link information
   * @return Anchor or null if link is invalid
   */
  Anchor build(Link link);

}
