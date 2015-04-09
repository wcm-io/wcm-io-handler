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
package io.wcm.handler.url.suffix;

import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.splitSuffix;

import java.util.ArrayList;
import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.commons.Filter;

/**
 * Implementation of {@link SuffixStateKeepingStrategy} that calls a Filter for each suffix part from the
 * current request to decide if it should be kept when constructing a new suffix.
 */
@ProviderType
public final class FilteringSuffixStateStrategy implements SuffixStateKeepingStrategy {

  private final Filter<String> suffixPartFilter;

  /**
   * @param suffixPartFilter the {@link Filter} that defines
   */
  public FilteringSuffixStateStrategy(Filter<String> suffixPartFilter) {
    this.suffixPartFilter = suffixPartFilter;
  }

  @Override
  public List<String> getSuffixPartsToKeep(SlingHttpServletRequest request) {

    // get and split suffix parts from the current request
    String existingSuffix = request.getRequestPathInfo().getSuffix();
    String[] suffixPartArray = splitSuffix(existingSuffix);

    // iterate over all these suffix parts and gather those that should be kept
    List<String> suffixPartsToKeep = new ArrayList<>();
    for (int i = 0; i < suffixPartArray.length; i++) {
      String nextPart = suffixPartArray[i];

      // for each part: check filter if it should be inc
      if (suffixPartFilter == null || suffixPartFilter.includes(nextPart)) {
        suffixPartsToKeep.add(nextPart);
      }
    }

    return suffixPartsToKeep;
  }

}
