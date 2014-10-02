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

import io.wcm.handler.commons.dom.Anchor;
import io.wcm.handler.url.UrlMode;

import org.apache.sling.api.resource.Resource;

import com.day.cq.wcm.api.Page;

/**
 * Manages links defined by editors.
 * General usage: <li>getAnchor: Generate a HTML Anchor element containing link and additional markup e.g. for windows
 * properties and user tracking.</li> <li>getLinkUrl: Gets an URL for this link (ignores all window features and user
 * tracking settings).</li> <li>getLinkMetadata: Returns both Anchor element and URL and additional link resolving
 * metadata.</li>
 */
public interface LinkHandler {

  /**
   * Special link used to notify invalid links.
   */
  String INVALID_LINK = "/invalid///link";

  /**
   * Generate a HTML Anchor element containing link and additional markup e.g. for windows properties and user tracking.
   * @param resource Resource with target link properties
   * @return Anchor element or null if link is invalid or not specified
   */
  Anchor getAnchor(Resource resource);

  /**
   * Generate a HTML Anchor element containing link and additional markup e.g. for windows properties and user tracking.
   * @param resource Resource with target link properties
   * @param selectors Selector to add to target link (applies only to internal links)
   * @return Anchor element or null if link is invalid or not specified
   */
  Anchor getAnchor(Resource resource, String selectors);

  /**
   * Generate a HTML Anchor element containing link and additional markup e.g. for windows properties and user tracking.
   * @param resource Resource with target link properties
   * @param selectors Selector to add to target link (applies only to internal links)
   * @param extension Defines extension for the target link (applies only to internal links)
   * @return Anchor element or null if link is invalid or not specified
   */
  Anchor getAnchor(Resource resource, String selectors, String extension);

  /**
   * Generate a HTML Anchor element containing link and additional markup e.g. for windows properties and user tracking.
   * @param resource Resource with target link properties
   * @param selectors Selector to add to target link (applies only to internal links)
   * @param extension Defines extension for the target link (applies only to internal links)
   * @param suffix Suffix to add to target link (applies only to internal links)
   * @return Anchor element or null if link is invalid or not specified
   */
  Anchor getAnchor(Resource resource, String selectors, String extension, String suffix);

  /**
   * Generate a HTML Anchor element containing link and additional markup e.g. for windows properties and user tracking.
   * @param resource Resource with target link properties
   * @param linkArgs Link arguments for controlling link building and markup generation
   * @return Anchor element or null if link is invalid or not specified
   */
  Anchor getAnchor(Resource resource, LinkArgsType linkArgs);

  /**
   * Generate a HTML Anchor element containing link and additional markup e.g. for windows properties and user tracking.
   * @param page Target page of the link
   * @return Anchor element or null if link is invalid or not specified
   */
  Anchor getAnchor(Page page);

  /**
   * Generate a HTML Anchor element containing link and additional markup e.g. for windows properties and user tracking.
   * @param page Target page of the link
   * @param selectors Selector to add to target link (applies only to internal links)
   * @return Anchor element or null if link is invalid or not specified
   */
  Anchor getAnchor(Page page, String selectors);

  /**
   * Generate a HTML Anchor element containing link and additional markup e.g. for windows properties and user tracking.
   * @param page Target page of the link
   * @param selectors Selector to add to target link (applies only to internal links)
   * @param extension Defines extension for the target link (applies only to internal links)
   * @return Anchor element or null if link is invalid or not specified
   */
  Anchor getAnchor(Page page, String selectors, String extension);

  /**
   * Generate a HTML Anchor element containing link and additional markup e.g. for windows properties and user tracking.
   * @param page Target page of the link
   * @param selectors Selector to add to target link (applies only to internal links)
   * @param extension Defines extension for the target link (applies only to internal links)
   * @param suffix Suffix to add to target link (applies only to internal links)
   * @return Anchor element or null if link is invalid or not specified
   */
  Anchor getAnchor(Page page, String selectors, String extension, String suffix);

  /**
   * Generate a HTML Anchor element containing link and additional markup e.g. for windows properties and user tracking.
   * @param page Target page of the link
   * @param linkArgs Link arguments for controlling link building and markup generation
   * @return Anchor element or null if link is invalid or not specified
   */
  Anchor getAnchor(Page page, LinkArgsType linkArgs);

  /**
   * Generate a HTML Anchor element containing link and additional markup e.g. for windows properties and user tracking.
   * @param linkReference Link reference
   * @return Anchor element or null if link is invalid or not specified
   */
  Anchor getAnchor(LinkReference linkReference);

