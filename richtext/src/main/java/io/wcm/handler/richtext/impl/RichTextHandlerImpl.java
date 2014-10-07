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
package io.wcm.handler.richtext.impl;

import io.wcm.handler.richtext.RichTextHandler;
import io.wcm.handler.richtext.RichTextRewriteContentHandler;
import io.wcm.handler.richtext.util.RichTextUtil;
import io.wcm.sling.models.annotations.AemObject;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;

/**
 * Default implementation of {@link RichTextHandler}
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
}, adapters = RichTextHandler.class)
public final class RichTextHandlerImpl implements RichTextHandler {

  static final Logger log = LoggerFactory.getLogger(RichTextHandlerImpl.class);

  @Self
  private RichTextRewriteContentHandler rewriteContentHandler;
  @AemObject
  private Page currentPage;

  @Override
  public void addContent(String text, Element parent, boolean supressParsingError) {
    try {
      addContent(text, parent);
    }
    catch (JDOMException ex) {
      // suppress error, log in debug mode
      if (supressParsingError) {
        log.debug("Unable to parse XHTML text."
            + (currentPage != null ? " Current page is " + currentPage.getPath() + "." : ""), ex);
      }
      else {
        throw new IllegalArgumentException("Unable to parse XHTML text.", ex);
      }
    }
  }

  @Override
  public void addContent(String text, Element parent) throws JDOMException {
    // ignore empty text blocks
    if (isEmpty(text)) {
      return;
    }
    // Add xhtml elements to parent
    parent.addContent(getContent(text));
  }

  @Override
  public void addPlaintextContent(String text, Element parent) {
    if (StringUtils.isEmpty(text)) {
      return;
    }
    String[] lines = StringUtils.splitByWholeSeparatorPreserveAllTokens(text, "\n");
    for (int i = 0; i < lines.length; i++) {
      if (i > 0) {
        parent.addContent(new Element("br"));
      }
      parent.addContent(lines[i]);
    }
  }

  @Override
  public List<Content> getContent(String text) throws JDOMException {

    // Parse text
    Element contentParent = RichTextUtil.parseText(text, true);

    // Rewrite content (e.g. anchor tags)
    rewriteContent(contentParent);

    // return xhtml elements
    return contentParent.cloneContent();
  }

  @Override
  public boolean isEmpty(String text) {
    return RichTextUtil.isEmpty(text);
  }

  /**
   * Rewrites special elements like anchors and images in content.
   * @param parent Parent element
   */
  private void rewriteContent(Element parent) {
    RichTextUtil.rewriteContent(parent, rewriteContentHandler);
  }

}
