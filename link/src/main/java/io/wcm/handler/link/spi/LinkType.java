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
package io.wcm.handler.link.spi;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.LinkRequest;

/**
 * Defines a link type supported by {@link LinkHandler}.
 * <p>
 * This interface has to be implemented by a Sling Model class. The adaptables
 * should be {@link org.apache.sling.api.SlingHttpServletRequest} and {@link org.apache.sling.api.resource.Resource}.
 * </p>
 */
@ConsumerType
public abstract class LinkType {

  /**
   * @return Link type ID (is stored as identifier in repository)
   */
  @JsonProperty("linkType")
  public abstract @NotNull String getId();

  /**
   * @return Link type label (displayed in link dialogs)
   */
  @JsonIgnore
  public @NotNull String getLabel() {
    return getId();
  }

  /**
   * @return Name of the property in which the primary link reference is stored
   */
  @JsonIgnore
  public abstract @Nullable String getPrimaryLinkRefProperty();

  /**
   * Checks whether a link reference can be handled by this link type
   * @param linkRequest Link reference
   * @return true if this link type can handle the given link reference
   */
  public boolean accepts(@NotNull LinkRequest linkRequest) {
    ValueMap props = linkRequest.getResourceProperties();
    // check for matching link type ID in link resource
    String linkTypeId = props.get(LinkNameConstants.PN_LINK_TYPE, String.class);
    if (StringUtils.isNotEmpty(linkTypeId)) {
      return StringUtils.equals(linkTypeId, getId());
    }
    // if not link type is set at all check if link ref attribute contains a valid link
    // or a link reference is given with auto-detection of it's type
    else {
      String propertyName = getPrimaryLinkRefProperty();
      String linkRef = null;
      if (propertyName != null) {
        linkRef = props.get(propertyName, String.class);
      }
      if (linkRef == null) {
        linkRef = linkRequest.getReference();
      }
      if (linkRef != null) {
        return accepts(linkRef);
      }
      return false;
    }
  }

  /**
   * Checks whether a link reference string can be handled by this link type
   * @param linkRef Link reference string
   * @return true if this link type can handle the given link reference
   */
  public abstract boolean accepts(@NotNull String linkRef);

  /**
   * Resolves a link
   * @param link Link metadata
   * @return Resolved link metadata. Never null.
   */
  public abstract @NotNull Link resolveLink(@NotNull Link link);

  /**
   * Granite UI component resource type to be used for editing this link type's properties in edit dialog.
   * @return Granite UI component resource type or null, if none is available
   */
  public @Nullable String getEditComponentResourceType() {
    return null;
  }

  /**
   * Returns true if a RTE plugin is available for this link type. If not, it is not possible to select
   * this link type in the rich text editor.
   * @return true if a RTE plugin is available.
   */
  public boolean hasRichTextPlugin() {
    return false;
  }

}
