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
import io.wcm.handler.media.MediaItem;
import io.wcm.handler.media.Rendition;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;

import com.day.cq.wcm.api.Page;

/**
 * Holds information about a link processed and resolved by {@link LinkHandler}
 */
public final class LinkMetadata {

  private final LinkReference originalLinkReference;
  private final LinkReference linkReference;
  private final LinkType linkType;
  private boolean linkReferenceInvalid;
  private Anchor anchor;
  private String linkUrl;
  private Page targetPage;
  private MediaItem targetMediaItem;
  private Rendition targetRendition;

  /**
   * @param originalLinkReference Original link reference
   * @param linkReference Processed link reference
   * @param linkType Link type
   */
  public LinkMetadata(LinkReference originalLinkReference, LinkReference linkReference, LinkType linkType) {
    this.originalLinkReference = originalLinkReference;
    this.linkReference = linkReference;
    this.linkType = linkType;
  }

  /**
   * @return Original link reference
   */
  public LinkReference getOriginalLinkReference() {
    return this.originalLinkReference;
  }

  /**
   * @return Processed link reference
   */
  public LinkReference getLinkReference() {
    return this.linkReference;
  }

  /**
   * @return Link type
   */
  public LinkType getLinkType() {
    return this.linkType;
  }

  /**
   * @return true if a link reference was set, but the reference was invalid and could not be resolved
   */
  public boolean isLinkReferenceInvalid() {
    return this.linkReferenceInvalid;
  }

  /**
   * @param linkReferenceInvalid true if a link reference was set, but the reference was invalid and could not be
   *          resolved
   */
  public void setLinkReferenceInvalid(boolean linkReferenceInvalid) {
    this.linkReferenceInvalid = linkReferenceInvalid;
  }

  /**
   * @return Anchor element
   */
  public Anchor getAnchor() {
    return this.anchor;
  }

  /**
   * @return Map with all attributes of the anchor element. Returns null if anchor element is null.
   */
  public Map<String, String> getAnchorAttributes() {
    if (getAnchor() == null) {
      return null;
    }
    Map<String, String> attributes = new HashMap<>();
    for (Attribute attribute : getAnchor().getAttributes()) {
      attributes.put(attribute.getName(), attribute.getValue());
    }
    return attributes;
  }

  /**
   * @param anchor Anchor element
   */
  public void setAnchor(Anchor anchor) {
    this.anchor = anchor;
  }

  /**
   * @return Link URL
   */
  public String getLinkUrl() {
    return this.linkUrl;
  }

  /**
   * @param linkUrl Link URL
   */
  public void setLinkUrl(String linkUrl) {
    this.linkUrl = linkUrl;
  }

  /**
   * @return Target page referenced by the link (applies only for internal links)
   */
  public Page getTargetPage() {
    return this.targetPage;
  }

  /**
   * @param targetPage Target page referenced by the link (applies only for internal links)
   */
  public void setTargetPage(Page targetPage) {
    this.targetPage = targetPage;
  }

  /**
   * @return Target media item (applies only for media links)
   */
  public MediaItem getTargetMediaItem() {
    return this.targetMediaItem;
  }

  /**
   * @param targetMediaItem Target media item (applies only for media links)
   */
  public void setTargetMediaItem(MediaItem targetMediaItem) {
    this.targetMediaItem = targetMediaItem;
  }

  /**
   * @return Target media rendition (applies only for media links)
   */
  public Rendition getTargetRendition() {
    return this.targetRendition;
  }

  /**
   * @param targetRendition Target media rendition (applies only for media links)
   */
  public void setTargetRendition(Rendition targetRendition) {
    this.targetRendition = targetRendition;
  }

  /**
   * @return true if link is valid and was resolved successfully
   */
  public boolean isValid() {
    return getLinkType() != null
        && getLinkUrl() != null
        && !StringUtils.equals(getLinkUrl(), LinkHandler.INVALID_LINK);
  }

  @Override
  public String toString() {
    return this.linkUrl;
  }

}
