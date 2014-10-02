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

import io.wcm.handler.link.LinkArgsType;
import io.wcm.handler.url.UrlMode;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Interface for value object for passing multiple optional arguments to {@link io.wcm.handler.link.LinkHandler} class
 * for controlling link building and
 * markup rendering.
 * @param <T> Subclass implementation to support "builder pattern" with correct return type
 */
@SuppressWarnings("unchecked")
public abstract class AbstractLinkArgs<T extends LinkArgsType> implements LinkArgsType<T> {

  private String selectors;
  private String fileExtension;
  private String suffix;
  private String fragment;
  private String queryString;
  private UrlMode urlMode;

  protected AbstractLinkArgs() {
    // not values
  }

  protected AbstractLinkArgs(String selectors) {
    this.selectors = selectors;
  }

  protected AbstractLinkArgs(String selectors, String fileExtension) {
    this.selectors = selectors;
    this.fileExtension = fileExtension;
  }

  protected AbstractLinkArgs(String selectors, String fileExtension, String suffix) {
    this.selectors = selectors;
    this.fileExtension = fileExtension;
    this.suffix = suffix;
  }

  @Override
  public final String getSelectors() {
    return this.selectors;
  }

  @Override
  public final T setSelectors(String value) {
    this.selectors = value;
    return (T)this;
  }

  @Override
  public final String getFileExtension() {
    return this.fileExtension;
  }

  @Override
  public final T setFileExtension(String value) {
    this.fileExtension = value;
    return (T)this;
  }

  @Override
  public final String getSuffix() {
    return this.suffix;
  }

  @Override
  public final T setSuffix(String value) {
    this.suffix = value;
    return (T)this;
  }

  @Override
  public final String getFragment() {
    return this.fragment;
  }

  @Override
  public final T setFragment(String value) {
    this.fragment = value;
    return (T)this;
  }

  @Override
  public final String getQueryString() {
    return this.queryString;
  }

  @Override
  public final T setQueryString(String value) {
    this.queryString = value;
    return (T)this;
  }

  @Override
  public final UrlMode getUrlMode() {
    return this.urlMode;
  }

  @Override
  public final T setUrlMode(UrlMode value) {
    this.urlMode = value;
    return (T)this;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public T clone() throws CloneNotSupportedException {
    return (T)super.clone();
  }

}