  /**
   * Gets an URL for this link.
   * @param resource Resource with target link properties
   * @return Link URL or null if link is invalid or not specified
   */
  String getLinkUrl(Resource resource);

  /**
   * Gets an URL for this link.
   * @param resource Resource with target link properties
   * @param selectors Selector to add to target link (applies only to internal links)
   * @return Link URL or null if link is invalid or not specified
   */
  String getLinkUrl(Resource resource, String selectors);

  /**
   * Gets an URL for this link.
   * @param resource Resource with target link properties
   * @param selectors Selector to add to target link (applies only to internal links)
   * @param extension Defines extension for the target link (applies only to internal links)
   * @return Link URL or null if link is invalid or not specified
   */
  String getLinkUrl(Resource resource, String selectors, String extension);

  /**
   * Gets an URL for this link.
   * @param resource Resource with target link properties
   * @param selectors Selector to add to target link (applies only to internal links)
   * @param extension Defines extension for the target link (applies only to internal links)
   * @param suffix Suffix to add to target link (applies only to internal links)
   * @return Link URL or null if link is invalid or not specified
   */
  String getLinkUrl(Resource resource, String selectors, String extension, String suffix);

  /**
   * Gets an URL for this link.
   * @param resource Resource with target link properties
   * @param selectors Selector to add to target link (applies only to internal links)
   * @param extension Defines extension for the target link (applies only to internal links)
   * @param suffix Suffix to add to target link (applies only to internal links)
   * @param urlMode Controls how the link URL is build (applies to internal and media library links)
   * @return Link URL or null if link is invalid or not specified
   */
  String getLinkUrl(Resource resource, String selectors, String extension, String suffix, UrlMode urlMode);

  /**
   * Gets an URL for this link.
   * @param resource Resource with target link properties
   * @param linkArgs Link arguments for controlling link building and markup generation
   * @return Link URL or null if link is invalid or not specified
   */
  String getLinkUrl(Resource resource, LinkArgsType linkArgs);

  /**
   * Gets an URL for this link.
   * @param page Content or redirect page.
   * @return Link URL or null if link is invalid or not specified
   */
  String getLinkUrl(Page page);

  /**
   * Gets an URL for this link.
   * @param page Content or redirect page.
   * @param selectors Selector to add to target link (applies only to internal links)
   * @return Link URL or null if link is invalid or not specified
   */
  String getLinkUrl(Page page, String selectors);

  /**
   * Gets an URL for this link.
   * @param page Content or redirect page.
   * @param selectors Selector to add to target link (applies only to internal links)
   * @param extension Defines extension for the target link (applies only to internal links)
   * @return Link URL or null if link is invalid or not specified
   */
  String getLinkUrl(Page page, String selectors, String extension);

  /**
   * Gets an URL for this link.
   * @param page Content or redirect page.
   * @param selectors Selector to add to target link (applies only to internal links)
   * @param extension Defines extension for the target link (applies only to internal links)
   * @param suffix Suffix to add to target link (applies only to internal links)
   * @return Link URL or null if link is invalid or not specified
   */
  String getLinkUrl(Page page, String selectors, String extension, String suffix);

  /**
   * Gets an URL for this link.
   * @param page Content or redirect page.
   * @param selectors Selector to add to target link (applies only to internal links)
   * @param extension Defines extension for the target link (applies only to internal links)
   * @param suffix Suffix to add to target link (applies only to internal links)
   * @param urlMode Controls how the link URL is build (applies to internal and media library links)
   * @return Link URL or null if link is invalid or not specified
   */
  String getLinkUrl(Page page, String selectors, String extension, String suffix, UrlMode urlMode);

  /**
   * Gets an URL for this link.
   * @param page Content or redirect page.
   * @param linkArgs Link arguments for controlling link building and markup generation
   * @return Link URL or null if link is invalid or not specified
   */
  String getLinkUrl(Page page, LinkArgsType linkArgs);

  /**
   * Gets an URL for this link.
   * @param linkReference Link reference
   * @return Link URL or null if link is invalid or not specified
   */
  String getLinkUrl(LinkReference linkReference);

  /**
   * Get all metadata that can be resolved for this link.
   * @param resource Resource with target link properties
   * @return Resolved link metadata. Never null.
   */
  LinkMetadata getLinkMetadata(Resource resource);

  /**
   * Get all metadata that can be resolved for this link.
   * @param page Content or redirect page.
   * @return Resolved link metadata. Never null.
   */
  LinkMetadata getLinkMetadata(Page page);

  /**
   * Get all metadata that can be resolved for this link.
   * @param linkReference Link reference
   * @return Resolved link metadata. Never null.
   */
  LinkMetadata getLinkMetadata(LinkReference linkReference);

}
