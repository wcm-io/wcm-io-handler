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
package io.wcm.handler.url.rewriter.impl;

import org.apache.cocoon.xml.sax.AttributesImpl;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

class ComparableAttributes extends AttributesImpl {

  @Override
  public int hashCode() {
    return serializedString(this).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Attributes)) {
      return false;
    }
    return StringUtils.equals(serializedString(this), serializedString((Attributes)obj));
  }

  @Override
  public String toString() {
    return serializedString(this);
  }

  static String serializedString(Attributes attributes) {
    StringBuilder serialized = new StringBuilder();
    for (int i = 0; i < attributes.getLength(); i++) {
      serialized.append(attributes.getLocalName(i))
      .append("=")
      .append(attributes.getValue(i))
      .append(";");
    }
    return serialized.toString();
  }

}
