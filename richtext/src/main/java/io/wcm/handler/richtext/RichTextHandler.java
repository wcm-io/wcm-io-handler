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
package io.wcm.handler.richtext;

import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 * Manages XHTML rich text fragment formatting.
 * The rich text fragments are produced with CQ5 Rich Edit control.
 */
public interface RichTextHandler {

  /**
   * Adds rich text content to given HTML element.
   * @param text XHTML text string (root element not needed)
   * @param parent Parent HTML element
   * @throws JDOMException Is thrown if the text could not be parsed as XHTML
   */
  void addContent(String text, Element parent) throws JDOMException;

  /**
   * Adds rich text content to given HTML element.
   * @param text XHTML text string (root element not needed)
   * @param parent Parent HTML element
   * @param supressParsingError If set to true, parsing errors (JDOMExceptions) are suppressed, and logged only in
   *          debug mode. Otherwise the JDOMException is wrapped in a {@link IllegalArgumentException} and thrown again.
   * @throws IllegalArgumentException Is thrown if the text cannot be parsed, and pSuppressParsingError is not set to
   *           true
   */
  void addContent(String text, Element parent, boolean supressParsingError);

  /**
   * Add multi line plain text to parent element. Splits text by \n and inserts a HTML break for each newline.
   * @param text Multiline text
   * @param parent Parent HTML element
   */
  void addPlaintextContent(String text, Element parent);

  /**
   * Gets rich text content as DOM elements.
   * @param text XHTML text string (root element not needed)
   * @return List of content DOM elements
   * @throws JDOMException Is thrown if the text could not be parsed as XHTML
   */
  List<Content> getContent(String text) throws JDOMException;

  /**
   * Check if the given formatted text block is empty. A text block containing only one paragraph element and
   * whitespaces is considered as empty. A text block with more than 20 characters (raw data)
   * is never considered as empty.
   * @param text XHTML text string (root element not needed)
   * @return true if text block is empty
   */
  boolean isEmpty(String text);

}
