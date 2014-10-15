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

import org.apache.commons.lang3.StringUtils;
import org.jdom2.CDATA;
import org.jdom2.Element;
import org.jdom2.Text;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Html script element.
 * This script block is initialized by default for "text/javascript" type.
 * <p>
 * Script blocks are generated in a special XHTML conformant way, to work around some browser XHTML JavaScript problems:
 *
 * <pre>
 * &lt;script type="text/javascript"&gt;
 * //&lt;![CDATA[
 * script...
 * //]]&gt;
 * &lt;/script&gt;
 * </pre>
 */
@ProviderType
public final class Script extends AbstractNonSelfClosingHtmlElement<Script> {
  private static final long serialVersionUID = 1L;

  private static final String ELEMENT_NAME = "script";

  private static final String ATTRIBUTE_TYPE = "type";
  private static final String ATTRIBUTE_SRC = "src";

  /**
   * Type "text/javascript".
   */
  public static final String TYPE_JAVASCRIPT = "text/javascript";

  /**
   * Initializes html element.
   */
  public Script() {
    super(ELEMENT_NAME);
    setType(TYPE_JAVASCRIPT);
  }

  /**
   * Initializes html element.
   * @param script Script block
   */
  public Script(String script) {
    this();
    setText(script);
  }

  /**
   * Html "type" attribute.
   * @return Value of attribute
   */
  public String getType() {
    return getAttributeValue(ATTRIBUTE_TYPE);
  }

  /**
   * Html "type" attribute.
   * @param value Value of attribute
   * @return Self reference
   */
  public Script setType(String value) {
    setAttribute(ATTRIBUTE_TYPE, value);
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
  public Script setSrc(String value) {
    setAttribute(ATTRIBUTE_SRC, value);
    return this;
  }

  /**
   * {@inheritDoc} Overrides standard setText method to add special xHTML conformant CDATA block
   * as workaround for browsers that does not interpret XHTML-encoded script blocks correctly.
   */
  @Override
  public Element setText(String script) {
    if (StringUtils.isEmpty(script)) {
      super.setText(script);
    }
    else {
      this.addContent(new Text("\n//"));

      // encapsulate script block in CDATA block
      this.addContent(new CDATA("\n"
          + script
          + "\n//"));

      this.addContent("\n");
    }
    return this;
  }

}
