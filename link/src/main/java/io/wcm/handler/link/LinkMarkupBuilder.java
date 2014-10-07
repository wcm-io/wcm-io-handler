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

import io.wcm.handler.commons.dom.Anchor;

/**
 * Builds XHTML markup for links
 */
public interface LinkMarkupBuilder {

  /**
   * Checks whether this builder can generate markup for the given link.
   * @param linkMetadata Link metadata
   * @return true if this markup builder can handle the given link
   */
  boolean accepts(LinkMetadata linkMetadata);

  /**
   * Build link anchor markup
   * @param linkMetadata Link metadata with resolved link information
   * @return Anchor or null if link is invalid
   */
  Anchor build(LinkMetadata linkMetadata);

}
