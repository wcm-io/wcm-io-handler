/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.handler.richtext.spi;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ConsumerType;

import com.google.common.collect.ImmutableList;

import io.wcm.handler.richtext.DefaultRewriteContentHandler;
import io.wcm.handler.richtext.util.RewriteContentHandler;
import io.wcm.sling.commons.caservice.ContextAwareService;

/**
 * {@link RichTextHandlerConfig} OSGi services provide application-specific configuration for rich text handling.
 * Applications can set service properties or bundle headers as defined in {@link ContextAwareService} to apply this
 * configuration only for resources that match the relevant resource paths.
 */
@ConsumerType
public abstract class RichTextHandlerConfig implements ContextAwareService {

  private static final List<Class<? extends RewriteContentHandler>> DEFAULT_REWRITE_CONTENT_HANDLERS = ImmutableList.<Class<? extends RewriteContentHandler>>of(
      DefaultRewriteContentHandler.class);

  /**
   * Defines a list of rewrite content handlers. If multiple implementations are provided, all
   * are called on after another for the whole rich text fragment.
   * @return Available rewrite content handler
   */
  public @NotNull List<Class<? extends RewriteContentHandler>> getRewriteContentHandlers() {
    return DEFAULT_REWRITE_CONTENT_HANDLERS;
  }

}
