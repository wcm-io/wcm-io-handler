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

import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.KEY_VALUE_DELIMITER;
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.decodeKey;
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.decodeResourcePathPart;
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.decodeValue;
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.splitSuffix;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import com.day.cq.commons.Filter;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Parses suffixes from Sling URLs build with {@link SuffixBuilder}.
 */
public class SuffixParser {

  private final SlingHttpServletRequest request;

  // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CONSTRUCTORS

  /**
   * Create a {@link SuffixParser} with the default {@link SuffixStateKeepingStrategy} (which discards all existing
   * suffix state when constructing a new suffix)
   * @param request Sling request
   */
  public SuffixParser(SlingHttpServletRequest request) {
    this.request = request;
  }

  /**
   * Create a {@link SuffixParser} that keeps only the suffix parts matched by the given filter when constructing
   * a new suffix
   * @param request Sling request
   * @param suffixPartFilter the filter that is called for each suffix part
   */
  public SuffixParser(SlingHttpServletRequest request, Filter<String> suffixPartFilter) {
    this.request = request;
  }



  // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // SUFFIX PARSING / EXTRACTION

  /**
   * Extract the value of a named suffix part from this request's suffix
   * @param key key of the suffix part
   * @param defaultValue the default value to return if suffix part not set
   * @return the value of that named parameter (or null if not used)
   */
  public String getPart(String key, String defaultValue) {
    String value = findSuffixPartByKey(key);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  /**
   * Extract the value of a named boolean suffix part from this request's suffix
   * @param key key of the suffix part
   * @param defaultValue the default value to return if suffix part not set or not a boolean
   * @return the value of that named parameter (or null if not used)
   */
  public boolean getPart(String key, boolean defaultValue) {
    String value = findSuffixPartByKey(key);
    if (value == null) {
      return defaultValue;
    }

    // value must match exactly "true" or "false"
    if ("true".equals(value)) {
      return true;
    }
    if ("false".equals(value)) {
      return false;
    }

    // invalid boolean value - return default
    return defaultValue;
  }

  /**
   * Extract the value of a named numerical suffix part from this request's suffix
   * @param key key of the suffix part
   * @param defaultValue the default value to return if suffix part not set
   * @return the value of that named parameter (or null if not used)
   */
  public int getPart(String key, int defaultValue) {
    String value = findSuffixPartByKey(key);
    if (value == null) {
      return defaultValue;
    }
    return NumberUtils.toInt(value, defaultValue);
  }

  /**
   * Extract the value of a named numerical suffix part from this request's suffix
   * @param key key of the suffix part
   * @param defaultValue the default value to return if suffix part not set
   * @return the value of that named parameter (or null if not used)
   */
  public long getPart(String key, long defaultValue) {
    String value = findSuffixPartByKey(key);
    if (value == null) {
      return defaultValue;
    }
    return NumberUtils.toLong(value, defaultValue);
  }

  /**
   * Extract the value of a named suffix part from this request's suffix
   * @param key key of the suffix part
   * @return the value of that named parameter (or null if not used)
   */
  private String findSuffixPartByKey(String key) {
    for (String part : splitSuffix(getRequest().getRequestPathInfo().getSuffix())) {
      if (part.indexOf(KEY_VALUE_DELIMITER) >= 0) {
        String partKey = decodeKey(part);
        if (partKey.equals(key)) {
          String value = decodeValue(part);
          return value;
        }
      }
    }
    return null;
  }

  /**
   * Get a resource within the current page by interpreting the suffix as a JCR path relative to this page's jcr:content
   * node
   * @return the Resource or null if no such resource exists
   */
  public Resource getResource() {
    return getResource((Filter<Resource>)null, null);
  }

  /**
   * Parse the suffix as resource paths and return the first resource that exists
   * @param basePath the suffix path will be resolved relative to this path (null for current page's jcr:content node)
   * @return the resource or null if no such resource was selected by suffix
   */
  public Resource getResource(String basePath) {
    return getResource((Filter<Resource>)null, basePath);
  }

  /**
   * Parse the suffix as resource paths, return the first resource from the suffix (relativ to the current page's
   * content) that matches the given filter.
   * @param filter a filter that selects only the resource you're interested in.
   * @return the resource or null if no such resource was selected by suffix
   */
  public Resource getResource(Filter<Resource> filter) {
    return getResource(filter, (String)null);
  }

  /**
   * Get the first item returned by {@link #getResources(Filter, String)} or null if list is empty
   * @param filter the resource filter
   * @param basePath the suffix path is relative to this path (null for current page's jcr:content node)
   * @return the first {@link Resource} or null
   */
  public Resource getResource(Filter<Resource> filter, String basePath) {
    List<Resource> suffixResources = getResources(filter, basePath);
    if (suffixResources.isEmpty()) {
      return null;
    }
    else {
      return suffixResources.get(0);
    }
  }

  /**
   * Get the resources selected in the suffix of the URL
   * @param filter optional filter to select only specific resources
   * @param basePath the suffix path is relative to this path (null for current page's jcr:content node)
   * @return a list containing the Resources
   */
  public List<Resource> getResources(Filter<Resource> filter, String basePath) {

    // resolve base path or fallback to current page's content if not specified
    Resource baseResource;
    if (StringUtils.isNotBlank(basePath)) {
      baseResource = getRequest().getResourceResolver().getResource(basePath);
    }
    else {
      PageManager pageManager = getRequest().getResourceResolver().adaptTo(PageManager.class);
      Page currentPage = pageManager.getContainingPage(getRequest().getResource());
      if (currentPage != null) {
        baseResource = currentPage.getContentResource();
      }
      else {
        baseResource = getRequest().getResource();
      }
    }

    // split the suffix to extract the paths of the selected components
    String[] suffixParts = splitSuffix(getRequest().getRequestPathInfo().getSuffix());

    // iterate over all parts and gather those resources
    List<Resource> selectedResources = new ArrayList<>();
    for (String path : suffixParts) {

      // if path contains the key/value-delimiter then don't try to resolve it as a content path
      if (StringUtils.contains(path, KEY_VALUE_DELIMITER)) {
        continue;
      }

      String decodedPath = decodeResourcePathPart(path);

      // lookup the resource specified by the path (which is relative to the current page's content resource)
      Resource resource = getRequest().getResourceResolver().getResource(baseResource, decodedPath);
      if (resource == null) {
        // no resource found with given path, continue with next path in suffix
        continue;
      }

      // if a filter is given - check
      if (filter == null || filter.includes(resource)) {
        selectedResources.add(resource);
      }

    }

    return selectedResources;
  }

  SlingHttpServletRequest getRequest() {
    return this.request;
  }

}
