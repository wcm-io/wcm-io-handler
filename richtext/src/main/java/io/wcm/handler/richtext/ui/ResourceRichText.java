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
package io.wcm.handler.richtext.ui;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import io.wcm.handler.richtext.RichTextHandler;
import io.wcm.handler.richtext.RichTextNameConstants;

/**
 * Generic resource-based model for rendering formatted XHTML rich text.
 * <p>
 * Optional use parameters when referencing model from Sightly template:
 * </p>
 * <ul>
 * <li><code>propertyName</code>: Property name in which the text is stored in the resource</li>
 * </ul>
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class ResourceRichText {

  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  @Default(values = RichTextNameConstants.PN_TEXT)
  private String propertyName;

  @Self
  private RichTextHandler richTextHandler;
  @SlingObject
  private Resource resource;

  private String markup;

  @PostConstruct
  @SuppressWarnings("null")
  private void activate() {
    String xhtmlString = resource.getValueMap().get(propertyName, String.class);
    markup = richTextHandler.get(xhtmlString).buildMarkup();
  }

  /**
   * Returns true if rich text is present and valid.
   * @return Rich text is valid
   */
  public boolean isValid() {
    return StringUtils.isNotBlank(getMarkup());
  }

  /**
   * Returns the formatted text as XHTML markup.
   * @return Rich text markup
   */
  public String getMarkup() {
    return markup;
  }

}
