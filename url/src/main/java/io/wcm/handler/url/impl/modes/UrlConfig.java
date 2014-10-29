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
package io.wcm.handler.url.impl.modes;

import io.wcm.config.api.Configuration;
import io.wcm.handler.url.UrlParams;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.Adaptable;

/**
 * Helper class for accessing site url configuration in URL mode implementation.
 */
class UrlConfig {

  private final Configuration config;
  private final String siteUrl;
  private final String siteUrlSecure;
  private final String siteUrlAuthor;

  public UrlConfig(Adaptable adaptable) {
    this.config = adaptable.adaptTo(Configuration.class);
    if (this.config != null) {
      this.siteUrl = config.get(UrlParams.SITE_URL);
      this.siteUrlSecure = StringUtils.defaultString(config.get(UrlParams.SITE_URL_SECURE), this.siteUrl);
      this.siteUrlAuthor = config.get(UrlParams.SITE_URL_AUTHOR);
    }
    else {
      this.siteUrl = null;
      this.siteUrlSecure = null;
      this.siteUrlAuthor = null;
    }
  }

  /**
   * @return Site URL
   */
  public String getSiteUrl() {
    return this.siteUrl;
  }

  /**
   * @return Site URL secure (fallback to Site URL)
   */
  public String getSiteUrlSecure() {
    return this.siteUrlSecure;
  }

  /**
   * @return Site URL author
   */
  public String getSiteUrlAuthor() {
    return this.siteUrlAuthor;
  }

  /**
   * @return true if at least Site URL is set
   */
  public boolean isValid() {
    return StringUtils.isNotEmpty(this.siteUrl);
  }

  /**
   * @return true if site url for author is set
   */
  public boolean hasSiteUrlAuthor() {
    return StringUtils.isNotEmpty(this.siteUrlAuthor);
  }

}
