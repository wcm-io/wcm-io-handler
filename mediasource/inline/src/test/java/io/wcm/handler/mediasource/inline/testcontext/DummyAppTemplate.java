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
package io.wcm.handler.mediasource.inline.testcontext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * List of templates with special handling in code.
 */
public enum DummyAppTemplate {

  /**
   * Structure element
   */
  CONTENT("/apps/test/templates/content");

  private final String templatePath;
  private final String resourceType;

  private DummyAppTemplate(String templatePath) {
    this.templatePath = templatePath;

    // build resource type from template path
    String resourceTypeFromTemplatePath = null;
    final Pattern TEMPLATE_PATH_PATTERN = Pattern.compile("^/apps/([^/]+)/templates(/.*)?/([^/]+)$");
    Matcher templateParts = TEMPLATE_PATH_PATTERN.matcher(templatePath);
    if (templateParts.matches()) {
      resourceTypeFromTemplatePath = "/apps/" + templateParts.group(1) + "/components" + StringUtils.defaultString(templateParts.group(2))
          + "/page/" + templateParts.group(3);
    }

    this.resourceType = resourceTypeFromTemplatePath;
  }

  private DummyAppTemplate(String templatePath, String resourceType) {
    this.templatePath = templatePath;
    this.resourceType = resourceType;
  }

  /**
   * Template path
   * @return Path
   */
  public String getTemplatePath() {
    return this.templatePath;
  }

  /**
   * Resource type
   * @return Path
   */
  public String getResourceType() {
    return this.resourceType;
  }

}
