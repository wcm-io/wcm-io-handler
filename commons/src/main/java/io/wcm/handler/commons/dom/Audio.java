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

/**
 * Html audio (audio) element.
 */
public final class Audio extends AbstractNonSelfClosingHtmlElement<Audio> {
  private static final long serialVersionUID = 1L;

  private static final String ELEMENT_NAME = "audio";

  private static final String ATTRIBUTE_AUTOPLAY = "autoplay";
  private static final String ATTRIBUTE_CONTROLS = "controls";
  private static final String ATTRIBUTE_LOOP = "loop";
  private static final String ATTRIBUTE_PRELOAD = "preload";
  private static final String ATTRIBUTE_SRC = "src";

  /**
   * Initializes html element.
   */
  public Audio() {
    super(ELEMENT_NAME);
  }

  /**
   * Html "autoplay" attribute.
   * @return Value of attribute
   */
  public boolean isAutoPlay() {
    return getEmptyAttributeValueAsBoolean(ATTRIBUTE_AUTOPLAY);
  }

  /**
   * Html "autoplay" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public Audio setAutoPlay(boolean value) {
    setEmptyAttributeValueAsBoolean(ATTRIBUTE_AUTOPLAY, value);
    return this;
  }

  /**
   * Html "controls" attribute.
   * @return Value of attribute
   */
  public boolean isControls() {
    return getEmptyAttributeValueAsBoolean(ATTRIBUTE_CONTROLS);
  }

  /**
   * Html "controls" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public Audio setControls(boolean value) {
    setEmptyAttributeValueAsBoolean(ATTRIBUTE_CONTROLS, value);
    return this;
  }

  /**
   * Html "loop" attribute.
   * @return Value of attribute
   */
  public boolean isLoop() {
    return getEmptyAttributeValueAsBoolean(ATTRIBUTE_LOOP);
  }

  /**
   * Html "loop" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public Audio setLoop(boolean value) {
    setEmptyAttributeValueAsBoolean(ATTRIBUTE_LOOP, value);
    return this;
  }

  /**
   * Html "preload" attribute.
   * @return Value of attribute
   */
  public String getPreload() {
    return getAttributeValue(ATTRIBUTE_PRELOAD);
  }

  /**
   * Html "preload" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public Audio setPreload(String value) {
    setAttribute(ATTRIBUTE_PRELOAD, value);
    return this;
  }

  /**
   * Html "src" attribute.
   * @return Value of attribute
   */
  public String getSrc() {
    return getAttributeValue(ATTRIBUTE_SRC);
  }

  /**
   * Html "src" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public Audio setSrc(String value) {
    setAttribute(ATTRIBUTE_SRC, value);
    return this;
  }

}
