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
package io.wcm.handler.link.spi.helpers;

import io.wcm.handler.link.markup.SimpleLinkMarkupBuilder;
import io.wcm.handler.link.spi.LinkHandlerConfig;
import io.wcm.handler.link.spi.LinkMarkupBuilder;
import io.wcm.handler.link.spi.LinkProcessor;
import io.wcm.handler.link.spi.LinkType;
import io.wcm.handler.link.type.ExternalLinkType;
import io.wcm.handler.link.type.InternalLinkType;
import io.wcm.handler.link.type.MediaLinkType;

import java.util.List;

import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;

/**
 * Default implementation of configuration options of {@link LinkHandlerConfig} interface.
 */
public abstract class AbstractLinkHandlerConfig implements LinkHandlerConfig {

  private static final List<Class<? extends LinkType>> LINK_TYPES = ImmutableList.<Class<? extends LinkType>>of(
      InternalLinkType.class,
      ExternalLinkType.class,
      MediaLinkType.class
      );

  private static final List<Class<? extends LinkMarkupBuilder>> LINK_MARKUP_BUILDERS = ImmutableList.<Class<? extends LinkMarkupBuilder>>of(
      SimpleLinkMarkupBuilder.class
      );

  @Override
  public List<Class<? extends LinkType>> getLinkTypes() {
    return LINK_TYPES;
  }

  @Override
  public List<Class<? extends LinkMarkupBuilder>> getMarkupBuilders() {
    return LINK_MARKUP_BUILDERS;
  }

  @Override
  public List<Class<? extends LinkProcessor>> getPreProcessors() {
    // no processors
    return ImmutableList.of();
  }

  @Override
  public List<Class<? extends LinkProcessor>> getPostProcessors() {
    // no processors
    return ImmutableList.of();
  }

  @Override
  public boolean isValidLinkTarget(Page page) {
    // by default accept all pages
    return true;
  }

  @Override
  public boolean isRedirect(Page page) {
    // not supported by default
    return false;
  }

}
