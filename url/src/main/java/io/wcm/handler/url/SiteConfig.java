/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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

import org.apache.sling.caconfig.annotation.Configuration;
import org.apache.sling.caconfig.annotation.Property;

/**
 * Context-Aware URL Handler Site configuration.
 */
@Configuration(label = "wcm.io Handler Site URLs",
    description = "Defines Site URLs for Author and Publish environments.")
public @interface SiteConfig {

  /**
   * @return Site URL
   */
  @Property(label = "Site URL",
      description = "Public website URL for non-secure access.")
  String siteUrl();

  /**
   * @return Site URL Secure
   */
  @Property(label = "Site URL Secure",
      description = "Public website URL for secure access.")
  String siteUrlSecure();

  /**
   * @return Author URL
   */
  @Property(label = "Author URL",
      description = "URL for author instance.")
  String siteUrlAuthor();

}
