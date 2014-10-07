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
package io.wcm.handler.url;

import io.wcm.config.api.Configuration;
import io.wcm.handler.url.integrator.IntegratorHandler;
import io.wcm.handler.url.integrator.IntegratorPlaceholder;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.util.RunMode;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.Adaptable;

import com.day.cq.wcm.api.Page;

/**
 * Default URL modes sufficient for the most usecases.
 */
public enum UrlModes implements UrlMode {

  /**
   * Default mode: Does generate a full externalized Url only if both site-url and site-url-secure parameter
   * are set in site config. If not set, only URLs without hostname are generated.
   * If the target is an internal content page, from the site config parameters site-url or site-url-secure
   * is chosen automatically depending on the secure state of the page.
   */
  DEFAULT {

    @Override
    public String getLinkUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage, Page targetPage) {
      // default to full url for content URLs
      return FULL_URL.getLinkUrlPrefix(adaptable, runModes, currentPage, targetPage);
    }

    @Override
    public String getResourceUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage) {
      // in integrator template mode default to full URL mode, otherwise to no-hostname mode
      IntegratorHandler integratorHandler = AdaptTo.notNull(adaptable, IntegratorHandler.class);
      if (integratorHandler.isIntegratorTemplateMode()) {
        return FULL_URL.getResourceUrlPrefix(adaptable, runModes, currentPage);
      }
      return NO_HOSTNAME.getResourceUrlPrefix(adaptable, runModes, currentPage);
    }

  },

  /**
   * Default mode: Does generate a externalized Url without any protocol and hostname,
   * independent of any setting in site config.
   */
  NO_HOSTNAME {

    @Override
    public String getLinkUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage, Page targetPage) {
      return null;
    }

    @Override
    public String getResourceUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage) {
      return null;
    }

  },

  /**
   * Enforce the generation of a full Url with protocol and hostname.
   * If the target is an internal content page, from the site config parameters site-url or site-url-secure
   * is chosen automatically depending on the secure state of page.
   */
  FULL_URL {

    @Override
    public String getLinkUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage, Page targetPage) {
      UrlHandlerConfig urlHandlerConfig = AdaptTo.notNull(adaptable, UrlHandlerConfig.class);

      // if integrator template mode with placeholders is active return link url placeholder
      IntegratorHandler integratorHandler = AdaptTo.notNull(adaptable, IntegratorHandler.class);
      if (integratorHandler.isIntegratorTemplateMode()
          && integratorHandler.getIntegratorMode().isUseUrlPlaceholders()) {
        if (targetPage != null && urlHandlerConfig.isSecure(targetPage)) {
          return IntegratorPlaceholder.URL_CONTENT_SECURE;
        }
        else {
          return IntegratorPlaceholder.URL_CONTENT;
        }
      }

      Configuration config = adaptable.adaptTo(Configuration.class);
      if (config == null) {
        return null;
      }

      // in author mode return author site url
      if (RunMode.isAuthor(runModes)) {
        String siteUrlAuthor = config.get(UrlParams.SITE_URL_AUTHOR);
        if (StringUtils.isNotEmpty(siteUrlAuthor)) {
          return siteUrlAuthor;
        }
      }

      // return secure or non-secure site url
      String siteUrl = config.get(UrlParams.SITE_URL);
      if (targetPage != null && urlHandlerConfig.isSecure(targetPage)) {
        String siteUrlSecure = config.get(UrlParams.SITE_URL_SECURE);
        return StringUtils.defaultIfEmpty(siteUrlSecure, siteUrl);
      }
      return siteUrl;
    }

    @Override
    public String getResourceUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage) {

      // if integrator template mode with placeholders is active return resource url placeholder
      IntegratorHandler integratorHandler = AdaptTo.notNull(adaptable, IntegratorHandler.class);
      if (integratorHandler.isIntegratorTemplateMode()
          && integratorHandler.getIntegratorMode().isUseUrlPlaceholders()) {
        return IntegratorPlaceholder.URL_CONTENT_PROXY;
      }

      Configuration config = adaptable.adaptTo(Configuration.class);
      if (config == null) {
        return null;
      }

      // in author mode return author site url
      if (RunMode.isAuthor(runModes)) {
        String siteUrlAuthor = config.get(UrlParams.SITE_URL_AUTHOR);
        if (StringUtils.isNotEmpty(siteUrlAuthor)) {
          return siteUrlAuthor;
        }
      }

      // return secure or non-secure site url
      UrlHandlerConfig urlHandlerConfig = AdaptTo.notNull(adaptable, UrlHandlerConfig.class);
      String siteUrl = config.get(UrlParams.SITE_URL);
      if ((currentPage != null && urlHandlerConfig.isSecure(currentPage))
          || integratorHandler.isIntegratorTemplateSecureMode()) {
        String siteUrlSecure = config.get(UrlParams.SITE_URL_SECURE);
        return StringUtils.defaultIfEmpty(siteUrlSecure, siteUrl);
      }
      return siteUrl;
    }

  },

  /**
   * Enforce the generation of a full Url with protocol and hostname and non-secure mode.
   */
  FULL_URL_FORCENONSECURE {

    @Override
    public String getLinkUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage, Page targetPage) {

      // if integrator template mode with placeholders is active return link url placeholder
      IntegratorHandler integratorHandler = AdaptTo.notNull(adaptable, IntegratorHandler.class);
      if (integratorHandler.isIntegratorTemplateMode()
          && integratorHandler.getIntegratorMode().isUseUrlPlaceholders()) {
        return IntegratorPlaceholder.URL_CONTENT;
      }

      Configuration config = adaptable.adaptTo(Configuration.class);
      if (config == null) {
        return null;
      }

      // in author mode return author site url
      if (RunMode.isAuthor(runModes)) {
        String siteUrlAuthor = config.get(UrlParams.SITE_URL_AUTHOR);
        if (StringUtils.isNotEmpty(siteUrlAuthor)) {
          return siteUrlAuthor;
        }
      }

      // return non-secure site url
      return config.get(UrlParams.SITE_URL);
    }

    @Override
    public String getResourceUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage) {

      // if integrator template mode with placeholders is active return resource url placeholder
      IntegratorHandler integratorHandler = AdaptTo.notNull(adaptable, IntegratorHandler.class);
      if (integratorHandler.isIntegratorTemplateMode()
          && integratorHandler.getIntegratorMode().isUseUrlPlaceholders()) {
        return IntegratorPlaceholder.URL_CONTENT_PROXY;
      }

      Configuration config = adaptable.adaptTo(Configuration.class);
      if (config == null) {
        return null;
      }

      // in author mode return author site url
      if (RunMode.isAuthor(runModes)) {
        String siteUrlAuthor = config.get(UrlParams.SITE_URL_AUTHOR);
        if (StringUtils.isNotEmpty(siteUrlAuthor)) {
          return siteUrlAuthor;
        }
      }

      // return non-secure site url
      return config.get(UrlParams.SITE_URL);
    }

  },

  /**
   * Enforce the generation of a full Url with protocol and hostname and secure mode.
   * If site-url-secure is not set, site-url is used.
   */
  FULL_URL_FORCESECURE {

    @Override
    public String getLinkUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage, Page targetPage) {

      // if integrator template mode with placeholders is active return link url placeholder
      IntegratorHandler integratorHandler = AdaptTo.notNull(adaptable, IntegratorHandler.class);
      if (integratorHandler.isIntegratorTemplateMode()
          && integratorHandler.getIntegratorMode().isUseUrlPlaceholders()) {
        return IntegratorPlaceholder.URL_CONTENT_SECURE;
      }

      Configuration config = adaptable.adaptTo(Configuration.class);
      if (config == null) {
        return null;
      }

      // in author mode return author site url
      if (RunMode.isAuthor(runModes)) {
        String siteUrlAuthor = config.get(UrlParams.SITE_URL_AUTHOR);
        if (StringUtils.isNotEmpty(siteUrlAuthor)) {
          return siteUrlAuthor;
        }
      }

      // return secure site url
      return StringUtils.defaultIfEmpty(config.get(UrlParams.SITE_URL_SECURE), config.get(UrlParams.SITE_URL));
    }

    @Override
    public String getResourceUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage) {

      // if integrator template mode with placeholders is active return resource url placeholder
      IntegratorHandler integratorHandler = AdaptTo.notNull(adaptable, IntegratorHandler.class);
      if (integratorHandler.isIntegratorTemplateMode()
          && integratorHandler.getIntegratorMode().isUseUrlPlaceholders()) {
        return IntegratorPlaceholder.URL_CONTENT_PROXY;
      }

      Configuration config = adaptable.adaptTo(Configuration.class);
      if (config == null) {
        return null;
      }

      // in author mode return author site url
      if (RunMode.isAuthor(runModes)) {
        String siteUrlAuthor = config.get(UrlParams.SITE_URL_AUTHOR);
        if (StringUtils.isNotEmpty(siteUrlAuthor)) {
          return siteUrlAuthor;
        }
      }

      // return secure site url
      return StringUtils.defaultIfEmpty(config.get(UrlParams.SITE_URL_SECURE), config.get(UrlParams.SITE_URL));
    }

  },

  /**
   * Enforce the generation of a full Url with hostname and "//" as protocol (protocol-relative mode).
   * Using "//" instead of "http://" or "https://" results in using the same protocol as the current request
   * in the browser.
   */
  FULL_URL_PROTOCOLRELATIVE {

    @Override
    public String getLinkUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage, Page targetPage) {

      // if integrator template mode with placeholders is active return link url placeholder
      IntegratorHandler integratorHandler = AdaptTo.notNull(adaptable, IntegratorHandler.class);
      if (integratorHandler.isIntegratorTemplateMode()
          && integratorHandler.getIntegratorMode().isUseUrlPlaceholders()) {
        UrlHandlerConfig urlHandlerConfig = AdaptTo.notNull(adaptable, UrlHandlerConfig.class);
        if (targetPage != null && urlHandlerConfig.isSecure(targetPage)) {
          return IntegratorPlaceholder.URL_CONTENT_SECURE;
        }
        else {
          return IntegratorPlaceholder.URL_CONTENT;
        }
      }

      Configuration config = adaptable.adaptTo(Configuration.class);
      if (config == null) {
        return null;
      }

      // in author mode return author site url
      if (RunMode.isAuthor(runModes)) {
        String siteUrlAuthor = config.get(UrlParams.SITE_URL_AUTHOR);
        if (StringUtils.isNotEmpty(siteUrlAuthor)) {
          return siteUrlAuthor;
        }
      }

      // return site-url in protocol-relative mode
      return convertToProtocolRelative(config.get(UrlParams.SITE_URL));
    }

    @Override
    public String getResourceUrlPrefix(Adaptable adaptable, Set<String> runModes, Page currentPage) {

      // if integrator template mode with placeholders is active return resource url placeholder
      IntegratorHandler integratorHandler = AdaptTo.notNull(adaptable, IntegratorHandler.class);
      if (integratorHandler.isIntegratorTemplateMode()
          && integratorHandler.getIntegratorMode().isUseUrlPlaceholders()) {
        return IntegratorPlaceholder.URL_CONTENT_PROXY;
      }

      Configuration config = adaptable.adaptTo(Configuration.class);
      if (config == null) {
        return null;
      }

      // in author mode return author site url
      if (RunMode.isAuthor(runModes)) {
        String siteUrlAuthor = config.get(UrlParams.SITE_URL_AUTHOR);
        if (StringUtils.isNotEmpty(siteUrlAuthor)) {
          return siteUrlAuthor;
        }
      }

      // return secure or non-secure site url
      return convertToProtocolRelative(config.get(UrlParams.SITE_URL));
    }

    /**
     * Strips of protocol from given URL (if any protocol is included)
     * @param pPrefix Prefix with protocol
     * @return Prefix without protocol (protocol-relative mode)
     */
    private String convertToProtocolRelative(String pPrefix) {
      if (StringUtils.isEmpty(pPrefix)) {
        return null;
      }
      int index = StringUtils.indexOf(pPrefix, "://");
      if (index >= 0) {
        return pPrefix.substring(index + 1);
      }
      else {
        return pPrefix;
      }
    }

  };

}
