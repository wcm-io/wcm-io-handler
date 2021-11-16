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
package io.wcm.handler.url.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.sling.commons.util.Escape;

/**
 * Utility methods for externalizing URLs.
 */
final class Externalizer {

  private Externalizer() {
    // static util methods only
  }

  /**
   * Externalizes an URL by applying Sling Mapping. Hostname and scheme are not added because they are added by the
   * link handler depending on site URL configuration and secure/non-secure mode. URLs that are already externalized
   * remain untouched.
   * @param url Unexternalized URL (without scheme or hostname)
   * @param resolver Resource resolver
   * @param request Request
   * @return Exernalized URL without scheme or hostname, but with short URLs (if configured in Sling Mapping is
   *         configured), and the path is URL-encoded if it contains special chars.
   */
  public static @Nullable String externalizeUrl(@NotNull String url, @NotNull ResourceResolver resolver, @Nullable SlingHttpServletRequest request) {
    return externalizeUrlWithSlingMapping(url, resolver, request, false);
  }

  /**
   * Externalizes a URL by applying Sling Mapping. Hostname and scheme will be added. URLs that are already externalized
   * remain untouched.
   * @param url non-externalized URL (without scheme or hostname)
   * @param resolver Resource resolver
   * @param request Request
   * @return Externalized URL with scheme or hostname, short URLs (if configured in Sling Mapping),
   *         and the path is URL-encoded if it contains special chars.
   */
  public static @Nullable String externalizeUrlWithHost(@NotNull String url, @NotNull ResourceResolver resolver, @Nullable SlingHttpServletRequest request) {
    return externalizeUrlWithSlingMapping(url, resolver, request, true);
  }

  private static @Nullable String externalizeUrlWithSlingMapping(@NotNull String url, @NotNull ResourceResolver resolver,
      @Nullable SlingHttpServletRequest request, boolean keepHost) {

    // apply externalization only path part
    String path = url;

    // split off query string or fragment that may be appended to the URL
    String urlRemainder = null;
    int urlRemainderPos = StringUtils.indexOfAny(path, '?', '#');
    if (urlRemainderPos >= 0) {
      urlRemainder = path.substring(urlRemainderPos);
      path = path.substring(0, urlRemainderPos);
    }

    // apply reverse mapping based on current sling mapping configuration for current request
    // e.g. to support a host-based prefix stripping mapping configuration configured at /etc/map

    // please note: the sling map method does a lot of things:
    // 1. applies reverse mapping depending on the sling mapping configuration
    // (this can even add a hostname if defined in sling mapping configuration)
    // 2. applies namespace mangling (e.g. replace jcr: with _jcr_)
    // 3. adds webapp context path if required
    // 4. url-encodes the whole url
    if (request != null) {
      path = resolver.map(request, path);
    }
    else {
      path = resolver.map(path);
    }

    if (!keepHost) {
      // remove scheme and hostname (probably added by sling mapping), but leave path in escaped form
      try {
        path = new URI(path).getRawPath();
        // replace %2F back to / for better readability
        path = StringUtils.replace(path, "%2F", "/");
      } catch (URISyntaxException ex) {
        throw new RuntimeException("Sling map method returned invalid URI: " + path, ex);
      }
    }

    // build full URL again
    if (path == null) {
      return null;
    }
    else {
      return path + (urlRemainder != null ? urlRemainder : "");
    }
  }

  /**
   * Externalizes an URL without applying Sling Mapping. Instead the servlet context path is added and sling namespace
   * mangling is applied manually.
   * Hostname and scheme are not added because they are added by the link handler depending on site URL configuration
   * and secure/non-secure mode. URLs that are already externalized remain untouched.
   * @param url Unexternalized URL (without scheme or hostname)
   * @param request Request
   * @return Exernalized URL without scheme or hostname, the path is URL-encoded if it contains special chars.
   */
  public static @NotNull String externalizeUrlWithoutMapping(@NotNull String url, @Nullable SlingHttpServletRequest request) {

    // apply externalization only path part
    String path = url;

    // split off query string or fragment that may be appended to the URL
    String urlRemainder = null;
    int urlRemainderPos = StringUtils.indexOfAny(path, '?', '#');
    if (urlRemainderPos >= 0) {
      urlRemainder = path.substring(urlRemainderPos);
      path = path.substring(0, urlRemainderPos);
    }

    // apply namespace mangling (e.g. replace jcr: with _jcr_)
    path = mangleNamespaces(path);

    // add webapp context path
    if (request != null) {
      path = StringUtils.defaultString(request.getContextPath()) + path; //NOPMD
    }

    // url-encode path
    path = Escape.urlEncode(path);
    path = StringUtils.replace(path, "+", "%20");
    // replace %2F back to / for better readability
    path = StringUtils.replace(path, "%2F", "/");

    // build full URL again
    return path + (urlRemainder != null ? urlRemainder : "");
  }

  /*
   * Detect as externalized:
   * - everything staring with protocol and a colon is handled as externalized (http:, tel:, mailto:, javascript: etc.)
   * - everything starting with // or # is handles as exteranlized
   * - all other strings handles as not externalized
   */
  private static final Pattern EXTERNALIZED_PATTERN = Pattern.compile("^([^/]+:|//|#).*$");

  /**
   * Checks if the given URL is already externalized.
   * For this check some heuristics are applied.
   * @param url URL
   * @return true if path is already externalized.
   */
  public static boolean isExternalized(@NotNull String url) {
    return EXTERNALIZED_PATTERN.matcher(url).matches();
  }

  /**
   * Checks if the given URL can be externalize, that means seems to be an content path that needs externalization.
   * @param url URL
   * @return true if url seems to be a path than needs externaliziation
   */
  public static boolean isExternalizable(@NotNull String url) {
    return StringUtils.startsWith(url, "/");
  }

  private static final String MANGLED_NAMESPACE_PREFIX = "/_";
  private static final String MANGLED_NAMESPACE_SUFFIX = "_";
  private static final char NAMESPACE_SEPARATOR = ':';
  private static final Pattern NAMESPACE_PATTERN = Pattern.compile("/([^:/]+):");

  /**
   * Mangle the namespaces in the given path for usage in sling-based URLs.
   * <p>
   * Example: /path/jcr:content to /path/_jcr_content
   * </p>
   * @param path Path to mangle
   * @return Mangled path
   */
  public static @NotNull String mangleNamespaces(@NotNull String path) {
    if (!StringUtils.contains(path, NAMESPACE_SEPARATOR)) {
      return path;
    }
    Matcher matcher = NAMESPACE_PATTERN.matcher(path);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      String replacement = MANGLED_NAMESPACE_PREFIX + matcher.group(1) + MANGLED_NAMESPACE_SUFFIX;
      matcher.appendReplacement(sb, replacement);
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

}
