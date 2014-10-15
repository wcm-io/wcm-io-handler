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

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.LinkRequest;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.spi.LinkHandlerConfig;
import io.wcm.handler.url.UrlHandler;
import io.wcm.handler.url.spi.UrlHandlerConfig;
import io.wcm.sling.models.annotations.AemObject;
import io.wcm.wcm.commons.contenttype.FileExtension;
import io.wcm.wcm.commons.util.RunMode;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMMode;

/**
 * Default implementation of {@link io.wcm.handler.link.spi.LinkType} for internal links.
 * Internal links are links to content pages inside the CMS.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ProviderType
public final class InternalLinkType extends AbstractLinkType {

  /**
   * Link type ID
   */
  public static final String ID = "internal";

  @Self
  private LinkHandlerConfig linkHandlerConfig;
  @Self
  private UrlHandlerConfig urlHandlerConfig;
  @Self
  private LinkHandler linkHandler;
  @Self
  private UrlHandler urlHandler;
  @AemObject
  private PageManager pageManager;
  @AemObject
  private WCMMode wcmMode;
  @OSGiService
  private SlingSettingsService slingSettings;

  /**
   * @return Link type ID (is stored as identifier in repository)
   */
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getPrimaryLinkRefProperty() {
    return LinkNameConstants.PN_LINK_CONTENT_REF;
  }

  @Override
  public boolean accepts(String linkRef) {
    // accept as internal link if the ref starts with "/content/"
    return StringUtils.startsWith(linkRef, "/content/")
        && !MediaLinkType.isDefaultMediaContentPath(linkRef);
  }

  @Override
  public boolean accepts(LinkRequest linkRequest) {
    if (acceptPage(linkRequest.getPage())) {
      // support direct links to pages
      return true;
    }
    // check for matching link type ID in link resource
    return super.accepts(linkRequest);
  }

  @Override
  public Link resolveLink(Link link) {
    LinkRequest linkRequest = link.getLinkRequest();
    ValueMap props = linkRequest.getResourceProperties();

    // flag to indicate whether any link reference parameter set
    boolean referenceSet = false;

    // first try to get direct link target page
    Page targetPage = link.getLinkRequest().getPage();
    if (targetPage != null) {
      referenceSet = true;
    }

    // if no target page is set get internal path that points to target page
    if (targetPage == null) {
      String targetPath = props.get(getPrimaryLinkRefProperty(), String.class);
      if (StringUtils.isNotEmpty(targetPath)) {
        referenceSet = true;
      }
      targetPage = getTargetPage(targetPath);
    }

    // if target page is a redirect or integrator page recursively resolve link to which the redirect points to
    // (skip this redirection if edit mode is active)
    if (targetPage != null
        && (linkHandlerConfig.isRedirect(targetPage) || urlHandlerConfig.isIntegrator(targetPage))
        && wcmMode != WCMMode.EDIT) {
      return recursiveResolveLink(targetPage, link);
    }

    // build link url
    String linkUrl = null;
    if (targetPage != null) {
      link.setTargetPage(targetPage);

      String selectors = linkRequest.getSelectors();
      String fileExtension = StringUtils.defaultString(linkRequest.getExtension(), FileExtension.HTML);
      String suffix = linkRequest.getSuffix();
      String queryString = linkRequest.getQueryString();
      String fragment = linkRequest.getFragement();

      // optionally override query parameters and fragment from link resource
      queryString = props.get(LinkNameConstants.PN_LINK_QUERY_PARAM, queryString);
      fragment = props.get(LinkNameConstants.PN_LINK_ANCHOR_NAME, fragment);

      // build url
      linkUrl = urlHandler.get(targetPage.getPath())
          .selectors(selectors)
          .extension(fileExtension)
          .suffix(suffix)
          .queryString(queryString)
          .fragment(fragment).build();

      // externalize url
      linkUrl = urlHandler.get(linkUrl)
          .urlMode(linkRequest.getUrlMode())
          .buildExternalLinkUrl(targetPage);
    }

    if (linkUrl == null) {
      // mark link as invalid if a reference was set that could not be resolved
      if (referenceSet) {
        link.setLinkReferenceInvalid(true);
      }
    }

    // set link url
    link.setUrl(linkUrl);

    return link;
  }

  /**
   * Resolves link of redirect or integrator page. Those pages contain the link reference information in their
   * content resource (jcr:content node). This information is used to resolve the link.
   * @param redirectPage Redirect or integrator page
   * @param link Link metadata
   * @return Link metadata
   */
  private Link recursiveResolveLink(Page redirectPage, Link link) {

    // set link reference to content resource of redirect page, keep other parameters
    LinkRequest linkRequest = link.getLinkRequest();
    LinkRequest redirectLinkRequest = new LinkRequest(
        redirectPage.getContentResource(),
        null,
        linkRequest.getUrlMode(),
        linkRequest.getSelectors(),
        linkRequest.getExtension(),
        linkRequest.getSuffix(),
        linkRequest.getQueryString(),
        linkRequest.getFragement());

    // check of maximum recursive calls via threadlocal to avoid endless loops, return invalid link if one is detected
    LinkResolveCounter linkResolveCounter = LinkResolveCounter.get();
    if (linkResolveCounter == null) {
      linkResolveCounter = new LinkResolveCounter();
      LinkResolveCounter.getThreadLocal().set(linkResolveCounter);
    }
    try {
      linkResolveCounter.increaseCount();

      if (linkResolveCounter.isMaximumReached()) {
        // endless loop detected - set link to invalid link
        link.setUrl(null);
        return link;
      }

      // resolve link by recursive call to link handler, track recursion count
      return linkHandler.get(redirectLinkRequest).build();
    }
    finally {
      linkResolveCounter.decreaseCount();
      if (linkResolveCounter.getCount() == 0) {
        LinkResolveCounter.getThreadLocal().remove();
      }
    }
  }

  /**
   * Check if a given page is valid and acceptable to link upon.
   * @param page Page
   * @return true if link is acceptable
   */
  private boolean acceptPage(Page page) {
    if (page == null) {
      return false;
    }

    // check for jcr:content node
    if (!page.hasContent()) {
      return false;
    }

    // check if page is valid concerning on/off-time (only in publish environment)
    if (RunMode.isPublish(slingSettings.getRunModes()) && !page.isValid()) {
      return false;
    }

    // check if page is acceptable based on link handler config
    if (!linkHandlerConfig.isValidLinkTarget(page)) {
      return false;
    }

    return true;
  }

  /**
   * Returns the target page for the given internal content link reference.
   * Checks validity of page.
   * @param targetPath Contentbus reference.
   * @return Target page or null if target reference is invalid.
   */
  private Page getTargetPage(String targetPath) {

    if (StringUtils.isEmpty(targetPath)) {
      return null;
    }

    // Rewrite target to current site context
    String rewrittenPath = urlHandler.rewritePathToContext(targetPath);
    if (StringUtils.isEmpty(rewrittenPath)) {
      return null;
    }

    // Get target page referenced by target path and check for acceptance
    Page targetPage = pageManager.getPage(rewrittenPath);
    if (acceptPage(targetPage)) {
      return targetPage;
    }
    else {
      return null;
    }
  }

  /**
   * Get synthetic link resource for this link type.
   * @param resourceResolver Resource resolver
   * @param pageRef Path to target page
   * @return Synthetic link resource
   */
  public static Resource getSyntheticLinkResource(ResourceResolver resourceResolver, String pageRef) {
    Map<String, Object> map = new HashMap<>();
    map.put(LinkNameConstants.PN_LINK_TYPE, ID);
    map.put(LinkNameConstants.PN_LINK_CONTENT_REF, pageRef);
    return new SyntheticLinkResource(resourceResolver, map);
  }

}
