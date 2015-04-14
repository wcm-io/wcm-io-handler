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
package io.wcm.handler.url.rewriter.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for {@link UrlExternalizerTransformer}.
 */
class UrlExternalizerTransformerConfig {

  static final String PN_REWRITE_ELEMENTS = "rewriteElements";

  private static final String[] REWRITE_ELEMENTS_DEFAULT = {
    "img:src", "link:href", "script:src"
  };

  private static final String ELEMENT_ATTRIBUTE_SEPARATOR = ":";

  private final Map<String, String> elementAttributeNames;

  private static final Logger log = LoggerFactory.getLogger(UrlExternalizerTransformerConfig.class.getName());

  public UrlExternalizerTransformerConfig(ValueMap config) {
    this.elementAttributeNames = toElementAttributeNamesMap(config.get(PN_REWRITE_ELEMENTS, REWRITE_ELEMENTS_DEFAULT));
  }

  private static Map<String, String> toElementAttributeNamesMap(String[] elementAttributeNames) {
    Map<String, String> map = new HashMap<>();
    for (String item : elementAttributeNames) {
      String elementName = StringUtils.trim(StringUtils.substringBefore(item, ELEMENT_ATTRIBUTE_SEPARATOR));
      String attributeName = StringUtils.trim(StringUtils.substringAfter(item, ELEMENT_ATTRIBUTE_SEPARATOR));
      if (StringUtils.isBlank(elementName) || StringUtils.isBlank(attributeName)) {
        log.info("Invalid URL externalizier transformer configuration - skipping invalid element entry: " + item);
      }
      else if (map.containsKey(elementName)) {
        log.info("Invalid URL externalizier transformer configuration - skipping duplicate element name: " + item);
      }
      else {
        map.put(elementName, attributeName);
      }
    }
    return map;
  }

  public Map<String, String> getElementAttributeNames() {
    return this.elementAttributeNames;
  }

}
