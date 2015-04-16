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

import io.wcm.handler.url.impl.modes.DefaultUrlMode;
import io.wcm.handler.url.impl.modes.FullUrlForceNonSecureUrlMode;
import io.wcm.handler.url.impl.modes.FullUrlForceSecureUrlMode;
import io.wcm.handler.url.impl.modes.FullUrlProtocolRelativeUrlMode;
import io.wcm.handler.url.impl.modes.FullUrlUrlMode;
import io.wcm.handler.url.impl.modes.NoHostnameUrlMode;

/**
 * Default URL modes sufficient for the most usecases.
 */
public final class UrlModes {

  private UrlModes() {
    // constants only
  }

  /**
   * Default mode: Does generate a full externalized URL only if both siteUrl and siteUrlSecure parameter
   * are set in context-specific configuration. If not set, only URLs without hostname are generated.
   * <p>
   * If the target is an internal content page, siteUrl or siteUrlSecure is chosen automatically depending on the secure
   * state of the page.
   * </p>
   */
  public static final UrlMode DEFAULT = new DefaultUrlMode();

  /**
   * Default mode: Does generate a externalized URL without any protocol and hostname,
   * independent of any setting in context-specific configuration.
   */
  public static final UrlMode NO_HOSTNAME = new NoHostnameUrlMode();

  /**
   * Enforce the generation of a full URL with protocol and hostname.
   * <p>
   * If the target is an internal content page, siteUrl or siteUrlSecure is chosen automatically depending on the secure
   * state of page.
   * </p>
   * <p>
   * On author instance always the author URL is used.
   * </p>
   */
  public static final UrlMode FULL_URL = new FullUrlUrlMode(false);

  /**
   * Enforce the generation of a full URL with protocol and hostname and non-secure mode.
   * <p>
   * On author instance always the author URL is used.
   * </p>
   */
  public static final UrlMode FULL_URL_FORCENONSECURE = new FullUrlForceNonSecureUrlMode(false);

  /**
   * Enforce the generation of a full URL with protocol and hostname and secure mode.
   * <p>
   * If siteUrlSecure is not set, siteUrl is used.
   * </p>
   * <p>
   * On author instance always the author URL is used.
   * </p>
   */
  public static final UrlMode FULL_URL_FORCESECURE = new FullUrlForceSecureUrlMode(false);

  /**
   * Enforce the generation of a full URL with hostname and "//" as protocol (protocol-relative mode).
   * <p>
   * Using "//" instead of "http://" or "https://" results in using the same protocol as the current request in the
   * browser.
   * </p>
   * <p>
   * On author instance always the author URL is used.
   * </p>
   */
  public static final UrlMode FULL_URL_PROTOCOLRELATIVE = new FullUrlProtocolRelativeUrlMode(false);

  /**
   * Enforce the generation of a full URL with protocol and hostname.
   * <p>
   * If the target is an internal content page, siteUrl or siteUrlSecure is chosen automatically depending on the secure
   * state of page.
   * </p>
   * <p>
   * Always uses the Site URLs configured for the publish instance, even if running on an author instance.
   * </p>
   */
  public static final UrlMode FULL_URL_PUBLISH = new FullUrlUrlMode(true);

  /**
   * Enforce the generation of a full URL with protocol and hostname and non-secure mode.
   * <p>
   * Always uses the Site URLs configured for the publish instance, even if running on an author instance.
   * </p>
   */
  public static final UrlMode FULL_URL_PUBLISH_FORCENONSECURE = new FullUrlForceNonSecureUrlMode(true);

  /**
   * Enforce the generation of a full URL with protocol and hostname and secure mode.
   * <p>
   * If siteUrlSecure is not set, siteUrl is used.
   * </p>
   * <p>
   * Always uses the Site URLs configured for the publish instance, even if running on an author instance.
   * </p>
   */
  public static final UrlMode FULL_URL_PUBLISH_FORCESECURE = new FullUrlForceSecureUrlMode(true);

  /**
   * Enforce the generation of a full URL with hostname and "//" as protocol (protocol-relative mode).
   * <p>
   * Using "//" instead of "http://" or "https://" results in using the same protocol as the current request in the
   * browser.
   * </p>
   * <p>
   * Always uses the Site URLs configured for the publish instance, even if running on an author instance.
   * </p>
   */
  public static final UrlMode FULL_URL_PUBLISH_PROTOCOLRELATIVE = new FullUrlProtocolRelativeUrlMode(true);

}
