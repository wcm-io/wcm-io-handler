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
import static io.wcm.handler.url.suffix.impl.UrlSuffixUtil.getRelativePath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import io.wcm.handler.url.suffix.impl.ExcludeNamedPartsFilter;
import io.wcm.handler.url.suffix.impl.ExcludeResourcePartsFilter;
import io.wcm.handler.url.suffix.impl.ExcludeSpecificResourceFilter;
import io.wcm.handler.url.suffix.impl.FilterOperators;
import io.wcm.handler.url.suffix.impl.IncludeAllPartsFilter;
import io.wcm.handler.url.suffix.impl.IncludeNamedPartsFilter;
import io.wcm.handler.url.suffix.impl.IncludeResourcePartsFilter;
import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Builds suffixes to be used in Sling URLs and that can be parsed with {@link SuffixParser}.
 */
@ProviderType
public final class SuffixBuilder {

  private final List<String> initialSuffixParts;
  private final Map<String, Object> parameterMap = new HashMap<>();
  private final List<String> resourcePaths = new ArrayList<>();

  /**
   * Create a {@link SuffixBuilder} which discards all existing suffix state when constructing a new suffix.
   */
  public SuffixBuilder() {
    this.initialSuffixParts = new ArrayList<>();
  }

  /**
   * Create a {@link SuffixBuilder} with a custom {@link SuffixStateKeepingStrategy} (see convenience methods like
   * {@link #thatKeepsResourceParts(SlingHttpServletRequest)} for often-used strategies)
   * @param request Sling request
   * @param stateStrategy the strategy to use to decide which parts of the suffix of the current request needs to be
   *          kept in new constructed links
   */
  public SuffixBuilder(@NotNull SlingHttpServletRequest request, @NotNull SuffixStateKeepingStrategy stateStrategy) {
    this.initialSuffixParts = stateStrategy.getSuffixPartsToKeep(request);
  }

  /**
   * Create a {@link SuffixBuilder} that keeps only the suffix parts matched by the given filter when constructing
   * a new suffix
   * @param request Sling request
   * @param suffixPartFilter the filter that is called for each suffix part
   */
  public SuffixBuilder(@NotNull SlingHttpServletRequest request, @NotNull Predicate<String> suffixPartFilter) {
    this(request, new FilteringSuffixStateStrategy(suffixPartFilter));
  }

  /**
   * @return a {@link SuffixBuilder} that discards all existing suffix state when constructing a new suffix
   */
  public static @NotNull SuffixBuilder thatDiscardsAllSuffixState() {
    return new SuffixBuilder();
  }

  /**
   * @param request Sling request
   * @return a {@link SuffixBuilder} that discards everything but the *resource* parts of the suffix
   */
  public static @NotNull SuffixBuilder thatKeepsResourceParts(@NotNull SlingHttpServletRequest request) {
    Predicate<String> filter = new IncludeResourcePartsFilter();
    return new SuffixBuilder(request, filter);
  }

  /**
   * @param request Sling request
   * @param keysToKeep Keys to keep
   * @return a {@link SuffixBuilder} that keeps only the named key/value-parts defined by pKeysToKeep
   */
  public static @NotNull SuffixBuilder thatKeepsNamedParts(@NotNull SlingHttpServletRequest request,
      @NotNull String @NotNull ... keysToKeep) {
    Predicate<String> filter = new IncludeNamedPartsFilter(keysToKeep);
    return new SuffixBuilder(request, filter);
  }

  /**
   * @param request Sling request
   * @param keysToKeep Keys to keep
   * @return a {@link SuffixBuilder} that keeps the named key/value-parts defined by pKeysToKeep and all resource
   *         parts
   */
  public static @NotNull SuffixBuilder thatKeepsNamedPartsAndResources(@NotNull SlingHttpServletRequest request,
      @NotNull String @NotNull... keysToKeep) {
    Predicate<String> filter = FilterOperators.or(new IncludeResourcePartsFilter(), new IncludeNamedPartsFilter(keysToKeep));
    return new SuffixBuilder(request, filter);
  }

  /**
   * @param request Sling request
   * @return a {@link SuffixBuilder} that keeps all parts from the current request's suffix when constructing a new
   *         suffix
   */
  public static @NotNull SuffixBuilder thatKeepsAllParts(@NotNull SlingHttpServletRequest request) {
    return new SuffixBuilder(request, new IncludeAllPartsFilter());
  }

  /**
   * @param request Sling request
   * @return a {@link SuffixBuilder} that will discard the resource parts, but keep all named key/value-parts
   */
  public static @NotNull SuffixBuilder thatDiscardsResourceParts(@NotNull SlingHttpServletRequest request) {
    ExcludeResourcePartsFilter filter = new ExcludeResourcePartsFilter();
    return new SuffixBuilder(request, filter);
  }

  /**
   * @param request Sling request
   * @param keysToDiscard the keys of the named parts to discard
   * @return a {@link SuffixBuilder} that will keep all parts except those named key/value-parts defined by
   *         pKeysToDiscard
   */
  public static @NotNull SuffixBuilder thatDiscardsNamedParts(@NotNull SlingHttpServletRequest request,
      @NotNull String @NotNull... keysToDiscard) {
    return new SuffixBuilder(request, new ExcludeNamedPartsFilter(keysToDiscard));
  }

  /**
   * @param request Sling request
   * @param keysToDiscard the keys of the named parts to discard
   * @return {@link SuffixBuilder} that will discard all resource parts and the named parts defined by pKeysToDiscard
   */
  public static @NotNull SuffixBuilder thatDiscardsResourceAndNamedParts(@NotNull SlingHttpServletRequest request,
      @NotNull String @NotNull... keysToDiscard) {
    Predicate<String> filter = FilterOperators.and(new ExcludeResourcePartsFilter(), new ExcludeNamedPartsFilter(keysToDiscard));
    return new SuffixBuilder(request, filter);
  }

