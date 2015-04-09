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
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.SUFFIX_PART_DELIMITER;
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.decodeKey;
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.decodeResourcePathPart;
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.decodeValue;
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.encodeKeyValuePart;
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.encodeResourcePathPart;
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.keyValuePairAsMap;
import io.wcm.handler.url.suffix.impl.ExcludeNamedPartsFilter;
import io.wcm.handler.url.suffix.impl.ExcludeResourcePartsFilter;
import io.wcm.handler.url.suffix.impl.ExcludeSpecificResourceFilter;
import io.wcm.handler.url.suffix.impl.FilterOperators;
import io.wcm.handler.url.suffix.impl.IncludeAllPartsFilter;
import io.wcm.handler.url.suffix.impl.IncludeNamedPartsFilter;
import io.wcm.handler.url.suffix.impl.IncludeResourcePartsFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import com.day.cq.commons.Filter;
import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;

/**
 * Builds suffixes to be used in Sling URLs and that can be parsed with {@link SuffixParser}.
 */
public class SuffixBuilder {

  // decides which of the suffix parts from the current request will be kept when constructing a new suffix
  private final SuffixStateKeepingStrategy stateStrategy;

  private final SlingHttpServletRequest request;

  // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CONSTRUCTORS

  /**
   * Create a {@link SuffixBuilder} with the default {@link SuffixStateKeepingStrategy} (which discards all existing
   * suffix state when constructing a new suffix)
   * @param request Sling request
   */
  public SuffixBuilder(SlingHttpServletRequest request) {
    this.request = request;
    this.stateStrategy = new DiscardSuffixStateStrategy();
  }

  /**
   * Create a {@link SuffixBuilder} with a custom {@link SuffixStateKeepingStrategy} (see convenience methods like
   * {@link #thatKeepsResourceParts(SlingHttpServletRequest)} for often-used strategies)
   * @param request Sling request
   * @param stateStrategy the strategy to use to decide which parts of the suffix of the current request needs to be
   *          kept in new constructed links
   */
  public SuffixBuilder(SlingHttpServletRequest request, SuffixStateKeepingStrategy stateStrategy) {
    this.request = request;
    this.stateStrategy = stateStrategy;
  }

  /**
   * Create a {@link SuffixBuilder} that keeps only the suffix parts matched by the given filter when constructing
   * a new suffix
   * @param request Sling request
   * @param suffixPartFilter the filter that is called for each suffix part
   */
  public SuffixBuilder(SlingHttpServletRequest request, Filter<String> suffixPartFilter) {
    this.request = request;
    this.stateStrategy = new FilteringSuffixStateStrategy(suffixPartFilter);
  }


  // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC CONVENIENCE METHODS FOR CREATING INSTANCES WITH A SPECIFIC BEHAVIOUR

  /**
   * @param request Sling request
   * @return a {@link SuffixBuilder} that discards all existing suffix state when constructing a new suffix
   */
  public static SuffixBuilder thatDiscardsAllSuffixState(SlingHttpServletRequest request) {
    return new SuffixBuilder(request);
  }

  /**
   * @param request Sling request
   * @return a {@link SuffixBuilder} that discards everything but the *resource* parts of the suffix
   */
  public static SuffixBuilder thatKeepsResourceParts(SlingHttpServletRequest request) {
    Filter<String> filter = new IncludeResourcePartsFilter();
    return new SuffixBuilder(request, filter);
  }

  /**
   * @param request Sling request
   * @param keysToKeep Keys to keep
   * @return a {@link SuffixBuilder} that keeps only the named key/value-parts defined by pKeysToKeep
   */
  public static SuffixBuilder thatKeepsNamedParts(SlingHttpServletRequest request, String... keysToKeep) {
    Filter<String> filter = new IncludeNamedPartsFilter(keysToKeep);
    return new SuffixBuilder(request, filter);
  }

