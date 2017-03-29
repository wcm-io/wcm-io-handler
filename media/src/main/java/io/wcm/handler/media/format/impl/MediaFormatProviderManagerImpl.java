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
package io.wcm.handler.media.format.impl;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.wcm.handler.commons.caservice.ContextAwareServiceResolver;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.spi.MediaFormatProvider;

/**
 * Default implementation of {@link MediaFormatProviderManager}.
 */
@Component(service = MediaFormatProviderManager.class, immediate = true)
public final class MediaFormatProviderManagerImpl implements MediaFormatProviderManager {

  @Reference
  private ContextAwareServiceResolver serviceResolver;

  @Override
  public SortedSet<MediaFormat> getMediaFormats(Resource contextResource) {
    return serviceResolver.resolveAll(MediaFormatProvider.class, contextResource)
        .flatMap(provider -> provider.getMediaFormats().stream())
        .collect(Collectors.toCollection(() -> new TreeSet<MediaFormat>()));
  }

}
