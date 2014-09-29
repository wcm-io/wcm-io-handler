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
import io.wcm.config.api.Parameter;
import io.wcm.config.editor.widgets.EditorNameConstants;
import io.wcm.config.editor.widgets.WidgetTypes;

/**
 * Configuration parameters for URL handler.
 */
public final class UrlParams {

  private UrlParams() {
    // constants only
  }

  /**
   * Application ID
   */
  public static final String APPLICATION_ID = "/apps/wcm-io/handler/url";

  /**
   * Site URL on public access from outside, for non-secure access (HTTP).
   */
  public static final Parameter<String> SITE_URL = create("siteUrl", String.class, APPLICATION_ID).
      properties(WidgetTypes.TEXTFIELD.getDefaultWidgetConfiguration()).property(EditorNameConstants.PN_GROUP, "Link Handling").
      property(EditorNameConstants.PN_DESCRIPTION, "Configuration of the site host.").build();


  /**
   * Site URL for public access from outside, for secure access (HTTPS).
   */
  public static final Parameter<String> SITE_URL_SECURE = create("siteUrlSecure", String.class, APPLICATION_ID).
      properties(WidgetTypes.TEXTFIELD.getDefaultWidgetConfiguration()).property(EditorNameConstants.PN_GROUP, "Link Handling").
      property(EditorNameConstants.PN_DESCRIPTION, "Configuration of the secure site host.").build();

  /**
   * Site URL on author instance.
   */
  public static final Parameter<String> SITE_URL_AUTHOR = create("siteUrlAuthor", String.class, APPLICATION_ID).
      properties(WidgetTypes.TEXTFIELD.getDefaultWidgetConfiguration()).property(EditorNameConstants.PN_GROUP, "Link Handling").
      property(EditorNameConstants.PN_DESCRIPTION, "Configuration of the author site host.").build();

}
