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

import com.day.cq.commons.Filter;

/**
 * Filter that discards all resource-suffix-parts and only keeps specific key-value-parts
 */
public class IncludeNamedPartsFilter implements Filter<String> {

  private final String[] keysToKeep;

  /**
   * @param keysToKeep Keys to keep
   */
  public IncludeNamedPartsFilter(String... keysToKeep) {
    this.keysToKeep = keysToKeep;
  }

  @Override
  public boolean includes(String suffixPart) {
    for (String key : keysToKeep) {
      if (suffixPart.startsWith(key + UrlSuffixUtil.KEY_VALUE_DELIMITER)) {
        return true;
      }
    }
    return false;
  }

}
