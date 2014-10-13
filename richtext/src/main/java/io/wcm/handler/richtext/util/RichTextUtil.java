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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;

import aQute.bnd.annotation.ProviderType;

/**
 * Utility methods for handling XHTML rich text fragments i.e. used for FCKEditor.
 */
@ProviderType
public final class RichTextUtil {

  private RichTextUtil() {
    // utility methods only
  }

  private static final int EMPTYTEXT_DEFAULT_TRESHOLD = 20;

  private static final String XHTML_ENTITY_DEF =
      "<!ENTITY % HTMLlat1 PUBLIC \"" + XHtmlResource.ENTITIES_LAT1.getPublicId() + "\" "
          + "\"" + XHtmlResource.ENTITIES_LAT1.getSystemId() + "\">"
          + "%HTMLlat1;"
          + "<!ENTITY % HTMLsymbol PUBLIC \"" + XHtmlResource.ENTITIES_SYMBOL.getPublicId() + "\" "
          + "\"" + XHtmlResource.ENTITIES_SYMBOL.getSystemId() + "\">"
          + "%HTMLsymbol;"
          + "<!ENTITY % HTMLspecial PUBLIC \"" + XHtmlResource.ENTITIES_SPECIAL.getPublicId() + "\" "
          + "\"" + XHtmlResource.ENTITIES_SPECIAL.getSystemId() + "\">"
          + "%HTMLspecial;";

  /**
   * Check if the given formatted text block is empty.
   * A text block containing only one paragraph element and whitespaces is considered as empty.
   * A text block with more than 20 characters (raw data) is never considered as empty.
   * @param text XHTML text string (root element not needed)
   * @return true if text block is empty
   */
  public static boolean isEmpty(String text) {
    return isEmpty(text, EMPTYTEXT_DEFAULT_TRESHOLD);
  }

  /**
   * Check if the given formatted text block is empty.
   * A text block containing only one paragraph element and whitespaces is considered as empty.
   * A text block with more than pTreshold characters (raw data) is never considered as empty.
   * @param text XHTML text string (root element not needed)
   * @param treshold Treshold value - only strings with less than this number of characters are checked.
   * @return true if text block is empty
   */
  public static boolean isEmpty(String text, int treshold) {

    // check if text block is really empty
    if (StringUtils.isEmpty(text)) {
      return true;
    }

    // check if text block has more than 20 chars
    if (text.length() > treshold) {
      return false;
    }

    // replace all whitespaces and nbsp's
    String cleanedText = StringUtils.replace(text, " ", "");
    cleanedText = StringUtils.replace(cleanedText, "&#160;", "");
    cleanedText = StringUtils.replace(cleanedText, "&nbsp;", "");
    cleanedText = StringUtils.replace(cleanedText, "\n", "");
    cleanedText = StringUtils.replace(cleanedText, "\r", "");
    return StringUtils.isEmpty(cleanedText) || "<p></p>".equals(cleanedText);
  }

  /**
   * Parses XHTML text string, and adds to parsed content to the given parent element.
   * @param parent Parent element to add parsed content to
   * @param text XHTML text string (root element not needed)
   * @throws JDOMException Is thrown if the text could not be parsed as XHTML
   */
  public static void addParsedText(Element parent, String text) throws JDOMException {
    addParsedText(parent, text, false);
  }

  /**
   * Parses XHTML text string, and adds to parsed content to the given parent element.
   * @param parent Parent element to add parsed content to
   * @param text XHTML text string (root element not needed)
   * @param xhtmlEntities If set to true, Resolving of XHtml entities in XHtml fragment is supported.
   * @throws JDOMException Is thrown if the text could not be parsed as XHTML
   */
  public static void addParsedText(Element parent, String text, boolean xhtmlEntities) throws JDOMException {
    Element root = parseText(text, xhtmlEntities);
    parent.addContent(root.cloneContent());
  }

