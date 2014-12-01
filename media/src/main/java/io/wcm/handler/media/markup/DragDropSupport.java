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
package io.wcm.handler.media.markup;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Controls whether the markup builder should apply drag&amp;drop support for the rendered markup.
 * (If at all and in which WCM mode the drag&amp;drop support depends on the implementation of the
 * markup builder and the media source.)
 */
@ProviderType
public enum DragDropSupport {

  /**
   * Check via heuristics if drag&amp;drop should be supported. The heuristic checks
   * if the input for the media handler was a resource, and if the path of the resource
   * matches with the path of the current edit context.
   */
  AUTO,

  /**
   * Never apply drag&amp;drop support.
   */
  NEVER,

  /**
   * Always apply drag&amp;drop support (if the media markup builder and media source implemention permit it).
   */
  ALWAYS

}