  /**
   * @param request Sling request
   * @param keysToKeep Keys to keep
   * @return a {@link SuffixBuilder} that keeps the named key/value-parts defined by pKeysToKeep and all resource
   *         parts
   */
  public static SuffixBuilder thatKeepsNamedPartsAndResources(SlingHttpServletRequest request, String... keysToKeep) {
    Filter<String> filter = FilterOperators.or(new IncludeResourcePartsFilter(), new IncludeNamedPartsFilter(keysToKeep));
    return new SuffixBuilder(request, filter);
  }

  /**
   * @param request Sling request
   * @return a {@link SuffixBuilder} that keeps all parts from the current request's suffix when constructing a new
   *         suffix
   */
  public static SuffixBuilder thatKeepsAllParts(SlingHttpServletRequest request) {
    return new SuffixBuilder(request, new IncludeAllPartsFilter());
  }

  /**
   * @param request Sling request
   * @return a {@link SuffixBuilder} that will discard the resource parts, but keep all named key/value-parts
   */
  public static SuffixBuilder thatDiscardsResourceParts(SlingHttpServletRequest request) {
    ExcludeResourcePartsFilter filter = new ExcludeResourcePartsFilter();
    return new SuffixBuilder(request, filter);
  }

  /**
   * @param request Sling request
   * @param keysToDiscard the keys of the named parts to discard
   * @return a {@link SuffixBuilder} that will keep all parts except those named key/value-parts defined by
   *         pKeysToDiscard
   */
  public static SuffixBuilder thatDiscardsNamedParts(SlingHttpServletRequest request, String... keysToDiscard) {
    return new SuffixBuilder(request, new ExcludeNamedPartsFilter(keysToDiscard));
  }

  /**
   * @param request Sling request
   * @param keysToDiscard the keys of the named parts to discard
   * @return {@link SuffixBuilder} that will discard all resource parts and the named parts defined by pKeysToDiscard
   */
  public static SuffixBuilder thatDiscardsResourceAndNamedParts(SlingHttpServletRequest request, String... keysToDiscard) {
    Filter<String> filter = FilterOperators.and(new ExcludeResourcePartsFilter(), new ExcludeNamedPartsFilter(keysToDiscard));
    return new SuffixBuilder(request, filter);
  }

