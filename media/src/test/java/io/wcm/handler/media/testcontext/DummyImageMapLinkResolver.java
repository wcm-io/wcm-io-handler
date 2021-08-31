/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.handler.media.testcontext;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.spi.ImageMapLinkResolver;
import io.wcm.testing.mock.aem.context.AemContextImpl;

public class DummyImageMapLinkResolver implements ImageMapLinkResolver<String> {

  private final AemContextImpl context;

  public DummyImageMapLinkResolver(AemContextImpl context) {
    this.context = context;
  }

  @Override
  public @Nullable String resolve(@NotNull String linkUrl, @NotNull Resource contextResource) {
    if (StringUtils.startsWith(linkUrl, "/content/")) {
      if (context.pageManager().getPage(linkUrl) != null) {
        return linkUrl + ".html";
      }
    }
    else if (!StringUtils.isBlank(linkUrl)) {
      return linkUrl;
    }
    return null;
  }

  @Override
  public @NotNull String resolveLink(@NotNull String linkUrl, @Nullable String linkWindowTarget,
      @NotNull Resource contextResource) {
    return StringUtils.defaultString(resolve(linkUrl, contextResource));
  }

  @Override
  public @Nullable String getLinkUrl(@Nullable String link) {
    if (StringUtils.isBlank(link)) {
      return null;
    }
    else {
      return link;
    }
  }

}
