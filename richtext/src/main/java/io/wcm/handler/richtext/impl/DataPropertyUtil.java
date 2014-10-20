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
package io.wcm.handler.richtext.impl;

import java.util.regex.Pattern;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Converts case sensitive headless camel case property names to case insensitive HTML5 data property names and vice
 * versa.
 */
final class DataPropertyUtil {

  private static final String HTML5_DATA_PREFIX = "data-";
  private static final Pattern HEADLESS_CAMEL_CASE_NAME_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
  private static final Pattern HTML5_DATA_NAME_PATTERN = Pattern.compile("^data\\-[a-z][a-z0-9\\-]*$");

  private DataPropertyUtil() {
    // static methods only
  }

  /**
   * Converts a headless camel case property name to a HTML5 data attribute name including "data-" prefix.
   * @param headlessCamelCaseName Headless camel case name
   * @return HTML5 data attribute name
   * @throws IllegalArgumentException If parameter name is not valid
   */
  public static String toHtml5DataName(String headlessCamelCaseName) {
    if (StringUtils.isEmpty(headlessCamelCaseName)) {
      throw new IllegalArgumentException("Property name is empty.");
    }
    if (!isHeadlessCamelCaseName(headlessCamelCaseName)) {
      throw new IllegalArgumentException("This is not a valid headless camel case property name: " + headlessCamelCaseName);
    }

    StringBuilder html5DataName = new StringBuilder(HTML5_DATA_PREFIX);
    for (int i = 0; i < headlessCamelCaseName.length(); i++) {
      char c = headlessCamelCaseName.charAt(i);
      if (CharUtils.isAsciiAlphaUpper(c)) {
        html5DataName.append('-');
      }
      html5DataName.append(Character.toLowerCase(c));
    }

    return html5DataName.toString();
  }

  /**
   * Converts a HTML5 data attribute name including "data-" prefix to a headless camel case name.
   * @param html5DataName Html5 data attribute name
   * @return Headless camel case name
   * @throws IllegalArgumentException If parameter name is not valid
   */
  public static String toHeadlessCamelCaseName(String html5DataName) {
    if (StringUtils.isEmpty(html5DataName)) {
      throw new IllegalArgumentException("Property name is empty.");
    }
    if (!isHtml5DataName(html5DataName)) {
      throw new IllegalArgumentException("This is not a valid HTML5 data property name: " + html5DataName);
    }

    String html5DataNameWithoutSuffix = StringUtils.substringAfter(html5DataName, HTML5_DATA_PREFIX);
    StringBuilder headlessCamelCaseName = new StringBuilder();
    boolean upperCaseNext = false;
    for (int i = 0; i < html5DataNameWithoutSuffix.length(); i++) {
      char c = html5DataNameWithoutSuffix.charAt(i);
      if (c == '-') {
        upperCaseNext = true;
      }
      else if (upperCaseNext) {
        headlessCamelCaseName.append(Character.toUpperCase(c));
        upperCaseNext = false;
      }
      else {
        headlessCamelCaseName.append(c);
      }
    }

    return headlessCamelCaseName.toString();
  }

  /**
   * @param name Property name
   * @return true if property is a valid headless camel case name which can be converted to a HTML5 data property name.
   */
  public static boolean isHeadlessCamelCaseName(String name) {
    if (StringUtils.isEmpty(name)) {
      return false;
    }
    return HEADLESS_CAMEL_CASE_NAME_PATTERN.matcher(name).matches();
  }

  /**
   * @param pName Property name
   * @return true if property name starts with "data-" prefix, and has only lowercase, number or hyphen chars.
   */
  public static boolean isHtml5DataName(String pName) {
    if (StringUtils.isEmpty(pName)) {
      return false;
    }
    return HTML5_DATA_NAME_PATTERN.matcher(pName).matches();
  }

}
