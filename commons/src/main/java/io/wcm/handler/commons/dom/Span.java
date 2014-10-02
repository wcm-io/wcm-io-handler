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
 * Html span element.
 */
public final class Span extends AbstractNonSelfClosingHtmlElement<Span> {
  private static final long serialVersionUID = 1L;

  private static final String ELEMENT_NAME = "span";

  /**
   * Initializes html element.
   */
  public Span() {
    super(ELEMENT_NAME);
  }

  /**
   * Initializes html element.
   * @param text Text
   */
  public Span(String text) {
    super(ELEMENT_NAME);
    setText(text);
  }

}
