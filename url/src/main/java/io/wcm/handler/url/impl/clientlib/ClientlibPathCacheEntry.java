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
package io.wcm.handler.url.impl.clientlib;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.wcm.wcm.commons.util.ToStringStyle;

/**
 * Entry for {@link ClientlibPathCache}.
 */
class ClientlibPathCacheEntry {

  private final String path;
  private final boolean isClientLibrary;
  private final boolean isAllowProxy;

  ClientlibPathCacheEntry(String path, boolean isClientLibrary, boolean isAllowProxy) {
    this.path = path;
    this.isClientLibrary = isClientLibrary;
    this.isAllowProxy = isAllowProxy;
  }

  public String getPath() {
    return this.path;
  }

  public boolean isClientLibrary() {
    return this.isClientLibrary;
  }

  public boolean isAllowProxy() {
    return this.isAllowProxy;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_OMIT_NULL_STYLE);
  }

}
