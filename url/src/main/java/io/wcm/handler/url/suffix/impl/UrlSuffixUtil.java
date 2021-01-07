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
package io.wcm.handler.url.suffix.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.apache.sling.api.resource.Resource;

/**
 * Static methods and constants for URL suffix handling.
 */
@SuppressWarnings("deprecation")
public final class UrlSuffixUtil {

  /**
   * Delimiter char for suffix parts and key/value pairs
   */
  public static final char SUFFIX_PART_DELIMITER = '/';

  /**
   * Delimiter char for suffix parts and key and value
   */
  public static final char KEY_VALUE_DELIMITER = '=';

  /**
   * Double escaping is necessary when constructing urls so that escaping is not resolved by the webserver
   */
  public static final String ESCAPE_DELIMITER = "~";

  /**
   * Slash value within suffix part
   */
  public static final String ESCAPED_SLASH = ESCAPE_DELIMITER + hexCode('/');

  /**
   * Map with special chars and their replacements that are escaped with special ~ and hexcode
   */
  private static final String[][] SPECIAL_CHARS_MAPPING = {
    // escape delimiter chars
    {
      Character.toString(SUFFIX_PART_DELIMITER), ESCAPE_DELIMITER + hexCode(SUFFIX_PART_DELIMITER)
    },
    {
      Character.toString(KEY_VALUE_DELIMITER), ESCAPE_DELIMITER + hexCode(KEY_VALUE_DELIMITER)
    },
    // '.' must be custom-escaped (if no file extension is added to suffix,
    // anything after a dot would be interpreted as file extension during parsing)
    {
      Character.toString('.'), ESCAPE_DELIMITER + hexCode('.')
    },
    // escape '%' to avoid confusion with URL escaping
    {
      Character.toString('%'), ESCAPE_DELIMITER + hexCode('%')
    },
    // '/' must be custom-escaped (dispatcher/webserver may filter out/misinterpret urls with unescaped slashes)
    {
      Character.toString('/'), ESCAPE_DELIMITER + hexCode('/')
    },
    // escape ':'
    {
      Character.toString(':'), ESCAPE_DELIMITER + hexCode(':')
    },
    // escape ' ' as well (singular problem occurred once)
    {
      Character.toString(' '), ESCAPE_DELIMITER + hexCode(' ')
    }
  };

  /**
   * Escape special chars for suffix.
   */
  private static final CharSequenceTranslator ESCAPE_SPECIAL_CHARS = new LookupTranslator(SPECIAL_CHARS_MAPPING);

  /**
   * Unesacpe special chars in suffix.
   */
  private static final CharSequenceTranslator UNESCAPE_SPECIAL_CHARS = new LookupTranslator(EntityArrays.invert(SPECIAL_CHARS_MAPPING));

  private UrlSuffixUtil() {
    // static methods only
  }

  /**
   * Convert to hex code
   * @param c char
   * @return Hex code
   */
  public static String hexCode(char c) {
    return Integer.toString(c, 16).toUpperCase();
  }

  /**
   * Encode resource path part
   * @param relativePath Relative path
   * @return Encodes path part
   */
  public static String encodeResourcePathPart(String relativePath) {
    return ESCAPE_SPECIAL_CHARS.translate(relativePath);
  }

  /**
   * Decode resource path part
   * @param suffixPart Suffix part
   * @return Decoded path part
   */
  public static String decodeResourcePathPart(String suffixPart) {
    return UNESCAPE_SPECIAL_CHARS.translate(suffixPart);
  }

  /**
   * Encode key value part
   * @param string String
   * @return Encoded string
   */
  public static String encodeKeyValuePart(String string) {
    return ESCAPE_SPECIAL_CHARS.translate(string);
  }

  /**
   * Decode value
   * @param suffixPart Suffix part
   * @return Decoded value
   */
  public static String decodeValue(String suffixPart) {
    // value is the part *after* KEY_VALUE_DELIMITER
    String value = StringUtils.substringAfter(suffixPart, Character.toString(KEY_VALUE_DELIMITER));
    return UNESCAPE_SPECIAL_CHARS.translate(value);
  }

  /**
   * Decode key
   * @param suffixPart Suffix part
   * @return Decoded key
   */
  public static String decodeKey(String suffixPart) {
    // key is the part *before* KEY_VALUE_DELIMITER
    String key = StringUtils.substringBefore(suffixPart, Character.toString(KEY_VALUE_DELIMITER));
    return UNESCAPE_SPECIAL_CHARS.translate(key);
  }

  /**
   * Split suffix
   * @param suffix Suffix
   * @return Suffix parts
   */
  public static String[] splitSuffix(String suffix) {
    String theSuffix = suffix;

    String[] parts;
    if (StringUtils.isBlank(theSuffix)) {
      // no suffix given - return empty list
      parts = new String[0];
    }
    else {
      // remove leading slash
      if (theSuffix.startsWith(ESCAPED_SLASH)) {
        theSuffix = theSuffix.substring(ESCAPED_SLASH.length());
      }

      // remove file extension
      theSuffix = StringUtils.substringBeforeLast(theSuffix, ".");

      // split the suffix to extract the paths of the selected components
      parts = StringUtils.split(theSuffix, SUFFIX_PART_DELIMITER);
    }

    return parts;
  }

  /**
   * Convert key value pair to map
   * @param key Key
   * @param value Value
   * @return Map
   */
  public static Map<String, Object> keyValuePairAsMap(String key, Object value) {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(key, value);
    return paramMap;
  }

  /**
   * @param resource the resource being addressed by the relative path
   * @param baseResource the resource used as base to resolve the relative path
   * @return the relative path (without leading slash)
   */
  public static String getRelativePath(Resource resource, Resource baseResource) {
    if (baseResource == null) {
      throw new IllegalArgumentException("the base resource for constructing relative path must not be null");
    }
    if (resource == null) {
      throw new IllegalArgumentException("the resource for constructing relative path must not be null");
    }
    String absolutePath = resource.getPath();
    String basePath = baseResource.getPath();

    if (absolutePath.equals(basePath)) {
      // relative path for the root resource is "."
      return ".";
    }

    // be picky about resources not located beneath the base resource
    if (!absolutePath.startsWith(basePath + "/")) {
      throw new IllegalArgumentException("the resource " + resource + " is not a descendent of the base resource " + baseResource);
    }

    // return relative path
    return StringUtils.substringAfter(absolutePath, basePath + "/");
  }

}
