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
package io.wcm.handler.richtext.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Entity resolver for XHtml DTD and Entities.
 * Loads XHtml resources/entity set from package de.gedas.day.gcq.richtext.xhtml.
 */
final class XHtmlEntityResolver implements EntityResolver {

  private static final XHtmlEntityResolver INSTANCE = new XHtmlEntityResolver();

  private final Map<String, String> xhtmlResourceMap = new HashMap<String, String>();
  private final String resourceFolder = "/" + getClass().getPackage().getName().replace('.', '/') + "/xhtml";

  private XHtmlEntityResolver() {
    // cache public id's from xhtml resources
    for (XHtmlResource resource : EnumSet.allOf(XHtmlResource.class)) {
      xhtmlResourceMap.put(resource.getPublicId(), resource.getFilename());
    }
  }

  /**
   * XHtmlEntityResolver instance.
   * @return XHtmlEntityResolver instance.
   */
  public static XHtmlEntityResolver getInstance() {
    return INSTANCE;
  }

  /**
   * Resolve XHtml resource entities, load from classpath resources.
   */
  @Override
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

    String filename = xhtmlResourceMap.get(publicId);
    if (filename != null) {
      String resourceName = resourceFolder + "/" + filename;
      InputStream is = XHtmlEntityResolver.class.getResourceAsStream(resourceName);

      if (is == null) {
        throw new IOException("Resource '" + resourceName + "' not found in class path.");
      }

      return new InputSource(is);
    }

    return null;
  }

}
