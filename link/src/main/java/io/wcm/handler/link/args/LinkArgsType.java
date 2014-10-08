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

import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.url.UrlMode;

/**
 * Interface for value object for passing multiple optional arguments to {@link LinkHandler} class
 * for controlling link building and markup rendering.
 * @param <T> Subclass implementation to support "builder pattern" with correct return type
 */
public interface LinkArgsType<T extends LinkArgsType> extends Cloneable {

  /**
   * @return Selector
   */
  String getSelectors();

  /**
   * @param selector Selector
   * @return this
   */
  T setSelectors(String selector);

  /**
   * @return File extension
   */
  String getFileExtension();

  /**
   * @param fileExtension File extension
   * @return this
   */
  T setFileExtension(String fileExtension);

  /**
   * @return Suffix
   */
  String getSuffix();

  /**
   * @param suffix Suffix
   * @return this
   */
  T setSuffix(String suffix);

  /**
   * @return Fragment
   */
  String getFragment();

  /**
   * @param fragment Fragment
   * @return this
   */
  T setFragment(String fragment);

  /**
   * @return Query string
   */
  String getQueryString();

  /**
   * @param queryString Query string
   * @return this
   */
  T setQueryString(String queryString);

  /**
   * @return URL mode
   */
  UrlMode getUrlMode();

  /**
   * @param urlMode URS mode
   * @return this
   */
  T setUrlMode(UrlMode urlMode);

  /**
   * custom clone-method for {@link LinkArgsType}
   * @return the cloned {@link LinkArgsType}
   * @throws CloneNotSupportedException
   */
  Object clone() throws CloneNotSupportedException;

}
