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

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;

/**
 * Define URL handling requests using builder pattern.
 */
@ProviderType
public interface UrlBuilder {

  /**
   * Set selectors
   * @param selectors Selector string
   * @return URL builder
   */
  UrlBuilder selectors(String selectors);

  /**
   * Set file extension
   * @param extension file extension
   * @return URL builder
   */
  UrlBuilder extension(String extension);

  /**
   * Set suffix
   * @param suffix Suffix string
   * @return URL builder
   */
  UrlBuilder suffix(String suffix);

  /**
   * Set query parameters string
   * @param queryString Query parameters string (properly url-encoded)
   * @return URL builder
   */
  UrlBuilder queryString(String queryString);

  /**
   * Set query parameters string
   * @param queryString Query parameters string (properly url-encoded)
   * @param inheritableParameterNames Names of query string parameters that should be inherited from the current request
   * @return URL builder
   */
  UrlBuilder queryString(String queryString, Set<String> inheritableParameterNames);

  /**
   * Set fragment identifier
   * @param fragment Fragment identifier
   * @return URL builder
   */
  UrlBuilder fragment(String fragment);

  /**
   * Set URL mode for externalizing the URL
   * @param urlMode URL mode. If null, default URL mode is used.
   * @return URL builder
   */
  UrlBuilder urlMode(UrlMode urlMode);

  /**
   * Build URL
   * @return URL
   */
  String build();

  /**
   * Build externalized URL that links to a content page.
   * This may only be used if a page was given in the {@link UrlHandler#get(Page)} call.
   * @return URL
   */
  String buildExternalLinkUrl();

  /**
   * Build externalized URL that links to a content page.
   * @param targetPage Target page of internal link (e.g. for checking url configuration and secure mode)
   * @return URL
   */
  String buildExternalLinkUrl(Page targetPage);

  /**
   * Build externalized URL that links to a resource (e.g. image, CSS or JavaScript reference).
   * @return URL
   */
  String buildExternalResourceUrl();

  /**
   * Build externalized URL that links to a resource (e.g. image, CSS or JavaScript reference).
   * @param targetResource Target resource of resource link (e.g. for checking url configuration)
   * @return URL
   */
  String buildExternalResourceUrl(Resource targetResource);

}
