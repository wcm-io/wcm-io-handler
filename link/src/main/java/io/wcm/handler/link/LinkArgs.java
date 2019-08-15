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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.handler.url.UrlMode;
import io.wcm.wcm.commons.util.ToStringStyle;

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
  private String linkTargetUrlFallbackProperty;

  /**
   * @return URL mode for externalizing the URL
   */
  public @Nullable UrlMode getUrlMode() {
    return this.urlMode;
  }

  /**
   * @param value URL mode for externalizing the URL
   * @return this
   */
  public @NotNull LinkArgs urlMode(@Nullable UrlMode value) {
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
  public @NotNull LinkArgs dummyLink(boolean value) {
    this.dummyLink = value;
    return this;
  }

  /**
   * @return Custom dummy link url. If null default dummy url is used.
   */
  public @Nullable String getDummyLinkUrl() {
    return this.dummyLinkUrl;
  }

  /**
   * @param value Custom dummy link url. If null default dummy url is used.
   * @return this
   */
  public @NotNull LinkArgs dummyLinkUrl(@Nullable String value) {
    this.dummyLinkUrl = value;
    return this;
  }

  /**
   * @return Selector string
   */
  public @Nullable String getSelectors() {
    return this.selectors;
  }

  /**
   * @param value Selector string
   * @return this
   */
  public @NotNull LinkArgs selectors(@Nullable String value) {
    this.selectors = value;
    return this;
  }

  /**
   * @return File extension
   */
  public @Nullable String getExtension() {
    return this.extension;
  }

  /**
   * @param value File extension
   * @return this
   */
  public @NotNull LinkArgs extension(@Nullable String value) {
    this.extension = value;
    return this;
  }

  /**
   * @return Suffix string
   */
  public @Nullable String getSuffix() {
    return this.suffix;
  }

  /**
   * @param value Suffix string
   * @return this
   */
  public @NotNull LinkArgs suffix(@Nullable String value) {
    this.suffix = value;
    return this;
  }

  /**
   * @return Query parameters string (properly url-encoded)
   */
  public @Nullable String getQueryString() {
    return this.queryString;
  }

  /**
   * @param value Query parameters string (properly url-encoded)
   * @return this
   */
  public @NotNull LinkArgs queryString(@Nullable String value) {
    this.queryString = value;
    return this;
  }

  /**
   * @return Fragment identifier
   */
  public @Nullable String getFragment() {
    return this.fragment;
  }

  /**
   * @param value Fragment identifier
   * @return this
   */
  public @NotNull LinkArgs fragment(@Nullable String value) {
    this.fragment = value;
    return this;
  }

  /**
   * Custom properties that my be used by application-specific markup builders or processors.
   * @param map Property map. Is merged with properties already set.
   * @return this
   */
  @SuppressWarnings({ "null", "unused" })
  public @NotNull LinkArgs properties(@NotNull Map<String, Object> map) {
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
  @SuppressWarnings({ "null", "unused" })
  public @NotNull LinkArgs property(@NotNull String key, @Nullable Object value) {
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
  public @NotNull ValueMap getProperties() {
    if (this.properties == null) {
      this.properties = new ValueMapDecorator(new HashMap<String, Object>());
    }
    return this.properties;
  }


  /**
   * Defines a "fallback" property name that is used to load link target information from a single property
   * instead of the link type + link type depending property name. This property is used for migration
   * from components that do not support Link Handler. It is only used for reading, and never written back to.
   * When opened and saved in the link dialog, the property is removed and instead the dedicated properties are used.
   * @param propertyName Property name
   * @return this
   */
  public @NotNull LinkArgs linkTargetUrlFallbackProperty(@Nullable String propertyName) {
    this.linkTargetUrlFallbackProperty = propertyName;
    return this;
  }

  /**
   * @return Property name
   */
  public @Nullable String getLinkTargetUrlFallbackProperty() {
    return this.linkTargetUrlFallbackProperty;
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
    clone.linkTargetUrlFallbackProperty = this.linkTargetUrlFallbackProperty;
    if (this.properties != null) {
      clone.properties = new ValueMapDecorator(new HashMap<String, Object>(this.properties));
    }

    return clone;
  }

}
