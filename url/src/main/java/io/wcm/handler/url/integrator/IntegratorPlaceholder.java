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
package io.wcm.handler.url.integrator;

import org.apache.commons.lang3.StringUtils;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Placeholders used in integrator template markup that has to be replaced by integrating applications.
 */
@ProviderType
public final class IntegratorPlaceholder {

  private IntegratorPlaceholder() {
    // constants only
  }

  /**
   * Placeholder for the main content markup of the external application.
   */
  public static final String APP_INCLUDE_CONTENT = "###APP_INCLUDE_CONTENT###";

  /**
   * Placeholder for markup of the external application that should be placed in the HTML HEAD element.
   */
  public static final String APP_INCLUDE_HEADER = "###APP_INCLUDE_HEADER###";

  /**
   * Placeholder for markup of the external application that should be placed before the end of the BODY element.
   */
  public static final String APP_INCLUDE_FOOTER = "###APP_INCLUDE_FOOTER###";

  /**
   * Placeholder for scheme and hostname in URLs pointing to content pages (non-secure mode, HTTP).
   */
  public static final String URL_CONTENT = "###URL_CONTENT###";

  /**
   * Placeholder for scheme and hostname in URLs pointing to content pages (secure mode, HTTPS).
   */
  public static final String URL_CONTENT_SECURE = "###URL_CONTENT_SECURE###";

  /**
   * Placeholder for scheme and hostname in URLs pointing to resources (e.g. CSS/JS/Image references or AJAX requests).
   * Secure- or non-secure mode depends on the external application. If AJAX requests are blocked by the
   * same origin policy the external application may decide to route these URLs through a proxy with its own
   * scheme and hostname.
   */
  public static final String URL_CONTENT_PROXY = "###URL_CONTENT_PROXY###";


  /**
   * Placeholder with HTML BEGIN and END comments
   * @param placeholder Placeholder
   * @return Tag with comments
   */
  public static String getTagWithComments(String placeholder) {
    return "\n<!-- " + getPlaceholderName(placeholder) + " START -->\n"
        + placeholder
        + "\n<!-- " + getPlaceholderName(placeholder) + " END -->\n";
  }

  /**
   * Get placeholder name without surrounding ###
   * @param placeholder Placeholder
   * @return Placeholder name
   */
  private static String getPlaceholderName(String placeholder) {
    return StringUtils.remove(placeholder, "###");
  }

}
