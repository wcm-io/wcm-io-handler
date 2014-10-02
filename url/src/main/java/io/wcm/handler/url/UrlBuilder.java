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
package io.wcm.handler.url;

import java.util.Set;

import com.day.cq.wcm.api.Page;

/**
 * Building URLs using Builder pattern.
 */
public interface UrlBuilder {

  /**
   * Set selectors
   * @param selectors Selector string
   * @return Url Builder
   */
  UrlBuilder selectors(String selectors);

  /**
   * Set file extension
   * @param extension file extension
   * @return Url Builder
   */
  UrlBuilder extension(String extension);

  /**
   * Set suffix
   * @param suffix Suffix string
   * @return Url Builder
   */
  UrlBuilder suffix(String suffix);

  /**
   * Set query parameters string
   * @param queryString Query parameters string (properly url-encoded)
   * @return Url Builder
   */
  UrlBuilder queryString(String queryString);

  /**
   * Set query parameters string
   * @param queryString Query parameters string (properly url-encoded)
   * @param inheritableParameterNames Names of query string parameters that should be inherited from the current request
   * @return Url Builder
   */
  UrlBuilder queryString(String queryString, Set<String> inheritableParameterNames);

  /**
   * Set fragment identifier
   * @param fragment Fragment identifier
   * @return Url Builder
   */
  UrlBuilder fragment(String fragment);

  /**
   * Externalize as link to a content page.
   * @param targetPage Target page of internal link (e.g. for checking secure mode)
   * @return Url Builder
   */
  UrlBuilder externalizeLink(Page targetPage);

  /**
   * Externalize as resource (e.g. image, CSS or JavaScript reference).
   * @return Url Builder
   */
  UrlBuilder externalizeResource();

  /**
   * Set URL mode for externalizing the URL
   * @param urlMode URL mode. If null, default URL mode is used.
   * @return Url Builder
   */
  UrlBuilder urlMode(UrlMode urlMode);

  /**
   * Build URL
   * @return URL
   */
  String build();

}
