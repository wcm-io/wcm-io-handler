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

import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.DataConversionException;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;

import aQute.bnd.annotation.ConsumerType;

/**
 * Generic DOM element.
 * This element implementation is derived from JDOM element implementation.
 * @param <T> Class extending Element
 */
@SuppressWarnings("unchecked")
@ConsumerType
public abstract class AbstractElement<T extends AbstractElement> extends org.jdom2.Element {
  private static final long serialVersionUID = 1L;

  // matches all control chars ([\x00-\x1F\x7F]), that are invalid inside XML
  private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}]");

  /**
   * Initializes DOM element.
   * @param name Element name
   */
  protected AbstractElement(String name) {
    super(name);
  }

  /**
   * Sets element name - should not be used for HtmlElement-derived classes!
   * @param value Element name
   * @return Self reference
   * @deprecated Deprecated
   */
  @Override
  @Deprecated
  public final org.jdom2.Element setName(String value) {
    return super.setName(value);
  }

  /**
   * Gets attribute value as integer.
   * @param attributeName Attribute name
   * @return Attribute value as integer or 0 if not set.
   */
  public final int getAttributeValueAsInteger(String attributeName) {
    Attribute attribute = getAttribute(attributeName);
    if (attribute == null) {
      return 0;
    }
    else {
      try {
        return attribute.getIntValue();
      }
      catch (DataConversionException ex) {
        return 0;
      }
    }
  }

  /**
   * Sets attribute value as long.
   * @param name Attribute name
   * @param value Attribute value as long
   * @return Self reference
   */
  public final T setAttributeValueAsLong(String name, long value) {
    setAttribute(name, Long.toString(value));
    return (T)this;
  }

  /**
   * Gets attribute value as long.
   * @param attributeName Attribute name
   * @return Attribute value as long or 0 if not set.
   */
  public final long getAttributeValueAsLong(String attributeName) {
    Attribute attribute = getAttribute(attributeName);
    if (attribute == null) {
      return 0;
    }
    else {
      try {
        return attribute.getLongValue();
      }
      catch (DataConversionException ex) {
        return 0;
      }
    }
  }

  /**
   * Sets attribute value as integer.
   * @param name Attribute name
   * @param value Attribute value as integer
   * @return Self reference
   */
  public final T setAttributeValueAsInteger(String name, int value) {
    setAttribute(name, Integer.toString(value));
    return (T)this;
  }

  /**
   * Appends the child to the end of the element's content list
   * @param content Child to append to end of content list. Null values are ignored.
   * @return The element on which the method was called.
   */
  @Override
  public final org.jdom2.Element addContent(Content content) {
    // ignore empty elements
    if (content == null) {
      return null;
    }
    return super.addContent(content);
  }

  /**
   * Inserts the child into the content list at the given index
   * @param index Location for adding the collection
   * @param content Child to append to end of content list. Null values are ignored.
   * @return The element on which the method was called.
   */
  @Override
  public final org.jdom2.Element addContent(int index, Content content) {
    // ignore empty elements
    if (content == null) {
      return null;
    }
    return super.addContent(index, content);
  }

  /**
   * This adds text content to this element. It does not replace the existing content as does setText().
   * @param text String to add. Null values are ignored.
   * @return This element modified
   */
  @Override
  public final org.jdom2.Element addContent(String text) {
    // ignore empty elements
    if (text == null) {
      return null;
    }
    return super.addContent(cleanUpString(text));
  }

  /**
   * Appends all children in the given collection to the end of the content list. In event of an exception
   * during add the original content will be unchanged and the objects in the supplied collection will be unaltered.
   * @param collection Collection to append. Null values are ignored.
   * @return the element on which the method was called
   */
  @Override
  public final org.jdom2.Element addContent(Collection collection) {
    // ignore empty elements
    if (collection == null) {
      return null;
    }
    return super.addContent(collection);
  }

  /**
   * Inserts the content in a collection into the content list at the given index. In event of an exception
   * the original content will be unchanged and the objects in the supplied collection will be unaltered.
   * @param index Location for adding the collection
   * @param collection Collection to insert. Null values are ignored.
   * @return The parent on which the method was called
   */
  @Override
  public final org.jdom2.Element addContent(int index, Collection collection) {
    // ignore empty elements
    if (collection == null) {
      return null;
    }
    return super.addContent(index, collection);
  }

  /**
   * Appends the child to the end of the element's content list.
   * Returns not the element itself (contrary to addContent), but a reference to the newly added element.
   * @param <ElementType> Type that extends Element
   * @param element Element to add. Null values are ignored.
   * @return The added element.
   */
  public final <ElementType extends AbstractElement> ElementType add(ElementType element) {
    this.addContent(element);
    return element;
  }

  /**
   * Sets the content of the element to be the text given. All existing text content and non-text context is removed.
   * If this element should have both textual content and nested elements, use <code>{@link #setContent}</code> instead.
   * Setting a null text value is equivalent to setting an empty string value.
   * @param text new text content for the element
   * @return the target element
   * @throws org.jdom2.IllegalDataException if the assigned text contains an illegal character such as a
   *           vertical tab (as determined by {@link org.jdom2.Verifier#checkCharacterData})
   */
  @Override
  public org.jdom2.Element setText(String text) {
    return super.setText(cleanUpString(StringUtils.defaultString(text)));
  }

  /**
   * <p>
   * This sets an attribute value for this element. Any existing attribute with the same name and namespace URI is
   * removed.
   * </p>
   * @param name name of the attribute to set
   * @param value value of the attribute to set
   * @param ns namespace of the attribute to set
   * @return this element modified
   * @throws org.jdom2.IllegalNameException if the given name is illegal as an attribute name, or if the namespace
   *           is an unprefixed default namespace
   * @throws org.jdom2.IllegalDataException if the given attribute value is illegal character data (as determined
   *           by {@link org.jdom2.Verifier#checkCharacterData}).
   * @throws org.jdom2.IllegalAddException if the attribute namespace prefix collides with another namespace
   *           prefix on the element.
   */
  @Override
  public final org.jdom2.Element setAttribute(String name, String value, Namespace ns) {
    // remove attribute if value is set to null
    if (value == null) {
      super.removeAttribute(name, ns);
      return this;
    }
    else {
      return super.setAttribute(name, cleanUpString(value), ns);
    }
  }

  /**
   * <p>
   * This sets an attribute value for this element. Any existing attribute with the same name and namespace URI is
   * removed.
   * </p>
   * @param name name of the attribute to set
   * @param value value of the attribute to set
   * @return this element modified
   * @throws org.jdom2.IllegalNameException if the given name is illegal as an attribute name.
   * @throws org.jdom2.IllegalDataException if the given attribute value is illegal character data
   *           (as determined by {@link org.jdom2.Verifier#checkCharacterData}).
   */
  @Override
  public final org.jdom2.Element setAttribute(String name, String value) {
    // remove attribute if value is set to null
    if (value == null) {
      super.removeAttribute(name);
      return this;
    }
    else {
      return super.setAttribute(name, cleanUpString(value));
    }
  }

  /**
   * Cleans up the given string and removes all control char characters not valid for XML.
   * These control chars e.g. 0x03 for UTF-8 can by pasted by copy&paste into the CMS RTE.
   * @param text Text
   * @return Cleaned up text
   */
  private String cleanUpString(String text) {
    if (StringUtils.isEmpty(text)) {
      return text;
    }
    else {
      return CONTROL_CHARS.matcher(text).replaceAll("");
    }
  }

  @Override
  public String toString() {
    return new XMLOutputter().outputString(this);
  }

  /**
   * @return Content of element serialized as string
   */
  public String toStringContentOnly() {
    return new XMLOutputter().outputElementContentString(this);
  }

}