  /**
   * @param request Sling request
   * @param resourcePathToDiscard relative path of the resource to discard
   * @param keysToDiscard the keys of the named parts to discard
   * @return {@link SuffixBuilder} that will discard *one specific resource path* and the named parts defined by
   *         pKeysToDiscard
   */
  public static @NotNull SuffixBuilder thatDiscardsSpecificResourceAndNamedParts(@NotNull SlingHttpServletRequest request,
      @NotNull String resourcePathToDiscard, @NotNull String @NotNull... keysToDiscard) {
    Predicate<String> filter = FilterOperators.and(new ExcludeSpecificResourceFilter(resourcePathToDiscard), new ExcludeNamedPartsFilter(keysToDiscard));
    return new SuffixBuilder(request, filter);
  }

  /**
   * Puts a key-value pair into the suffix.
   * @param key the key
   * @param value the value
   * @return this
   */
  @SuppressWarnings({ "null", "unused" })
  public @NotNull SuffixBuilder put(@NotNull String key, @NotNull Object value) {
    if (key == null) {
      throw new IllegalArgumentException("Key must not be null");
    }
    if (value != null) {
      validateValueType(value);
      parameterMap.put(key, value);
    }
    return this;
  }

  /**
   * Puts a map of key-value pairs into the suffix.
   * @param map map of key-value pairs
   * @return this
   */
  public @NotNull SuffixBuilder putAll(@NotNull Map<String, Object> map) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
    return this;
  }

  private void validateValueType(Object value) {
    Class clazz = value.getClass();
    boolean isValid = (clazz == String.class
        || clazz == Boolean.class
        || clazz == Integer.class
        || clazz == Long.class);
    if (!isValid) {
      throw new IllegalArgumentException("Unsupported value type: " + clazz.getName());
    }
  }

  /**
   * Puts a relative path of a resource into the suffix.
   * @param resource the resource
   * @param suffixBaseResource the base resource used to construct the relative path
   * @return this
   */
  public @NotNull SuffixBuilder resource(@NotNull Resource resource, @NotNull Resource suffixBaseResource) {
    // get relative path to base resource
    String relativePath = getRelativePath(resource, suffixBaseResource);
    resourcePaths.add(relativePath);
    return this;
  }

  /**
   * Constructs a suffix that contains multiple key-value pairs and address resources. Depending on the
   * {@link SuffixStateKeepingStrategy}, the suffix contains
   * further parts from the current request that should be kept when constructing new links.
   * @param resources resources to address
   * @param baseResource base resource to construct relative path
   * @return the suffix containing the map-content as encoded key value-pairs (and eventually other parts)
   */
  public @NotNull SuffixBuilder resources(@NotNull List<Resource> resources, @NotNull Resource baseResource) {
    for (Resource resource : resources) {
      resource(resource, baseResource);
    }
    return this;
  }

  /**
   * Puts a relative path of a page into the suffix.
   * @param page the page
   * @param suffixBasePage the base page used to construct the relative path
   * @return this
   */
  public @NotNull SuffixBuilder page(@NotNull Page page, @NotNull Page suffixBasePage) {
    return resource(AdaptTo.notNull(page, Resource.class), AdaptTo.notNull(suffixBasePage, Resource.class));
  }

  /**
   * Constructs a suffix that contains multiple key-value pairs and address pages. Depending on the
   * {@link SuffixStateKeepingStrategy}, the suffix contains
   * further parts from the current request that should be kept when constructing new links.
   * @param pages pages to address
   * @param suffixBasePage the base page used to construct the relative path
   * @return this
   */
  @SuppressWarnings("null")
  public @NotNull SuffixBuilder pages(@NotNull List<Page> pages, @NotNull Page suffixBasePage) {
    List<Resource> resources = Lists.transform(pages, new Function<Page, Resource>() {
      @Override
      public Resource apply(Page page) {
        return page.adaptTo(Resource.class);
      }
    });
    return resources(resources, AdaptTo.notNull(suffixBasePage, Resource.class));
  }

  /**
   * Build complete suffix.
   * @return the suffix
   */
  public @NotNull String build() {
    SortedMap<String, Object> sortedParameterMap = new TreeMap<>(parameterMap);

    // gather resource paths in a treeset (having them in a defined order helps with caching)
    SortedSet<String> resourcePathsSet = new TreeSet<>();

    // iterate over all parts that should be kept from the current request
    for (String nextPart : initialSuffixParts) {
      // if this is a key-value-part:
      if (nextPart.indexOf(KEY_VALUE_DELIMITER) > 0) {
        String key = decodeKey(nextPart);
        // decode and keep the part if it is not overridden in the given parameter-map
        if (!sortedParameterMap.containsKey(key)) {
          String value = decodeValue(nextPart);
          sortedParameterMap.put(key, value);
        }
      }
      else {
        // decode and keep the resource paths (unless they are specified again in resourcePaths)
        String path = decodeResourcePathPart(nextPart);
        if (!resourcePaths.contains(path)) {
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
    for (Entry<String, Object> entry : sortedParameterMap.entrySet()) {
      Object value = entry.getValue();
      if (value == null) {
        // don't add suffix part if value is null
        continue;
      }
      String encodedKey = encodeKeyValuePart(entry.getKey());
      String encodedValue = encodeKeyValuePart(value.toString());
      suffixParts.add(encodedKey + KEY_VALUE_DELIMITER + encodedValue);
    }

    // finally join these parts to a single string
    return StringUtils.join(suffixParts, SUFFIX_PART_DELIMITER);
  }

}