  /**
   * Parses XHTML text string. Adds a wrapping "root" element before parsing and returns this root element.
   * @param text XHTML text string (root element not needed)
   * @return Root element with parsed xhtml content
   * @throws JDOMException Is thrown if the text could not be parsed as XHTML
   */
  public static Element parseText(String text) throws JDOMException {
    return parseText(text, false);
  }

  /**
   * Parses XHTML text string. Adds a wrapping "root" element before parsing and returns this root element.
   * @param text XHTML text string (root element not needed)
   * @param xhtmlEntities If set to true, Resolving of XHtml entities in XHtml fragment is supported.
   * @return Root element with parsed xhtml content
   * @throws JDOMException Is thrown if the text could not be parsed as XHTML
   */
  public static Element parseText(String text, boolean xhtmlEntities) throws JDOMException {

    // add root element
    String xhtmlString =
        (xhtmlEntities ? "<!DOCTYPE root [" + XHTML_ENTITY_DEF + "]>" : "")
        + "<root>" + text + "</root>";

    try {
      SAXBuilder saxBuilder = new SAXBuilder();

      if (xhtmlEntities) {
        saxBuilder.setEntityResolver(XHtmlEntityResolver.getInstance());
      }

      Document doc = saxBuilder.build(new StringReader(xhtmlString));
      return doc.getRootElement();
    }
    catch (IOException ex) {
      throw new RuntimeException("Error parsing XHTML fragment.", ex);
    }

  }

  /**
   * Rewrites all children/sub-tree of the given parent element.
   * For rewrite operations the given rewrite content handler is called.
   * @param parent Parent element
   * @param rewriteContentHandler Rewrite content handler
   */
  public static void rewriteContent(Element parent, RewriteContentHandler rewriteContentHandler) {

    // iterate through content list and build new content list
    List<Content> originalContent = parent.getContent();
    List<Content> newContent = new ArrayList<Content>();
    for (Content contentElement : originalContent) {

      // handle element
      if (contentElement instanceof Element) {
        Element element = (Element)contentElement;

        // check if rewrite is needed for element
        List<Content> rewriteContent = rewriteContentHandler.rewriteElement(element);
        if (rewriteContent != null) {
          // element was removed
          if (rewriteContent.isEmpty()) {
            // do not add to newContent
          }

          // element is the same - rewrite child elements
          else if (rewriteContent.size() == 1 && rewriteContent.get(0) == element) {
            rewriteContent(element, rewriteContentHandler);
            newContent.add(element);
          }

          // element was replaced with other content - rewrite and add instead
          else {
            for (Content newContentItem : rewriteContent) {
              if (newContentItem instanceof Element) {
                Element newElement = (Element)newContentItem;
                rewriteContent(newElement, rewriteContentHandler);
              }
              newContent.add(newContentItem.clone());
            }
          }
        }

        // nothing to rewrite - do nothing, but rewrite child element
        else {
          rewriteContent(element, rewriteContentHandler);
          newContent.add(element);
        }

      }

      // handle text node
      else if (contentElement instanceof Text) {
        Text text = (Text)contentElement;

        // check if rewrite is needed for text node
        List<Content> rewriteContent = rewriteContentHandler.rewriteText(text);
        if (rewriteContent != null) {
          // element was removed
          if (rewriteContent.isEmpty()) {
            // do not add to newContent
          }

          // element is the same - ignore
          else if (rewriteContent.size() == 1 && rewriteContent.get(0) == text) {
            // add original element
            newContent.add(text);
          }

          // element was replaced with other content - add instead
          else {
            for (Content newContentItem : rewriteContent) {
              newContent.add(newContentItem.clone());
            }
          }
        }

        // nothing to rewrite - do nothing, but add original text element
        else {
          newContent.add(text);
        }

      }

      // unknown element - just add to new content
      else {
        newContent.add(contentElement);
      }

    }

    // replace original content with new content
    parent.removeContent();
    parent.addContent(newContent);

  }

}
