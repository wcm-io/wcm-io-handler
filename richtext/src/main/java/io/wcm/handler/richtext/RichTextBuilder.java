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

import io.wcm.handler.media.args.MediaArgsType;
import io.wcm.handler.url.UrlMode;

import java.util.Collection;

import org.jdom2.Content;

import aQute.bnd.annotation.ProviderType;

/**
 * Define rich text handling request using builder pattern.
 */
@ProviderType
public interface RichTextBuilder {

  /**
   * Set in which text mode the raw text should be interpreded.
   * @param textMode Text mode
   * @return Rich text builder
   */
  RichTextBuilder textMode(TextMode textMode);

  /**
   * Set media arguments to be applied when resolving inline images.
   * @param mediaArgs Media arguments
   * @return Rich text builder
   */
  RichTextBuilder mediaArgs(MediaArgsType mediaArgs);

  /**
   * Set URL mode to be applied for building URLs for inline links and inline images.
   * @param urlMode URL mode
   * @return Rich text builder
   */
  RichTextBuilder urlMode(UrlMode urlMode);

  /**
   * Build formatted markup and return metadata object containing results.
   * @return Rich text metadata. Never null, if the resolving failed the isValid() method returns false.
   */
  RichText build();

  /**
   * Build formatted markup string.
   * @return Formatted markup string or null if invalid or empty.
   */
  String buildMarkup();

  /**
   * Build formatted markup as DOM elements.
   * @return DOM elements or empty collection if invalid or empty.
   */
  Collection<Content> buildContent();

}
