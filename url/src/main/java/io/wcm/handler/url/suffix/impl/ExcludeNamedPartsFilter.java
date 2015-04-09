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

/**
 * Filter that keeps all resource-suffix-parts and only discards specific key-value-parts
 */
public class ExcludeNamedPartsFilter extends IncludeNamedPartsFilter {

  /**
   * @param keysToDiscard Keys to discard
   */
  public ExcludeNamedPartsFilter(String... keysToDiscard) {
    super(keysToDiscard);
  }

  @Override
  public boolean includes(String suffixPart) {
    // exact reversion of IncludeNamedPartsFilter
    return !super.includes(suffixPart);
  }

}
