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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;

import io.wcm.handler.richtext.RichText;
import io.wcm.handler.richtext.RichTextBuilder;
import io.wcm.handler.richtext.RichTextHandler;
import io.wcm.handler.richtext.RichTextNameConstants;
import io.wcm.handler.richtext.RichTextRequest;
import io.wcm.handler.richtext.TextMode;
import io.wcm.handler.richtext.spi.RichTextHandlerConfig;
import io.wcm.handler.richtext.util.RewriteContentHandler;
import io.wcm.handler.richtext.util.RewriteRawTextHandler;
import io.wcm.handler.richtext.util.RichTextUtil;
import io.wcm.sling.commons.caservice.ContextAwareServiceResolver;
import io.wcm.sling.models.annotations.AemObject;

/**
 * Default implementation of {@link RichTextHandler}.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
}, adapters = RichTextHandler.class)
public final class RichTextHandlerImpl implements RichTextHandler {

  static final Logger log = LoggerFactory.getLogger(RichTextHandlerImpl.class);

  @Self
  private Adaptable adaptable;
  @AemObject(injectionStrategy = InjectionStrategy.OPTIONAL)
  private Page currentPage;
  @OSGiService
  private ContextAwareServiceResolver serviceResolver;

  private List<RewriteContentHandler> rewriteContentHandlers;

  private List<RewriteRawTextHandler> rewriteRawTextHandlers;

  @Override
  public @NotNull RichTextBuilder get(Resource resource) {
    return new RichTextBuilderImpl(resource, this);
  }

  @Override
  public @NotNull RichTextBuilder get(String text) {
    return new RichTextBuilderImpl(text, this);
  }

  @NotNull
  RichText processRequest(@NotNull RichTextRequest richTextRequest) {
    String text = getRawText(richTextRequest);
    TextMode textMode = getTextMode(richTextRequest);

    List<Content> content;
    if (textMode == TextMode.XHTML) {
      content = processRichText(text);
    }
    else {
      content = processPlainText(text);
    }

    return new RichText(richTextRequest, content);
  }

  private String getRawText(RichTextRequest richTextRequest) {
    String text = richTextRequest.getResource() != null ? richTextRequest.getResourceProperties().get(RichTextNameConstants.PN_TEXT, String.class)
        : richTextRequest.getText();
    for (RewriteRawTextHandler rewriteRawTextHandler : getRewriteRawTextHandlers()) {
      text = rewriteRawTextHandler.rewriteText(text);
    }
    return text;
  }

  private TextMode getTextMode(RichTextRequest richTextRequest) {
    if (richTextRequest.getTextMode() != null) {
      return richTextRequest.getTextMode();
    }
    else if (richTextRequest.getResource() != null) {
      boolean textIsRich = richTextRequest.getResourceProperties().get(RichTextNameConstants.PN_TEXT_IS_RICH, true);
      return textIsRich ? TextMode.XHTML : TextMode.PLAIN;
    }
    else {
      return TextMode.XHTML;
    }
  }

  private List<Content> processRichText(String text) {
    if (isEmpty(text)) {
      return ImmutableList.of();
    }

    // Parse text
    try {
      Element contentParent = RichTextUtil.parseText(text, true);

      // Rewrite content (e.g. anchor tags)
      List<RewriteContentHandler> rewriters = getRewriterContentHandlers();
      for (RewriteContentHandler rewriter : rewriters) {
        RichTextUtil.rewriteContent(contentParent, rewriter);
      }

      // return xhtml elements
      return ImmutableList.copyOf(contentParent.cloneContent());
    }
    catch (JDOMException ex) {
      if (log.isDebugEnabled()) {
        log.debug("Unable to parse XHTML text."
            + (currentPage != null ? " Current page is " + currentPage.getPath() + "." : ""), ex);
      }
      return ImmutableList.of();
    }
  }

  private List<Content> processPlainText(String text) {
    if (StringUtils.isBlank(text)) {
      return ImmutableList.of();
    }

    List<Content> content = new ArrayList<>();
    String[] lines = StringUtils.splitByWholeSeparatorPreserveAllTokens(text, "\n");
    for (int i = 0; i < lines.length; i++) {
      if (i > 0) {
        content.add(new Element("br"));
      }
      content.add(new Text(lines[i]));
    }

    return ImmutableList.copyOf(content);
  }

  @Override
  public boolean isEmpty(String text) {
    return RichTextUtil.isEmpty(text);
  }

  private List<RewriteContentHandler> getRewriterContentHandlers() {
    if (rewriteContentHandlers == null) {
      processRichTextHandlerConfig();
    }
    return rewriteContentHandlers;
  }

  private List<RewriteRawTextHandler> getRewriteRawTextHandlers() {
    if (rewriteRawTextHandlers == null) {
      processRichTextHandlerConfig();
    }
    return this.rewriteRawTextHandlers;
  }

  private void processRichTextHandlerConfig() {
    RichTextHandlerConfig config = serviceResolver.resolve(RichTextHandlerConfig.class, adaptable);
    if (config != null) {
      rewriteContentHandlers = loadAdaptables(config.getRewriteContentHandlers());
      rewriteRawTextHandlers = loadAdaptables(config.getRewriteRawTextHandlers());
    }
  }

  private <T> List<T> loadAdaptables(List<Class<? extends T>> classes) {
    List<T> result = new ArrayList<>(classes.size());
    for (Class<? extends T> clazz : classes) {
      T adaptTo = adaptable.adaptTo(clazz);
      if (adaptTo == null) {
        throw new RuntimeException("Unable to adapt " + adaptable.getClass() + " to " + clazz.getName() + ". "
            + "Make sure the class is a Sling Model and adaptable from Resource and SlingHttpServletRequest.");
      }
      result.add(adaptTo);
    }
    return result;
  }

}
