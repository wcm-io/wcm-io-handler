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

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;

/**
 * Filters pages by template path.
 */
class PageTemplateFilter implements Predicate<Page> {

  private final String templatePath;

  /**
   * @param templatePath Template path
   */
  PageTemplateFilter(String templatePath) {
    this.templatePath = templatePath;
  }

  @Override
  public boolean test(Page page) {
    return StringUtils.equals(page.getProperties().get(NameConstants.PN_TEMPLATE, String.class), templatePath);
  }

}
