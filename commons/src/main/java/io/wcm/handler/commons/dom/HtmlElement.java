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
package io.wcm.handler.commons.dom;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Html element wrapper object.
 * This element class is an extension of JDOM Element.
 * @param <T> Class derived from HtmlElement
 */
@SuppressWarnings("unchecked")
@ConsumerType
public class HtmlElement<T extends HtmlElement> extends AbstractHtmlElementFactory<T> {
  private static final long serialVersionUID = 1L;

  private static final String ATTRIBUTE_ID = "id";
  private static final String ATTRIBUTE_CLASS = "class";
  private static final String ATTRIBUTE_STYLE = "style";
  private static final String ATTRIBUTE_TITLE = "title";
  private static final String ATTRIBUTE_DATA_PREFIX = "data-";

  /**
   * Initializes html element.
   * @param name Element name
   */
  public HtmlElement(String name) {
    super(name);
  }

  /**
   * Appends the child to the end of the element's content list.
   * Returns not the element itself (contrary to addContent), but a reference to the newly added element.
   * @param <HtmlElementType> Type that extends HtmlElement
   * @param element Element to add. Null values are ignored.
   * @return The added element.
   */
  public final <HtmlElementType extends HtmlElement> HtmlElementType add(HtmlElementType element) {
    this.addContent(element);
    return element;
  }

  /**
   * Gets "empty" attribute value as boolean (i.e. for "checked" attribute).
   * @param attributeName Attribute name
   * @return Attribute value as boolean or false if not set.
   */
  protected final boolean getEmptyAttributeValueAsBoolean(String attributeName) {
    return attributeName.equalsIgnoreCase(getAttributeValue(attributeName));
  }

  /**
   * Sets "empty" attribute value as boolean (i.e. for "checked" attribute).
   * @param attributeName Attribute name
   * @param value Attribute value as boolean
   * @return Self reference
   */
  protected final T setEmptyAttributeValueAsBoolean(String attributeName, boolean value) {
    if (value) {
      setAttribute(attributeName, attributeName.toLowerCase());
    }
    else {
      removeAttribute(attributeName);
    }
    return (T)this;
  }

  /**
   * Html "id" attribute.
   * @return Value of attribute
   */
  public final String getId() {
    return getAttributeValue(ATTRIBUTE_ID);
  }

  /**
   * Html "id" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public final T setId(String value) {
    setAttribute(ATTRIBUTE_ID, value);
    return (T)this;
  }

  /**
   * Html "class" attribute.
   * @return Value of attribute
   */
  public final String getCssClass() {
    return getAttributeValue(ATTRIBUTE_CLASS);
  }

  /**
   * Sets Html "class" attribute - unless you're really sure that you want to replace existing classes,
   * you probably should call {@link #addCssClass(String)} instead.
   * @param value Value of attribute
   * @return Self reference
   */
  public final T setCssClass(String value) {
    setAttribute(ATTRIBUTE_CLASS, value);
    return (T)this;
  }

  /**
   * Html "class" attribute. Adds a single, space-separated value while preserving existing ones.
   * @param value Value of attribute
   * @return Self reference
   */
  public final T addCssClass(String value) {
    if (StringUtils.isNotEmpty(value)) {
      return setCssClass(StringUtils.isNotEmpty(getCssClass()) ? getCssClass() + " " + value : value);
    }
    else {
      return (T)this;
    }
  }

  /**
   * Html "style" attribute.
   * @return Value of attribute with style key/value pairs
   */
  public final String getStyleString() {
    return getAttributeValue(ATTRIBUTE_STYLE);
  }

  /**
   * Html "style" attribute.
   * @return Returns map of style key/value pairs.
   */
  public final Map<String, String> getStyles() {
    Map<String, String> styleMap = new HashMap<String, String>();

    // de-serialize style string, fill style map
    String styleString = getStyleString();
    if (styleString != null) {
      String[] styles = StringUtils.split(styleString, ";");
      for (String styleSubString : styles) {
        String[] styleParts = StringUtils.split(styleSubString, ":");
        if (styleParts.length > 1) {
          styleMap.put(styleParts[0].trim(), styleParts[1].trim());
        }
      }
    }

    return styleMap;
  }

  /**
   * Html "style" attribute. Returns single style attribute value.
   * @param styleAttribute Style attribute name
   * @return Style attribute value
   */
  public final String getStyle(String styleAttribute) {
    return getStyles().get(styleAttribute);
  }

  /**
   * Html "style" attribute.
   * @param value Value of attribute with style key/value pairs
   * @return Self reference
   */
  public final T setStyleString(String value) {
    setAttribute(ATTRIBUTE_STYLE, value);
    return (T)this;
  }

  /**
   * Html "style" attribute. Sets single style attribute value.
   * @param styleAttribute Style attribute name
   * @param styleValue Style attribute value
   * @return Self reference
   */
  public final T setStyle(String styleAttribute, String styleValue) {

    // Add style to style map
    Map<String, String> styleMap = getStyles();
    styleMap.put(styleAttribute, styleValue);

    // Serialize style string
    StringBuilder styleString = new StringBuilder();
    for (Map.Entry style : styleMap.entrySet()) {
      styleString.append(style.getKey());
      styleString.append(':');
      styleString.append(style.getValue());
      styleString.append(';');
    }
    setStyleString(styleString.toString());
    return (T)this;
  }

  /**
   * Html "title" attribute.
   * @return Value of attribute
   */
  public final String getTitle() {
    return getAttributeValue(ATTRIBUTE_TITLE);
  }

  /**
   * Html "title" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public final T setTitle(String value) {
    setAttribute(ATTRIBUTE_TITLE, value);
    return (T)this;
  }

  /**
   * Custom Html5 "data-*" attribute.
   * @param attributeName Name of HTML5 data attribute (without the 'data-' prefix).
   * @return Value of attribute
   */
  public final String getData(String attributeName) {
    return getAttributeValue(ATTRIBUTE_DATA_PREFIX + attributeName);
  }

  /**
   * Custom Html5 "data-*" attribute.
   * @param attributeName Name of HTML5 data attribute (without the 'data-' prefix).
   * @param value Value of attribute
   * @return Self reference
   */
  public final T setData(String attributeName, String value) {
    setAttribute(ATTRIBUTE_DATA_PREFIX + attributeName, value);
    return (T)this;
  }

}
