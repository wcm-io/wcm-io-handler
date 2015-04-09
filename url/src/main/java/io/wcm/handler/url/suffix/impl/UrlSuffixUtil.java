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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

/**
 * Static methods and constants for URL suffix handling.
 */
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
  public static final Map<String, String> SPECIAL_CHARS_ESCAPEMAP;
  static {
    final Map<String, String> map = new HashMap<>();

    // escape delimiter chars
    map.put(Character.toString(SUFFIX_PART_DELIMITER), ESCAPE_DELIMITER + hexCode(SUFFIX_PART_DELIMITER));
    map.put(Character.toString(KEY_VALUE_DELIMITER), ESCAPE_DELIMITER + hexCode(KEY_VALUE_DELIMITER));

    // '.' must be custom-escaped (if no file extension is added to suffix,
    // anything after a dot would be interpreted as file extension during parsing)
    map.put(Character.toString('.'), ESCAPE_DELIMITER + hexCode('.'));

    // escape '%' to avoid confusion with URL escaping
    map.put(Character.toString('%'), ESCAPE_DELIMITER + hexCode('%'));

    // '/' must be custom-escaped (dispatcher/webserver may filter out/misinterpret urls with unescaped slashes)
    map.put(Character.toString('/'), ESCAPE_DELIMITER + hexCode('/'));

    // escape ':'
    map.put(Character.toString(':'), ESCAPE_DELIMITER + hexCode(':'));

    // escape ' ' as well (singular problem occured once)
    map.put(Character.toString(' '), ESCAPE_DELIMITER + hexCode(' '));

    SPECIAL_CHARS_ESCAPEMAP = Collections.unmodifiableMap(map);
  }

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
    String encodedPath = relativePath;

    // escape special chars
    for (Map.Entry<String, String> entry : SPECIAL_CHARS_ESCAPEMAP.entrySet()) {
      encodedPath = StringUtils.replace(encodedPath, entry.getKey(), entry.getValue());
    }

    return encodedPath;
  }

  /**
   * Decode resource path part
   * @param suffixPart Suffix part
   * @return Decoded path part
   */
  public static String decodeResourcePathPart(String suffixPart) {
    String unencodedPath = suffixPart;

    // un-escape special chars
    for (Map.Entry<String, String> entry : SPECIAL_CHARS_ESCAPEMAP.entrySet()) {
      unencodedPath = StringUtils.replace(unencodedPath, entry.getValue(), entry.getKey());
    }

    return unencodedPath;
  }

  /**
   * Encode key value part
   * @param string String
   * @return Encoded string
   */
  public static String encodeKeyValuePart(String string) {
    try {
      String encoded = string;

      // escape special chars
      for (Map.Entry<String, String> entry : SPECIAL_CHARS_ESCAPEMAP.entrySet()) {
        encoded = StringUtils.replace(encoded, entry.getKey(), entry.getValue());
      }

      // fully URL encode the Key/value to allow arbitrary Strings
      encoded = URLEncoder.encode(encoded, CharEncoding.UTF_8);

      return encoded;
    }
    catch (UnsupportedEncodingException ex) {
      throw new RuntimeException("utf-8 not supported on this system", ex);
    }
  }

  /**
   * Decode value
   * @param suffixPart Suffix part
   * @return Decoded value
   */
  public static String decodeValue(String suffixPart) {
    // value is the part *after* KEY_VALUE_DELIMITER
    String value = StringUtils.substringAfter(suffixPart, Character.toString(KEY_VALUE_DELIMITER));

    // un-escape special chars
    for (Map.Entry<String, String> entry : SPECIAL_CHARS_ESCAPEMAP.entrySet()) {
      value = StringUtils.replace(value, entry.getValue(), entry.getKey());
    }

    return value;
  }

  /**
   * Decode key
   * @param suffixPart Suffix part
   * @return Decoded key
   */
  public static String decodeKey(String suffixPart) {
    // key is the part *before* KEY_VALUE_DELIMITER
    String key = StringUtils.substringBefore(suffixPart, Character.toString(KEY_VALUE_DELIMITER));

    // un-escape special chars
    for (Map.Entry<String, String> entry : SPECIAL_CHARS_ESCAPEMAP.entrySet()) {
      key = StringUtils.replace(key, entry.getValue(), entry.getKey());
    }

    return key;
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
  public static SortedMap<String, String> keyValuePairAsMap(String key, String value) {
    SortedMap<String, String> paramMap = new TreeMap<>();
    paramMap.put(key, value);
    return paramMap;
  }

}
