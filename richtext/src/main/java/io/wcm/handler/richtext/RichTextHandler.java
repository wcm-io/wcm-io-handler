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
package io.wcm.handler.richtext;

import org.apache.sling.api.resource.Resource;

/**
 * Manages XHTML rich text fragment formatting.
 */
public interface RichTextHandler {

  /**
   * Build formatted text from content resource using default property names.
   * @param resource Resource
   * @return Rich text builder
   */
  RichTextBuilder get(Resource resource);

  /**
   * Build formatter text from given raw text string.
   * @param text Raw text
   * @return Rich text builder
   */
  RichTextBuilder get(String text);

  /**
   * Check if the given formatted text block is empty. A text block containing only one paragraph element and
   * whitespaces is considered as empty. A text block with more than 20 characters (raw data)
   * is never considered as empty.
   * @param text XHTML text string (root element not needed)
   * @return true if text block is empty
   */
  boolean isEmpty(String text);

}
