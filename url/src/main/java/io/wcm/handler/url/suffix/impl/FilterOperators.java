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
 * Contains static methods that combine filters with logical operations
 */
public final class FilterOperators {

  private FilterOperators() {
    // utility methods only
  }

  /**
   * @param <T>
   * @param filter1
   * @param filter2
   * @return a filter that includes those elements that are included by *both* specified filters
   */
  public static <T> Filter<T> and(final Filter<T> filter1, final Filter<T> filter2) {
    return new Filter<T>() {

      @Override
      public boolean includes(T pElement) {
        return filter1.includes(pElement) && filter2.includes(pElement);
      }
    };
  }

  /**
   * @param <T>
   * @param filter1
   * @param filter2
   * @return a filter that includes those elements that are included by *one* of the specified filters
   */
  public static <T> Filter<T> or(final Filter<T> filter1, final Filter<T> filter2) {
    return new Filter<T>() {

      @Override
      public boolean includes(T pElement) {
        return filter1.includes(pElement) || filter2.includes(pElement);
      }
    };
  }

}