  /**
   * @param request Sling request
   * @param resourcePathToDiscard relative path of the resource to discard
   * @param keysToDiscard the keys of the named parts to discard
   * @return {@link SuffixBuilder} that will discard *one specific resource path* and the named parts defined by
   *         pKeysToDiscard
   */
  public static SuffixBuilder thatDiscardsSpecificResourceAndNamedParts(SlingHttpServletRequest request, String resourcePathToDiscard,
      String... keysToDiscard) {
    Filter<String> filter = FilterOperators.and(new ExcludeSpecificResourceFilter(resourcePathToDiscard), new ExcludeNamedPartsFilter(keysToDiscard));
    return new SuffixBuilder(request, filter);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // SUFFIX CONSTRUCTION

  /**
   * Constructs a reduced suffix that does not add any new suffix part, and only keeps those as defined by the
   * {@link SuffixStateKeepingStrategy}
   * @return the suffix
   */
  public String build() {
    return build(new TreeMap<String, String>(), new String[0]);
  }

  /**
   * Constructs a suffix that addresses a page. Depending on the {@link SuffixStateKeepingStrategy}, the suffix contains
   * further parts from the current
   * request that should be kept when constructing new links.
   * @param page the page
   * @param suffixBasePage the base page used to construct the relative path
   * @return the suffix containing the relative path to the resource (and eventually other parts)
   */
  public String build(Page page, Page suffixBasePage) {
    return build(page.adaptTo(Resource.class), suffixBasePage.adaptTo(Resource.class));
  }

  /**
   * Constructs a suffix that contains both a jcr resource path and a key-value pair. Depending on the
   * {@link SuffixStateKeepingStrategy}, the suffix contains
   * further parts from the current request that should be kept when constructing new links.
   * @param page the page
   * @param suffixBasePage the base page used to construct the relative path
   * @param key the key
   * @param value the value
   * @return the suffix containing the relative path to the resource (and eventually other parts)
   */
  public String build(Page page, Page suffixBasePage, String key, String value) {
    return build(page.adaptTo(Resource.class), suffixBasePage.adaptTo(Resource.class), key, value);
  }

  /**
   * Constructs a suffix that contains both a jcr resource path and a key-value pair. Depending on the
   * {@link SuffixStateKeepingStrategy}, the suffix contains
   * further parts from the current request that should be kept when constructing new links.
   * @param page the page
   * @param suffixBasePage the base page used to construct the relative path
   * @param parameterMap map of key-value pairs
   * @return the suffix containing the relative path to the resource (and eventually other parts)
   */
  public String build(Page page, Page suffixBasePage, Map<String, String> parameterMap) {
    return build(page.adaptTo(Resource.class), suffixBasePage.adaptTo(Resource.class), parameterMap);
  }

  /**
   * Constructs a suffix that contains both a jcr resource path and a numerical key-value pair. Depending on the
   * {@link SuffixStateKeepingStrategy}, the suffix
   * contains further parts from the current request that should be kept when constructing new links.
   * @param page the page
   * @param suffixBasePage the base page used to construct the relative path
   * @param key the key
   * @param value the value
   * @return the suffix containing the relative path to the resource (and eventually other parts)
   */
  public String build(Page page, Page suffixBasePage, String key, int value) {
    return build(page.adaptTo(Resource.class), suffixBasePage.adaptTo(Resource.class), key, value);
  }

  /**
   * Constructs a suffix that contains both a jcr resource path and a boolean key-value pair. Depending on the
   * {@link SuffixStateKeepingStrategy}, the suffix
   * contains further parts from the current request that should be kept when constructing new links.
   * @param page the page
   * @param suffixBasePage the base page used to construct the relative path
   * @param key the key
   * @param value the value
   * @return the suffix containing the relative path to the resource (and eventually other parts)
   */
  public String build(Page page, Page suffixBasePage, String key, boolean value) {
    return build(page.adaptTo(Resource.class), suffixBasePage.adaptTo(Resource.class), key, value);
  }

  /**
   * Constructs a suffix that addresses a jcr resource. Depending on the {@link SuffixStateKeepingStrategy}, the suffix
   * contains further parts from the current
   * request that should be kept when constructing new links.
   * @param resource the resource
   * @param suffixBaseResource the base resource used to construct the relative path
   * @return the suffix containing the relative path to the resource (and eventually other parts)
   */
  public String build(Resource resource, Resource suffixBaseResource) {
    // get relative path to base resource
    String relativePath = getRelativePath(resource, suffixBaseResource);

    return this.build(new TreeMap<String, String>(), relativePath);
  }

  /**
   * Constructs a suffix that contains both a jcr resource path and a key-value pair. Depending on the
   * {@link SuffixStateKeepingStrategy}, the suffix contains
   * further parts from the current request that should be kept when constructing new links.
   * @param resource the resource
   * @param suffixBaseResource the base resource used to construct the relative path
   * @param parameterMap map of key-value pairs
   * @return the suffix containing the relative path to the resource (and eventually other parts)
   */
  public String build(Resource resource, Resource suffixBaseResource, Map<String, String> parameterMap) {
    return this.build(parameterMap, getRelativePath(resource, suffixBaseResource));
  }

  /**
   * Constructs a suffix that contains both a jcr resource path and a key-value pair. Depending on the
   * {@link SuffixStateKeepingStrategy}, the suffix contains
   * further parts from the current request that should be kept when constructing new links.
   * @param resource the resource
   * @param suffixBaseResource the base resource used to construct the relative path
   * @param key the key
   * @param value the value
   * @return the suffix containing the relative path to the resource (and eventually other parts)
   */
  public String build(Resource resource, Resource suffixBaseResource, String key, String value) {
    return this.build(keyValuePairAsMap(key, value), getRelativePath(resource, suffixBaseResource));
  }

  /**
   * Constructs a suffix that contains both a jcr resource path and a numerical key-value pair. Depending on the
   * {@link SuffixStateKeepingStrategy}, the suffix
   * contains further parts from the current request that should be kept when constructing new links.
   * @param resource the resource
   * @param suffixBaseResource the base resource used to construct the relative path
   * @param key the key
   * @param value the value
   * @return the suffix containing the relative path to the resource (and eventually other parts)
   */
  public String build(Resource resource, Resource suffixBaseResource, String key, int value) {
    return this.build(keyValuePairAsMap(key, Integer.toString(value)), getRelativePath(resource, suffixBaseResource));
  }

  /**
   * Constructs a suffix that contains both a jcr resource path and a boolean key-value pair. Depending on the
   * {@link SuffixStateKeepingStrategy}, the suffix
   * contains further parts from the current request that should be kept when constructing new links.
   * @param resource the resource
   * @param suffixBaseResource the base resource used to construct the relative path
   * @param key the key
   * @param value the value
   * @return the suffix containing the relative path to the resource (and eventually other parts)
   */
  public String build(Resource resource, Resource suffixBaseResource, String key, boolean value) {
    return this.build(keyValuePairAsMap(key, BooleanUtils.toStringTrueFalse(value)), getRelativePath(resource, suffixBaseResource));
  }

  /**
   * Constructs a suffix that contains a key-value pair. Depending on the {@link SuffixStateKeepingStrategy}, the suffix
   * contains further parts from the current
   * request that should be kept when constructing new links.
   * @param key the key
   * @param value the value
   * @return the suffix containing an encoded key value-pair (and eventually other parts)
   */
  public String build(String key, String value) {
    return build(keyValuePairAsMap(key, value), new String[0]);
  }

  /**
   * Constructs a suffix that contains a key-value pair with a numerical value. Depending on the
   * {@link SuffixStateKeepingStrategy}, the suffix contains further
   * parts from the current request that should be kept when constructing new links.
   * @param key the key
   * @param value the value
   * @return the suffix containing an encoded key value-pair (and eventually other parts)
   */
  public String build(String key, int value) {
    return build(key, Integer.toString(value));
  }

  /**
   * Constructs a suffix that contains a key-value pair with a numerical value. Depending on the
   * {@link SuffixStateKeepingStrategy}, the suffix contains further
   * parts from the current request that should be kept when constructing new links.
   * @param key the key
   * @param value the value
   * @return the suffix containing an encoded key value-pair (and eventually other parts)
   */
  public String build(String key, long value) {
    return build(key, Long.toString(value));
  }

  /**
   * Constructs a suffix that contains a key-value pair with a boolean value. Depending on the
   * {@link SuffixStateKeepingStrategy}, the suffix contains further
   * parts from the current request that should be kept when constructing new links.
   * @param key the key
   * @param value the value
   * @return the suffix containing an encoded key value-pair (and eventually other parts)
   */
  public String build(String key, boolean value) {
    return build(key, BooleanUtils.toStringTrueFalse(value));
  }

  /**
   * Constructs a suffix that contains multiple key-value pairs. Depending on the {@link SuffixStateKeepingStrategy},
   * the suffix contains further parts from the
   * current request that should be kept when constructing new links.
   * @param parameterMap map of key-value pairs
   * @return the suffix containing the map-content as encoded key value-pairs (and eventually other parts)
   */
  public String build(Map<String, String> parameterMap) {
    return this.build(parameterMap, new String[0]);
  }

  /**
   * Constructs a suffix that contains multiple key-value pairs and address resources. Depending on the
   * {@link SuffixStateKeepingStrategy}, the suffix contains
   * further parts from the current request that should be kept when constructing new links.
   * @param resources resources to address
   * @param baseResource base resource to construct relative path
   * @param parameterMap map of key-value pairs
   * @return the suffix containing the map-content as encoded key value-pairs (and eventually other parts)
   */
  String build(Collection<Resource> resources, Resource baseResource, Map<String, String> parameterMap) {
    String[] relativePaths = new String[resources.size()];
    Iterator<Resource> it = resources.iterator();
    int i = 0;
    while (it.hasNext()) {
      relativePaths[i] = getRelativePath(it.next(), baseResource);
      i++;
    }
    return this.build(parameterMap, relativePaths);
  }

  /**
   * The function that does the actual suffic constructing for all cases. Combines the suffix parts to be kept from the
   * current request with the ones specified
   * as parameters, (re)encodes and joines them in a single string
   * @param parameterMap map of key/value parameter
   * @param resourcePaths resource paths to be added
   * @return an URL-safe suffix
   */
  private String build(Map<String, String> parameterMap, String... resourcePaths) {
    SortedMap<String, String> sortedParameterMap = new TreeMap<>(parameterMap);

    // gather resource paths in a treeset (having them in a defined order helps with caching)
    Set<String> resourcePathsSet = new TreeSet<>();

    // iterate over all parts that should be kept from the current request
    for (String nextPart : getInitialSuffixParts()) {
      // if this is a key-value-part:
      if (nextPart.indexOf(KEY_VALUE_DELIMITER) > 0) {
        String key = decodeKey(nextPart);
        // decode and keep the part if it is not overriden in the given parameter-map
        if (!sortedParameterMap.containsKey(key)) {
          String value = decodeValue(nextPart);
          sortedParameterMap.put(key, value);
        }
      }
      else {
        // decode and keep the resource paths (unless they are specified again in pResourcepaths)
        String path = decodeResourcePathPart(nextPart);
        if (!ArrayUtils.contains(resourcePaths, path)) {
          resourcePathsSet.add(path);
        }
      }
    }

    // copy the resources specified as parameters to the sorted set of paths
    if (resourcePaths != null) {
      resourcePathsSet.addAll(ImmutableList.copyOf(resourcePaths));
    }

    // gather all suffix parts in this list
    List<String> suffixParts = new ArrayList<>();

    // now encode all resource paths
    for (String path : resourcePathsSet) {
      suffixParts.add(encodeResourcePathPart(path));
    }

    // now encode all entries from the parameter map
    for (Entry<String, String> entry : sortedParameterMap.entrySet()) {
      String value = entry.getValue();
      if (value == null) {
        // don't add suffix part if value is null
        continue;
      }
      String encodedKey = encodeKeyValuePart(entry.getKey());
      String encodedValue = encodeKeyValuePart(value);
      suffixParts.add(encodedKey + KEY_VALUE_DELIMITER + encodedValue);
    }

    // finally join these parts to a single string
    return StringUtils.join(suffixParts, SUFFIX_PART_DELIMITER);
  }

  /**
   * @param resource the resource being addressed by the relative path
   * @param baseResource the resource used as base to resolve the relative path
   * @return the relative path (without leading slash)
   */
  public static String getRelativePath(Resource resource, Resource baseResource) {
    if (baseResource == null) {
      throw new IllegalArgumentException("the base resource for constructing relative path must not be null");
    }
    if (resource == null) {
      throw new IllegalArgumentException("the resource for constructing relative path must not be null");
    }
    String absolutePath = resource.getPath();
    String basePath = baseResource.getPath();

    if (absolutePath.equals(basePath)) {
      // relative path for the root resource is "."
      return ".";
    }

    // be picky about resources not located beneath the base resource
    if (!absolutePath.startsWith(basePath + "/")) {
      throw new IllegalArgumentException("the resource " + resource + " is not a descendent of the base resource " + baseResource);
    }

    // return relative path
    return StringUtils.substringAfter(absolutePath, basePath + "/");
  }

  private List<String> getInitialSuffixParts() {
    // delegate the decision which parts of the suffix to keep to the SuffixStateKeepingStrategy being used
    return stateStrategy.getSuffixPartsToKeep(this);
  }

  SlingHttpServletRequest getRequest() {
    return this.request;
  }

}
