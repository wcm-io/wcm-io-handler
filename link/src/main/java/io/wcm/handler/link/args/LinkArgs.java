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
package io.wcm.handler.link.args;

import io.wcm.handler.url.UrlMode;

/**
 * Default implementation of link arguments
 */
public final class LinkArgs extends AbstractLinkArgs<LinkArgs> {

  /**
   * Empty constructor
   */
  public LinkArgs() {
    // not values
  }

  /**
   * @param selectors Selector
   */
  public LinkArgs(String selectors) {
    super(selectors);
  }

  /**
   * @param selectors Selector
   * @param fileExtension File extension
   */
  public LinkArgs(String selectors, String fileExtension) {
    super(selectors, fileExtension);
  }

  /**
   * @param selectors Selector
   * @param fileExtension File extension
   * @param suffix Suffix
   */
  public LinkArgs(String selectors, String fileExtension, String suffix) {
    super(selectors, fileExtension, suffix);
  }

  /**
   * Shortcut for building {@link LinkArgs} with selector.
   * @param selector Selector
   * @return Link args
   */
  public static LinkArgsType selector(String selector) {
    return new LinkArgs().setSelectors(selector);
  }

  /**
   * Shortcut for building {@link LinkArgs} with file extension.
   * @param fileExtensions File extension
   * @return Link args
   */
  public static LinkArgsType fileExtension(String fileExtensions) {
    return new LinkArgs().setFileExtension(fileExtensions);
  }

  /**
   * Shortcut for building {@link LinkArgs} with suffix.
   * @param suffix Suffix
   * @return Link args
   */
  public static LinkArgsType suffix(String suffix) {
    return new LinkArgs().setSuffix(suffix);
  }

  /**
   * Shortcut for building {@link LinkArgs} with fragment.
   * @param fragment Selector
   * @return Link args
   */
  public static LinkArgsType fragment(String fragment) {
    return new LinkArgs().setFragment(fragment);
  }

  /**
   * Shortcut for building {@link LinkArgs} with suffix.
   * @param queryString query string
   * @return Link args
   */
  public static LinkArgsType queryString(String queryString) {
    return new LinkArgs().setQueryString(queryString);
  }

  /**
   * Shortcut for building {@link LinkArgs} with URL mode.
   * @param urlMode URL mode
   * @return Link args
   */
  public static LinkArgsType urlMode(UrlMode urlMode) {
    return new LinkArgs().setUrlMode(urlMode);
  }

}
