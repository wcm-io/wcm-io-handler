/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import org.osgi.annotation.versioning.ProviderType;

/**
 * Html area element.
 */
@ProviderType
public final class Area extends HtmlElement<Area> {
  private static final long serialVersionUID = 1L;

  private static final String ELEMENT_NAME = "area";

  private static final String ATTRIBUTE_REL = "rel";
  private static final String ATTRIBUTE_HREF = "href";
  private static final String ATTRIBUTE_TARGET = "target";
  private static final String ATTRIBUTE_TABINDEX = "tabindex";
  private static final String ATTRIBUTE_ACCESSKEY = "accesskey";
  private static final String ATTRIBUTE_ALT = "alt";
  private static final String ATTRIBUTE_SHAPE = "shape";
  private static final String ATTRIBUTE_COORDS = "coords";

  /**
   * Initializes html element.
   */
  public Area() {
    super(ELEMENT_NAME);
  }

  /**
   * Html "rel" attribute.
   * @return Value of attribute
   */
  public String getRel() {
    return getAttributeValue(ATTRIBUTE_REL);
  }

  /**
   * Html "rel" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public Area setRel(String value) {
    setAttribute(ATTRIBUTE_REL, value);
    return this;
  }

  /**
   * Html "href" attribute.
   * @return Value of attribute
   */
  public String getHRef() {
    return getAttributeValue(ATTRIBUTE_HREF);
  }

  /**
   * Html "href" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public Area setHRef(String value) {
    setAttribute(ATTRIBUTE_HREF, value);
    return this;
  }

  /**
   * Html "target" attribute.
   * @return Value of attribute
   */
  public String getTarget() {
    return getAttributeValue(ATTRIBUTE_TARGET);
  }

  /**
   * Html "target" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public Area setTarget(String value) {
    setAttribute(ATTRIBUTE_TARGET, value);
    return this;
  }

  /**
   * Html "tabindex" attribute.
   * @return Value of attribute
   */
  public int getTabIndex() {
    return getAttributeValueAsInteger(ATTRIBUTE_TABINDEX);
  }

  /**
   * Html "tabindex" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public Area setTabIndex(int value) {
    setAttributeValueAsInteger(ATTRIBUTE_TABINDEX, value);
    return this;
  }

  /**
   * Html "accesskey" attribute.
   * @return Value of attribute
   */
  public String getAccessKey() {
    return getAttributeValue(ATTRIBUTE_ACCESSKEY);
  }

  /**
   * Html "accesskey" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public Area setAccessKey(String value) {
    setAttribute(ATTRIBUTE_ACCESSKEY, value);
    return this;
  }

  /**
   * Html "alt" attribute.
   * @return Value of attribute
   */
  public String getAlt() {
    return getAttributeValue(ATTRIBUTE_ALT);
  }

  /**
   * Html "alt" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public Area setAlt(String value) {
    setAttribute(ATTRIBUTE_ALT, value);
    return this;
  }

  /**
   * Html "shape" attribute.
   * @return Value of attribute
   */
  public String getShape() {
    return getAttributeValue(ATTRIBUTE_SHAPE);
  }

  /**
   * Html "shape" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public Area setShape(String value) {
    setAttribute(ATTRIBUTE_SHAPE, value);
    return this;
  }

  /**
   * Html "coords" attribute.
   * @return Value of attribute
   */
  public String getCoords() {
    return getAttributeValue(ATTRIBUTE_COORDS);
  }

  /**
   * Html "coords" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public Area setCoords(String value) {
    setAttribute(ATTRIBUTE_COORDS, value);
    return this;
  }

}
