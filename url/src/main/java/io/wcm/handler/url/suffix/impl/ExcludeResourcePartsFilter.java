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
 * Suffix-part filter that removes all resource-suffix-parts
 * (identified by not containing {@link UrlSuffixUtil#KEY_VALUE_DELIMITER})
 */
public class ExcludeResourcePartsFilter extends IncludeResourcePartsFilter {

  @Override
  public boolean includes(String suffixPart) {
    // exact reversion of IncludeResourcePartsFilter
    return !super.includes(suffixPart);
  }

}
