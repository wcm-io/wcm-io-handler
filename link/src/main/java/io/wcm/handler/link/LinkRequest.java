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
package io.wcm.handler.link;

import io.wcm.handler.url.UrlMode;

import java.util.HashMap;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import com.day.cq.wcm.api.Page;

/**
 * Holds all properties that are part of a link handling request.
 */
public final class LinkRequest {

  private final Resource resource;
  private final Page page;
  private final UrlMode urlMode;
  private final String selectors;
  private final String extension;
  private final String suffix;
  private final String queryString;
  private final String fragement;

  private ValueMap resourceProperties;

  /**
   * @param resource Resource containing properties that define the link target
   * @param page Target content page
   * @param urlMode URL mode for externalizing the URL
   * @param selectors Selector string
   * @param extension File extension
   * @param suffix Suffix string
   * @param queryString Query parameters string (properly url-encoded)
   * @param fragement Fragment identifier
   */
  public LinkRequest(Resource resource, Page page, UrlMode urlMode, String selectors, String extension,
      String suffix, String queryString, String fragement) {
    this.resource = resource;
    this.page = page;
    this.selectors = selectors;
    this.extension = extension;
    this.suffix = suffix;
    this.queryString = queryString;
    this.fragement = fragement;
    this.urlMode = urlMode;

    // validate parameters
    if (this.resource != null && this.page != null) {
      throw new IllegalArgumentException("Set resource or page, not both.");
    }
  }

  /**
   * @param resource Resource containing properties that define the link target
   * @param page Target content page
   * @param urlMode URL mode for externalizing the URL
   */
  public LinkRequest(Resource resource, Page page, UrlMode urlMode) {
    this(resource, page, urlMode, null, null, null, null, null);
  }

  /**
   * @return Resource containing properties that define the link target
   */
  public Resource getResource() {
    return this.resource;
  }

  /**
   * @return Target content page
   */
  public Page getPage() {
    return this.page;
  }

  /**
   * @return URL mode for externalizing the URL
   */
  public UrlMode getUrlMode() {
    return this.urlMode;
  }

  /**
   * @return Selector string
   */
  public String getSelectors() {
    return this.selectors;
  }

  /**
   * @return File extension
   */
  public String getExtension() {
    return this.extension;
  }

  /**
   * @return Suffix string
   */
  public String getSuffix() {
    return this.suffix;
  }

  /**
   * @return Query parameters string (properly url-encoded)
   */
  public String getQueryString() {
    return this.queryString;
  }

  /**
   * @return Fragment identifier
   */
  public String getFragement() {
    return this.fragement;
  }

  /**
   * @return Properties from resource containing target link. The value map is a copy
   *         of the original map so it is safe to change the property values contained in the map.
   */
  public ValueMap getResourceProperties() {
    if (this.resourceProperties == null) {
      // create a copy of the original map
      this.resourceProperties = new ValueMapDecorator(new HashMap<String, Object>());
      if (this.resource != null) {
        this.resourceProperties.putAll(resource.getValueMap());
      }
    }
    return this.resourceProperties;
  }

}
