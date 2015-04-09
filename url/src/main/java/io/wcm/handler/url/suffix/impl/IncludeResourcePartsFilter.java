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
 * Suffix-part filter that only includes resource-suffix parts
 * (identified by not containing {@link UrlSuffixUtil#KEY_VALUE_DELIMITER})
 */
public class IncludeResourcePartsFilter implements Filter<String> {

  @Override
  public boolean includes(String suffixPart) {
    return !StringUtils.contains(suffixPart, UrlSuffixUtil.KEY_VALUE_DELIMITER);
  }

}
