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
package io.wcm.handler.link.type;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.spi.LinkType;

/**
 * Default implementation of {@link io.wcm.handler.link.spi.LinkType} for external links.
 * External links are links to destinations outside the CMS.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ProviderType
public final class ExternalLinkType extends LinkType {

  /**
   * Link type ID
   */
  public static final @NotNull String ID = "external";

  /*
   * Matches all strings that seem to have a proper URL scheme - e.g. starting with http://, https://, mailto:, tel:
   * It also allows anchor links staring with #
   */
  private static final Pattern EXTERNALIZED_PATTERN = Pattern.compile("^(([^/]+:|//)|#).*$");

  /**
   * @return Link type ID (is stored as identifier in repository)
   */
  @Override
  public @NotNull String getId() {
    return ID;
  }

  @Override
  public @NotNull String getLabel() {
    return "External";
  }

  @Override
  public String getPrimaryLinkRefProperty() {
    return LinkNameConstants.PN_LINK_EXTERNAL_REF;
  }

  @Override
  @SuppressWarnings({ "unused", "null" })
  public boolean accepts(@NotNull String linkRef) {
    // test for null because earlier versions of this method did not have the @NotNull annotation
    if (linkRef == null) {
      return false;
    }
    // accept as external link if the ref contains "://" and mailto links
    return EXTERNALIZED_PATTERN.matcher(linkRef).matches();
  }

  @Override
  public @NotNull Link resolveLink(@NotNull Link link) {
    ValueMap props = link.getLinkRequest().getResourceProperties();

    // get external URL from link properties
    String linkUrl = props.get(LinkNameConstants.PN_LINK_EXTERNAL_REF, link.getLinkRequest().getReference());

    // check external link url
    if (StringUtils.isBlank(linkUrl)) {
      linkUrl = null;
    }

    // set link url
    link.setUrl(linkUrl);

    return link;
  }

  /**
   * Get synthetic link resource for this link type.
   * @param resourceResolver Resource resolver
   * @param path Resource path. Can be a non-existing path, but the path should be located somewhere within the
   *          applications content paths to make sure the handler configuration looked up via context-aware services
   *          is the expected one.
   * @param url Link URL
   * @return Synthetic link resource
   */
  public static @NotNull Resource getSyntheticLinkResource(@NotNull ResourceResolver resourceResolver,
      @NotNull String path, @NotNull String url) {
    Map<String, Object> map = new HashMap<>();
    map.put(LinkNameConstants.PN_LINK_TYPE, ID);
    map.put(LinkNameConstants.PN_LINK_EXTERNAL_REF, url);
    return new SyntheticLinkResource(resourceResolver, path, map);
  }

  /**
   * Get synthetic link resource for this link type.
   * @param resourceResolver Resource resolver
   * @param url Link URL
   * @return Synthetic link resource
   * @deprecated Please use {@link #getSyntheticLinkResource(ResourceResolver, String, String)}
   */
  @Deprecated
  public static @NotNull Resource getSyntheticLinkResource(@NotNull ResourceResolver resourceResolver, @NotNull String url) {
    Map<String, Object> map = new HashMap<>();
    map.put(LinkNameConstants.PN_LINK_TYPE, ID);
    map.put(LinkNameConstants.PN_LINK_EXTERNAL_REF, url);
    return new SyntheticLinkResource(resourceResolver, map);
  }

  @Override
  public String toString() {
    return ID;
  }

}
