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
package io.wcm.handler.link;

import io.wcm.handler.url.UrlMode;
import io.wcm.wcm.commons.util.ToStringStyle;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Holds parameters to influence the link resolving process.
 */
@ProviderType
public final class LinkArgs implements Cloneable {

  private UrlMode urlMode;
  private boolean dummyLink;
  private String dummyLinkUrl;
  private String selectors;
  private String extension;
  private String suffix;
  private String queryString;
  private String fragment;
  private ValueMap properties;

  /**
   * Default constructor
   */
  public LinkArgs() {
    // default constructor
  }

  /**
   * @return URL mode for externalizing the URL
   */
  public UrlMode getUrlMode() {
    return this.urlMode;
  }

  /**
   * @param value URL mode for externalizing the URL
   * @return this
   */
  public LinkArgs urlMode(UrlMode value) {
    this.urlMode = value;
    return this;
  }

  /**
   * @return If set to true, link handler returns a dummy link in edit mode when link is invalid.
   */
  public boolean isDummyLink() {
    return this.dummyLink;
  }

  /**
   * @param value If set to true, link handler returns a dummy link in edit mode when link is invalid.
   * @return this
   */
  public LinkArgs dummyLink(boolean value) {
    this.dummyLink = value;
    return this;
  }

  /**
   * @return Custom dummy link url. If null default dummy url is used.
   */
  public String getDummyLinkUrl() {
    return this.dummyLinkUrl;
  }

  /**
   * @param value Custom dummy link url. If null default dummy url is used.
   * @return this
   */
  public LinkArgs dummyLinkUrl(String value) {
    this.dummyLinkUrl = value;
    return this;
  }

  /**
   * @return Selector string
   */
  public String getSelectors() {
    return this.selectors;
  }

  /**
   * @param value Selector string
   * @return this
   */
  public LinkArgs selectors(String value) {
    this.selectors = value;
    return this;
  }

  /**
   * @return File extension
   */
  public String getExtension() {
    return this.extension;
  }

  /**
   * @param value File extension
   * @return this
   */
  public LinkArgs extension(String value) {
    this.extension = value;
    return this;
  }

  /**
   * @return Suffix string
   */
  public String getSuffix() {
    return this.suffix;
  }

  /**
   * @param value Suffix string
   * @return this
   */
  public LinkArgs suffix(String value) {
    this.suffix = value;
    return this;
  }

  /**
   * @return Query parameters string (properly url-encoded)
   */
  public String getQueryString() {
    return this.queryString;
  }

  /**
   * @param value Query parameters string (properly url-encoded)
   * @return this
   */
  public LinkArgs queryString(String value) {
    this.queryString = value;
    return this;
  }

  /**
   * @return Fragment identifier
   */
  public String getFragment() {
    return this.fragment;
  }

  /**
   * @param value Fragment identifier
   * @return this
   */
  public LinkArgs fragment(String value) {
    this.fragment = value;
    return this;
  }

  /**
   * Custom properties that my be used by application-specific markup builders or processors.
   * @param map Property map. Is merged with properties already set.
   * @return this
   */
  public LinkArgs properties(Map<String, Object> map) {
    if (map == null) {
      throw new IllegalArgumentException("Map argument must not be null.");
    }
    getProperties().putAll(map);
    return this;
  }

  /**
   * Custom properties that my be used by application-specific markup builders or processors.
   * @param key Property key
   * @param value Property value
   * @return this
   */
  public LinkArgs property(String key, Object value) {
    if (key == null) {
      throw new IllegalArgumentException("Key argument must not be null.");
    }
    getProperties().put(key, value);
    return this;
  }

  /**
   * Custom properties that my be used by application-specific markup builders or processors.
   * @return Value map
   */
  public ValueMap getProperties() {
    if (this.properties == null) {
      this.properties = new ValueMapDecorator(new HashMap<String, Object>());
    }
    return this.properties;
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
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_OMIT_NULL_STYLE);
  }

  /**
   * Custom clone-method for {@link LinkArgs}
   * @return the cloned {@link LinkArgs}
   */
  // CHECKSTYLE:OFF
  @Override
  public LinkArgs clone() { //NOPMD
    // CHECKSTYLE:ON
    LinkArgs clone = new LinkArgs();

    clone.urlMode = this.urlMode;
    clone.dummyLink = this.dummyLink;
    clone.dummyLinkUrl = this.dummyLinkUrl;
    clone.selectors = this.selectors;
    clone.extension = this.extension;
    clone.suffix = this.suffix;
    clone.queryString = this.queryString;
    clone.fragment = this.fragment;
    if (this.properties != null) {
      clone.properties = new ValueMapDecorator(new HashMap<String, Object>(this.properties));
    }

    return clone;
  }

}
