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
package io.wcm.handler.url.rewriter.impl;

import io.wcm.handler.url.UrlHandler;

import org.apache.cocoon.xml.sax.AbstractSAXPipe;
import org.apache.cocoon.xml.sax.AttributesImpl;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * HTML transformer that rewrites URLs in certain HTML element attributes.
 */
class UrlExternalizerTransformer extends AbstractSAXPipe implements Transformer {

  private UrlExternalizerTransformerConfig transformerConfig;
  private UrlHandler urlHandler;

  private static final Logger log = LoggerFactory.getLogger(UrlExternalizerTransformer.class.getName());

  @Override
  public void init(ProcessingContext pipelineContext, ProcessingComponentConfiguration config) {
    log.trace("Initialize UrlExternalizerTransformer with config: {}", config.getConfiguration());
    transformerConfig = new UrlExternalizerTransformerConfig(config.getConfiguration());
    urlHandler = pipelineContext.getRequest().adaptTo(UrlHandler.class);
  }

  @Override
  public void startElement(String nsUri, String name, String raw, Attributes attrs) throws SAXException {

    // check if for this element an attribute for rewriting is configured
    String rewriteAttr = transformerConfig.getElementAttributeNames().get(name);
    if (rewriteAttr == null) {
      log.trace("Rewrite element {}: Skip - No rewrite attribute configured.", name);
      super.startElement(nsUri, name, raw, attrs);
      return;
    }

    // validate URL handler
    if (urlHandler == null) {
      log.warn("Rewrite element {}: Skip - Unable to get URL handler/Integrator handler instance.", name);
      super.startElement(nsUri, name, raw, attrs);
      return;
    }

    // check if attribute exists
    int attributeIndex = attrs.getIndex(rewriteAttr);
    if (attributeIndex < 0) {
      log.trace("Rewrite element {}: Skip - Attribute does not exist: {}", name, rewriteAttr);
      super.startElement(nsUri, name, raw, attrs);
      return;
    }

    // rewrite URL
    String url = attrs.getValue(attributeIndex);
    if (StringUtils.isEmpty(url)) {
      log.trace("Rewrite element {}: Skip - URL is empty.", name);
      super.startElement(nsUri, name, raw, attrs);
      return;
    }

    // remove escaping
    url = StringEscapeUtils.unescapeHtml4(url);

    // externalize URL (if it is not already externalized)
    String rewrittenUrl = urlHandler.get(url).buildExternalResourceUrl();

    if (StringUtils.equals(url, rewrittenUrl)) {
      log.debug("Rewrite element {}: Skip - URL is already externalized: {}", name, url);
      super.startElement(nsUri, name, raw, attrs);
      return;
    }

    // set new attribute value
    log.debug("Rewrite element {}: Rewrite URL {} to {}", name, url, rewrittenUrl);
    AttributesImpl newAttrs = new AttributesImpl(attrs);
    newAttrs.setValue(attributeIndex, rewrittenUrl);
    super.startElement(nsUri, name, raw, newAttrs);
  }

  @Override
  public void dispose() {
    // nothing to do
  }

}
