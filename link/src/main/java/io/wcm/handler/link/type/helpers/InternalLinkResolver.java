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
package io.wcm.handler.link.type.helpers;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMMode;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkArgs;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.LinkRequest;
import io.wcm.handler.link.spi.LinkHandlerConfig;
import io.wcm.handler.url.UrlHandler;
import io.wcm.handler.url.spi.UrlHandlerConfig;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.models.annotations.AemObject;
import io.wcm.wcm.commons.contenttype.FileExtension;
import io.wcm.wcm.commons.instancetype.InstanceTypeService;
import io.wcm.wcm.commons.util.Path;

/**
 * Implements resolving an internal link for link types. The primary goal is to support the implementation
 * for {@link io.wcm.handler.link.type.InternalLinkType}, but it can be used by custom link type implementations as
 * well.
 * <p>
 * The link resolving process can be customized by providing a customized {@link InternalLinkResolverOptions} object.
 * </p>
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ProviderType
public final class InternalLinkResolver {

  @Self
  private LinkHandlerConfig linkHandlerConfig;
  @Self
  private UrlHandlerConfig urlHandlerConfig;
  @Self
  private LinkHandler linkHandler;
  @Self
  private UrlHandler urlHandler;
  @SlingObject
  private ResourceResolver resourceResolver;
  @AemObject
  private PageManager pageManager;
  @AemObject(injectionStrategy = InjectionStrategy.OPTIONAL)
  private Page currentPage;
  @AemObject(injectionStrategy = InjectionStrategy.OPTIONAL)
  private WCMMode wcmMode;
  @OSGiService
  private InstanceTypeService instanceTypeService;

  /**
   * Check if a given page is valid and acceptable to link upon.
   * @param page Page
   * @param options Options
   * @return true if link is acceptable
   */
  public boolean acceptPage(@Nullable Page page, @NotNull InternalLinkResolverOptions options) {
    if (page == null) {
      return false;
    }

    // check for jcr:content node
    if (!page.hasContent()) {
      return false;
    }

    // check if page is valid concerning on/off-time (only in publish environment)
    if (instanceTypeService.isPublish() && !page.isValid()) {
      return false;
    }

    // check if page is acceptable based on link handler config
    if (!linkHandlerConfig.isValidLinkTarget(page)) {
      return false;
    }

    return true;
  }

  /**
   * Resolves a link and stores the result in the link object.
   * Primary it sets the property "linkReferenceInvalid" and the URL is link resolving was successful.
   * @param link Link
   * @param options Options to influence the link resolution process
   * @return Resolved link object
   */
  public @NotNull Link resolveLink(@NotNull Link link, @NotNull InternalLinkResolverOptions options) {
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
      String targetPath = props.get(options.getPrimaryLinkRefProperty(), String.class);
      if (StringUtils.isEmpty(targetPath)) {
        targetPath = link.getLinkRequest().getReference();
      }
      if (StringUtils.isNotEmpty(targetPath)) {
        referenceSet = true;
      }
      targetPage = getTargetPage(targetPath, options);
    }

    UrlHandlerConfig resolvingUrlHandlerConfig = urlHandlerConfig;
    UrlHandler resolvingUrlHandler = urlHandler;

    // use URL handler from target context for link URL building
    if (targetPage != null && useTargetContext(options)) {
      Resource resource = targetPage.getContentResource();
      resolvingUrlHandlerConfig = AdaptTo.notNull(resource, UrlHandlerConfig.class);
      resolvingUrlHandler = AdaptTo.notNull(resource, UrlHandler.class);
    }

    // if target page is a redirect or integrator page recursively resolve link to which the redirect points to
    // (skip this redirection if edit mode is active)
    if (targetPage != null
        && (linkHandlerConfig.isRedirect(targetPage) || resolvingUrlHandlerConfig.isIntegrator(targetPage))
        && wcmMode != WCMMode.EDIT) {
      return recursiveResolveLink(targetPage, link);
    }

    // build link url
    String linkUrl = null;
    if (targetPage != null) {
      link.setTargetPage(targetPage);

      LinkArgs linkArgs = linkRequest.getLinkArgs();
      String selectors = linkArgs.getSelectors();
      String fileExtension = StringUtils.defaultString(linkArgs.getExtension(), FileExtension.HTML);
      String suffix = linkArgs.getSuffix();
      String queryString = linkArgs.getQueryString();
      String fragment = linkArgs.getFragment();

      // optionally override query parameters and fragment from link resource
      if (queryString == null) {
        queryString = props.get(LinkNameConstants.PN_LINK_QUERY_PARAM, String.class);
      }
      else {
        queryString = props.get(LinkNameConstants.PN_LINK_QUERY_PARAM, queryString);
      }
      if (fragment == null) {
        fragment = props.get(LinkNameConstants.PN_LINK_FRAGMENT, String.class);
      }
      else {
        fragment = props.get(LinkNameConstants.PN_LINK_FRAGMENT, fragment);
      }

      // build link url
      linkUrl = resolvingUrlHandler.get(targetPage)
          .selectors(selectors)
          .extension(fileExtension)
          .suffix(suffix)
          .queryString(queryString)
          .fragment(fragment)
          .urlMode(linkArgs.getUrlMode())
          .buildExternalLinkUrl(targetPage);
    }

    // mark link as invalid if a reference was set that could not be resolved
    if (linkUrl == null && referenceSet) {
      link.setLinkReferenceInvalid(true);
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
        linkRequest.getLinkArgs());

    // check of maximum recursive calls via threadlocal to avoid endless loops, return invalid link if one is detected
    LinkResolveCounter linkResolveCounter = LinkResolveCounter.get();
    try {
      linkResolveCounter.increaseCount();

      if (linkResolveCounter.isMaximumReached()) {
        // endless loop detected - set link to invalid link
        link.setUrl(null);
        return link;
      }

      // resolve link by recursive call to link handler, track recursion count
      Link resolvedLink = linkHandler.get(redirectLinkRequest).build();
      resolvedLink.addRedirectPage(redirectPage);
      return resolvedLink;
    }
    finally {
      linkResolveCounter.decreaseCount();
    }
  }

  /**
   * Returns the target page for the given internal content link reference.
   * Checks validity of page.
   * @param targetPath Repository path
   * @return Target page or null if target reference is invalid.
   */
  private Page getTargetPage(String targetPath, InternalLinkResolverOptions options) {

    if (StringUtils.isEmpty(targetPath)) {
      return null;
    }

    // Rewrite target to current site context
    String rewrittenPath;
    if (options.isRewritePathToContext() && !useTargetContext(options)) {
      rewrittenPath = urlHandler.rewritePathToContext(SyntheticNavigatableResource.get(targetPath, resourceResolver));
    }
    else {
      rewrittenPath = targetPath;
    }
    if (StringUtils.isEmpty(rewrittenPath)) {
      return null;
    }

    // Get target page referenced by target path and check for acceptance
    Page targetPage = pageManager.getPage(rewrittenPath);
    if (acceptPage(targetPage, options)) {
      return targetPage;
    }
    else {
      return null;
    }
  }

  /**
   * Checks if target context should be used.
   * @param options Link resolver options
   * @return true if target context should be used
   */
  private boolean useTargetContext(InternalLinkResolverOptions options) {
    if (options.isUseTargetContext() && !options.isRewritePathToContext()) {
      return true;
    }
    // even is use target context is not activated use it if current page is an experience fragment
    // otherwise it will be always impossible to resolve internal links
    else if (currentPage != null && Path.isExperienceFragmentPath(currentPage.getPath())) {
      return true;
    }
    return false;
  }

}
