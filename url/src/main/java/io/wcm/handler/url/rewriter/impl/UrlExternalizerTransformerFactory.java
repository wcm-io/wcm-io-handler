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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.rewriter.Transformer;
import org.apache.sling.rewriter.TransformerFactory;

/**
 * AEM Link Checker Transformer for externalizing URLs with URL handler.
 */
@Component(immediate = true)
@Service(value = TransformerFactory.class)
@Property(name = "pipeline.type", value = "wcm-io-urlhandler-externalizer")
public class UrlExternalizerTransformerFactory implements TransformerFactory {

  @Override
  public Transformer createTransformer() {
    return new UrlExternalizerTransformer();
  }

}
