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

import static io.wcm.config.api.ParameterBuilder.create;
import static io.wcm.config.editor.EditorProperties.DESCRIPTION;
import static io.wcm.config.editor.EditorProperties.GROUP;
import static io.wcm.config.editor.EditorProperties.PARAMETER_LABEL;
import static io.wcm.handler.url.impl.ApplicationProviderImpl.APPLICATION_ID;
import static io.wcm.handler.url.impl.ApplicationProviderImpl.APPLICATION_LABEL;
import io.wcm.config.api.Parameter;
import io.wcm.config.editor.WidgetTypes;

/**
 * Configuration parameters for URL handler.
 */
public final class UrlParams {

  private UrlParams() {
    // constants only
  }

  /**
   * Site URL on public access from outside, for non-secure access (HTTP).
   */
  public static final Parameter<String> SITE_URL = create("siteUrl", String.class, APPLICATION_ID)
      .property(PARAMETER_LABEL, "Site URL")
      .property(DESCRIPTION, "Public website URL for non-secure access.")
      .property(GROUP, APPLICATION_LABEL)
      .properties(WidgetTypes.TEXTFIELD.getDefaultWidgetConfiguration())
      .build();

  /**
   * Site URL for public access from outside, for secure access (HTTPS).
   */
  public static final Parameter<String> SITE_URL_SECURE = create("siteUrlSecure", String.class, APPLICATION_ID)
      .property(PARAMETER_LABEL, "Site URL Secure")
      .property(DESCRIPTION, "Public website URL for secure access.")
      .property(GROUP, APPLICATION_LABEL)
      .properties(WidgetTypes.TEXTFIELD.getDefaultWidgetConfiguration())
      .build();

  /**
   * Site URL on author instance.
   */
  public static final Parameter<String> SITE_URL_AUTHOR = create("siteUrlAuthor", String.class, APPLICATION_ID)
      .property(PARAMETER_LABEL, "Author URL")
      .property(DESCRIPTION, "URL for author instance.")
      .property(GROUP, APPLICATION_LABEL)
      .properties(WidgetTypes.TEXTFIELD.getDefaultWidgetConfiguration())
      .build();

}
