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

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.richtext.RichText;
import io.wcm.handler.richtext.RichTextBuilder;
import io.wcm.handler.richtext.RichTextRequest;
import io.wcm.handler.richtext.TextMode;
import io.wcm.handler.url.UrlMode;

import java.util.Collection;

import org.apache.sling.api.resource.Resource;
import org.jdom2.Content;

/**
 * Default implementation of {@link RichTextBuilder}.
 */
final class RichTextBuilderImpl implements RichTextBuilder {

  private final RichTextHandlerImpl richTextHandler;
  private final Resource resource;
  private final String text;

  private UrlMode urlMode;
  private TextMode textMode;
  private MediaArgs mediaArgs;

  /**
   * @param resource Resource
   */
  public RichTextBuilderImpl(Resource resource, RichTextHandlerImpl richTextHandler) {
    this.resource = resource;
    this.text = null;
    this.richTextHandler = richTextHandler;
  }

  /**
   * @param text Raw text
   */
  public RichTextBuilderImpl(String text, RichTextHandlerImpl richTextHandler) {
    this.resource = null;
    this.text = text;
    this.richTextHandler = richTextHandler;
  }

  @Override
  public RichTextBuilder urlMode(UrlMode value) {
    this.urlMode = value;
    return this;
  }

  @Override
  public RichTextBuilder textMode(TextMode value) {
    this.textMode = value;
    return this;
  }

  @Override
  public RichTextBuilder mediaArgs(MediaArgs value) {
    this.mediaArgs = value;
    return this;
  }

  @Override
  public RichText build() {
    RichTextRequest request = new RichTextRequest(this.resource, this.text, this.urlMode,
        this.textMode, this.mediaArgs);
    return this.richTextHandler.processRequest(request);
  }

  @Override
  public String buildMarkup() {
    return build().getMarkup();
  }

  @Override
  public Collection<Content> buildContent() {
    return build().getContent();
  }

}
