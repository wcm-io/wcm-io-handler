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
package io.wcm.handler.url.suffix.impl;

import org.apache.commons.lang3.StringUtils;

import com.day.cq.commons.Filter;

/**
 * Suffix-part filter that removes specific resource paths.
 */
public class ExcludeSpecificResourceFilter implements Filter<String> {

  private final String resourcePathToRemove;

  /**
   * @param resourcePathToRemove Resource path to remove
   */
  public ExcludeSpecificResourceFilter(String resourcePathToRemove) {
    this.resourcePathToRemove = resourcePathToRemove;
  }

  @Override
  public boolean includes(String suffixPart) {
    if (StringUtils.contains(suffixPart, UrlSuffixUtil.KEY_VALUE_DELIMITER)) {
      return true;
    }

    // We need to unescape the suffix part before comparing it to the source path to remove
    String unescapedPart = UrlSuffixUtil.decodeResourcePathPart(suffixPart);

    return !unescapedPart.equals(resourcePathToRemove);
  }

}
