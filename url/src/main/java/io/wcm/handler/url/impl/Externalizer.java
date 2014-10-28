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

/**
 * Utility methods for externalizing URLs.
 */
final class Externalizer {

  private Externalizer() {
    // static util methods only
  }

  /**
   * Externalizes an URL by applying Sling Mapping. Hostname and scheme are not added because they are added by the
   * link handler depending on site URL configuratoin and secure/non-secure mode. URLs that are already externalized
   * remain untouched.
   * @param url Unexternalized URL
   * @param resolver Resource resolver
   * @param request Request
   * @return Exernalized URL
   */
  public static String externalizeUrl(String url, ResourceResolver resolver, SlingHttpServletRequest request) {

    // apply externalization only path part
    String path = url;

    // split off query string or fragment that may be appendend to the URL
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

    // remove scheme and hostname (probably added by sling mapping), but leave path in escaped form
    try {
      path = new URI(path).getRawPath();
      // replace %2F back to / for better readability
      path = StringUtils.replace(path, "%2F", "/");
    }
    catch (URISyntaxException ex) {
      throw new RuntimeException("Sling map method returned invalid URI: " + path, ex);
    }

    // build full URL again
    return path + (urlRemainder != null ? urlRemainder : "");
  }

  /**
   * Externalizes an URL without applying Sling Mapping. Instead the servlet context path is added and sling namespace
   * mangling is applied manually.
   * Hostname and scheme are not added because they are added by the link handler depending on site URL configuratoin
   * and secure/non-secure mode. URLs that are already externalized remain untouched.
   * @param url Unexternalized URL
   * @param request Request
   * @return Exernalized URL
   */
  public static String externalizeUrlWithoutMapping(String url, SlingHttpServletRequest request) {

    // apply externalization only path part
    String path = url;

    // split off query string or fragment that may be appendend to the URL
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

    // build full URL again
    return path + (urlRemainder != null ? urlRemainder : "");
  }

  /**
   * Checks if the given URL is already externalized.
   * For this check some heuristics are applied.
   * @param url URL
   * @return true if path is already externalized.
   */
  public static boolean isExternalized(String url) {
    return StringUtils.contains(url, "://") // protocol detected
        || StringUtils.startsWith(url, "//") // protocol-relative mode detected
        || StringUtils.startsWith(url, "mailto:") // mailto link detected
        || StringUtils.startsWith(url, "#"); // anchor or integrator placeholder detected
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
  public static String mangleNamespaces(String path) {
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
